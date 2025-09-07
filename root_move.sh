#!/usr/bin/env bash
# root_move.sh  -- RUN ONLY IF YOU HAVE HOST ANDROID ROOT (VERY RARE)
# WARNING: This script attempts direct copy/bind operations on the host storage.
# DO NOT RUN UNLESS YOU are certain you have host root and know the risks.
set -euo pipefail
SRC="${1:-}"
DST="/storage/emulated/0/Android/data/tech.ula/files/storage"
if [[ -z "$SRC" ]]; then
  echo "Usage: $0 /path/in/userland/to/move"
  exit 2
fi
if [[ ! -d "$DST" ]]; then
  echo "Destination does not exist: $DST"
  exit 3
fi
echo "[!] Running direct copy to $DST (requires host Android root)"
echo "Source: $SRC"
# Attempt direct copy preserving attributes
cp -a "$SRC"/* "$DST"/
# Optionally create symlink at source to destination (risky)
# ln -s "$DST" "$SRC.link"
echo "[+] Copy complete. Verify permissions:"
ls -l "$DST" | head -n 20
echo "[!] If files are unreadable by app, you may need to chown/chmod to app UID (requires host root)."