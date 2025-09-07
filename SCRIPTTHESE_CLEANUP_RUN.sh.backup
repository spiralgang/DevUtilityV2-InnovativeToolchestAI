#!/usr/bin/env bash
#
# SCRIPTTHESE_CLEANUP_RUN.sh
# Combined dynamic recovery + cleanup helper for UserLAnd/PRoot environments
#
# Purpose
# - Try targeted (subtree) tars of app data first, then full-process-root tar only if space allows.
# - Stop further attempts as soon as one method succeeds.
# - Optionally upload resulting archive to an FTP server (useful with CX "Access from network" FTP).
# - Optionally remove created archive(s) to free space after successful upload or on demand.
# - Conservative defaults: never delete app sandbox data (e.g., /data/data/tech.ula) unless explicitly requested.
#
# Key behavior
#  - Detects process-root (so run this from the same shell that saw / as the app-root).
#  - Prefers host-visible destinations in this order: /storage/internal, /sdcard, /data/media/0, /tmp.
#  - Estimates size (du -s) and checks destination free space to avoid "no space left" writes.
#  - Writes logs to OUT_DIR (default /tmp/recovery_output).
#  - Outputs an exact host-shell (real-root) snippet if a host tar is required.
#
# Usage examples
#   # Dry run (no writes) and verbose:
#   bash SCRIPTTHESE_CLEANUP_RUN.sh --dry-run --verbose
#
#   # Try recovery, upload to FTP, then delete local archive on success:
#   bash SCRIPTTHESE_CLEANUP_RUN.sh --ftp-url "ftp://192.168.1.203:6871" --ftp-user pc --ftp-pass 426974 \
#       --remove-after-upload --yes --verbose
#
#   # Remove large recovery artifacts older than 1 day in OUT_DIR (promptless):
#   bash SCRIPTTHESE_CLEANUP_RUN.sh --clean-archives --keep-days 1 --yes
#
set -o errexit
set -o nounset
set -o pipefail

# ---- Configuration & defaults ----
OUT_DIR="${OUT_DIR:-/tmp/recovery_output}"
LOG_DIR="${OUT_DIR}/logs"
ARCHIVE_PREFIX="${ARCHIVE_PREFIX:-techula}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
VERBOSE=false
DRY_RUN=false
YES=false
REMOVE_AFTER_UPLOAD=false
REMOVE_SRC=false        # extremely dangerous; off by default
EXEC_HOST=false         # try to run su -c tar from here (often not real host root)
FTP_URL=""
FTP_USER=""
FTP_PASS=""
KEEP_DAYS=3             # default retention for --clean-archives
CLEAN_ARCHIVES=false

# Preferred destinations (order)
DEST_CANDIDATES=( "/storage/internal" "/sdcard" "/data/media/0" "/tmp" )

# Subtrees to try first (targeted)
SUBTREES=( "files" "databases" "shared_prefs" "cache" "code_cache" )

# size margin bytes (add to estimate to be safer)
SIZE_MARGIN_BYTES=$((100*1024*1024)) # 100MB margin

# ---- Helpers ----
die(){ printf 'ERR: %s\n' "$*" >&2; exit 1; }
log(){ printf '%s\n' "$*"; }
v(){ $VERBOSE && printf '[v] %s\n' "$*"; }
confirm(){
  if $YES || $DRY_RUN; then return 0; fi
  printf "%s [y/N]: " "$1" >&2
  read -r r
  case "$r" in [Yy]*) return 0;; *) return 1;; esac
}
ensure_dirs(){
  mkdir -p "$OUT_DIR" "$LOG_DIR"
}

get_proc_root(){
  readlink -f "/proc/$$/root" 2>/dev/null || echo "/"
}

discover_pkg_path(){
  # Return first matching package path we think is tech.ula
  local tries=( "/data/data/tech.ula" "/data/user/0/tech.ula" "/host-rootfs/data/data/tech.ula" "/host-rootfs/data/user/0/tech.ula" )
  for p in "${tries[@]}"; do
    if [ -d "$p" ]; then
      printf '%s' "$p"
      return 0
    fi
  done
  return 1
}

bytes_to_human(){
  awk -v b="$1" 'function human(x){
    s="B KB MB GB TB"; split(s,u);
    for(i=5;i>=1;i--){
      p=1024^(i-1);
      if(x>=p) { printf("%.1f %s", x/p, u[i+0]); return }
    }
    printf("%d B", x)
  }
  BEGIN{ human(b) }'
}

