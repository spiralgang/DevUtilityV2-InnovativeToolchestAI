#!/usr/bin/env bash
# scripts/find_and_backup.sh
# Hunt for files (by name, content, or extension) across local mounts, git repos, and configured rclone remotes.
# Optionally encrypt & push a selected file(s) to a remote via rclone (Backblaze B2 / S3 / Google Drive / Mega).
#
# Usage:
#   ./find_and_backup.sh --name "100 Network Tools"           # search by filename substring
#   ./find_and_backup.sh --content "100 Network Tools"       # grep content
#   ./find_and_backup.sh --backup /path/to/file --remote b2:backup-bucket/path
#
# Safety: this script DOES NOT print tokens or secrets. It avoids uploading without explicit --backup flag.
set -euo pipefail

# defaults
SEARCH_NAME=""
SEARCH_CONTENT=""
BACKUP_FILE=""
RCLONE_REMOTE=""

show_help(){
  cat <<EOF
Usage: $0 [--name NAME] [--content STR] [--backup FILE --remote REMOTE]
Examples:
  $0 --name "100 Network Tools"
  $0 --content "Linux Networking Tools"
  $0 --backup ./docs/100_network_tools.txt --remote b2:icedman/backup
EOF
  exit 1
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --name) SEARCH_NAME="$2"; shift 2;;
    --content) SEARCH_CONTENT="$2"; shift 2;;
    --backup) BACKUP_FILE="$2"; shift 2;;
    --remote) RCLONE_REMOTE="$2"; shift 2;;
    -h|--help) show_help;;
    *) echo "Unknown: $1"; show_help;;
  esac
done

# Helper: look for git repos in cwd & upward
find_git_repos(){
  printf "%s\n" "$(pwd)"
  find /data /storage "$HOME" / -maxdepth 4 -type d -name ".git" 2>/dev/null | sed 's/\/.git$//'
}

echo "[*] Checking common cloud CLIs (no creds printed)..."
if command -v gcloud >/dev/null 2>&1; then
  echo " - gcloud available; current account:"
  gcloud config list --format="value(core.account)" 2>/dev/null || true
fi
if command -v aws >/dev/null 2>&1; then
  echo " - aws CLI available; profile list:"
  aws configure list-profiles 2>/dev/null || true
fi
if command -v az >/dev/null 2>&1; then
  echo " - az CLI available; active account:"
  az account show --query user 2>/dev/null || true
fi

# rclone remotes
if command -v rclone >/dev/null 2>&1; then
  echo " - rclone remotes:"
  rclone listremotes 2>/dev/null || true
fi

# local filename search (fast heuristic)
if [[ -n "$SEARCH_NAME" ]]; then
  echo "[*] Searching for filenames matching: $SEARCH_NAME"
  # search /data, $HOME and mounted storage first (avoids root flood)
  find /data "$HOME" /storage -type f -iname "*${SEARCH_NAME// /'*'}*" -maxdepth 6 2>/dev/null | sed -n '1,200p' || true
fi

# content search (slower)
if [[ -n "$SEARCH_CONTENT" ]]; then
  echo "[*] Grepping filesystem for content: $SEARCH_CONTENT (this may take time)"
  # restrict to plausible user locations to avoid scanning /proc /sys
  grep -R --line-number -I --exclude-dir={.git,node_modules} -e "$SEARCH_CONTENT" $HOME /data /storage 2>/dev/null | sed -n '1,200p' || true
fi

# GitHub / local git hint (if gh installed)
if command -v gh >/dev/null 2>&1; then
  echo "[*] You can search GitHub repos with gh: 'gh repo list --limit 200' and 'gh search code \"$SEARCH_CONTENT\" --repo owner/repo'"
fi

# Backup flow (explicit)
if [[ -n "$BACKUP_FILE" ]]; then
  if [[ ! -f "$BACKUP_FILE" ]]; then
    echo "[!] Backup file not found: $BACKUP_FILE"; exit 2
  fi
  if [[ -z "$RCLONE_REMOTE" ]]; then
    echo "[!] No --remote specified for rclone; aborting upload"; exit 2
  fi
  # create a temporary encrypted archive to avoid exposing content to remote
  TMP_ARCH="$(mktemp -u)/$(basename "$BACKUP_FILE").tar.gz"
  mkdir -p "$(dirname "$TMP_ARCH")"
  tar -czf "$TMP_ARCH" -C "$(dirname "$BACKUP_FILE")" "$(basename "$BACKUP_FILE")"
  # symmetric encryption (requires openssl) - passphrase read interactively (not echoed)
  echo -n "Enter passphrase to encrypt backup (will not be echoed): "
  read -s PASS; echo
  ENC_FILE="${TMP_ARCH}.enc"
  openssl enc -aes-256-cbc -pbkdf2 -iter 200000 -salt -in "$TMP_ARCH" -out "$ENC_FILE" -pass pass:"$PASS"
  rm -f "$TMP_ARCH"
  echo "[*] Uploading encrypted backup to rclone remote: $RCLONE_REMOTE"
  rclone copy "$ENC_FILE" "$RCLONE_REMOTE" --progress
  echo "[*] Upload complete. Removing local encrypted copy."
  rm -f "$ENC_FILE"
  echo "[*] Backup done. Keep passphrase safe to decrypt later:"
  echo "  To download & decrypt:"
  echo "    rclone copy $RCLONE_REMOTE ./ && openssl enc -d -aes-256-cbc -pbkdf2 -in $(basename "$ENC_FILE") -out backup.tar.gz -pass pass:<your-pass>"
fi

echo "[*] Done."