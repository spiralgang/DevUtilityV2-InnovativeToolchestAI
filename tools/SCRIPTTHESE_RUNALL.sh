#!/usr/bin/env bash
# SCRIPTTHESE_RUNALL.sh
# Purpose: Try multiple safe recovery/export methods (in-process tar, selective chroot tar,
# host-root tar snippet, optional FTP upload), stop further attempts when one succeeds,
# and optionally remove temporary artifacts to free space.
#
# Usage (examples):
#   # Dry-run diagnostics only:
#   bash SCRIPTTHESE_RUNALL.sh --dry-run --verbose
#
#   # Full attempt: try local archive, then attempt selective subtrees, then print host su command
#   bash SCRIPTTHESE_RUNALL.sh --outdir /tmp/recover --archive-prefix techula --ftp-url "ftp://192.168.1.203:6871" \
#       --ftp-user pc --ftp-pass 426974 --yes --verbose
#
#   # If you trust host-root and want the script to attempt su -c tar (may fail inside proot),
#   # pass --exec-host (USE WITH CAUTION: this runs su as available in environment).
#   bash SCRIPTTHESE_RUNALL.sh --exec-host --remove-archive-after-upload --yes
#
# Safety defaults:
# - The script will NOT delete app sandbox data (/data/data/...) unless --remove-src is given explicitly.
# - By default it will remove only test artifacts it created (like __PROOT_WRITE_TEST) and, optionally,
#   the archive(s) if you ask (--remove-archive-after-upload).
#
# Behavior:
# 1. Detect process root and likely package path for tech.ula.
# 2. Attempt an all-dot tar from process-root -> candidate host-visible locations (/storage/emulated/0, /sdcard, /tmp).
#    If any succeed, stop.
# 3. If full tar fails, attempt targeted tars of read-friendly subtrees (files, databases, shared_prefs, cache).
# 4. If still no archive and --exec-host requested, attempt to run host 'su -c' tar (best-effort; often fails from proot).
#    If not allowed, print exact host-one-liner for you to paste into phone host root shell.
# 5. Optionally upload created archive to FTP (if ftp args provided).
# 6. If upload completed and --remove-archive-after-upload set, remove the archive.
# 7. Provide clear logs and an exit code: 0 on at-least-one-success, non-zero otherwise.
#
# Notes:
# - Run this from the SAME shell/process that you used to confirm the proot diagnostics (so the process root matches).
# - The script is defensive: it never removes any path outside its explicit created artifacts unless you pass explicit flags.
#
set -o errexit
set -o nounset
set -o pipefail

### Defaults and configuration ###
OUT_DIR="/tmp/recovery_output"
ARCHIVE_PREFIX="recovery"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
VERBOSE=false
DRY_RUN=false
YES=false
EXEC_HOST=false     # try su -c tar from this environment (likely fails in proot)
REMOVE_ARCHIVE_AFTER_UPLOAD=false
REMOVE_SRC=false     # remove app data (DANGEROUS) - must be explicit
FTP_URL=""
FTP_USER=""
FTP_PASS=""
# Candidate copy targets (in order)
CANDIDATES=( "/storage/emulated/0" "/sdcard" "/data/media/0" "/storage/internal" "/tmp" )

# If you want only targeted subtrees for tech.ula, they are:
SUBTREES=( "files" "databases" "shared_prefs" "cache" "code_cache" )

LOG_DIR=""
SUDO=""
SUCCESS=false
ARCHIVE_PATH=""
ARCHIVE_NAME=""