# return free bytes on dest mount
free_space_bytes(){
  local dest="$1"
  # If dest doesn't exist, return 0
  [ -d "$dest" ] || { printf '0'; return 0; }
  # POSIX df compat: use df -P and parse available kilobytes
  local kb
  kb=$(df -P -k "$dest" 2>/dev/null | awk 'NR==2{print $4}')
  if [ -z "$kb" ]; then
    printf '0'
  else
    printf '%d' "$(( kb * 1024 ))"
  fi
}

# estimate size in bytes of list of sources
estimate_size_bytes(){
  local -n arr=$1
  local sum=0
  for src in "${arr[@]}"; do
    if [ -e "$src" ]; then
      # du -s gives kilobytes
      local kb
      kb=$(du -s --apparent-size -k "$src" 2>/dev/null | awk '{print $1}' || echo 0)
      sum=$(( sum + kb * 1024 ))
    fi
  done
  printf '%d' "$sum"
}

# try tar command with args, write stderr to log, return 0 on success
run_tar(){
  local tar_cmd=("$@")
  local errf="${LOG_DIR}/tar_$(date +%s).err"
  v "Running: ${tar_cmd[*]}"
  if $DRY_RUN; then
    v "[dry-run] would run: ${tar_cmd[*]}"
    return 1
  fi
  "${tar_cmd[@]}" 2> "$errf" || true
  # consider success if file exists and size > 2048
  local destfile="${tar_cmd[-1]}"
  if [ -f "$destfile" ] && [ "$(stat -c%s "$destfile" 2>/dev/null || echo 0)" -ge 2048 ]; then
    v "tar created $destfile (size: $(bytes_to_human "$(stat -c%s "$destfile")"))"
    return 0
  fi
  v "tar failed; head of $errf:"
  sed -n '1,120p' "$errf" || true
  return 1
}

write_host_snippet(){
  local out="${LOG_DIR}/host_snippet_${TIMESTAMP}.txt"
  mkdir -p "$LOG_DIR"
  cat > "$out" <<EOF
# Host-root (run this on Android host shell as the real root user)
# Typical commands to create an archive on shared storage:

# If tech.ula is at /data/data/tech.ula:
su -c 'tar -C / -czf /storage/emulated/0/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz data/data/tech.ula'

# If device uses /data/user/0 layout:
su -c 'tar -C / -czf /storage/emulated/0/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz data/user/0/tech.ula'

# If /storage/emulated/0 unavailable try /data/media/0:
su -c 'tar -C / -czf /data/media/0/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz data/data/tech.ula'

# After running, verify from the chroot:
ls -lh /storage/emulated/0/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz /sdcard/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz /data/media/0/${ARCHIVE_PREFIX}-techula-${TIMESTAMP}.tgz 2>/dev/null || true
EOF
  log "Host snippet written: $out"
}

ftp_upload(){
  local localfile="$1"
  local url="$FTP_URL"
  local user="$FTP_USER"
  local pass="$FTP_PASS"
  [ -n "$url" ] || { v "No FTP URL provided"; return 1; }
  if $DRY_RUN; then
    v "[dry-run] would upload $localfile -> $url (user=${user})"
    return 0
  fi
  # ensure curl exists
  if ! command -v curl >/dev/null 2>&1; then
    err "curl not found; cannot upload via FTP from this environment."
    return 1
  fi
  local filename
  filename="$(basename "$localfile")"
  # ensure url does not end with slash
  url="${url%/}"
  local dest="${url}/${filename}"
  v "Uploading $localfile -> $dest"
  if [ -n "$user" ]; then
    curl -T "$localfile" --ftp-create-dirs -u "${user}:${pass}" "$dest" --silent --show-error --fail 2> "${LOG_DIR}/ftp.err" || true
  else
    curl -T "$localfile" --ftp-create-dirs "$dest" --silent --show-error --fail 2> "${LOG_DIR}/ftp.err" || true
  fi
  if [ $? -eq 0 ]; then
    log "FTP upload succeeded -> ${dest}"
    return 0
  else
    err "FTP upload failed; see ${LOG_DIR}/ftp.err"
    sed -n '1,120p' "${LOG_DIR}/ftp.err" || true
    return 1
  fi
}

