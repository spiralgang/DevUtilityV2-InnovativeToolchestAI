#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PY="${PYTHON:-python3}"
STATE_DIR="${STATE_DIR:-${SCRIPT_DIR}/state}"

usage() {
  cat <<EOF
Tech.Ula Caretaker
  index              -> metadata index + duplicates plan
  consolidate        -> plan/symlink with --apply
  plan-binds         -> emit -b lines
  proot-audit        -> parse live proot binds
  xdg-env            -> generate XDG exports
  python-site        -> emit .pth for shared site
  content-index      -> full-text content index (FTS5 or fallback)
  fts-search <q>     -> run full-text query
  cdc-index          -> CDC chunking index
  cdc-similar <path> -> list files sharing many chunks
  simhash-index      -> compute simhash for text files
  simhash-near <path> [maxHD] -> near-duplicates by Hamming distance (default 8)
EOF
}

cmd="${1:-}"; shift || true
case "${cmd}" in
  index) "${PY}" "${SCRIPT_DIR}/scripts/index_and_consolidate.py" index --roots auto --hash --state "${STATE_DIR}" ;;
  consolidate) "${PY}" "${SCRIPT_DIR}/scripts/index_and_consolidate.py" consolidate --mode "${1:-plan}" ${2:+--apply} --state "${STATE_DIR}" ;;
  plan-binds) "${PY}" "${SCRIPT_DIR}/scripts/index_and_consolidate.py" plan-binds --emit --state "${STATE_DIR}" ;;
  proot-audit) bash "${SCRIPT_DIR}/scripts/proot_cmdline_audit.sh" ;;
  xdg-env) bash "${SCRIPT_DIR}/scripts/generate_profile_env.sh" --state "${STATE_DIR}" ;;
  python-site) "${PY}" "${SCRIPT_DIR}/scripts/python_site_unifier.py" --state "${STATE_DIR}" ;;
  content-index) "${PY}" "${SCRIPT_DIR}/scripts/content_indexer.py" --state "${STATE_DIR}" ;;
  fts-search)
    q="${1:-}"; [[ -n "$q" ]] || { echo "need query"; exit 2; }
    "${PY}" "${SCRIPT_DIR}/scripts/fts_search.py" --state "${STATE_DIR}" --query "$q"
    ;;
  cdc-index) "${PY}" "${SCRIPT_DIR}/scripts/chunker.py" index --state "${STATE_DIR}" ;;
  cdc-similar)
    p="${1:-}"; [[ -n "$p" ]] || { echo "need path"; exit 2; }
    "${PY}" "${SCRIPT_DIR}/scripts/chunker.py" similar --state "${STATE_DIR}" --path "$p"
    ;;
  simhash-index) "${PY}" "${SCRIPT_DIR}/scripts/near_duplicate.py" index --state "${STATE_DIR}" ;;
  simhash-near)
    p="${1:-}"; hd="${2:-8}"; [[ -n "$p" ]] || { echo "need path"; exit 2; }
    "${PY}" "${SCRIPT_DIR}/scripts/near_duplicate.py" near --state "${STATE_DIR}" --path "$p" --max-hd "${hd}"
    ;;
  *) usage ;;
esac