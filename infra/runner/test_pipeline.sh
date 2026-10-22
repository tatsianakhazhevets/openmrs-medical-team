#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/run_pipeline.sh"

WORK_DIR=$(mktemp -d)
trap 'rm -rf "$WORK_DIR"' EXIT

REPORTS="$WORK_DIR/reports"
PIPELINE="$WORK_DIR/pipeline"
CAPTURES="$WORK_DIR/captures"
SPEC="$ROOT/infra/openmrs-api.json"
mkdir -p "$REPORTS" "$PIPELINE" "$CAPTURES"

cat > "$REPORTS/TEST-sample.xml" <<'EOF'
<testsuite name="sample" tests="3" failures="1" errors="0" skipped="1">
  <testcase classname="Sample" name="passed"/>
  <testcase classname="Sample" name="failed">
    <failure message="assertion">failure trace</failure>
  </testcase>
  <testcase classname="Sample" name="skipped"><skipped/></testcase>
</testsuite>
EOF

cat > "$CAPTURES/capture-coverage.json" <<'EOF'
{"paths":{"/patient/abc/allergy":{"post":{}},"/not-declared":{"get":{}}}}
EOF

write_junit_summary "$PIPELINE/failures.jsonl" > "$PIPELINE/junit.json"
jq -e '.total == 3 and .passed == 1 and .failed == 1 and .skipped == 1 and .failures[0].trace == "failure trace"' \
  "$PIPELINE/junit.json" >/dev/null

write_openapi_coverage "$PIPELINE/observed.jsonl" > "$PIPELINE/openapi.json"
jq -e '.coveredOperations == 1 and .coveragePercent > 0 and .unexpectedOperations[0].path == "/not-declared"' \
  "$PIPELINE/openapi.json" >/dev/null

curl() {
  local output
  while (($#)); do
    if [[ "$1" == --output ]]; then
      output=$2
      shift
    fi
    shift
  done
  printf '%s' "$MOCK_BODY" > "$output"
  printf '%s' "$MOCK_STATUS"
}
sleep() { SECONDS=$((SECONDS + 5)); }
MOCK_STATUS=302
MOCK_BODY=''
if wait_for_url "Redirect" "http://unused" 1; then
  printf 'A redirect must not pass readiness.\n' >&2
  exit 1
fi
MOCK_STATUS=200
MOCK_BODY='<html>Installation wizard</html>'
session_filter='type == "object" and (.authenticated | type == "boolean")'
if wait_for_url "HTML" "http://unused" 1 "$session_filter"; then
  printf 'An HTML page must not pass REST readiness.\n' >&2
  exit 1
fi
MOCK_BODY='{"authenticated":false}'
wait_for_url "REST session" "http://unused" 1 "$session_filter"

# Exceed Linux's per-argument limit with both failure details and coverage JSON.
{
  printf '<testsuite tests="1" failures="1" errors="0" skipped="0"><testcase classname="Large" name="failed"><failure message="'
  awk 'BEGIN { for (i=0; i<150000; i++) printf "m" }'
  printf '">'
  awk 'BEGIN { for (i=0; i<200000; i++) printf "t" }'
  printf '</failure></testcase></testsuite>\n'
} > "$REPORTS/TEST-large.xml"
rm "$REPORTS/TEST-sample.xml"
printf '<testsuite tests="0" failures="0" errors="0" skipped="0"/>\n' > "$REPORTS/TEST-empty.xml"
SPEC="$WORK_DIR/large-spec.json"
jq '.paths += (reduce range(0; 2000) as $index ({};
  .["/pipeline-check-\($index)"] = {"get":{}}))' "$ROOT/infra/openmrs-api.json" > "$SPEC"

mvn() {
  if [[ "$*" == *"clean verify"* ]]; then
    printf 'Simulated failing test run.\n'
    return 7
  fi
  return 0
}
TARGET="$WORK_DIR"
if main; then
  printf 'A failing Maven run must still fail the pipeline.\n' >&2
  exit 1
fi
jq -e '.execution.status == "failed" and .execution.testExitCode == 7
  and .execution.coverageExitCode == 0 and .tests.total == 1
  and .tests.failed == 1 and (.tests.failures[0].trace | length) == 200000
  and (.tests.failures[0].message | length) == 150000
  and .openApi.totalOperations > 0' "$PIPELINE/metrics.json" >/dev/null
[[ $(wc -c < "$PIPELINE/openapi-operations.json") -gt 131072 ]]
for report in summary.md summary.html openapi-coverage.html; do
  [[ -s "$PIPELINE/$report" ]]
done

printf '<testsuite tests="1" failures="0" errors="0" skipped="0"><testcase name="passed"/></testsuite>\n' > "$REPORTS/TEST-large.xml"
mvn() { return 0; }
main
jq -e '.execution.status == "passed" and .execution.testExitCode == 0
  and .tests.passed == 1 and .tests.failed == 0' "$PIPELINE/metrics.json" >/dev/null

rm "$CAPTURES/capture-coverage.json"
mvn() {
  if [[ "$*" != *"clean verify"* ]]; then
    printf 'Swagger CLI must not be invoked without captures.\n' > "$WORK_DIR/unexpected-coverage-invocation"
  fi
  return 0
}
if main; then
  printf 'Missing Swagger captures must not produce a successful pipeline.\n' >&2
  exit 1
fi
[[ ! -f "$WORK_DIR/unexpected-coverage-invocation" ]]
grep -q 'no request captures' "$PIPELINE/swagger-coverage.log"
jq -e '.execution.status == "failed" and .execution.testExitCode == 0
  and .execution.coverageExitCode == 1 and .swaggerConditions == null
  and .openApi.coveredOperations == 0' "$PIPELINE/metrics.json" >/dev/null

MOCK_STATUS=302
OPENMRS_STARTUP_TIMEOUT_SECONDS=1
mvn() { touch "$WORK_DIR/unexpected-maven-invocation"; return 0; }
if main; then
  printf 'A readiness timeout must fail the pipeline.\n' >&2
  exit 1
fi
[[ ! -f "$WORK_DIR/unexpected-maven-invocation" ]]
jq -e '.execution.testExitCode == 1 and .execution.coverageExitCode == 1
  and .execution.status == "failed"' "$PIPELINE/metrics.json" >/dev/null

printf 'Pipeline shell checks passed.\n'
