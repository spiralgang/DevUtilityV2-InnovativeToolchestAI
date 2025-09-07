#!/usr/bin/env bash
# scripts/collect_and_encrypt_upload.sh
# Collect high-value files across repos, archive, encrypt, and upload via rclone.
#
# Usage:
#   ./scripts/collect_and_encrypt_upload.sh
#   UPLOAD_REMOTE="b2:mybucket/ubulite_backups" ./scripts/collect_and_encrypt_upload.sh
#
# Environment:
#   UPLOAD_REMOTE  - rclone remote target (required if not passed interactively)
#   EXTRA_PATHS    - optional colon-separated extra paths to include
#
# Safety:
# - This script does not print secret values.
# - Encryption uses AES-256-CBC via openssl and requires a passphrase you enter interactively.
# - rclone must be configured on the runner/device. Use rclone config on a trusted machine.
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../" && pwd)"
TMPDIR="$(mktemp -d "${TMPDIR:-/tmp/}ubulite_snapshot.XXXX")"
ARCHIVE="${TMPDIR}/ubulite_snapshot_$(date +%Y%m%dT%H%M%SZ).tar.gz"
ENC_ARCHIVE="${ARCHIVE}.enc"

echo "[*] Root: $ROOT"
echo "[*] Working tmpdir: $TMPDIR"

# default hotspots (edit as needed)
HOTSPOTS=(
  "$ROOT/README.ubulite_weave.md"
  "$ROOT/README.android.md"
  "$ROOT/README.md"
  "$ROOT/package.json"
  "$ROOT/src"
  "$ROOT/scripts"
  "$ROOT/icsman"           # optional: if present
  "$ROOT/iceman_drone_tool" # package (if present)
  "$ROOT/www"
  "$ROOT/web"
  "$ROOT/BigModel"
  "$ROOT/docs"
  "$ROOT/.github/workflows"
)

# Add extra paths if provided
if [[ -n "${EXTRA_PATHS:-}" ]]; then
  IFS=':' read -ra ADDR <<< "$EXTRA_PATHS"
  for p in "${ADDR[@]}"; do HOTSPOTS+=("$p"); done
fi

echo "[*] Assembling list of files to archive..."
LIST_FILE="${TMPDIR}/filelist.txt"
> "$LIST_FILE"
for p in "${HOTSPOTS[@]}"; do
  if [[ -e "$p" ]]; then
    # If directory, add directory contents; if file, add file
    if [[ -d "$p" ]]; then
      find "$p" -type f -maxdepth 6 2>/dev/null >> "$LIST_FILE" || true
    else
      echo "$p" >> "$LIST_FILE"
    fi
  fi
done

# If nothing found, include README.md and scripts as fallback
if [[ ! -s "$LIST_FILE" ]]; then
  echo "[WARN] No hotspot files found in default list; falling back to README and scripts"
  find "$ROOT" -maxdepth 2 -type f \( -name "README*" -o -path "*/scripts/*" \) >> "$LIST_FILE" || true
fi

echo "[*] Files to archive (first 200 lines):"
sed -n '1,200p' "$LIST_FILE"

echo "[*] Creating archive: $ARCHIVE"
tar -czf "$ARCHIVE" -T "$LIST_FILE" --warning=no-file-changed

echo "[*] Archive size: $(du -h "$ARCHIVE" | awk '{print $1}')"

# encrypt archive
echo -n "Enter passphrase to encrypt the archive (will not be echoed): "
read -rs PASS
echo
if [[ -z "$PASS" ]]; then
  echo "[ERROR] Empty passphrase. Aborting."
  exit 2
fi

echo "[*] Encrypting archive..."
# Use PBKDF2 and 200k iterations for some brute-force resistance
openssl enc -aes-256-cbc -pbkdf2 -iter 200000 -salt -in "$ARCHIVE" -out "$ENC_ARCHIVE" -pass pass:"$PASS"

echo "[*] Encrypted archive created: $ENC_ARCHIVE"
echo "[*] Remove plain archive for safety"
shred -u "$ARCHIVE" || rm -f "$ARCHIVE"

# Upload via rclone
if [[ -z "${UPLOAD_REMOTE:-}" ]]; then
  read -rp "Enter rclone remote target (e.g. b2:mybucket/ubulite_backups) or press Enter to skip upload: " UPLOAD_REMOTE
fi

if [[ -n "${UPLOAD_REMOTE:-}" ]]; then
  if ! command -v rclone >/dev/null 2>&1; then
    echo "[ERROR] rclone not found in PATH. Install or configure it first."
    echo "You can copy $ENC_ARCHIVE manually to a safe machine."
    exit 3
  fi
  echo "[*] Uploading to $UPLOAD_REMOTE ..."
  rclone copy "$ENC_ARCHIVE" "$UPLOAD_REMOTE" --progress
  echo "[*] Upload complete. Removing local encrypted copy."
  rm -f "$ENC_ARCHIVE"
  echo "[*] Done."
else
  echo "[*] Upload skipped. Encrypted archive left at: $ENC_ARCHIVE"
fi

# cleanup
echo "[*] Cleaning tmpdir: $TMPDIR"
rm -rf "$TMPDIR"
echo "[*] Finished."