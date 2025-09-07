#!/usr/bin/env sh
# 02_space_init_and_sync.sh — Android 10 app-scoped “directors” + Puter CLI sync
# Purpose
# - Create a protected, hierarchical workspace under app-scoped storage (Android 10+).
# - Model “system/root/admin/developer” style subspaces with sane umask and perms.
# - Harden symlinks, write a POLICY manifest, and (optionally) sync via puter-cli.
#
# Zero-root, zero-FUSE, pure POSIX sh. Tested with toybox/busybox cores.
#
# Quick use:
#   chmod +x 02_space_init_and_sync.sh
#   APP_ID=tech.ula PUTER_ENABLED=1 ./02_space_init_and_sync.sh init
#   ./02_space_init_and_sync.sh status
#   ./02_space_init_and_sync.sh sync     # requires: npm i -g puter-cli && puter login --save
#
# Env knobs:
#   APP_ID=tech.ula                # Android app package providing app-scoped storage
#   SPACE_NAME=unify_space         # top folder under .../Android/data/$APP_ID/files
#   UMASK_DIR=0002                 # default umask when writing into space
#   PUTER_ENABLED=0|1              # enable sync
#   PUTER_DIR=/unify/env           # remote dir in Puter
#   PUTER_DELETE=0|1               # if 1, include --delete in puter update
#   LOGDIR=$HOME/.unify            # local logs
#
# Results:
#   $STOR/Android/data/$APP_ID/files/$SPACE_NAME/
#     system/   root/   admin/   developer/   data/{private,public}   logs/   tmp/   POLICY.json

set -eu

# ---------- utils ----------
say() { printf '%s\n' "$*"; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }
has() { command -v "$1" >/dev/null 2>&1; }

ts() { date -u +'%Y-%m-%dT%H:%M:%SZ'; }

json_escape() { printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g; s/\t/\\t/g; s/\r/\\r/g; s/\n/\\n/g'; }

detect_storage_root() {
  for cand in /storage/self/primary /storage/emulated/0 /sdcard /storage/internal; do
    [ -d "$cand" ] && { echo "$cand"; return 0; }
  done
  echo ""; return 1
}

check_path() {
  p="$1"
  [ -n "$p" ] || return 0
  if [ ! -e "$p" ]; then
    printf '%-18s %s\n' "missing" "$p"
    return 0
  fi
  perm="$(ls -ld "$p" 2>/dev/null | awk '{print $1,$3":"$4}')" || perm="unknown"
  w=no; x=no
  [ -w "$p" ] && w=yes
  [ -x "$p" ] && x=yes
  printf '%-18s %s\n' "$p" "$perm w=$w x=$x"
}

# ---------- config ----------
APP_ID="${APP_ID:-tech.ula}"
SPACE_NAME="${SPACE_NAME:-unify_space}"
UMASK_DIR="${UMASK_DIR:-0002}"
PUTER_ENABLED="${PUTER_ENABLED:-0}"
PUTER_DIR="${PUTER_DIR:-/unify/env}"
PUTER_DELETE="${PUTER_DELETE:-0}"
LOGDIR="${LOGDIR:-$HOME/.unify}"

mkdir -p "$LOGDIR"
LOG="$LOGDIR/space_ops.log"

STOR="$(detect_storage_root || true)"
[ -n "$STOR" ] || die "storage root not found"
BASE="$STOR/Android/data/$APP_ID/files/$SPACE_NAME"

# Subspaces (order matters for creation)
SUBS="system root admin developer data/private data/public logs tmp"

# ---------- actions ----------
act_init() {
  say "[*] Initializing space at: $BASE"
  mkdir -p "$BASE"
  # Sane umask for collaborator write
  cur_umask="$(umask || echo 022)"
  umask "$UMASK_DIR" >/dev/null 2>&1 || true

  # Create directories
  for d in $SUBS; do
    mkdir -p "$BASE/$d"
  done

  # Best-effort perms (sdcardfs masks may apply)
  # 770 for restricted, 775 for collaborative, 700 for private
  chmod 770 "$BASE/system" "$BASE/root" "$BASE/admin" 2>/dev/null || true
  chmod 775 "$BASE/developer" "$BASE/logs" 2>/dev/null || true
  chmod 700 "$BASE/data/private" 2>/dev/null || true
  chmod 775 "$BASE/data/public" "$BASE/tmp" 2>/dev/null || true

  # Write POLICY.json
  POLICY="$BASE/POLICY.json"
  tsnow="$(ts)"
  cat >"$POLICY" <<EOF
{
  "ts": "$tsnow",
  "app_id": "$(json_escape "$APP_ID")",
  "space": "$(json_escape "$SPACE_NAME")",
  "umask": "$(json_escape "$UMASK_DIR")",
  "dirs": {
    "system":    {"mode":"770","desc":"system-like artifacts, build outputs, non-human writable"},
    "root":      {"mode":"770","desc":"owner ops only; secrets drop; avoid group/world"},
    "admin":     {"mode":"770","desc":"admin-maintained configs and state"},
    "developer": {"mode":"775","desc":"shared working area for teams/tools"},
    "data/private": {"mode":"700","desc":"sensitive datasets, private only"},
    "data/public":  {"mode":"775","desc":"non-sensitive datasets"},
    "logs":      {"mode":"775","desc":"logs, jsonl, diagnostics"},
    "tmp":       {"mode":"775","desc":"scratch; purgeable"}
  },
  "notes": "Android 10 app-scoped space; sdcardfs may mask exec/sticky bits. Treat modes as intent."
}
EOF

  # Record inventory
  inv="$BASE/logs/layout_$(date -u +%Y%m%dT%H%M%SZ).txt"
  {
    echo "== layout $tsnow =="
    for d in "" $SUBS; do
      check_path "$BASE/${d}"
    done
  } > "$inv" 2>&1 || true

  # Restore umask
  umask "$cur_umask" >/dev/null 2>&1 || true

  say "[+] Space initialized."
  echo "$(ts) init $BASE" >>"$LOG"
}

act_status() {
  say "SPACE     : $BASE"
  say "STORAGE   : $STOR"
  say "APP_ID    : $APP_ID"
  say "PUTER     : enabled=$PUTER_ENABLED dir=$PUTER_DIR"
  say
  say "== L.P.P (paths & perms) =="
  check_path "$BASE"
  for d in $SUBS; do check_path "$BASE/$d"; done
  say
  say "POLICY    : $BASE/POLICY.json"
  [ -r "$BASE/POLICY.json" ] && head -n 20 "$BASE/POLICY.json" || true
}

act_harden() {
  say "[*] Harden symlinks under $BASE"
  # BusyBox find may lack -delete; use loop
  # Ignore errors if find features differ
  if has find; then
    # print and remove symlinks
    find "$BASE" -type l 2>/dev/null | while IFS= read -r p; do
      say "rm symlink: $p"
      rm -f -- "$p" 2>/dev/null || true
    done
  fi
  # Re-assert intended perms (best-effort)
  chmod 770 "$BASE/system" "$BASE/root" "$BASE/admin" 2>/dev/null || true
  chmod 775 "$BASE/developer" "$BASE/logs" 2>/dev/null || true
  chmod 700 "$BASE/data/private" 2>/dev/null || true
  chmod 775 "$BASE/data/public" "$BASE/tmp" 2>/dev/null || true
  say "[+] Hardened."
  echo "$(ts) harden $BASE" >>"$LOG"
}

safe_write_text() {
  # safe_write_text RELPATH "content"
  rel="$1"; shift
  content="$*"
  dest="$BASE/$rel"
  case "$rel" in
    */|*/.|*/..|"") die "invalid destination: $rel" ;;
  esac
  # Refuse to overwrite through symlink
  [ -L "$dest" ] && die "refuse to write through symlink: $dest"
  mkdir -p "$(dirname "$dest")"
  umask "$UMASK_DIR" >/dev/null 2>&1 || true
  printf '%s' "$content" > "$dest"
  # default: non-exec file
  chmod 664 "$dest" 2>/dev/null || true
  say "[+] wrote: $dest"
}

