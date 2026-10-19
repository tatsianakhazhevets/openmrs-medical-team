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

printf 'Pipeline shell checks passed.\n'
