#!/usr/bin/env bash
# Seed/refresh ipset sets from banlist. Requires root, ipset, dig/host, nft (optional).
set -euo pipefail

BANLIST="${BANLIST:-/etc/ban/banlist.txt}"
SET4="${SET4:-blocked4}"
SET6="${SET6:-blocked6}"
RESOLVER="${RESOLVER:-1.1.1.1}"

needs() { command -v "$1" >/dev/null 2>&1 || { echo "Missing $1" >&2; exit 1; }; }
needs ipset

# Create sets if missing
ipset list "$SET4" >/dev/null 2>&1 || ipset create "$SET4" hash:ip timeout 86400
ipset list "$SET6" >/dev/null 2>&1 || ipset create "$SET6" hash:ip family inet6 timeout 86400

add4() { ipset add "$SET4" "$1" timeout 86400 -exist; }
add6() { ipset add "$SET6" "$1" timeout 86400 -exist; }

while IFS= read -r line; do
  entry="$(echo "$line" | sed 's/#.*$//' | xargs || true)"
  [ -z "$entry" ] && continue
  if [[ "$entry" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+(/[0-9]+)?$ ]]; then
    add4 "$entry"
  elif [[ "$entry" =~ : ]]; then
    # IPv6 literal/CIDR
    add6 "$entry"
  else
    # domain: resolve A/AAAA and add
    if command -v dig >/dev/null 2>&1; then
      for a in $(dig +short A "$entry" @"$RESOLVER"); do add4 "$a"; done
      for aaaa in $(dig +short AAAA "$entry" @"$RESOLVER"); do add6 "$aaaa"; done
    elif command -v host >/dev/null 2>&1; then
      host -4 "$entry" | awk '/has address/ {print $4}' | while read -r a; do add4 "$a"; done
      host -6 "$entry" | awk '/has IPv6 address/ {print $5}' | while read -r a; do add6 "$a"; done
    else
      echo "Missing dig/host for DNS resolution" >&2
    fi
  fi
done < "$BANLIST"