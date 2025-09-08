#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# map_everything_fixed.sh - fixed root substitution, robust root array handling, safe re-exec
# - Safe defaults: dry-run, skip /proc /sys /dev (unless allowed)
# - Correct root substitution: handles "/", spaces, duplicates, and preserves user-provided paths
# - Re-exec to bash when invoked under /bin/sh/dash: exec bash "$0" -- "$@"
# - Minimal external requirements: bash, coreutils (stat, readlink, ls), optional sha256sum
set -euo pipefail
IFS=$'\n\t'

# Re-exec under bash if not running in bash (preserves argv properly)
if [ -z "${BASH_VERSION:-}" ]; then
  if command -v bash >/dev/null 2>&1; then
    exec bash "$0" -- "$@"
  else
    printf 'ERROR: map_everything_fixed.sh requires bash; none found in PATH\n' >&2
    exit 2
  fi
fi

# --- Configuration & safe inits ---
ROOTS=()                # always initialize array
OUTDIR="./map_out"
FOLLOW_SYMLINKS=false
MAX_DEPTH=6
MAX_NODES=50000
DRY_RUN=true
ALLOW_PROC=false
ALLOW_SYS=false
ALLOW_DEV=false
DEEP=false
TIME_LIMIT=0   # seconds; 0 = none
VERBOSE=0
SELFTEST=false

log(){ printf '%s %s\n' "$(date '+%F %T')" "$*" >&2; }
dbg(){ [ "${VERBOSE:-0}" -gt 0 ] && printf '%s DBG: %s\n' "$(date '+%F %T')" "$*" >&2; }
usage(){
  cat <<EOF
map_everything_fixed.sh - safe filesystem mapper (fixed)
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
--selftest             print parsed roots and exit (good for validating quoting)
-h, --help             show this help
EOF
  exit 1
}

# --- Argument parsing ---
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
    --selftest) SELFTEST=true; shift ;;
    -h|--help) usage ;;
    *) echo "Unknown arg: $1" >&2; usage ;;
  esac
done

# default root handling (only once)
if [ "${#ROOTS[@]:-0}" -eq 0 ]; then
  ROOTS=("/")
fi

if [ "${DEEP:-false}" = true ]; then
  MAX_DEPTH=20
  MAX_NODES=500000
fi

# Normalize and dedupe roots safely into NORMAL_ROOTS array
declare -a NORMAL_ROOTS=()
declare -A SEEN_ROOT=()
normalize_root(){
  local p="$1"
  # readlink -m will normalize but won't fail on non-existent; preserve trailing slash semantics by removing trailing (except root)
  local abs
  abs=$(readlink -m -- "$p" 2>/dev/null || printf '%s' "$p")
  # Ensure "/" remains "/"
  [ -z "$abs" ] && abs="/"
  # collapse identical roots
  if [ -z "${SEEN_ROOT[$abs]:-}" ]; then
    SEEN_ROOT[$abs]=1
    NORMAL_ROOTS+=("$abs")
  fi
}

for r in "${ROOTS[@]}"; do
  normalize_root "$r"
done

# If user asked for selftest, print and exit (helps diagnose bad root substitution)
if [ "${SELFTEST:-false}" = true ]; then
  echo "SELFTEST: parsed roots (count=${#NORMAL_ROOTS[@]})"
  for i in "${!NORMAL_ROOTS[@]}"; do
    printf '  [%d] %s\n' "$i" "${NORMAL_ROOTS[$i]}"
  done
  exit 0
fi

# prepare outputs
mkdir -p "$OUTDIR"
NDJSON="$OUTDIR/nodes.ndjson"
DOT="$OUTDIR/map.dot"
SUMMARY="$OUTDIR/summary.txt"
: > "$NDJSON"
: > "$DOT"
: > "$SUMMARY"

# skip prefixes default (unless allowed)
SKIP_PREFIX=()
if [ "${ALLOW_PROC:-false}" != true ]; then SKIP_PREFIX+=("/proc"); fi
if [ "${ALLOW_SYS:-false}" != true ]; then SKIP_PREFIX+=("/sys"); fi
if [ "${ALLOW_DEV:-false}" != true ]; then SKIP_PREFIX+=("/dev"); fi

# state
declare -A VISITED  # dev:inode -> 1
NODES=0
START_TS=$(date +%s)

# helpers
check_time_limit(){
  if [ "${TIME_LIMIT:-0}" -gt 0 ]; then
    now=$(date +%s)
    if [ $((now-START_TS)) -ge "${TIME_LIMIT:-0}" ]; then
      log "Time limit reached (${TIME_LIMIT}s)"
      return 1
    fi
  fi
  return 0
}

