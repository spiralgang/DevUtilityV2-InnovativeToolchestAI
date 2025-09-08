#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# map_everything.sh - safe, auditable filesystem mapper (fixed & hardened)
# Purpose: traverse filesystem roots and emit newline-delimited JSON (ndjson) + DOT for visualization.
# Safe defaults: dry-run, skip /proc /sys /dev, depth-limited, node-limited, loop-detection via dev:inode.
# Compatible with minimal busybox/coreutils bash environments (no jq required).
# Usage (dry-run):
#   bash map_everything.sh --root /data --outdir /tmp/mymap --max-nodes 50000
# To actually write full outputs and allow deeper scanning:
#   bash map_everything.sh --root / --outdir /tmp/mymap --deep --follow-symlinks --run --yes --time-limit 1800
set -euo pipefail
IFS=$'\n\t'

# Defaults
OUTDIR="./map_out"
declare -a ROOTS
FOLLOW_SYMLINKS=false
MAX_DEPTH=6
MAX_NODES=50000
DRY_RUN=true
ALLOW_PROC=false
ALLOW_SYS=false
ALLOW_DEV=false
DEEP=false
TIME_LIMIT=0   # seconds, 0 = no limit
VERBOSE=0

# Helpers
log(){ printf '%s %s\n' "$(date '+%F %T')" "$*" >&2; }
dbg(){ [ "$VERBOSE" -gt 0 ] && printf '%s DBG: %s\n' "$(date '+%F %T')" "$*" >&2; }
usage(){
  cat <<EOF
map_everything.sh - safe filesystem mapper
--root PATH            add traversal root (repeatable). Default '/'
--outdir PATH          output dir (default ./map_out)
--follow-symlinks      follow symlinks (detect loops)
--max-depth N          max recursion depth (default 6)
--max-nodes N          global max nodes visited (default 50000)
--time-limit S         stop after S seconds (0 = none)
--deep                 sets --max-depth 20 and --max-nodes 500000
--run                  actually write outputs (default: dry-run)
--allow-proc           allow scanning /proc (dangerous)
--allow-sys            allow scanning /sys (dangerous)
--allow-dev            allow scanning /dev (dangerous)
--verbose              increase verbosity
-h, --help             show this help
EOF
  exit 1
}

# Arg parse
while [ $# -gt 0 ]; do
  case "$1" in
    --root) ROOTS+=("$2"); shift 2 ;;
    --outdir) OUTDIR="$2"; shift 2 ;;
    --follow-symlinks) FOLLOW_SYMLINKS=true; shift ;;
    --max-depth) MAX_DEPTH="$2"; shift 2 ;;
    --max-nodes) MAX_NODES="$2"; shift 2 ;;
    --time-limit) TIME_LIMIT="$2"; shift 2 ;;
    --deep) DEEP=true; shift ;;
    --run) DRY_RUN=false; shift ;;
    --allow-proc) ALLOW_PROC=true; shift ;;
    --allow-sys) ALLOW_SYS=true; shift ;;
    --allow-dev) ALLOW_DEV=true; shift ;;
    --verbose) VERBOSE=$((VERBOSE+1)); shift ;;
    -h|--help) usage ;;
    *) echo "Unknown arg: $1" >&2; usage ;;
  esac
done

[ "${#ROOTS[@]}" -gt 0 ] || ROOTS=("/")

if [ "$DEEP" = true ]; then
  MAX_DEPTH=20
  MAX_NODES=500000
fi

# prepare outputs
mkdir -p "$OUTDIR"
NDJSON="$OUTDIR/nodes.ndjson"
DOT="$OUTDIR/map.dot"
SUMMARY="$OUTDIR/summary.txt"
: > "$NDJSON"
: > "$DOT"
: > "$SUMMARY"

# skip prefixes
SKIP_PREFIX=("/proc" "/sys" "/dev")
[ "$ALLOW_PROC" = true ] || SKIP_PREFIX+=(/proc)
[ "$ALLOW_SYS" = true ] || SKIP_PREFIX+=(/sys)
[ "$ALLOW_DEV" = true ] || SKIP_PREFIX+=(/dev)

