#!/usr/bin/env bash
set -uo pipefail

ROOT=/workspace
TARGET="$ROOT/target"
PIPELINE="$TARGET/pipeline"
REPORTS="$TARGET/surefire-reports"
SPEC="$ROOT/infra/openmrs-api.json"
CAPTURES="$TARGET/swagger-coverage-output"

log() {
  printf '[%s] [pipeline] %s\n' "$(date -u +'%Y-%m-%dT%H:%M:%SZ')" "$*"
}

redact_log() {
  mkdir -p "$(dirname "$2")"
  sed -E 's/(Authorization[[:space:]]*[:=][[:space:]]*Basic[[:space:]]+)[A-Za-z0-9+/=]+/\1[REDACTED]/Ig' "$1" > "$2"
}

run_maven() {
  local output_file=$1
  shift
  local raw_log
  raw_log=$(mktemp)
  log "Running Maven command"
  "$@" >"$raw_log" 2>&1
  local result=$?
  redact_log "$raw_log" "$output_file"
  rm -f "$raw_log"
  log "Maven command completed with exit code $result"
  return "$result"
}

wait_for_url() {
  local label=$1
  local url=$2
  local timeout=$3
  local response_filter=${4:-}
  local deadline=$((SECONDS + timeout))
  local status response_file
  response_file=$(mktemp)

  while (( SECONDS < deadline )); do
    status=$(curl --silent --show-error --output "$response_file" --write-out '%{http_code}' --max-time 5 "$url" 2>/dev/null || true)
    if [[ "$status" =~ ^2[0-9][0-9]$ ]]; then
      if [[ -z "$response_filter" ]] || jq -e "$response_filter" "$response_file" >/dev/null 2>&1; then
        rm -f "$response_file"
        log "$label is ready (HTTP $status)"
        return 0
      fi
    fi
    sleep 5
  done

  rm -f "$response_file"
  log "$label did not become ready within ${timeout}s: $url"
  return 1
}

xml_sum() {
  local expression=$1
  shift
  local total=0 file value
  for file in "$@"; do
    [[ -f "$file" ]] || continue
    value=$(xmllint --nonet --xpath "$expression" "$file" 2>/dev/null || printf '0')
    total=$(awk -v total="$total" -v value="${value:-0}" 'BEGIN { printf "%.0f", total + value }')
  done
  printf '%s' "$total"
}

