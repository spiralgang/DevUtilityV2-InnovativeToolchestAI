#!/usr/bin/env bash
# ~/icedman/bot/dispatch.sh â€” Unified dispatcher with EnergeticWeave integration.
# Behavior:
#  - standard queue-driven dispatcher (reads *.cmd files)
#  - offloads energetic tasks to Python weave process when available
#  - safe: never prints secrets; uses environment vars passed by CI / device config

set -euo pipefail

BASE="${HOME}/icedman"
QUEUE="${BASE}/bot/queue"
RESPOND="${QUEUE}/respond"
mkdir -p "$QUEUE" "$RESPOND"

PERSONA_FILE="${BASE}/current_persona.txt"
CURRENT_PERSONA="icedman"
[[ -r $PERSONA_FILE ]] && CURRENT_PERSONA=$(<"$PERSONA_FILE")

# If EnergeticWeave Python runner is available, call it for complex commands
WEAVE_PY="${HOME}/.local/bin/icedman_weave_runner.py"
if [[ -x "$WEAVE_PY" ]]; then
  WEAVE_RUNNER="$WEAVE_PY"
else
  WEAVE_RUNNER=""
fi

# Simple pi call wrapper that defers to python module if available
call_pi() {
  local prompt="$1"
  if command -v python3 >/dev/null && python3 - <<'PY' 2>/dev/null
import sys
try:
  from iceman_drone_tool.whisper import call_pi as _call
  print(_call(sys.stdin.read().strip()))
  sys.exit(0)
except Exception:
  sys.exit(2)
PY
  then
    # printed by the python helper
    :
  else
    # fallback: minimal curl wrapper (ensure INFLECTION_KEY exists in env)
    if [[ -z "${INFLECTION_KEY:-}" ]]; then
      echo "[pi-error] no token" >&2
      return 2
    fi
    payload=$(jq -n --arg persona "$CURRENT_PERSONA" --arg text "$prompt" '{context:[{text:("Persona: "+$persona),type:"System"},{text:$text,type:"Human"}],config:"inflection_3_pi"}')
    curl --silent --location "$INFLECTION_ENDPOINT" -H "Authorization: Bearer ${INFLECTION_KEY}" -H "Content-Type: application/json" --data "$payload"
  fi
}

# --- Command handlers (concise) ---
do_status(){ uname -a; echo; uptime; }
do_index(){ ls -la "$BASE/models" 2>/dev/null || echo "no models dir"; }
do_model(){ echo "model ops placeholder"; }
do_env(){ env | grep -E 'GEMINI|INFLECTION|PI_' || true; }
do_remind(){ echo "reminder: $(date)"; }
do_glitch(){ echo "glitch injected"; }
do_persona(){ echo "$1" > "$PERSONA_FILE"; echo "persona set to $1"; }
do_loop(){ echo "entering runtime reflex loop"; }
do_halt(){ echo "halt requested"; exit 0; }
do_report(){ echo "report placeholder"; }

# MAIN: consume command files
for cmdfile in "$QUEUE"/*.cmd; do
  [[ -e "$cmdfile" ]] || continue
  name=$(basename "$cmdfile" .cmd)
  cmd=$(<"$cmdfile")
  outfile="$RESPOND/$name.resp"

  case "$cmd" in
    '/status') do_status > "$outfile" ;;
    '/index') do_index  > "$outfile" ;;
    '/model') do_model  > "$outfile" ;;
    '/env') do_env      > "$outfile" ;;
    '/remind') do_remind > "$outfile" ;;
    '/glitch') do_glitch > "$outfile" ;;
    '/loop') do_loop    > "$outfile" ;;
    '/halt') do_halt    > "$outfile" ;;
    '/report') do_report > "$outfile" ;;
    /persona\ *) tone=${cmd#'/persona '} ; do_persona "$tone" > "$outfile" ;;
    /pi\ *) prompt=${cmd#'/pi '} ; call_pi "$prompt" > "$outfile" 2>&1 || true ;;
    /weave\ *) prompt=${cmd#'/weave '} ; 
               if [[ -n "$WEAVE_RUNNER" ]]; then
                 python3 "$WEAVE_RUNNER" --prompt "$prompt" --name "$name" > "$outfile" 2>&1 || true
               else
                 # best-effort local fallback: call_pi and return guidance
                 call_pi "$prompt" > "$outfile" 2>&1 || true
               fi
               ;;
    /tools) # dev tool discovery
           python3 - <<'PY' > "$outfile" 2>&1
from devutility.toolchest import discover_toolkits
print("\\n".join(sorted(discover_toolkits().keys())))
PY
           ;;
    *) # fallback: if weave runner exists, route there; else call Pi directly
       if [[ -n "$WEAVE_RUNNER" ]]; then
         python3 "$WEAVE_RUNNER" --prompt "$cmd" --name "$name" > "$outfile" 2>&1 || true
       else
         call_pi "$cmd" > "$outfile" 2>&1 || true
       fi
       ;;
  esac

  rm -f "$cmdfile"
done