# cleanup archives in OUT_DIR older than KEEP_DAYS
clean_archives(){
  local days="$1"
  v "Cleaning archives in ${OUT_DIR} older than ${days} days"
  if $DRY_RUN; then
    v "[dry-run] find ${OUT_DIR} -type f -mtime +${days} -name \"${ARCHIVE_PREFIX}_*.tgz\" -print"
    return 0
  fi
  find "${OUT_DIR}" -type f -mtime +"${days}" -name "${ARCHIVE_PREFIX}_*.tgz" -print -exec rm -f {} \; 2>/dev/null || true
  log "Cleanup complete (older than ${days} days removed)"
}

# remove single archive safely
maybe_remove_archive(){
  local p="$1"
  if [ -z "$p" ] || [ ! -f "$p" ]; then return 0; fi
  if $DRY_RUN; then
    v "[dry-run] would rm -f $p"
    return 0
  fi
  rm -f "$p" && log "Removed archive: $p"
}

# ---- Main flow ----
parse_args(){
  while [ $# -gt 0 ]; do
    case "$1" in
      --outdir) OUT_DIR="$2"; LOG_DIR="${OUT_DIR}/logs"; shift 2;;
      --archive-prefix) ARCHIVE_PREFIX="$2"; shift 2;;
      --ftp-url) FTP_URL="$2"; shift 2;;
      --ftp-user) FTP_USER="$2"; shift 2;;
      --ftp-pass) FTP_PASS="$2"; shift 2;;
      --exec-host) EXEC_HOST=true; shift;;
      --remove-after-upload) REMOVE_AFTER_UPLOAD=true; shift;;
      --remove-src) REMOVE_SRC=true; shift;;
      --yes) YES=true; shift;;
      --dry-run) DRY_RUN=true; shift;;
      --verbose) VERBOSE=true; shift;;
      --clean-archives) CLEAN_ARCHIVES=true; shift;;
      --keep-days) KEEP_DAYS="$2"; shift 2;;
      -h|--help) cat <<USAGE
Usage: $0 [options]
Options:
  --outdir PATH           Output directory (default: $OUT_DIR)
  --archive-prefix NAME   Prefix for archives (default: $ARCHIVE_PREFIX)
  --ftp-url URL           FTP URL to upload archive (optional)
  --ftp-user USER         FTP username
  --ftp-pass PASS         FTP password
  --exec-host             Try to run host 'su -c tar ...' from this environment (may fail)
  --remove-after-upload   Remove local archive after successful FTP upload
  --remove-src            Remove source app directory after success (DANGEROUS)
  --clean-archives        Cleanup old archives in OUT_DIR (honors --keep-days)
  --keep-days N           Days to keep archives when cleaning (default: $KEEP_DAYS)
  --yes                   Non-interactive yes
  --dry-run               Don't write or delete, just show actions
  --verbose               Verbose logging
  -h, --help              Show this help
USAGE
        exit 0;;
      *) die "Unknown argument: $1";;
    esac
  done
}

# parse CLI
parse_args "$@"
ensure_dirs

log "SCRIPTTHESE_CLEANUP_RUN starting: $(date)"
v "OUT_DIR=${OUT_DIR}, ARCHIVE_PREFIX=${ARCHIVE_PREFIX}, DRY_RUN=${DRY_RUN}, EXEC_HOST=${EXEC_HOST}"

# clean archives only mode
if $CLEAN_ARCHIVES; then
  if $YES || confirm "Remove archives older than ${KEEP_DAYS} days from ${OUT_DIR}?"; then
    clean_archives "$KEEP_DAYS"
    exit 0
  else
    log "Skipping archive cleanup."
    exit 0
  fi
fi

PROC_ROOT="$(get_proc_root)"
log "Process root (this shell): $PROC_ROOT"
PKG_PATH="$(discover_pkg_path || true)"
if [ -n "$PKG_PATH" ]; then
  log "Candidate package path: $PKG_PATH"
else
  log "No package path auto-detected; script will still try process-root tars."
fi

# quick write test in process root (non-destructive)
TESTFILE="${PROC_ROOT%/}/__PROOT_WRITE_TEST"
if $DRY_RUN; then
  v "[dry-run] would attempt to write $TESTFILE"
else
  set +e
  printf 'test\n' > "$TESTFILE" 2>/dev/null
  if [ $? -eq 0 ]; then
    v "WROTE test file at $TESTFILE"
    rm -f "$TESTFILE" 2>/dev/null || true
  else
    v "No write permission at process root (expected in many PRoot cases)"
  fi
  set -e
fi