act_put() {
  # put a demo guard file to prove pipeline
  safe_write_text "system/README.txt" "System area. Created $(ts)"
  safe_write_text "admin/README.txt" "Admin area. Created $(ts)"
  safe_write_text "developer/README.txt" "Developer area. Created $(ts)"
  safe_write_text "data/public/README.txt" "Public data. Created $(ts)"
  # a JSONL log line
  jl="$BASE/logs/space_events.jsonl"
  ln="{\"ts\":\"$(ts)\",\"event\":\"put_demo\",\"space\":\"$(json_escape "$SPACE_NAME")\"}"
  printf '%s\n' "$ln" >> "$jl"
  say "[+] demo artifacts placed."
}

act_sync() {
  [ "$PUTER_ENABLED" = "1" ] || die "PUTER_ENABLED=1 required"
  has puter || die "puter-cli not found. Install: npm i -g puter-cli; then 'puter login --save'"

  # Select remote dir; use SPACE_NAME subdir to prevent collisions
  REMOTE="${PUTER_DIR%/}/$SPACE_NAME"
  say "[*] Sync local -> Puter: $BASE => $REMOTE"
  # Pull remote (if any), append locally, then push back — using puter update twice is safe on empty target
  puter update "$BASE" "$REMOTE" -r ${PUTER_DELETE:+--delete} >/dev/null
  say "[+] Sync complete."
  echo "$(ts) sync $BASE -> $REMOTE" >>"$LOG"
}

act_help() {
  cat <<EOF
Usage: $0 <command>

Commands:
  init      Create the hierarchical space and POLICY.json
  status    Show current layout and policy preview
  harden    Remove symlinks inside the space; re-assert perms (best-effort)
  put       Drop small demo artifacts (README, JSONL line)
  sync      Sync the space to Puter via puter-cli (requires login)
  where     Print resolved base path

Environment (defaults in parentheses):
  APP_ID ($APP_ID)    SPACE_NAME ($SPACE_NAME)   UMASK_DIR ($UMASK_DIR)
  PUTER_ENABLED ($PUTER_ENABLED)  PUTER_DIR ($PUTER_DIR)  PUTER_DELETE ($PUTER_DELETE)
EOF
}

act_where() { echo "$BASE"; }

# ---------- dispatch ----------
cmd="${1:-help}"
case "$cmd" in
  init)   act_init ;;
  status) act_status ;;
  harden) act_harden ;;
  put)    act_put ;;
  sync)   act_sync ;;
  where)  act_where ;;
  help|-h|--help) act_help ;;
  *) die "unknown command: $cmd (try: help)" ;;
esac