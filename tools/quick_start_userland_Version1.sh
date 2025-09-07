#!/usr/bin/env bash
# quick_start_userland.sh
# Usage: ./quick_start_userland.sh /path/to/share [http-port]
# Creates tar, ensures openssh-server & python3 installed, starts sshd and an HTTP server.
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

echo "[1/5] Ensure apt & install minimal packages (may take time)"
if command -v apt >/dev/null 2>&1; then
  apt update
  DEBIAN_FRONTEND=noninteractive apt install -y --no-install-recommends openssh-server python3 curl || true
else
  echo "[!] apt not present; aborting install steps. Ensure sshd and python3 exist."
fi

echo "[2/5] Create compressed archive of target"
TAR="/tmp/$(basename "$TARGET")-share-$(date +%Y%m%d_%H%M%S).tar.gz"
tar -C "$(dirname "$TARGET")" -czf "$TAR" "$(basename "$TARGET")"
echo "Archive: $TAR"

echo "[3/5] Ensure sshd is running for local SFTP"
if ss -tlnp 2>/dev/null | grep -qE ':22|:8022'; then
  echo "sshd appears to be listening."
else
  if [ -f /etc/init.d/ssh ]; then
    service ssh start || /etc/init.d/ssh start || true
  elif command -v /usr/sbin/sshd >/dev/null 2>&1; then
    /usr/sbin/sshd || true
  fi
fi

echo "[4/5] Start HTTP server (loopback) as fallback on port ${PORT}"
cd /tmp
# Start only if port is free
if ss -tlnp 2>/dev/null | grep -q ":${PORT}"; then
  echo "Port ${PORT} in use; HTTP server not started."
else
  python3 -m http.server "${PORT}" --bind 127.0.0.1 >/dev/null 2>&1 &
  disown
  echo "HTTP server running at http://127.0.0.1:${PORT}/$(basename "$TAR")"
fi

echo
echo "=== READY ==="
echo "SFTP: host=127.0.0.1 port=8022 (or 22). User: $(whoami)"
echo "HTTP: http://127.0.0.1:${PORT}/$(basename "$TAR")"
echo "Use Solid Explorer/CX to SFTP and the Document picker (SAF) to paste into Android/data/tech.ula/files/storage."
echo "================"