write_junit_summary() {
  local failures_file=$1
  local total=0 failed=0 skipped=0 file failure_count index name suite details_dir
  details_dir=$(mktemp -d)
  local -a report_files=()
  shopt -s nullglob
  report_files=("$REPORTS"/TEST-*.xml)
  shopt -u nullglob

  for file in "${report_files[@]}"; do
    total=$((total + $(xml_sum 'string(sum(/*[local-name()="testsuite"]/@tests | /*[local-name()="testsuites"]/*[local-name()="testsuite"]/@tests))' "$file")))
    failed=$((failed + $(xml_sum 'string(sum(/*[local-name()="testsuite"]/@failures | /*[local-name()="testsuites"]/*[local-name()="testsuite"]/@failures | /*[local-name()="testsuite"]/@errors | /*[local-name()="testsuites"]/*[local-name()="testsuite"]/@errors))' "$file")))
    skipped=$((skipped + $(xml_sum 'string(sum(/*[local-name()="testsuite"]/@skipped | /*[local-name()="testsuites"]/*[local-name()="testsuite"]/@skipped))' "$file")))

    failure_count=$(xmllint --nonet --xpath 'count(//*[local-name()="testcase"][*[local-name()="failure" or local-name()="error"]])' "$file" 2>/dev/null || printf '0')
    failure_count=${failure_count%.*}
    for ((index = 1; index <= failure_count; index++)); do
      name=$(xmllint --nonet --xpath "string((//*[local-name()=\"testcase\"][*[local-name()=\"failure\" or local-name()=\"error\"]])[$index]/@name)" "$file" 2>/dev/null || true)
      suite=$(xmllint --nonet --xpath "string((//*[local-name()=\"testcase\"][*[local-name()=\"failure\" or local-name()=\"error\"]])[$index]/@classname)" "$file" 2>/dev/null || true)
      xmllint --nonet --xpath "string((//*[local-name()=\"testcase\"][*[local-name()=\"failure\" or local-name()=\"error\"]])[$index]/*[local-name()=\"failure\" or local-name()=\"error\"]/@message)" "$file" > "$details_dir/message" || return 1
      xmllint --nonet --xpath "string((//*[local-name()=\"testcase\"][*[local-name()=\"failure\" or local-name()=\"error\"]])[$index]/*[local-name()=\"failure\" or local-name()=\"error\"]/text())" "$file" > "$details_dir/trace" || return 1
      jq -n --arg suite "$suite" --arg test "$name" --rawfile message "$details_dir/message" --rawfile trace "$details_dir/trace" \
        '{suite:$suite,test:$test,message:($message | rtrimstr("\n")),trace:($trace | rtrimstr("\n"))}' >> "$failures_file" || return 1
    done
  done
  rm -f "$details_dir/message" "$details_dir/trace"
  rmdir "$details_dir"

  local passed=$((total - failed - skipped))
  (( passed < 0 )) && passed=0
  local executed=$((total - skipped))
  local success_rate=0 failure_rate=0 skipped_rate=0
  if (( executed > 0 )); then
    success_rate=$(awk -v p="$passed" -v e="$executed" 'BEGIN { printf "%.2f", p/e*100 }')
    failure_rate=$(awk -v f="$failed" -v e="$executed" 'BEGIN { printf "%.2f", f/e*100 }')
  fi
  if (( total > 0 )); then
    skipped_rate=$(awk -v s="$skipped" -v t="$total" 'BEGIN { printf "%.2f", s/t*100 }')
  fi
  jq -s --argjson total "$total" --argjson passed "$passed" --argjson failed "$failed" \
    --argjson skipped "$skipped" --argjson successRate "$success_rate" \
    --argjson failureRate "$failure_rate" --argjson skippedRate "$skipped_rate" \
    '{total:$total,passed:$passed,failed:$failed,skipped:$skipped,successRatePercent:$successRate,failureRatePercent:$failureRate,skippedRatePercent:$skippedRate,suites:[],failures:.}' \
    "$failures_file"
}

