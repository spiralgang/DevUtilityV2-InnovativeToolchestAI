#!/usr/bin/env bash
# userland_get_all_appdata.sh
# Robust helper to locate and extract Android /data/user/0 app data from inside a UserLAnd chroot.
# - Tries to find readable copies of /data/user/0 in common mapped locations
# - Dry-run lists what is visible and sizes without creating large archives
# - If readable, creates a timestamped tar.gz in /tmp (preserves files)
# - If not readable, prints exact host-side commands to run as host root (su -c) to produce an archive on /sdcard
# - Optional run-as attempt for a single package (e.g., tech.ula) when that package is debuggable
#
# Usage:
#   ./userland_get_all_appdata.sh --dry-run
#   ./userland_get_all_appdata.sh --source /data/user/0 --out /tmp/android_userdata.tgz
#   ./userland_get_all_appdata.sh --run-as tech.ula   # try run-as to archive that package's data
#   ./userland_get_all_appdata.sh --host-tar /sdcard/android_userdata.tgz
#
# WARNING: /data/user/0 can be very large. Use --dry-run to inspect before archiving.
set -euo pipefail
IFS=$'\n\t'

# Config (override via CLI)
OUT_DIR="/tmp"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
DEFAULT_OUT="${OUT_DIR}/android_userdata-${TIMESTAMP}.tar.gz"
OUT_FILE=""
DRY_RUN=false
ATTEMPT_RUN_AS=""     # package name for run-as attempt (optional)
FORCE=false
VERBOSE=false

log(){ [ "$VERBOSE" = true ] && printf '%s\n' "$*"; }
info(){ printf '%s\n' "$*"; }
err(){ printf 'ERROR: %s\n' "$*' >&2'; } 2>/dev/null || true

usage(){
  cat <<EOF
Usage: $(basename "$0") [options]

Options:
  --dry-run                Do not archive; list accessible paths, per-app sizes and file counts.
  --out PATH               Output archive path (default: $DEFAULT_OUT)
  --source PATH            Use this source path rather than auto-detection (example: /data/user/0)
  --run-as PACKAGE         Try 'run-as PACKAGE' to archive that package's data (works if package is debuggable)
  --host-tar PATH          (helper) If the chroot cannot read /data, run this host-side command as root:
                           su -c "tar -C /data/user/0 -czf /sdcard/<basename>"  (script prints exact command)
  --force                  Skip confirmation for large archives
  --verbose                Show extra debug logging
  -h, --help               Show this message
EOF
  exit 1
}

# CLI parse
SOURCE_OVERRIDE=""
HOST_TAR_SUGGEST=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=true; shift;;
    --out) OUT_FILE="$2"; shift 2;;
    --source) SOURCE_OVERRIDE="$2"; shift 2;;
    --run-as) ATTEMPT_RUN_AS="$2"; shift 2;;
    --host-tar) HOST_TAR_SUGGEST="$2"; shift 2;;
    --force) FORCE=true; shift;;
    --verbose) VERBOSE=true; shift;;
    -h|--help) usage;;
    *) echo "Unknown arg: $1"; usage;;
  esac
done

OUT_FILE="${OUT_FILE:-$DEFAULT_OUT}"
PIDLOG="/tmp/userland_get_all_appdata.pid"
LOG="/tmp/userland_get_all_appdata.log"

candidates=(
  "/data/user/0"
  "/host-rootfs/data/user/0"
  "/host-rootfs/system_root/data/user/0"
  "/host-rootfs/*/data/user/0"
  "/storage/internal"
  "/storage/internal/Android/data"
  "/storage/internal/Android/data/tech.ula"
  "/storage/internal/Android/data/tech.ula/files"
  "/storage/emulated/0"
  "/sdcard"
  "/mnt/sdcard"
  "/data/media/0"
  "/host-rootfs/data/data"    # some layouts
)

# Helper: test readability of a path (can list)
is_readable_dir(){
  local p="$1"
  if [ -d "$p" ] && ls -A "$p" >/dev/null 2>&1; then
    return 0
  fi
  return 1
}

# Show sizes/counts for immediate children (fast-ish)
report_dir_summary(){
  local p="$1"
  printf '\nSummary for: %s\n' "$p"
  # count immediate subdirs and files
  if ! is_readable_dir "$p"; then
    printf "  Not readable/listable from chroot (permission denied or not present)\n"
    return 1
  fi
  # list top-level package dirs under p (typical /data/user/0/<pkg>)
  for d in "$p"/*; do
    [ -e "$d" ] || continue
    if [ -d "$d" ]; then
      # count files and bytes (du may be expensive; show du -sh for each top dir but limit)
      size=$(du -sh "$d" 2>/dev/null | awk '{print $1}')
      files=$(find "$d" -type f 2>/dev/null | wc -l)
      printf "  %s  | size=%s files=%d\n" "$(basename "$d")" "$size" "$files"
    fi
  done
  # overall size
  total_size=$(du -sh "$p" 2>/dev/null | awk '{print $1}')
  printf "  TOTAL (approx): %s\n\n" "$total_size"
  return 0
}

# Attempt to auto-resolve a candidate that exists and is listable
resolve_candidate(){
  if [ -n "$SOURCE_OVERRIDE" ]; then
    if is_readable_dir "$SOURCE_OVERRIDE"; then
      echo "$SOURCE_OVERRIDE"
      return 0
    else
      echo ""
      return 1
    fi
  fi

  for pat in "${candidates[@]}"; do
    # expand globs in case of wildcards
    for p in $(eval echo $pat); do
      [ -n "$p" ] || continue
      if is_readable_dir "$p"; then
        echo "$p"
        return 0
      fi
    done
  done

  # try a narrow find in root for 'data/user/0' pattern (fast depth)
  found=$(find / -maxdepth 6 -type d -path '*/data/user/0' -print 2>/dev/null | head -n1 || true)
  if [ -n "$found" ] && is_readable_dir "$found"; then
    echo "$found"
    return 0
  fi

  # not found/readable
  echo ""
  return 1
}