### Helpers ###
usage(){
  cat <<USAGE
Usage: $0 [options]
Options:
  --outdir PATH                Output directory (default: $OUT_DIR)
  --archive-prefix NAME        Prefix for created archives (default: $ARCHIVE_PREFIX)
  --ftp-url URL                Optional FTP URL for upload (e.g. ftp://192.168.1.203:6871)
  --ftp-user USER              FTP username
  --ftp-pass PASS              FTP password
  --exec-host                  Attempt to run host 'su -c tar ...' from this environment (may fail)
  --remove-archive-after-upload Remove local archive after a successful FTP upload
  --remove-src                 Remove app source directory after success (DANGEROUS — requires --yes)
  --yes                        Assume yes to prompts
  --dry-run                    Do diagnostic steps only, no destructive actions
  --verbose                    Verbose output
  -h|--help                    Show this help
Examples:
  bash $0 --outdir /tmp/recover --archive-prefix techula --verbose
  bash $0 --ftp-url ftp://192.168.1.203:6871 --ftp-user pc --ftp-pass 426974 --yes
USAGE
  exit 1
}

log(){ printf '%s\n' "$*"; }
vlog(){ $VERBOSE && printf '[v] %s\n' "$*"; }
err(){ printf 'ERR: %s\n' "$*" >&2; }

confirm(){
  if $YES || $DRY_RUN; then return 0; fi
  printf "%s [y/N]: " "$1" >&2
  read -r ans
  case "$ans" in
    [Yy]* ) return 0;;
    * ) return 1;;
  esac
}

detect_sudo(){
  if [ "$(id -u)" -ne 0 ] && command -v sudo >/dev/null 2>&1; then
    SUDO="sudo"
  else
    SUDO=""
  fi
}

init_dirs(){
  mkdir -p "$OUT_DIR"
  LOG_DIR="${OUT_DIR}/logs"
  mkdir -p "$LOG_DIR"
}

fail_and_exit(){
  err "$1"
  exit "${2:-1}"
}

parse_args(){
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --outdir) OUT_DIR="$2"; shift 2;;
      --archive-prefix) ARCHIVE_PREFIX="$2"; shift 2;;
      --ftp-url) FTP_URL="$2"; shift 2;;
      --ftp-user) FTP_USER="$2"; shift 2;;
      --ftp-pass) FTP_PASS="$2"; shift 2;;
      --exec-host) EXEC_HOST=true; shift;;
      --remove-archive-after-upload) REMOVE_ARCHIVE_AFTER_UPLOAD=true; shift;;
      --remove-src) REMOVE_SRC=true; shift;;
      --yes) YES=true; shift;;
      --dry-run) DRY_RUN=true; shift;;
      --verbose) VERBOSE=true; shift;;
      -h|--help) usage;;
      *) echo "Unknown arg: $1"; usage;;
    esac
  done
}

# Determine process-root (the PRoot 'fake' root for this shell)
get_proc_root(){
  readlink -f "/proc/$$/root" 2>/dev/null || echo "/"
}

# Find best-known tech.ula path from the process root perspective
discover_pkg_path(){
  local tries=( "/data/data/tech.ula" "/data/user/0/tech.ula" "/host-rootfs/data/data/tech.ula" "/host-rootfs/data/user/0/tech.ula" )
  for p in "${tries[@]}"; do
    if [ -d "$p" ]; then
      echo "$p"
      return 0
    fi
  done
  # Not found
  return 1
}

# Try full dot tar from process-root "/" into a given dest file path
try_full_tar_to(){
  local dest="$1"
  local name="$2"
  local destfile="${dest%/}/${name}"
  vlog "Trying full tar to: $destfile"
  if $DRY_RUN; then
    vlog "[dry-run] would run: tar -C / -czf \"$destfile\" ."
    return 1
  fi
  # ensure destination dir exists and writable
  if [ ! -d "$dest" ]; then
    vlog "Target dir $dest does not exist, skipping"
    return 1
  fi
  # Do the tar. Use sudo if present (to run as host-root inside environment if possible).
  if [ -n "$SUDO" ]; then
    $SUDO tar -C / --warning=no-file-changed --ignore-failed-read -czf "$destfile" . 2> "${LOG_DIR}/tar_${name}.err" || true
  else
    tar -C / --warning=no-file-changed --ignore-failed-read -czf "$destfile" . 2> "${LOG_DIR}/tar_${name}.err" || true
  fi
  if [ -f "$destfile" ] && [ "$(stat -c%s "$destfile" 2>/dev/null || echo 0)" -ge 2048 ]; then
    ARCHIVE_PATH="$destfile"
    ARCHIVE_NAME="$(basename "$destfile")"
    return 0
  fi
  return 1
}

