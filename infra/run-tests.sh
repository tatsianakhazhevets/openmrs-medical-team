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

Starts the local OpenMRS and Selenoid services, restarts their containers,
then runs the test suite and generates reports in target/pipeline/.

Options:
  --skip-restart  Do not restart app/browser containers after startup.
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
RUNNER_COMPOSE=(docker compose --env-file "$ENV_FILE" --project-name "$TEST_PROJECT" -f infra/docker-compose.yaml -f infra/docker-compose.pipeline.yaml)

docker info >/dev/null
"${BASE_COMPOSE[@]}" -f infra/docker-compose.pipeline.yaml config --quiet

printf '\n== Pull configured Selenoid browser images ==\n'
while IFS= read -r image; do
  [[ -n "$image" ]] && docker pull "$image"
done < <(grep -E '^[[:space:]]*"image"[[:space:]]*:' infra/config/browsers.json | sed -E 's/.*"([^"]+)".*/\1/' | sort -u)

printf '\n== Start OpenMRS and Selenoid ==\n'
"${BASE_COMPOSE[@]}" up --detach --wait gateway selenoid selenoid-ui

if [[ "$RESTART_APP" == true ]]; then
  printf '\n== Restart Docker Compose application services ==\n'
  "${BASE_COMPOSE[@]}" restart db backend frontend gateway selenoid selenoid-ui
  "${BASE_COMPOSE[@]}" up --detach --wait gateway selenoid selenoid-ui
fi

printf '\n== Run API and UI tests in the containerized runner ==\n'
"${RUNNER_COMPOSE[@]}" run --build --rm --no-deps test-runner