# Build list of candidate sources for a full-root tar (prefer small list if running low on space)
# Primary full-source is '.' (process root). We will also attempt targeted subtrees first.
FULL_SOURCES=( "." )

# Build estimated size for targeted subtrees if PKG_PATH exists
TARGETED_EST_BYTES=0
if [ -n "$PKG_PATH" ]; then
  # Only include existing subtrees
  declare -a existing_subs=()
  for s in "${SUBTREES[@]}"; do [ -d "${PKG_PATH}/${s}" ] && existing_subs+=("${PKG_PATH}/${s}"); done
  if [ ${#existing_subs[@]} -gt 0 ]; then
    TARGETED_EST_BYTES="$(estimate_size_bytes existing_subs)"
    v "Estimated sum size of existing subtrees: $(bytes_to_human "$TARGETED_EST_BYTES")"
  else
    v "No targeted subtrees present (files/databases/shared_prefs/cache/code_cache)"
  fi
fi

# Try sequence:
# 1) targeted tars into the first dest with enough space
# 2) if none, attempt full tar (process-root) only if destination free >= estimate + margin
SUCCESS=false
ARCHIVE_PATH=""

# helper to pick a dest with sufficient space
choose_dest_with_space(){
  local need_bytes="$1"
  for d in "${DEST_CANDIDATES[@]}"; do
    if [ -d "$d" ]; then
      local free
      free=$(free_space_bytes "$d")
      v "Free on $d: $(bytes_to_human "$free")"
      if [ "$free" -ge "$need_bytes" ]; then
        printf '%s' "$d"
        return 0
      fi
    else
      v "Dest $d missing"
    fi
  done
  return 1
}

# 1) Targeted tars
if [ -n "$PKG_PATH" ]; then
  v "Attempting targeted subtree tars (subtrees: ${SUBTREES[*]})"
  # prefer a single destination for all targeted pieces:
  # decide required bytes = targeted_est + margin
  local need=$(( TARGETED_EST_BYTES + SIZE_MARGIN_BYTES ))
  dest="$(choose_dest_with_space "$need" || true)"
  if [ -n "$dest" ]; then
    v "Chosen dest for targeted tars: $dest"
    for s in "${SUBTREES[@]}"; do
      if [ -d "${PKG_PATH}/${s}" ]; then
        local name="${ARCHIVE_PREFIX}_${s}_${TIMESTAMP}.tgz"
        local destfile="${dest%/}/${name}"
        v "Trying targeted: ${PKG_PATH}/${s} -> ${destfile}"
        if run_tar tar -C "${PKG_PATH}" -czf "${destfile}" "${s}"; then
          SUCCESS=true
          ARCHIVE_PATH="${destfile}"
          log "Created targeted archive: $ARCHIVE_PATH"
          break
        else
          v "Targeted tar for ${s} failed, continuing"
          rm -f "${destfile}" 2>/dev/null || true
        fi
      fi
    done
  else
    v "No destination with enough free space for targeted tars (need $(bytes_to_human "$need"))"
  fi
else
  v "No PKG_PATH available for targeted tars"
fi

# 2) Full-process-root tar if targeted failed
if ! $SUCCESS; then
  v "Targeted tars did not succeed (or none attempted). Considering full-process-root tar..."
  # estimate size: use du -s on process root but watch out (fast path: estimate only top-level)
  # We'll attempt du -s on PROC_ROOT; if it fails, fallback to a smaller default estimate (500MB)
  EST_BYTES=0
  if $DRY_RUN; then
    v "[dry-run] would run du -s on ${PROC_ROOT}"
    EST_BYTES=$(( 500 * 1024 * 1024 ))
  else
    set +e
    local du_kb
    du_kb=$(du -s -k "${PROC_ROOT}" 2>/dev/null | awk '{print $1}' || echo 0)
    set -e
    if [ "$du_kb" -gt 0 ]; then
      EST_BYTES=$(( du_kb * 1024 ))
      v "Estimated process-root size: $(bytes_to_human "$EST_BYTES")"
    else
      EST_BYTES=$(( 500 * 1024 * 1024 )) # fallback 500MB
      v "du failed; using fallback estimate $(bytes_to_human "$EST_BYTES")"
    fi
  fi
  need_full=$(( EST_BYTES + SIZE_MARGIN_BYTES ))
  dest_full="$(choose_dest_with_space "$need_full" || true)"
  if [ -n "$dest_full" ]; then
    name="${ARCHIVE_PREFIX}_full_${TIMESTAMP}.tgz"
    destfile="${dest_full%/}/${name}"
    v "Attempting full tar -> ${destfile} (need $(bytes_to_human "$need_full"))"
    if run_tar tar -C / --null -T <(printf '%s\0' .) -czf "${destfile}" .; then
      SUCCESS=true
      ARCHIVE_PATH="${destfile}"
      log "Created full-process-root archive: $ARCHIVE_PATH"
    else
      v "Full tar failed (see tar logs)"
      rm -f "${destfile}" 2>/dev/null || true
    fi
  else
    v "No dest with enough free space for full tar (need $(bytes_to_human "$need_full"))"
  fi
fi

# 3) Optional exec-host attempt (rarely succeeds inside PRoot)
if ! $SUCCESS && $EXEC_HOST; then
  log "Attempting host 'su -c tar' from this environment (may fail inside PRoot)"
  host_dest_candidate="/storage/internal/${ARCHIVE_PREFIX}-host-${TIMESTAMP}.tgz"
  if $DRY_RUN; then
    v "[dry-run] would run: su -c 'tar -C / -czf ${host_dest_candidate} data/data/tech.ula'"
  else
    if command -v su >/dev/null 2>&1; then
      su -c "tar -C / -czf '${host_dest_candidate}' data/data/tech.ula" 2> "${LOG_DIR}/su_tar.err" || true
      if [ -f "${host_dest_candidate}" ]; then
        SUCCESS=true
        ARCHIVE_PATH="${host_dest_candidate}"
        log "Host tar created archive at ${ARCHIVE_PATH}"
      else
        v "Host tar attempt did not create archive; see ${LOG_DIR}/su_tar.err"
      fi
    else
      v "No su binary visible here"
    fi
  fi
fi

# If still not success, write host snippet & exit
if ! $SUCCESS; then
  write_host_snippet
  err "No archive created from this environment. A host-root tar is required. See host snippet in logs: ${LOG_DIR}"
  exit 2
fi

# If we have an archive and FTP credentials provided, upload and optionally remove the local copy
if [ -n "${ARCHIVE_PATH:-}" ] && [ -f "$ARCHIVE_PATH" ]; then
  log "Archive ready: $ARCHIVE_PATH"
  if [ -n "$FTP_URL" ]; then
    if ftp_upload "$ARCHIVE_PATH"; then
      if $REMOVE_AFTER_UPLOAD; then
        if $DRY_RUN; then
          v "[dry-run] would remove $ARCHIVE_PATH after upload"
        else
          rm -f "$ARCHIVE_PATH" && log "Removed local archive after successful upload: $ARCHIVE_PATH"
          ARCHIVE_PATH=""
        fi
      fi
    else
      v "FTP upload failed or was not performed"
    fi
  else
    v "No FTP upload requested"
  fi
fi

# Optionally remove source (dangerous) if asked
if $REMOVE_SRC && [ -n "${PKG_PATH:-}" ]; then
  v "--remove-src requested"
  if $YES || confirm "Permanently remove source directory ${PKG_PATH}? This will delete app data. Proceed?"; then
    if $DRY_RUN; then
      v "[dry-run] would rm -rf ${PKG_PATH}"
    else
      rm -rf "${PKG_PATH}" && log "Removed source directory: ${PKG_PATH}"
    fi
  else
    v "User declined to remove source directory"
  fi
fi

# Final optional: removal of recovered archives if user low on space
# If the device is low on free space (<200MB) and an archive exists, offer to delete it
if [ -n "${ARCHIVE_PATH:-}" ] && [ -f "${ARCHIVE_PATH}" ]; then
  # check free on dest dir of archive
  arch_dest_dir=$(dirname "$ARCHIVE_PATH")
  free_now=$(free_space_bytes "$arch_dest_dir")
  if [ "$free_now" -lt $((200*1024*1024)) ]; then
    v "Free space on ${arch_dest_dir} is low: $(bytes_to_human "$free_now")"
    if $YES || confirm "Free space low. Remove created archive ${ARCHIVE_PATH} to free space?"; then
      maybe_remove_archive "$ARCHIVE_PATH"
      ARCHIVE_PATH=""
    fi
  fi
fi

log "Done. Logs: ${LOG_DIR}"
[ -n "${ARCHIVE_PATH:-}" ] && log "Archive present: ${ARCHIVE_PATH}"
exit 0