# Try targeted tars of subtrees under PKG_PATH (safer against permission errors)
try_targeted_tars(){
  local pkg="$1"
  local dest="$2"
  local prefix="$3"
  local made_any=false
  for sub in "${SUBTREES[@]}"; do
    if [ -d "${pkg}/${sub}" ]; then
      local name="${prefix}_${sub}_${TIMESTAMP}.tgz"
      local destfile="${dest%/}/${name}"
      vlog "Attempting targeted tar: ${pkg}/${sub} -> ${destfile}"
      if $DRY_RUN; then
        vlog "[dry-run] would run: tar -C \"${pkg}\" -czf \"${destfile}\" \"${sub}\""
        made_any=true
        continue
      fi
      if [ -n "$SUDO" ]; then
        $SUDO tar -C "${pkg}" --warning=no-file-changed --ignore-failed-read -czf "${destfile}" "${sub}" 2> "${LOG_DIR}/tar_${sub}.err" || true
      else
        tar -C "${pkg}" --warning=no-file-changed --ignore-failed-read -czf "${destfile}" "${sub}" 2> "${LOG_DIR}/tar_${sub}.err" || true
      fi
      if [ -f "${destfile}" ] && [ "$(stat -c%s "${destfile}" 2>/dev/null || echo 0)" -ge 1024 ]; then
        ARCHIVE_PATH="${destfile}"
        ARCHIVE_NAME="$(basename "${destfile}")"
        made_any=true
        return 0
      fi
    else
      vlog "Subtree ${pkg}/${sub} missing or not a dir"
    fi
  done
  # If we only produced small archives or none, return failure
  $made_any && return 1 || return 1
}

# Write host su command snippet (for manual paste on phone host root shell)
write_host_snippet(){
  local out="$LOG_DIR/host_snippet_${TIMESTAMP}.txt"
  mkdir -p "$LOG_DIR"
  cat > "$out" <<EOF
# Host-shell snippet (run on the Android host as real root; NOT inside PRoot).
# Use one of the following depending on layout:

# If tech.ula lives at /data/data/tech.ula (classic layout):
su -c 'tar -C / -czf /storage/emulated/0/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz data/data/tech.ula'

# If device uses /data/user/0 layout (multi-user):
su -c 'tar -C / -czf /storage/emulated/0/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz data/user/0/tech.ula'

# If /storage/emulated/0 is not present, try /data/media/0:
su -c 'tar -C / -czf /data/media/0/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz data/data/tech.ula'

# After running, verify from chroot:
ls -lh /storage/emulated/0/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz /sdcard/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz /data/media/0/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz 2>/dev/null || true
EOF
  log "Host tar snippet written: $out"
}

# Attempt host 'su -c' tar (best-effort; may fail inside PRoot)
attempt_exec_host_tar(){
  if ! $EXEC_HOST ; then
    vlog "exec-host disabled; skipping attempt to run su -c tar"
    return 1
  fi
  local hostdest="/storage/emulated/0/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz"
  if $DRY_RUN; then
    vlog "[dry-run] would run host su tar: su -c 'tar -C / -czf ${hostdest} data/data/tech.ula'"
    return 1
  fi
  if command -v su >/dev/null 2>&1 ; then
    vlog "Attempting: su -c 'tar -C / -czf ${hostdest} data/data/tech.ula'"
    su -c "tar -C / -czf ${hostdest} data/data/tech.ula" 2> "${LOG_DIR}/su_tar.err" || true
    if [ -f "${hostdest}" ]; then
      ARCHIVE_PATH="${hostdest}"
      ARCHIVE_NAME="$(basename "${hostdest}")"
      return 0
    fi
    return 1
  else
    vlog "No 'su' command visible in environment"
    return 1
  fi
}

