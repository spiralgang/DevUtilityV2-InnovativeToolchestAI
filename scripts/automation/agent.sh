#!/usr/bin/env bash
set -euo pipefail

# Offline, local-only
export TRANSFORMERS_OFFLINE=1
export HF_HUB_OFFLINE=1
export GIT_TERMINAL_PROMPT=0

# Colors
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; RED='\033[0;31m'; NC='\033[0m'

usage() { echo "Usage: $0 {run|plan|apply|dry-run|help}"; exit 1; }

cmd="${1:-run}"
mkdir -p logs scratch

case "$cmd" in
  run)
    echo -e "${BLUE}▶ Activating local agent...${NC}"
    python3 agent_core.py --mode run
    ;;
  plan|dry-run)
    echo -e "${BLUE}▶ Generating plan (no changes)...${NC}"
    python3 agent_core.py --mode plan
    ;;
  apply)
    echo -e "${BLUE}▶ Applying last plan...${NC}"
    if [[ -f scratch/plan.sh ]]; then
      chmod +x scratch/plan.sh
      bash scratch/plan.sh
      echo -e "${GREEN}✓ Plan applied${NC}"
    else
      echo -e "${RED}✗ No plan found at scratch/plan.sh${NC}"
      exit 1
    fi
    ;;
  help|--help|-h) usage ;;
  *) usage ;;
esac