#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ENV_FILE:-$ROOT/.env}"
APP_PROJECT="${APP_COMPOSE_PROJECT:-infra}"
TEST_PROJECT="${TEST_COMPOSE_PROJECT:-openmrs-tests}"
RESTART_APP=true

usage() {
  cat <<'EOF'
Usage: bash infra/run-tests.sh [--skip-restart]

Starts the database, restores the reference test snapshot into a new database,
starts OpenMRS and Selenoid, then runs the test suite and generates reports.
An existing OpenMRS database is preserved.

Options:
  --skip-restart  Do not restart already-running database/app/browser containers.
  -h, --help     Show this help.
EOF
}

while (($#)); do
  case "$1" in
    --skip-restart) RESTART_APP=false ;;
    -h|--help) usage; exit 0 ;;
    *) printf 'Unknown option: %s\n' "$1" >&2; usage >&2; exit 2 ;;
  esac
  shift
done

if [[ ! -f "$ENV_FILE" ]]; then
  cp "$ROOT/.env.example" "$ENV_FILE"
  printf 'Created %s from .env.example. Review local credentials before sharing this environment.\n' "$ENV_FILE"
fi

cd "$ROOT"
BASE_COMPOSE=(docker compose --env-file "$ENV_FILE" --project-name "$APP_PROJECT" -f infra/docker-compose.yaml)
if [[ "${BUILD_ENVIRONMENT:-local}" == ci ]]; then
  BASE_COMPOSE+=(-f infra/docker-compose.ci.yaml)
fi
RUNNER_COMPOSE=(docker compose --env-file "$ENV_FILE" --project-name "$TEST_PROJECT" -f infra/docker-compose.yaml)
if [[ "${BUILD_ENVIRONMENT:-local}" == ci ]]; then
  RUNNER_COMPOSE+=(-f infra/docker-compose.ci.yaml)
fi
RUNNER_COMPOSE+=(-f infra/docker-compose.pipeline.yaml)

collect_startup_diagnostics() {
  local service container_id logs_file
  printf '\n== Compose service status ==\n' >&2
  "${BASE_COMPOSE[@]}" ps --all >&2 || true
  for service in db backend; do
    container_id=$("${BASE_COMPOSE[@]}" ps --all -q "$service" 2>/dev/null || true)
    if [[ -n "$container_id" ]]; then
      printf '%s state: ' "$service" >&2
      docker inspect --format '{{.State.Status}}{{if .State.Health}} (health: {{.State.Health.Status}}){{end}}' "$container_id" >&2 || true
    fi
  done
  printf '\n== Database and backend logs (last 200 lines) ==\n' >&2
  logs_file=$(mktemp)
  "${BASE_COMPOSE[@]}" logs --no-color --timestamps --tail=200 db backend >"$logs_file" 2>&1 || true
  sed -E \
    -e 's/((password|passwd|token|secret|api[_-]?key)[^=:]{0,20}[=:][[:space:]]*)[^[:space:],;]+/\1[REDACTED]/Ig' \
    -e 's/(Authorization[[:space:]]*:[[:space:]]*(Basic|Bearer)[[:space:]]+)[^[:space:]]+/\1[REDACTED]/Ig' \
    -e 's|://[^:/[:space:]]+:[^@/[:space:]]+@|://[REDACTED]@|g' \
    "$logs_file" >&2
  rm -f "$logs_file"
}

wait_for_database() {
  local deadline=$((SECONDS + 120))
  while (( SECONDS < deadline )); do
    if "${BASE_COMPOSE[@]}" exec -T db sh -c \
      'mariadb --protocol=TCP --host=127.0.0.1 --user="$MYSQL_USER" --password="$MYSQL_PASSWORD" "$MYSQL_DATABASE" --batch --skip-column-names --execute "SELECT 1"' \
      >/dev/null 2>&1; then
      printf 'MariaDB is accepting queries.\n'
      return 0
    fi
    sleep 2
  done
  printf 'MariaDB did not accept queries within 120 seconds.\n' >&2
  return 1
}