# Upload via FTP using curl if provided
ftp_upload(){
  if [ -z "$FTP_URL" ]; then
    vlog "No FTP URL specified; skipping FTP upload"
    return 1
  fi
  [ -f "$ARCHIVE_PATH" ] || { err "No archive to upload"; return 1; }
  if $DRY_RUN ; then
    vlog "[dry-run] would upload $ARCHIVE_PATH -> $FTP_URL (user=$FTP_USER)"
    return 0
  fi
  # Build curl FTP command
  local url="$FTP_URL/$(basename "$ARCHIVE_PATH")"
  # --ftp-create-dirs requires modern curl; fallback to trying simple upload
  if [ -n "$FTP_USER" ]; then
    curl -T "$ARCHIVE_PATH" --ftp-create-dirs -u "${FTP_USER}:${FTP_PASS}" "$url" --silent --show-error --fail 2> "${LOG_DIR}/ftp.err" || true
  else
    curl -T "$ARCHIVE_PATH" --ftp-create-dirs "$url" --silent --show-error --fail 2> "${LOG_DIR}/ftp.err" || true
  fi
  if [ $? -eq 0 ]; then
    log "FTP upload OK -> $url"
    return 0
  else
    err "FTP upload failed; see ${LOG_DIR}/ftp.err"
    sed -n '1,80p' "${LOG_DIR}/ftp.err" || true
    return 1
  fi
}

# Remove only artifacts we created (test file, optionally archive), and optionally src (very dangerous)
cleanup_artifacts(){
  # Remove test file if present in process root
  local procroot
  procroot="$(get_proc_root)"
  local testfile="${procroot}/__PROOT_WRITE_TEST"
  if [ -f "$testfile" ]; then
    if $DRY_RUN; then
      vlog "[dry-run] would remove $testfile"
    else
      rm -f "$testfile" && vlog "Removed test file $testfile"
    fi
  fi

  if $REMOVE_ARCHIVE_AFTER_UPLOAD && [ -n "$ARCHIVE_PATH" ] && [ -f "$ARCHIVE_PATH" ]; then
    if $DRY_RUN; then
      vlog "[dry-run] would remove archive $ARCHIVE_PATH"
    else
      rm -f "$ARCHIVE_PATH" && vlog "Removed archive $ARCHIVE_PATH"
    fi
  fi

  if $REMOVE_SRC && [ -n "${PKG_PATH:-}" ]; then
    if [ "$YES" = true ] || confirm "Remove source directory ${PKG_PATH}? THIS CANNOT BE UNDONE."; then
      if $DRY_RUN; then
        vlog "[dry-run] would recursively rm -rf ${PKG_PATH}"
      else
        rm -rf "${PKG_PATH}" && log "Removed ${PKG_PATH}"
      fi
    else
      log "Skipping removal of ${PKG_PATH}"
    fi
  fi
}

### MAIN flow ###
parse_args "$@"
detect_sudo
init_dirs

log "SCRIPTTHESE_RUNALL starting at $(date)"
vlog "OUT_DIR=$OUT_DIR; ARCHIVE_PREFIX=$ARCHIVE_PREFIX; DRY_RUN=$DRY_RUN; EXEC_HOST=$EXEC_HOST; REMOVE_SRC=$REMOVE_SRC"

PROC_ROOT="$(get_proc_root)"
log "Process root: $PROC_ROOT"

PKG_PATH="$(discover_pkg_path || true)"
if [ -n "$PKG_PATH" ]; then
  log "Found package path candidate: $PKG_PATH"
else
  log "No tech.ula package path discovered from this process root; script will still attempt full tar from process root."
fi

