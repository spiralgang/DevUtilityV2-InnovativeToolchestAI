#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

#
# userland_recover_techula_dotfiles.sh
# Enhanced extraction helper that explicitly detects and preserves hidden ("dot") files.
# Designed to run inside a UserLAnd chroot as root. Non-destructive by default (use --dry-run).
#
# Key behaviors added vs. prior scripts:
# - Always inspects and reports dotfiles (leading '.') at top-level and recursively.
# - Uses tar -C "$SRC" -czf "$OUT" . to ensure dotfiles are included in the archive.
# - When expanding globs, uses bash dotglob to include hidden files if necessary.
# - Adds explicit checks & counters for dotfiles so the presence (or absence) is obvious in output.
#
# Usage:
#   ./userland_recover_techula_dotfiles.sh --dry-run --source /data/data/tech.ula
#   ./userland_recover_techula_dotfiles.sh --source /data/data/tech.ula --out /tmp/techula.tgz --serve
#   ./userland_recover_techula_dotfiles.sh --help
#
set -euo pipefail
IFS=$'\n\t'

# Defaults
SOURCE="/data/data/tech.ula"
OUT="/tmp/techula-backup-$(date +%Y%m%d_%H%M%S).tgz"
DRY_RUN=false
SERVE=false
HTTP_BIND="127.0.0.1"
HTTP_PORT=8000
VERBOSE=false
QUIET=false
PID_DIR="/tmp/userland_recover"
PID_FILE="$PID_DIR/http.pid"
LOG_FILE="$PID_DIR/http.log"

log(){ [ "$QUIET" = false ] && printf '%s\n' "$*"; }
vlog(){ [ "$VERBOSE" = true ] && printf '[v] %s\n' "$*"; }
die(){ printf 'ERROR: %s\n' "$*" >&2; exit 1; }

usage(){
  cat <<USAGE
Usage: $(basename "$0") [options]
  --source PATH     path to the app data dir (default: $SOURCE)
  --out PATH        output archive (default: $OUT)
  --dry-run         only inspect, report and exit (no tar)
  --serve           after creating archive, start loopback http server (background)
  --port N          http port for --serve (default $HTTP_PORT)
  --verbose         extra debug output
  --quiet           minimal output
  -h, --help        show help
USAGE
  exit 1
}

# --- parse args ---
while [[ $# -gt 0 ]]; do
  case "$1" in
    --source) SOURCE="$2"; shift 2;;
    --out) OUT="$2"; shift 2;;
    --dry-run) DRY_RUN=true; shift;;
    --serve) SERVE=true; shift;;
    --port) HTTP_PORT="$2"; shift 2;;
    --verbose) VERBOSE=true; shift;;
    --quiet) QUIET=true; shift;;
    -h|--help) usage;;
    *) echo "Unknown arg: $1"; usage;;
  esac
done

mkdir -p "$(dirname "$OUT")" "$PID_DIR" 2>/dev/null || true

# normalize path
SOURCE="$(realpath -m "$SOURCE" 2>/dev/null || echo "$SOURCE")"
vlog "Resolved SOURCE -> $SOURCE"
vlog "OUT -> $OUT"

# helpers for dotfile detection
count_top_dotfiles(){
  # count top-level hidden (dot) entries in directory (exclude '.' and '..')
  local dir="$1"
  # use nullglob and dotglob to not break on no-match
  (
    shopt -s nullglob dotglob
    set -- "$dir"/.[!.]* "$dir"/..?*
    # count only existing entries
    cnt=0
    for p in "$@"; do [ -e "$p" ] && ((cnt++)); done
    printf '%d' "$cnt"
  )
}

count_recursive_dotfiles(){
  local dir="$1"
  # find files (not dirs) whose basename begins with '.' - case sensitive
  find "$dir" -type f -name '.*' 2>/dev/null | wc -l || echo 0
}

list_sample_dotfiles(){
  local dir="$1"
  find "$dir" -maxdepth 3 -name '.*' 2>/dev/null | head -n 40 || true
}

# quick readable check
is_listable_dir(){
  local d="$1"
  [ -d "$d" ] && ls -A "$d" >/dev/null 2>&1
}

# show permission and selinux hints
show_attrs(){
  local d="$1"
  echo "---- attributes for: $d ----"
  ls -ld "$d" 2>/dev/null || true
  if command -v stat >/dev/null 2>&1; then
    stat -c 'mode=%A uid=%u gid=%g size=%s mtime=%y %n' "$d" 2>/dev/null || true
  fi
  # ls -Z may not exist; ignore failures
  ls -ldZ "$d" 2>/dev/null || true
  echo "---- end attributes ----"
}