seed_database_if_empty() {
  local table_counts total_table_count core_table_count
  if ! table_counts=$(printf '%s\n' \
    "SELECT COUNT(*), COALESCE(SUM(table_name IN ('users', 'global_property', 'liquibasechangelog')), 0) FROM information_schema.tables WHERE table_schema = DATABASE();" |
    "${BASE_COMPOSE[@]}" exec -T db sh -c \
      'mariadb --protocol=TCP --host=127.0.0.1 --user="$MYSQL_USER" --password="$MYSQL_PASSWORD" "$MYSQL_DATABASE" --batch --skip-column-names'); then
    printf 'Could not inspect the OpenMRS database schema.\n' >&2
    return 1
  fi
  read -r total_table_count core_table_count <<< "$table_counts"
  if [[ ! "$total_table_count" =~ ^[0-9]+$ || ! "$core_table_count" =~ ^[0-9]+$ ]]; then
    printf 'Could not interpret the OpenMRS database schema inspection result.\n' >&2
    return 1
  fi

  case "$core_table_count" in
    0)
      if [[ "$total_table_count" != 0 ]]; then
        printf 'Database contains tables but no OpenMRS core schema; refusing to import the snapshot.\n' >&2
        return 1
      fi
      printf '\n== Restore preinitialized OpenMRS database snapshot ==\n'
      if ! "${BASE_COMPOSE[@]}" exec -T db sh -c \
        'mariadb --max-allowed-packet=256M --user=root --password="$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' \
        < "$ROOT/infra/db/openmrs-test.sql"; then
        printf 'OpenMRS database snapshot restore failed.\n' >&2
        return 1
      fi
      if ! "${BASE_COMPOSE[@]}" exec -T db sh -c \
        'mariadb --user=root --password="$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' \
        < "$ROOT/infra/db/reset-lucene-index.sql"; then
        printf 'Could not reset the Lucene index version after snapshot restore.\n' >&2
        return 1
      fi
      printf 'OpenMRS database snapshot restored and Lucene index reset.\n'
      ;;
    3)
      printf 'Existing OpenMRS database detected; preserving its data.\n'
      ;;
    *)
      printf 'Database has an incomplete or unexpected OpenMRS schema (%s/3 core tables); refusing to overwrite it.\n' \
        "$core_table_count" >&2
      return 1
      ;;
  esac
}

docker info >/dev/null
"${BASE_COMPOSE[@]}" -f infra/docker-compose.pipeline.yaml config --quiet

APP_ALREADY_RUNNING=$("${BASE_COMPOSE[@]}" ps -q backend)

printf '\n== Start database while browser images are pulled ==\n'
if ! "${BASE_COMPOSE[@]}" up --detach db; then
  collect_startup_diagnostics
  exit 1
fi

printf '\n== Pull configured Selenoid browser images ==\n'
while IFS= read -r image; do
  if [[ -n "$image" ]] && ! docker pull "$image"; then
    printf 'Could not pull configured browser image: %s\n' "$image" >&2
    collect_startup_diagnostics
    exit 1
  fi
done < <(grep -E '^[[:space:]]*"image"[[:space:]]*:' infra/config/browsers.json | sed -E 's/.*"([^"]+)".*/\1/' | sort -u)

printf '\n== Wait for database query readiness ==\n'
if ! "${BASE_COMPOSE[@]}" up --detach --wait --wait-timeout 120 db || ! wait_for_database; then
  collect_startup_diagnostics
  exit 1
fi

if [[ -n "$APP_ALREADY_RUNNING" && "$RESTART_APP" == true ]]; then
  printf '\n== Restart the existing database ==\n'
  if ! "${BASE_COMPOSE[@]}" restart db || ! wait_for_database; then
    collect_startup_diagnostics
    exit 1
  fi
fi

printf '\n== Restore snapshot only when database has no OpenMRS schema ==\n'
if ! seed_database_if_empty; then
  collect_startup_diagnostics
  exit 1
fi

if [[ -n "$APP_ALREADY_RUNNING" && "$RESTART_APP" == true ]]; then
  printf '\n== Restart already-running application services ==\n'
  if ! "${BASE_COMPOSE[@]}" restart backend frontend gateway selenoid selenoid-ui; then
    collect_startup_diagnostics
    exit 1
  fi
fi

printf '\n== Start OpenMRS and Selenoid ==\n'
if ! "${BASE_COMPOSE[@]}" up --detach --wait gateway selenoid selenoid-ui; then
  collect_startup_diagnostics
  exit 1
fi

printf '\n== Run API and UI tests in the containerized runner ==\n'
if "${RUNNER_COMPOSE[@]}" run --build --rm --no-deps test-runner; then
  exit 0
else
  result=$?
  collect_startup_diagnostics
  exit "$result"
fi