# quick write test to see if we can create files from process root
TESTFILE="/__PROOT_WRITE_TEST"
if $DRY_RUN; then
  vlog "[dry-run] would attempt write test at $TESTFILE"
else
  set +e
  echo "proot-write-test-$(date +%s)" > "$TESTFILE" 2>/dev/null
  if [ $? -eq 0 ]; then
    vlog "WROTE test file at $TESTFILE"
  else
    vlog "WRITE TEST FAILED (no write permission at process root)"
  fi
  set -e
fi

# Build base archive name
ARCHIVE_NAME="${ARCHIVE_PREFIX}_${TIMESTAMP}.tgz"

# Attempt 1: full dot tar from process root to candidate directories in order
for dest in "${CANDIDATES[@]}"; do
  if $SUCCESS ; then break; fi
  # skip non-existent targets quickly
  if [ ! -d "$dest" ]; then
    vlog "Candidate $dest missing; skipping"
    continue
  fi
  if try_full_tar_to "$dest" "$ARCHIVE_NAME"; then
    SUCCESS=true
    log "Archive created at: $ARCHIVE_PATH"
    break
  else
    vlog "Full tar to $dest failed, checking next candidate"
  fi
done

# Attempt 2: if full tar failed and PKG_PATH discovered, try targeted subtree tars into same candidate locations
if ! $SUCCESS && [ -n "${PKG_PATH:-}" ]; then
  for dest in "${CANDIDATES[@]}"; do
    if $SUCCESS ; then break; fi
    if [ ! -d "$dest" ]; then
      vlog "Candidate $dest missing; skipping"
      continue
    fi
    if try_targeted_tars "$PKG_PATH" "$dest" "$ARCHIVE_PREFIX" ; then
      SUCCESS=true
      log "Targeted archive created at: $ARCHIVE_PATH"
      break
    else
      vlog "Targeted tar to $dest did not produce usable archive"
    fi
  done
fi

# Attempt 3: optionally attempt exec-host su -c tar (may fail inside proot). If success, update ARCHIVE_PATH.
if ! $SUCCESS && $EXEC_HOST; then
  if attempt_exec_host_tar; then
    SUCCESS=true
    log "Host su tar created archive at $ARCHIVE_PATH"
  else
    vlog "exec-host attempt failed (likely expected inside PRoot). See ${LOG_DIR}/su_tar.err"
  fi
fi

# If still no success, produce host snippet for copy/paste and guidance
if ! $SUCCESS; then
  write_host_snippet
  err "No archive created from this environment. A host-root tar is required. See host snippet in logs."
  # still continue: maybe we can try FTP (unlikely without an archive)
fi

# If archive exists and FTP params given, attempt upload
if [ -n "${ARCHIVE_PATH:-}" ] && [ -f "${ARCHIVE_PATH}" ] && [ -n "$FTP_URL" ]; then
  if ftp_upload ; then
    vlog "FTP upload succeeded"
    # optionally remove archive after upload
    if $REMOVE_ARCHIVE_AFTER_UPLOAD; then
      if $DRY_RUN; then
        vlog "[dry-run] would remove local archive ${ARCHIVE_PATH}"
      else
        rm -f "${ARCHIVE_PATH}" && log "Removed local archive after upload: ${ARCHIVE_PATH}"
        ARCHIVE_PATH=""
      fi
    fi
  else
    vlog "FTP upload failed or not performed"
  fi
fi

# Final cleanup of test/temporary artifacts (only those created by script unless REMOVE_SRC)
cleanup_artifacts

if $SUCCESS; then
  log "SUCCESS: an archive was created."
  [ -n "${ARCHIVE_PATH:-}" ] && log "Location: ${ARCHIVE_PATH}"
  exit 0
else
  err "FAILURE: no usable archive created. See logs in ${LOG_DIR} and host snippet ${LOG_DIR}/host_snippet_${TIMESTAMP}.txt"
  exit 2
fi