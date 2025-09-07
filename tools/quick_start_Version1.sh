#!/usr/bin/env bash
# quick_start.sh
# Usage: ./quick_start.sh /path/to/share [http-port]
# - Installs minimal packages (if missing)
# - Creates a tarball of path
# - Ensures sshd is started (for SFTP)
# - Starts a local python HTTP server (fallback)
set -euo pipefail
TARGET="${1:-}"
PORT="${2:-8000}"
if [[ -z "$TARGET" ]]; then
  echo "Usage: $0 /path/to/share [http-port]"
  exit 2
fi
if [[ ! -e "$TARGET" ]]; then
  echo "ERROR: target not found: $TARGET"
  exit 3
fi

echo "[1/5] Ensure apt index and packages (may prompt)"
if command -v apt >/dev/null 2>&1; then
  apt update
  DEBIAN_FRONTEND=noninteractive apt install -y --no-install-recommends openssh-server python3 nano curl || true
else
  echo "[!] apt not found; ensure openssh-server and python3 are available"
fi

echo "[2/5] Create compressed archive of target (one file transfer)"
TAR="/tmp/$(basename "$TARGET")-share-$(date +%Y%m%d_%H%M%S).tar.gz"
tar -C "$(dirname "$TARGET")" -czf "$TAR" "$(basename "$TARGET")"
echo "Archive created: $TAR"

echo "[3/5] Ensure sshd is running for local SFTP"
if ss -tlnp 2>/dev/null | grep -qE ':22|:8022'; then
  echo "sshd appears to be listening already."
else
  if [ -f /etc/init.d/ssh ]; then
    service ssh start || /etc/init.d/ssh start || true
  elif command -v /usr/sbin/sshd >/dev/null 2>&1; then
    /usr/sbin/sshd || true
  fi
fi

echo "[4/5] Starting HTTP server (bind 127.0.0.1:${PORT})"
cd /tmp
# kill any previous simple server on same port (best effort)
if lsof -i TCP:"${PORT}" >/dev/null 2>&1; then
  echo "Port ${PORT} in use; try picking another port"
else
  python3 -m http.server "${PORT}" --bind 127.0.0.1 >/dev/null 2>&1 &
  disown
fi

echo
echo "=== READY ==="
echo "SFTP: connect to 127.0.0.1 port 8022 (or 22). Username: $(whoami)."
echo "HTTP: http://127.0.0.1:${PORT}/$(basename "$TAR")"
echo "Use Solid Explorer or CX to SFTP and copy via SAF into Android/data/tech.ula/files/storage"
echo "Archive path: $TAR"
echo "================"