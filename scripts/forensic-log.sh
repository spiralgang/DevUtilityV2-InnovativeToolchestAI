#!/usr/bin/env bash
set -euo pipefail

mkdir -p logs
LOG_FILE="logs/activation.jsonl"

jqsafe() { python3 - <<'PY' "$@"
import json,sys
print(json.dumps(sys.argv[1]))
PY
}

ts() { date -u +%Y-%m-%dT%H:%M:%SZ; }

case "${1:-}" in
  start)
    echo "{ \"ts\": \"$(ts)\", \"phase\": \"start\", \"actor\": $(jqsafe "${ACTOR:-unknown}"), \"event\": $(jqsafe "${EVENT:-unknown}"), \"ref\": $(jqsafe "${REF:-unknown}"), \"sha\": $(jqsafe "${SHA:-unknown}"), \"run_id\": $(jqsafe "${RUN_ID:-unknown}") }" >> "$LOG_FILE"
    ;;
  step)
    step="${2:-unnamed}"
    msg="${3:-}"
    echo "{ \"ts\": \"$(ts)\", \"phase\": \"step\", \"name\": $(jqsafe "$step"), \"msg\": $(jqsafe "$msg") }" >> "$LOG_FILE"
    ;;
  end)
    echo "{ \"ts\": \"$(ts)\", \"phase\": \"end\" }" >> "$LOG_FILE"
    ;;
  *)
    echo "usage: $0 start|step <name> [msg]|end" >&2
    exit 2
    ;;
esac