# Main flow
info "Resolving possible Android app-data path (this searches mapped host roots)..."
RESOLVED="$(resolve_candidate || true)"

if [ -z "$RESOLVED" ]; then
  info "No readable /data/user/0 found inside chroot using common mappings."
  info ""
  info "Possible reasons:"
  info " - You are root inside the chroot, not host root. /data on the Android host is protected by SELinux unless host-root access is available."
  info " - The host's /data may be mounted under /host-rootfs or not mounted into the chroot."
  info ""
  info "I will still attempt a focused search for likely filenames, but the most reliable way (if you have host root) is to run the host-side tar command I print below."
  info ""
  # Find candidate files with your tech.ula path name
  base="tech.ula"
  info "Searching for evidence of tech.ula data paths (may take a moment)..."
  found_files=$(find / -maxdepth 6 -type f -iname '*tech.ula*' 2>/dev/null | head -n 10 || true)
  if [ -n "$found_files" ]; then
    info "Found candidates (first 10):"
    printf '%s\n' "$found_files"
  else
    info "No obvious tech.ula artifacts found in shallow search."
  fi

  # Print host-root helper commands to run on Android host (only if you have host root)
  suggested_host_tar="${HOST_TAR_SUGGEST:-/sdcard/android_userdata-${TIMESTAMP}.tar.gz"}"
  cat <<EOF

If you have host (Android) root, run this ON THE ANDROID HOST (in a host shell/emulator/terminal) to produce an archive the chroot can read:

  su -c "tar -C /data/user/0 -czf '${suggested_host_tar}' ."

After that completes, inside the chroot try:
  ls -lh /storage/internal/$(basename "${suggested_host_tar}")  || ls -lh /sdcard/$(basename "${suggested_host_tar}")  || ls -lh /storage/emulated/0/$(basename "${suggested_host_tar}")

If the archive appears in one of those paths, you can serve it (example):
  cd /storage/internal && python3 -m http.server 8000 --bind 127.0.0.1

And then download in Solid Explorer via:
  http://127.0.0.1:8000/$(basename "${suggested_host_tar}")

EOF
  exit 0
fi

info "Resolved readable path: $RESOLVED"
report_dir_summary "$RESOLVED"

if [ "$DRY_RUN" = true ]; then
  info "Dry-run requested; exiting after summary. Use --out to create the tar.gz."
  exit 0
fi

# Confirm large archive creation
approx_size=$(du -sh "$RESOLVED" 2>/dev/null | awk '{print $1}' || echo "unknown")
if [ "$FORCE" != true ]; then
  info "About to create archive of: $RESOLVED"
  info "Approx size: $approx_size"
  printf "Write archive to: %s\n" "$OUT_FILE"
  read -p "Proceed? (y/N) " yn
  case "$yn" in
    [Yy]*) ;;
    *) info "Aborted by user."; exit 0;;
  esac
fi

# Create archive (preserve as much as possible)
mkdir -p "$(dirname "$OUT_FILE")"
info "Creating archive (this may take time) -> $OUT_FILE"
# If source is directory, create tar preserving attributes, else copy file
if [ -d "$RESOLVED" ]; then
  # Use tar and try to preserve numeric owners; gzip for space
  tar -C "$RESOLVED" -czf "$OUT_FILE" . 2> "$LOG" || { err "tar failed; inspect $LOG"; exit 2; }
else
  # if user passed a file path
  cp -a "$RESOLVED" "$OUT_FILE"
fi

info "Archive created: $OUT_FILE"
info "ls -lh $OUT_FILE"
ls -lh "$OUT_FILE" || true

info ""
info "Next steps (on-phone):"
info " - In Solid Explorer or browser open: http://127.0.0.1:8000/$(basename "$OUT_FILE") after serving the containing folder"
info " - Or SFTP into 127.0.0.1:8022 (or 22) from Solid Explorer to pull from /tmp"
info ""
info "If you need a local HTTP serve command to run now, try:"
printf '  cd %s && python3 -m http.server 8000 --bind 127.0.0.1 &\n' "$(dirname "$OUT_FILE")"
info ""
info "If you wanted to use run-as for the package (attempted earlier), you can try:"
if [ -n "$ATTEMPT_RUN_AS" ]; then
  info "Attempting run-as $ATTEMPT_RUN_AS..."
  if command -v run-as >/dev/null 2>&1; then
    temp_host="/data/local/tmp/runas-${ATTEMPT_RUN_AS}-${TIMESTAMP}.tar.gz"
    info "If run-as works, this will create: $temp_host on host (then copy to /sdcard):"
    printf '  run-as %s sh -c "tar -C /data/data/%s -czf %s ."\n' "$ATTEMPT_RUN_AS" "$ATTEMPT_RUN_AS" "$temp_host"
  else
    info "run-as is not available in this environment."
  fi
fi

# Successful finish
info "Done."