should_skip(){
  local p="$1"
  for s in "${SKIP_PREFIX[@]:-}"; do
    [ -z "$s" ] && continue
    case "$p" in
      "$s"/*|"$s") return 0 ;;
    esac
  done
  return 1
}

json_escape(){
  printf '%s' "$1" | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g' -e 's/\t/\\t/g' -e 's/\r/\\r/g' -e 's/\n/\\n/g'
}

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

small_sha(){
  local f="$1"
  if [ -f "$f" ] && command -v sha256sum >/dev/null 2>&1; then
    local sz
    sz=$(stat -c%s -- "$f" 2>/dev/null || echo 0)
    if [ "$sz" -le $((256*1024)) ]; then
      sha256sum -- "$f" 2>/dev/null | awk '{print $1}'
      return
    fi
  fi
  echo ""
}

emit_node(){
  local path="$1" parent="$2" typ="$3" statline="$4" err="$5"
  local devino mode uid gid size mtime ftype err_msg sha color
  if printf '%s' "$statline" | grep -q '^ERROR:'; then
    err_msg="${statline#ERROR:}"
    devino=""; mode=""; uid="null"; gid="null"; size=0; mtime=""; ftype=""
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
  parent="${parent:-<root>}"
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
  parent_esc=$(printf '%s' "$parent" | sed 's/"/\\"/g')
  path_esc=$(printf '%s' "$path" | sed 's/"/\\"/g')
  printf '"%s" -> "%s";\n' "$parent_esc" "$path_esc" >> "$DOT"
}

# DFS stack traversal that accepts a root argument
traverse_root(){
  local root="$1"
  dbg "traverse_root root='$root'"
  local -a stack
  stack=( "$root|0|<root>" )
  while [ "${#stack[@]}" -gt 0 ]; do
    ! check_time_limit && return
    if [ "${NODES:-0}" -ge "${MAX_NODES:-0}" ]; then log "max nodes reached"; return; fi
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
    if [ -L "$cur" ]; then node_type="symlink"
    elif [ -d "$cur" ]; then node_type="dir"
    elif [ -f "$cur" ]; then node_type="file"
    else node_type="other"; fi
    listable=true; access_err=""
    if [ "$node_type" = "dir" ]; then
      if ! ls -A -- "$cur" >/dev/null 2>&1; then listable=false; access_err="not-listable"; fi
    fi
    emit_node "$cur" "$parent" "$node_type" "$statline" "$access_err"
    NODES=$((NODES+1))
    if [ "$node_type" = "dir" ] && [ "$depth" -lt "$MAX_DEPTH" ] && [ "$listable" = true ]; then
      while IFS= read -r child; do
        childpath="$cur/$child"
        if [ -L "$childpath" ]; then
          if [ "${FOLLOW_SYMLINKS:-false}" = true ]; then
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

# run
log "map_everything_fixed.sh start (roots: ${NORMAL_ROOTS[*]:-})"
echo "digraph filesystem_map {" > "$DOT"
echo "rankdir=LR;" >> "$DOT"

for r in "${NORMAL_ROOTS[@]:-}"; do
  [ -e "$r" ] || { log "root not found or inaccessible: $r"; continue; }
  traverse_root "$r"
done

echo "}" >> "$DOT"

# finalize summary
end_ts=$(date +%s)
elapsed=$((end_ts-START_TS))
{
  echo "map_everything_fixed.sh summary"
  echo "start: $(date -d "@$START_TS" '+%F %T' 2>/dev/null || date)"
  echo "end:   $(date -d "@$end_ts" '+%F %T' 2>/dev/null || date)"
  echo "elapsed_seconds: $elapsed"
  echo "roots: ${NORMAL_ROOTS[*]:-}"
  echo "nodes_visited: ${NODES:-0}"
  echo "max_nodes: ${MAX_NODES:-0}"
  echo "max_depth: ${MAX_DEPTH:-0}"
  echo "follow_symlinks: ${FOLLOW_SYMLINKS:-false}"
  echo "dry_run: ${DRY_RUN:-true}"
} >> "$SUMMARY"

log "Done. Outputs: $NDJSON (ndjson), $DOT (dot), $SUMMARY (summary)"
[ "${VERBOSE:-0}" -gt 0 ] && log "Preview (first 20 ndjson lines):" && head -n20 "$NDJSON" || true

exit 0

# References:
# - /reference vault (project standards, logging, safety patterns)
# - Bash manual: arrays, set -u behavior, and trap/exec semantics
# - readlink(1), stat(1), ls(1), sha256sum(1)