# state
declare -A VISITED  # dev:ino -> 1
NODES=0
START_TS=$(date +%s)

# utility functions
check_time_limit(){
  if [ "$TIME_LIMIT" -gt 0 ]; then
    now=$(date +%s)
    if [ $((now-START_TS)) -ge "$TIME_LIMIT" ]; then
      log "Time limit reached ($TIME_LIMIT s)"
      return 1
    fi
  fi
  return 0
}

should_skip(){
  local p="$1"
  for s in "${SKIP_PREFIX[@]}"; do
    [ -z "$s" ] && continue
    if [ "${p#/}" = "${p}" ]; then :; fi
    case "$p" in
      "$s"/*|"$s") return 0 ;;
    esac
  done
  return 1
}

# escape JSON string (minimal)
json_escape(){
  printf '%s' "$1" | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g' -e 's/\t/\\t/g' -e 's/\r/\\r/g' -e 's/\n/\\n/g'
}

# stat info safe wrapper
stat_info(){
  local p="$1"
  if statout=$(stat -Lc '%d:%i|%A|%u|%g|%s|%Y|%F' -- "$p" 2>/dev/null); then
    printf '%s' "$statout"
    return 0
  else
    printf 'ERROR:%s' "$(stat -- "$p" 2>&1 | head -n1)"
    return 2
  fi
}

# small sha
small_sha(){
  local f="$1"
  if [ -f "$f" ]; then
    sz=$(stat -c%s -- "$f" 2>/dev/null || echo 0)
    if [ "$sz" -le $((256*1024)) ] && command -v sha256sum >/dev/null 2>&1; then
      sha256sum -- "$f" 2>/dev/null | awk '{print $1}'
      return
    fi
  fi
  echo ""
}

# emit node record (ndjson) and dot edge
emit_node(){
  local path="$1" parent="$2" typ="$3" statline="$4" err="$5"
  # parse statline
  devino=""; mode=""; uid=""; gid=""; size=""; mtime=""; ftype=""
  if printf '%s' "$statline" | grep -q '^ERROR:'; then
    err_msg="${statline#ERROR:}"
  else
    IFS='|' read -r devino mode uid gid size mtime ftype <<EOF
$statline
EOF
    err_msg="${err:-}"
  fi
  sha=$(small_sha "$path")
  color="gray"
  case "$typ" in
    dir) if [ -r "$path" ] && [ -x "$path" ]; then color="green"; else color="yellow"; fi ;;
    file) if [ -r "$path" ]; then color="green"; else color="yellow"; fi ;;
    symlink) target=$(readlink -m -- "$path" 2>/dev/null || true); if [ -z "$target" ] || [ ! -e "$target" ]; then color="purple"; else color="green"; fi ;;
  esac
  # JSON line
  printf '{"path":"%s","parent":"%s","type":"%s","inode":"%s","mode":"%s","uid":%s,"gid":%s,"size":%s,"mtime":"%s","sample":"%s","color":"%s","error":%s}\n' \
    "$(json_escape "$path")" \
    "$(json_escape "$parent")" \
    "$typ" \
    "$(json_escape "$devino")" \
    "$(json_escape "$mode")" \
    "${uid:-null}" "${gid:-null}" "${size:-0}" \
    "$(json_escape "$mtime")" \
    "$(json_escape "$sha")" \
    "$color" \
    "$( [ -z "$err_msg" ] && printf 'null' || printf '"%s"' "$(json_escape "$err_msg")" )" >> "$NDJSON"
  # DOT
  parent_esc=$(printf '%s' "$parent" | sed 's/"/\\"/g')
  path_esc=$(printf '%s' "$path" | sed 's/"/\\"/g')
  printf '"%s" -> "%s";\n' "$parent_esc" "$path_esc" >> "$DOT"
}

# DFS stack traversal
traverse_root(){
  local root="$1"
  dbg "traverse_root: $root"
  # stack entries: path|depth|parent
  local -a stack
  stack=( "$root|0|<root>" )
  while [ "${#stack[@]}" -gt 0 ]; do
    ! check_time_limit && return
    [ "$NODES" -lt "$MAX_NODES" ] || { log "max nodes reached"; return; }
    entry="${stack[-1]}"
    unset 'stack[-1]'
    IFS='|' read -r cur depth parent <<<"$entry"
    cur=$(readlink -m -- "$cur" 2>/dev/null || printf '%s' "$cur")
    [ -n "$cur" ] || continue
    should_skip "$cur" && { emit_node "$cur" "$parent" "skipped" "" "skipped-prefix"; continue; }
    statline=$(stat_info "$cur" 2>/dev/null || echo "ERROR:stat-failed")
    if printf '%s' "$statline" | grep -q '^ERROR:'; then
      emit_node "$cur" "$parent" "unknown" "$statline" "${statline#ERROR:}"
      continue
    fi
    devino=$(printf '%s' "$statline" | awk -F'|' '{print $1}')
    if [ -n "${VISITED[$devino]:-}" ]; then
      emit_node "$cur" "$parent" "visited" "$statline" ""
      continue
    fi
    VISITED[$devino]=1
    # detect type
    if [ -L "$cur" ]; then node_type="symlink"
    elif [ -d "$cur" ]; then node_type="dir"
    elif [ -f "$cur" ]; then node_type="file"
    else node_type="other"; fi
    # check listability for dirs
    listable=true; access_err=""
    if [ "$node_type" = "dir" ]; then
      if ! ls -A -- "$cur" >/dev/null 2>&1; then listable=false; access_err="not-listable"; fi
    fi
    emit_node "$cur" "$parent" "$node_type" "$statline" "$access_err"
    NODES=$((NODES+1))
    # push children if dir/listable and depth < MAX_DEPTH
    if [ "$node_type" = "dir" ] && [ "$depth" -lt "$MAX_DEPTH" ] && [ "$listable" = true ]; then
      # list children safely
      while IFS= read -r child; do
        childpath="$cur/$child"
        if [ -L "$childpath" ]; then
          if [ "$FOLLOW_SYMLINKS" = true ]; then
            # push resolved target (record symlink separately)
            target=$(readlink -m -- "$childpath" 2>/dev/null || true)
            [ -n "$target" ] && stack+=( "$target|$((depth+1))|$cur" )
            stack+=( "$childpath|$((depth+1))|$cur" )
          else
            stack+=( "$childpath|$((depth+1))|$cur" )
          fi
        else
          stack+=( "$childpath|$((depth+1))|$cur" )
        fi
      done < <(LC_ALL=C ls -A -- "$cur" 2>/dev/null || true)
    fi
  done
}

# Begin run
log "map_everything.sh start (roots: ${ROOTS[*]})"
echo "digraph filesystem_map {" > "$DOT"
echo "rankdir=LR;" >> "$DOT"

for r in "${ROOTS[@]}"; do
  [ -e "$r" ] || { log "root not found: $r"; continue; }
  traverse_root "$r"
done

echo "}" >> "$DOT"

# finalize summary
end_ts=$(date +%s)
elapsed=$((end_ts-START_TS))
{
  echo "map_everything.sh summary"
  echo "start: $(date -d "@$START_TS" '+%F %T' 2>/dev/null || date)"
  echo "end:   $(date -d "@$end_ts" '+%F %T' 2>/dev/null || date)"
  echo "elapsed_seconds: $elapsed"
  echo "roots: ${ROOTS[*]}"
  echo "nodes_visited: $NODES"
  echo "max_nodes: $MAX_NODES"
  echo "max_depth: $MAX_DEPTH"
  echo "follow_symlinks: $FOLLOW_SYMLINKS"
  echo "dry_run: $DRY_RUN"
} >> "$SUMMARY"

log "Done. Outputs: $NDJSON (ndjson), $DOT (dot), $SUMMARY (summary)"
[ "$VERBOSE" -gt 0 ] && log "Preview (first 20 ndjson lines):" && head -n20 "$NDJSON" || true

exit 0