write_openapi_coverage() {
  local observed_file=$1
  local capture
  : > "$observed_file"
  shopt -s nullglob
  local -a capture_files=("$CAPTURES"/*-coverage.json)
  shopt -u nullglob

  for capture in "${capture_files[@]}"; do
    jq -c '[.paths // {} | to_entries[] as $path | $path.value | to_entries[]
      | select(.key | IN("get","put","post","delete","options","head","patch"))
      | {path:$path.key,method:(.key|ascii_downcase)}]' "$capture" >> "$observed_file" || return 1
  done
  jq -s 'add // [] | unique_by([.path,.method])' "$observed_file" > "$observed_file.tmp" || return 1
  mv "$observed_file.tmp" "$observed_file" || return 1

  jq -n --slurpfile spec "$SPEC" --slurpfile observed "$observed_file" '
    def methods: ["get","put","post","delete","options","head","patch"];
    def path_matches($actual; $template):
      ($actual | split("/")) as $actual_parts
      | ($template | split("/")) as $template_parts
      | ($actual_parts | length) == ($template_parts | length)
        and all(range(0; ($actual_parts | length));
          . as $index
          | ($template_parts[$index] | test("^\\{[^{}]+\\}$"))
            or $actual_parts[$index] == $template_parts[$index]);
    ($spec[0] | .paths | to_entries
      | map(. as $path | $path.value | to_entries[]
        | select(.key | IN("get","put","post","delete","options","head","patch"))
        | {path:$path.key,method:(.key|ascii_downcase)})) as $declared
    | ($observed[0] // []) as $seen
    | [$declared[] as $operation
      | select(any($seen[]; .method == $operation.method and path_matches(.path; $operation.path)))
      | $operation] as $covered
    | [$seen[] as $operation
      | select(([$declared[] | select(.method == $operation.method and path_matches($operation.path; .path))] | length) == 0)
      | $operation] as $unexpected
    | {
        specification: "infra/openmrs-api.json",
        apiVersion: $spec[0].info.version,
        basePath: ($spec[0].basePath // ""),
        totalEndpoints: ([$declared[].path] | unique | length),
        coveredEndpoints: ([$covered[].path] | unique | length),
        totalOperations: ($declared | length),
        coveredOperations: ($covered | length),
        coveragePercent: (if ($declared | length) == 0 then 0 else ((($covered | length) / ($declared | length) * 10000 | round) / 100) end),
        operations: [$declared[] as $operation
          | $operation + {covered:any($covered[]; .path == $operation.path and .method == $operation.method)}],
        unexpectedOperations: $unexpected
      }'
}

main() {
  local started_at pipeline_started=$SECONDS
  started_at=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
  mkdir -p "$PIPELINE" "$CAPTURES"
  rm -f "$PIPELINE"/maven.log "$PIPELINE"/swagger-coverage.log "$PIPELINE"/swagger-coverage-results.json "$PIPELINE"/swagger-coverage-report.html

  local test_exit=0 coverage_exit=0 test_started=$SECONDS test_duration=0 pipeline_duration
  if ! wait_for_url "OpenMRS" "${OPENMRS_URL:-http://host.docker.internal/openmrs/ws/rest/v1/session}" "${OPENMRS_STARTUP_TIMEOUT_SECONDS:-1200}" 'type == "object" and (.authenticated | type == "boolean")'; then
    test_exit=1
  elif ! wait_for_url "Selenoid" "${SELENOID_STATUS_URL:-http://host.docker.internal:4444/status}" 300; then
    test_exit=1
  else
    local raw_maven_log
    raw_maven_log=$(mktemp)
    test_started=$SECONDS
    log "Running Maven test suite"
    # Word splitting is intentional for caller-supplied Maven arguments.
    mvn --batch-mode --no-transfer-progress ${MAVEN_ARGS:-} clean verify >"$raw_maven_log" 2>&1
    test_exit=$?
    mkdir -p "$PIPELINE"
    redact_log "$raw_maven_log" "$PIPELINE/maven.log"
    rm -f "$raw_maven_log"
    test_duration=$((SECONDS - test_started))
    log "Maven test suite completed with exit code $test_exit"
  fi

  mkdir -p "$PIPELINE"
  (
    cd "$PIPELINE"
    run_maven "$PIPELINE/swagger-coverage.log" mvn --batch-mode --no-transfer-progress \
      --file "$ROOT/infra/coverage-pom.xml" \
      org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
      -Dexec.classpathScope=test \
      -Dexec.mainClass=com.github.viclovsky.swagger.coverage.CommandLine \
      "-Dexec.args=-s $SPEC -i $CAPTURES"
  ) || coverage_exit=$?

  local failures_file="$PIPELINE/junit-failures.jsonl"
  : > "$failures_file"
  local tests_file="$PIPELINE/junit-summary.json"
  local api_file="$PIPELINE/openapi-operations.json"
  local condition_json completed_at
  if ! write_junit_summary "$failures_file" > "$tests_file"; then
    log "Failed to generate JUnit summary" >&2
    return 1
  fi
  if ! write_openapi_coverage "$PIPELINE/observed-operations.jsonl" > "$api_file"; then
    log "Failed to generate OpenAPI coverage" >&2
    return 1
  fi
  rm -f "$failures_file" "$PIPELINE/observed-operations.jsonl"

  condition_json=null
  if [[ -s "$PIPELINE/swagger-coverage-results.json" ]]; then
    condition_json=$(jq '{totalConditions:.conditionCounter.all,coveredConditions:.conditionCounter.covered,coveragePercent:(if .conditionCounter.all == 0 then 0 else ((.conditionCounter.covered / .conditionCounter.all * 10000 | round) / 100) end)}' "$PIPELINE/swagger-coverage-results.json")
  fi

  completed_at=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
  pipeline_duration=$((SECONDS - pipeline_started))
  local status=passed
  (( test_exit != 0 || coverage_exit != 0 )) && status=failed

  jq -n \
    --arg environment "${BUILD_ENVIRONMENT:-local}" \
    --arg startedAt "$started_at" --arg completedAt "$completed_at" \
    --arg runUrl "${GITHUB_RUN_URL:-}" --arg runId "${GITHUB_RUN_ID:-}" \
    --arg runAttempt "${GITHUB_RUN_ATTEMPT:-}" --arg commit "${GITHUB_SHA:-}" \
    --arg branch "${GITHUB_REF_NAME:-}" --arg actor "${GITHUB_ACTOR:-}" \
    --arg repository "${GITHUB_REPOSITORY:-}" --arg status "$status" \
    --argjson testExit "$test_exit" --argjson coverageExit "$coverage_exit" \
    --argjson duration "$pipeline_duration" --argjson testDuration "$test_duration" --slurpfile tests "$tests_file" \
    --slurpfile openApi "$api_file" --argjson conditions "$condition_json" '
      $tests[0] as $tests | $openApi[0] as $openApi |
      {
        schemaVersion:1,
        execution:{
          environment:$environment,startedAt:$startedAt,completedAt:$completedAt,
          durationSeconds:$duration,runUrl:(if $runUrl == "" then null else $runUrl end),
          runId:(if $runId == "" then null else $runId end),
          runAttempt:(if $runAttempt == "" then null else $runAttempt end),
          commit:(if $commit == "" then null else $commit end),
          branch:(if $branch == "" then null else $branch end),
          actor:(if $actor == "" then null else $actor end),
          repository:(if $repository == "" then null else $repository end),
          testExitCode:$testExit,coverageExitCode:$coverageExit,
          testDurationSeconds:$testDuration,status:$status
        },
        tests:$tests,openApi:$openApi,swaggerConditions:$conditions,
        stability:{
          failureRatePercent:$tests.failureRatePercent,
          skippedRatePercent:$tests.skippedRatePercent,
          trendAvailable:false,
          trendNote:"Metrics are versioned by run and commit; persistent history is not configured."
        },
        artifacts:[
          "target/pipeline/summary.html","target/pipeline/summary.md",
          "target/pipeline/metrics.json","target/pipeline/openapi-coverage.html",
          "target/pipeline/openapi-operations.json","target/pipeline/swagger-coverage-report.html",
          "target/surefire-reports/","target/allure-results/",
          "target/pipeline/maven.log","target/pipeline/swagger-coverage.log"
        ]
      }' > "$PIPELINE/metrics.json" || {
        log "Failed to generate pipeline metrics" >&2
        return 1
      }
  rm -f "$tests_file"

  local total passed failed skipped success_rate covered operations coverage_percent
  total=$(jq -r '.tests.total' "$PIPELINE/metrics.json")
  passed=$(jq -r '.tests.passed' "$PIPELINE/metrics.json")
  failed=$(jq -r '.tests.failed' "$PIPELINE/metrics.json")
  skipped=$(jq -r '.tests.skipped' "$PIPELINE/metrics.json")
  success_rate=$(jq -r '.tests.successRatePercent' "$PIPELINE/metrics.json")
  covered=$(jq -r '.openApi.coveredOperations' "$PIPELINE/metrics.json")
  operations=$(jq -r '.openApi.totalOperations' "$PIPELINE/metrics.json")
  coverage_percent=$(jq -r '.openApi.coveragePercent' "$PIPELINE/metrics.json")

  {
    printf '# Test execution summary\n\n'
    printf '**Result:** %s  \n' "${status^^}"
    printf '**Environment:** %s  \n' "${BUILD_ENVIRONMENT:-local}"
    printf '**Commit:** %s  \n' "${GITHUB_SHA:-local}"
    printf '**Branch:** %s\n\n' "${GITHUB_REF_NAME:-local}"
    printf '| Metric | Result |\n|---|---:|\n'
    printf '| Total tests | %s |\n| Passed | %s |\n| Failed | %s |\n| Skipped | %s |\n' "$total" "$passed" "$failed" "$skipped"
    printf '| Success rate (excluding skipped) | %s%% |\n| Test duration | %ss |\n' "$success_rate" "$test_duration"
    printf '| OpenAPI operations covered | %s / %s (%s%%) |\n\n' "$covered" "$operations" "$coverage_percent"
    printf '**Reports:** [HTML summary](summary.html), [OpenAPI operations](openapi-coverage.html), [Swagger conditions](swagger-coverage-report.html), and [metrics](metrics.json).\n'
    printf '**Artifacts:** `target/pipeline/` locally; download the workflow artifact from the run summary in CI.\n\n'
    printf '## Failures\n\n'
    jq -r '.tests.failures[] | "- `\(.suite).\(.test)`: \((.message // "No failure message") | gsub("\n"; " "))"' "$PIPELINE/metrics.json"
  } > "$PIPELINE/summary.md"

  local failure_rows
  failure_rows=$(jq -r '.tests.failures[] | "<tr><td>\(.suite | @html)</td><td>\(.test | @html)</td><td><pre>\(.message | @html)</pre></td></tr>"' "$PIPELINE/metrics.json")
  [[ -n "$failure_rows" ]] || failure_rows='<tr><td colspan="3">No failed tests.</td></tr>'
  cat > "$PIPELINE/summary.html" <<EOF
<!doctype html><html lang="en"><head><meta charset="utf-8"><title>Test execution summary</title>
<style>body{font:16px system-ui,sans-serif;max-width:1000px;margin:2rem auto;padding:0 1rem;color:#202124}table{border-collapse:collapse;width:100%;margin:1rem 0}th,td{border:1px solid #dadce0;padding:.6rem;text-align:left;vertical-align:top}th{background:#f1f3f4}pre{white-space:pre-wrap;overflow-wrap:anywhere}.failed{color:#b3261e}.passed{color:#137333}</style></head><body>
<h1>Test execution summary</h1><p class="$status"><strong>${status^^}</strong> — ${BUILD_ENVIRONMENT:-local}</p>
<table><tr><th>Metric</th><th>Value</th></tr><tr><td>Total tests</td><td>$total</td></tr><tr><td>Passed</td><td>$passed</td></tr><tr><td>Failed</td><td>$failed</td></tr><tr><td>Skipped</td><td>$skipped</td></tr><tr><td>Success rate</td><td>$success_rate%</td></tr><tr><td>Duration</td><td>${test_duration}s</td></tr><tr><td>OpenAPI operations</td><td>$covered / $operations ($coverage_percent%)</td></tr></table>
<h2>Failure diagnostics</h2><table><tr><th>Suite</th><th>Test</th><th>Failure</th></tr>$failure_rows</table>
<p><a href="openapi-coverage.html">OpenAPI operation coverage</a> · <a href="swagger-coverage-report.html">Swagger condition coverage</a></p></body></html>
EOF

  jq -r '
    "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\"><title>OpenAPI coverage</title>" +
    "<style>body{font:16px system-ui,sans-serif;max-width:1100px;margin:2rem auto;padding:0 1rem;color:#202124}table{border-collapse:collapse;width:100%}th,td{border:1px solid #dadce0;padding:.5rem;text-align:left}th{background:#f1f3f4}</style></head><body>" +
    "<h1>OpenAPI operation coverage</h1><p>Specification version \(.openApi.apiVersion): \(.openApi.coveredOperations) of \(.openApi.totalOperations) operations covered (\(.openApi.coveragePercent)%).</p>" +
    "<p>Endpoint paths covered: \(.openApi.coveredEndpoints) of \(.openApi.totalEndpoints).</p><table><tr><th>Method</th><th>Path</th><th>Status</th></tr>" +
    ([.openApi.operations[] | "<tr><td>\(.method)</td><td>\(.path | @html)</td><td>\(if .covered then "Covered" else "Not covered" end)</td></tr>"] | join("")) +
    "</table></body></html>"
  ' "$PIPELINE/metrics.json" > "$PIPELINE/openapi-coverage.html"

  log "Tests: $passed passed, $failed failed, $skipped skipped"
  log "OpenAPI: $covered/$operations operations covered ($coverage_percent%)"
  log "Reports written to $PIPELINE"
  (( test_exit == 0 && coverage_exit == 0 ))
}

if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
  main "$@"
fi
