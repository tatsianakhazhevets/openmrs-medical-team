# Reproducible test pipeline

The default execution path runs the OpenMRS stack, browser service, Java test suite, and report generation through Docker Compose. The repository's Maven version, Java version, service topology, and test invocation are the same locally and in CI; the main environment difference is the configured credentials and build metadata.

## Prerequisites

- Docker Desktop with Linux containers and Docker Compose v2 (or Docker Engine and Compose v2).
- At least 8 GB of memory available to Docker; 12 GB is recommended for the OpenMRS backend and browser containers.
- Network access to pull the pinned application, database, Maven, and browser images and resolve Maven dependencies.
- Bash (Git Bash or WSL on Windows) and Docker Desktop with Compose v2.
- The browser image configured in `infra/config/browsers.json` must be pulled before the first Selenoid run; Selenoid does not fetch browser images automatically.

Java and Maven are not required on the host. The pipeline runner image provides Maven, Java 17, jq, and XML tooling.

## First run

From the repository root, create `.env` from `.env.example` if it does not already exist, then adjust local values if needed. Keep real credentials out of source control. The example values are for an isolated local test environment, not a shared or production system.

```bash
bash infra/run-tests.sh
```

The script creates `.env` from `.env.example` if needed, starts MariaDB while pulling the browser images in `infra/config/browsers.json`, restores the bundled reference database snapshot only when the database has no OpenMRS schema, then starts OpenMRS and Selenoid. Already-running database/application/browser containers are restarted by default; a newly created stack starts once. Compose considers MariaDB ready only after a TCP query succeeds, and the backend health check requires a JSON REST session response; the gateway waits for healthy backend and frontend services. Before Maven runs, the runner also validates the REST session JSON and Selenoid endpoint. To keep already-running containers up without restarting them:

```bash
bash infra/run-tests.sh --skip-restart
```

This skips restarts of already-running database/application/browser containers; it does not change database state. A fresh database is restored from `infra/db/openmrs-test.sql` before the backend starts, avoiding the reference project's initial schema, metadata, and demo-data setup. The restored Lucene version is cleared so OpenMRS rebuilds its search index for the current runtime data.

## Common executions

Run all current API and UI tests and restart the application services:

```bash
bash infra/run-tests.sh
```

GitHub Actions runs Checkstyle as a separate step before creating the CI environment or starting Docker services. It checks production and test Java sources and fails the job on violations. To run the same check locally, use `mvn --batch-mode checkstyle:check`.

Run only `LaboratoryApiTests` without changing any test source:

```bash
MAVEN_ARGS='-Dtest=LaboratoryApiTests' bash infra/run-tests.sh
```

Use `--skip-restart` with the targeted command if you want to run against the current app containers. For alternate credentials or service URLs, set the corresponding Compose variables in `.env` or in the calling shell.

Stop only the application stack while preserving local data:

```bash
docker compose --env-file .env -f infra/docker-compose.yaml down
```

Reset local OpenMRS data when a clean database is needed:

```bash
docker compose --env-file .env -f infra/docker-compose.yaml down --volumes
```

The reset removes the named database volume. It does not delete the bind-mounted `infra/openmrs-data` directory or the separate Maven cache used by the runner. Do not remove the data directory unless you have inspected and backed up any local state you need.

## Configuration and secrets

Compose settings are loaded from the root `.env` file and can also be provided by the calling environment. The pipeline runner maps the existing application configuration keys to Compose service DNS names:

| Setting | Local default | Purpose |
|---|---|---|
| `OMRS_DB_HOST` | `db` | MariaDB service name as seen by OpenMRS |
| `OMRS_DB_NAME`, `OMRS_DB_USER`, `OMRS_DB_PASSWORD` | local-only values | Database initialization and backend connection |
| `MYSQL_ROOT_PASSWORD` | local-only value | MariaDB root account |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | `admin` / `Admin123` | Test application account |
| `API_BASE_URL`, `API_VERSION`, `OPENMRS_URL` | Local Compose gateway | API target and readiness URL |
| `UI_BASE_URL`, `UI_REMOTE`, `SELENOID_STATUS_URL` | Local gateway and Selenoid | UI target and Selenium endpoint |
| `OPENMRS_STARTUP_TIMEOUT_SECONDS` | `600` | Maximum readiness wait for the OpenMRS REST service |
| `UI_VIDEO` | `false` | Enable Selenoid video recording when configured |

For GitHub Actions, store the complete `.env` file contents as an `ENV_FILE` secret in the `Test` environment. The workflow also accepts an `ENV_FILE` Actions variable as a fallback, but variables are not masked and should only contain non-sensitive configuration; use a secret whenever the file contains credentials. The workflow writes this value to a permission-restricted `.env` file, uses it for Compose interpolation, and does not print its contents. Do not use production credentials or real patient data. Compose service logs redact common password, token, authorization, and URL-credential patterns; still protect artifacts and logs, which may contain synthetic request/response details. Artifact retention is seven days by default.

CI uses `infra/docker-compose.ci.yaml` to pair a fresh OpenMRS data volume with the fresh database volume. The runner waits until MariaDB accepts queries, restores `infra/db/openmrs-test.sql` into the empty CI database, and only then starts the backend. It mounts only the matching, read-only reference configuration checksums into the fresh data volume; local runtime properties, mutable application data, and Lucene files are not reused. Both named volumes are unique to the workflow run and removed during CI cleanup; local data is unchanged. The snapshot's `admin` password hash is regenerated with a fresh salt to match the target's test-only `Admin123` default; passwords and salts for other copied users are cleared, so the suite should authenticate using the configured admin account. The runner waits for a successful JSON REST session response, not a redirect to the installation wizard, before starting Maven. CI restores ownership of container-created `target` files before collecting service diagnostics.

The workflow, Compose runner default, and `.env.example` set the backend REST readiness limit to 600 seconds; an explicit environment or `.env` value overrides the default. Backend and frontend health retries also fail within that default startup budget, rather than leaving `docker compose up --wait` unbounded. The runner polls every five seconds, logs progress and the last HTTP status once per minute, and fails with service status and recent database/backend logs if startup or test execution fails. It does not treat a redirect or an HTML setup page as a ready API.

To run against an externally managed OpenMRS environment, set `API_BASE_URL`, `API_VERSION`, and `OPENMRS_URL` to URLs reachable from the runner container, set `UI_BASE_URL`, `UI_REMOTE`, and `SELENOID_STATUS_URL` if UI tests are also needed, then use `docker compose --env-file .env --project-name openmrs-tests -f infra/docker-compose.yaml -f infra/docker-compose.pipeline.yaml run --build --rm --no-deps test-runner`. The command does not start local services; the selected external target and remote WebDriver must already be reachable. Use a dedicated non-production environment.

## Reports and metrics

The runner writes these files under `target/pipeline/`:

- `summary.html` and `summary.md`: pass/fail/skip counts, rates, duration, and failure diagnostics.
- `metrics.json`: versioned run metadata, JUnit totals, stability indicators, and OpenAPI operation coverage.
- `openapi-coverage.html` and `openapi-operations.json`: operation-level coverage inventory.
- `swagger-coverage-report.html` and `swagger-coverage-results.json`: parameter/status-condition coverage from Swagger Coverage.
- `maven.log` and `swagger-coverage.log`: command output with Basic Authorization values redacted.

Swagger Coverage is invoked only when request capture files exist. If startup or test execution produces no captures, the pipeline records an explicit coverage error instead of invoking the CLI on an empty directory (which causes a `minResultTime` exception). The summary is still generated and the pipeline remains failed.