# attempt small read (non-destructive)
small_read_test(){
  local d="$1"
  local sample
  sample="$(find "$d" -type f -size -64k -print 2>/dev/null | head -n1 || true)"
  if [ -n "$sample" ]; then
    echo "Reading head bytes from sample file: $sample"
    head -c 256 "$sample" | sed -n '1,10p' || true
    echo "----- end sample head -----"
    return 0
  else
    echo "No small (<64k) readable files found quickly under $d (may still contain dotfiles but be blocked by permissions/SELinux)."
    return 1
  fi
}

# create archive ensuring dotfiles are included
create_archive(){
  local src="$1"
  local dest="$2"
  if [ ! -d "$src" ]; then
    # if src is a file, copy
    if [ -f "$src" ]; then
      cp -a "$src" "$dest"
      return 0
    fi
    die "Source not a directory or file: $src"
  fi

  # Use tar -C "$src" -czf "$dest" .  which includes all files including dotfiles.
  # This is intentionally done from inside the directory to preserve dotfiles.
  echo "Creating archive (this includes hidden files) -> $dest"
  if tar -C "$src" -czf "$dest" . 2>/tmp/userland_recover_tar.err; then
    echo "Archive created: $dest"
    ls -lh "$dest" || true
    rm -f /tmp/userland_recover_tar.err 2>/dev/null || true
    return 0
  else
    echo "tar failed; capture at /tmp/userland_recover_tar.err"
    head -n 200 /tmp/userland_recover_tar.err || true
    return 2
  fi
}

# serve archive on loopback
serve_bg(){
  local file="$1"
  if ! command -v python3 >/dev/null 2>&1; then
    die "python3 missing. Install: apt update && apt install -y python3"
  fi
  local dir name pid
  dir="$(dirname "$file")"
  name="$(basename "$file")"
  # avoid double-start
  if [ -f "$PID_FILE" ]; then
    pid="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      echo "HTTP server already running (PID $pid) -> http://${HTTP_BIND}:${HTTP_PORT}/${name}"
      return 0
    else
      rm -f "$PID_FILE" 2>/dev/null || true
    fi
  fi
  cd "$dir" || die "cannot cd to $dir"
  nohup python3 -m http.server "$HTTP_PORT" --bind "$HTTP_BIND" > "$LOG_FILE" 2>&1 &
  echo $! > "$PID_FILE"
  sleep 0.2
  pid="$(cat "$PID_FILE" 2>/dev/null || true)"
  if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
    echo "HTTP server started: http://${HTTP_BIND}:${HTTP_PORT}/${name}  (PID $pid)"
  else
    die "Failed to start HTTP server; see $LOG_FILE"
  fi
}

# ---- main ----
echo "userland_recover_techula_dotfiles: starting"
echo "Source: $SOURCE"
if ! is_listable_dir "$SOURCE"; then
  echo "WARNING: Source exists but is not listable/readable from this chroot (permission/SELinux may block)."
  show_attrs "$SOURCE"
  echo
  echo "Try host-root tar fallback (on device host shell):"
  echo "  su -c \"tar -C /data/user/0 -czf /sdcard/$(basename "$OUT") .\""
  exit 2
fi

echo "Directory appears listable. Showing attributes:"
show_attrs "$SOURCE"

# top-level hidden entries count
top_hidden_count="$(count_top_dotfiles "$SOURCE")"
echo "Top-level hidden (dot) entries count: $top_hidden_count"

# recursive hidden files count
rec_hidden_count="$(count_recursive_dotfiles "$SOURCE")"
echo "Recursive hidden files (dot-prefixed filenames) count: $rec_hidden_count"

if [ "$top_hidden_count" -gt 0 ] || [ "$rec_hidden_count" -gt 0 ]; then
  echo "Sample hidden entries (up to 40, depth 3):"
  list_sample_dotfiles "$SOURCE"
else
  echo "No dotfiles detected quickly. Still possible files are present but unreadable by chroot."
fi

# small read test (non-destructive)
small_read_test "$SOURCE" || true

if [ "$DRY_RUN" = true ]; then
  echo "Dry-run requested: stopping after inspection."
  exit 0
fi

# create archive (includes dotfiles)
if create_archive "$SOURCE" "$OUT"; then
  echo "Archive created successfully and includes dotfiles (tar command used '.')"
else
  echo "Archive creation failed. Inspect /tmp/userland_recover_tar.err for tar stderr."
  echo "If tar failed with permission errors, run on host (phone) shell as host-root:"
  echo "  su -c \"tar -C /data/user/0 -czf /sdcard/$(basename "$OUT") .\""
  exit 3
fi

if [ "$SERVE" = true ]; then
  serve_bg "$OUT"
  echo "Open on phone and download: http://${HTTP_BIND}:${HTTP_PORT}/$(basename "$OUT")"
  echo "Then use SAF-capable file manager (Solid/CX) -> Copy -> Local -> Open Document -> Android/data/tech.ula/files/storage -> Paste"
fi

echo "Done."