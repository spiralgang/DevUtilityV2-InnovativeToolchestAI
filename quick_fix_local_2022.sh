#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# Fix ECONNREFUSED on localhost:2022 (Image #4)
# 1) Start an SSH server on 127.0.0.1:2022
# 2) Open firewall locally
# 3) Validate listener

set -euo pipefail
PORT="${PORT:-2022}"

if command -v sshd >/dev/null 2>&1; then
  sudo sshd -D -p "$PORT" -o ListenAddress=127.0.0.1 -o Protocol=2 &
  disown
elif command -v dropbear >/dev/null 2>&1; then
  dropbear -p "127.0.0.1:$PORT"
else
  echo "No SSH server found (sshd/dropbear)." >&2
fi

# Allow loopback port (nftables)
if command -v nft >/dev/null 2>&1; then
  nft -j list ruleset >/dev/null 2>&1 || true
  nft add table inet local || true
  nft 'add chain inet local allow { type filter hook input priority 0; policy accept; }' 2>/dev/null || true
  nft add rule inet local allow iif lo tcp dport "$PORT" accept 2>/dev/null || true
fi

echo "Check:"
ss -ltnp | awk -v p=":$PORT" '$4 ~ p'