Raw Surefire XML, Allure result files/attachments, per-request Swagger captures, and Compose service logs are retained separately for investigation. In CI, the same files are uploaded as the `test-artifacts-<run>-<attempt>` artifact, linked from the run summary, and retained for seven days.

Report aggregation reads JSON and large failure diagnostics from files rather than command-line arguments. Report size therefore does not depend on the operating system's argument-size limit.

On pushes and manual runs on `main`, `master`, or `develop`, the workflow publishes an Allure report when Allure results are available, even when tests fail. History is retained on the `gh-pages` branch, and the generated site is deployed explicitly using GitHub Pages Actions. A push made with `GITHUB_TOKEN` does not itself trigger a branch-based Pages build. In repository **Settings > Pages > Build and deployment**, select **GitHub Actions** as the source, and allow these branches to deploy to the `github-pages` environment if deployment protection rules are configured. The `gh-pages` branch must exist before the first publication (an initial README-only branch is sufficient). The publication job summary links directly to the report for that run.

When generated, the Swagger condition-coverage HTML is included in that run's Allure report with a direct link, and is also retained in the workflow artifacts. Pull requests and runs on other branches do not publish. Publication runs separately after tests and does not mask the Maven test result; if no Allure results are generated, the publish job is skipped.

The checked-in Swagger 2.0 contract is a baseline snapshot (version 2.8.7); update it deliberately when the target OpenMRS API contract changes. Operation coverage is distinct from condition coverage: the former measures observed method/path pairs, while the latter measures declared request, response, and parameter conditions. Neither is currently a release threshold. `metrics.json` has a stable schema and commit/run identifiers; historical trend aggregation for these metrics is separate from Allure report history and is not configured.

## Troubleshooting

- **OpenMRS does not become ready:** inspect `target/pipeline/compose.log` in CI or run `docker compose ... logs backend db`. A partial schema is not overwritten automatically; reset the isolated database volume only after confirming its data can be discarded.
- **Database connection fails:** verify `OMRS_DB_HOST=db` and that the database credentials match the values used to initialize its volume. If credentials changed after initialization, reset the local volume.
- **UI tests cannot connect to a browser:** check Selenoid startup and `infra/config/browsers.json`; verify Docker can pull the configured Chrome image and the Docker daemon socket is available to Selenoid.
- **Port 80, 3306, or 4444 is busy:** stop the conflicting local service before starting the stack.
- **A report is missing after failure:** check `target/pipeline/maven.log`, `swagger-coverage.log`, `compose.log`, and the Surefire XML files. Reports are generated after the Maven process exits, including when tests fail.
- **Appointment creation fails near midnight:** set an explicit appointment time within the service day, including its duration. The UI defaults to the current browser time; a late-night start plus the duration can cross midnight and produce `Appointment time is outside of service hours`. The creation test uses 10:00 AM and waits for the form to close before checking the schedule.
- **Maven cannot resolve dependencies:** confirm outbound access to Maven Central and rerun; the named Maven cache retains successfully downloaded dependencies.
- **The Bash script cannot run in PowerShell:** use Git Bash or WSL with Docker Desktop integration, or run `bash infra/run-tests.sh` from a Bash terminal.

## Design decisions

- **Compose override instead of editing the application stack:** isolates test-runner concerns from the existing OpenMRS service definition and preserves local changes to that file.
- **One containerized test runner instead of host Maven or a second test image:** keeps Java and Maven versions consistent and makes the same command work locally and in CI. A named `.m2` volume avoids repeated downloads.
- **Shared request-filter instrumentation:** collects Swagger data at the existing Rest Assured request specification boundary without changing individual tests or assertions.
- **Built-in HTML/JSON reports plus JUnit and Allure raw data:** keeps the primary summary dependency-free while preserving detailed test and HTTP diagnostics. Allure history and published reports are retained on `gh-pages`.
- **No coverage threshold yet:** the checked-in API contract is broader than current tested resources and may differ from the runtime API. Reports make the gap visible without changing existing pass/fail behavior.
