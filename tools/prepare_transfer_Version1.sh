#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# prepare_transfer.sh
# Usage:
#   ./prepare_transfer.sh /path/in/userland/to/share [PORT]
#
# What it does:
#  - Creates a tar.gz of the path (fast single file transfer option)
#  - Ensures sshd is installed/started (if apt available) and prints SFTP info
#  - Starts a temporary HTTP server (python3) serving the tar for easy download
#
set -euo pipefail
TARGET="${1:-}"
PORT="${2:-8000}"
TSTAMP=$(date +%Y%m%d_%H%M%S)
if [[ -z "$TARGET" ]]; then
  echo "Usage: $0 /path/to/share [http-port]"
  exit 2
fi
if [[ ! -e "$TARGET" ]]; then
  echo "ERROR: target does not exist: $TARGET"
  exit 3
fi

TGZ="/tmp/$(basename "$TARGET")-${TSTAMP}.tar.gz"
echo "[+] Creating compressed archive: $TGZ (this may take a while)"
if command -v pigz >/dev/null 2>&1; then
  tar --warning=no-file-changed -cf - -C "$(dirname "$TARGET")" "$(basename "$TARGET")" | pigz -9 > "$TGZ"
else
  tar --warning=no-file-changed -czf "$TGZ" -C "$(dirname "$TARGET")" "$(basename "$TARGET")"
fi
echo "[+] Archive created."

# Ensure python3 available for http serving
if ! command -v python3 >/dev/null 2>&1; then
  echo "[!] python3 not found. Attempting to install via apt (requires root & network)."
  if command -v apt >/dev/null 2>&1; then
    apt update && apt install -y python3
  else
    echo "[!] No package manager found. HTTP serve will not be available."
  fi
fi

# Try to ensure sshd running (for SFTP). Many UserLAnd installs already have an sshd shim.
SSHD_PORT=8022
start_sshd=false
if command -v ss >/dev/null 2>&1 || command -v netstat >/dev/null 2>&1; then
  if ss -tlnp 2>/dev/null | grep -qE ':22|:8022'; then
    echo "[+] An SSH daemon is already listening (local)."
  else
    start_sshd=true
  fi
else
  start_sshd=true
fi

if $start_sshd ; then
  if command -v service >/dev/null 2>&1 && [ -f /etc/init.d/ssh ]; then
    echo "[+] Starting ssh service..."
    service ssh start || /etc/init.d/ssh start || true
  elif command -v /usr/sbin/sshd >/dev/null 2>&1; then
    echo "[+] Starting sshd directly..."
    /usr/sbin/sshd || true
  else
    echo "[!] No sshd found. To enable SFTP install openssh-server: apt install -y openssh-server"
  fi
fi

# Print connection info
USER_NAME="$(whoami)"
echo
echo "=== CONNECTION INFO ==="
echo "SFTP / SSH (try these from an Android SFTP client like Solid Explorer or CX):"
echo "Host: 127.0.0.1"
echo "Port: ${SSHD_PORT}  (try 8022 or 22; UserLAnd often proxies 8022)"
echo "User: ${USER_NAME}"
echo "Password: the linux user password you set (or use key auth if configured)"
echo
echo "HTTP (direct download):"
echo "Serve file: $TGZ"
echo "Command to start HTTP server in /tmp: (will serve $TGZ from /tmp)"
echo "  cd /tmp && python3 -m http.server ${PORT} --bind 127.0.0.1 &"
echo "Then open in Android browser or Solid Explorer: http://127.0.0.1:${PORT}/$(basename "$TGZ")"
echo
echo "Notes:"
echo " - Use Solid Explorer / CX to connect via SFTP to 127.0.0.1:${SSHD_PORT} to browse files directly and copy via SAF into Android/data/tech.ula/files/storage."
echo " - If SFTP fails, start the python HTTP server (see command above) and download the archive to Downloads, then move into app storage via SAF."
echo "======================="