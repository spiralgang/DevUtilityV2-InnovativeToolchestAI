#!/usr/bin/env bash
# Start server in "uncensored" runtime mode.
# - Applies the non-destructive overrides (creates configs/*.override.json)
# - Exports GUARDRAIL_BYPASS=1 so code can detect bypass at runtime
# - Starts the backend server (python backend/server.py)
#
# Usage:
#   chmod +x scripts/start_uncensored.sh
#   ./scripts/start_uncensored.sh [-- <args passed to backend>]
#
# Notes:
# - This script does NOT delete any workflow or config files.
# - All changes are backed up to forensic_backups/guardrail_backup_<timestamp>/

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PYTHON=${PYTHON:-python3}

echo "[UNCENSORED START] Ensuring neutralizer exists and applying overrides..."
"$PYTHON" "$ROOT/scripts/guardrail_neutralizer.py" --apply

export GUARDRAIL_BYPASS=1
echo "[UNCENSORED START] GUARDRAIL_BYPASS=1 set in environment."

# Start backend server (adjust if your start command differs)
echo "[UNCENSORED START] Starting backend server (python backend/server.py)..."
exec "$PYTHON" "$ROOT/backend/server.py" "$@"
