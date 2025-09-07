#!/usr/bin/env bash
# detect_fdo_contacts.sh
# Quick diagnostic to find processes contacting freedesktop.org (or its IPs).
# Usage: sudo ./detect_fdo_contacts.sh [--capture-seconds N]
# Requires: sh, awk, grep, getent/dig (preferred), ss or netstat, lsof (if available), tcpdump (optional)
set -eu

CAPTURE_SECONDS="${1:-0}"   # pass seconds to enable tcpdump capture (root required)

LOG=/tmp/fdo_diagnostic.$(date +%s).log
exec > >(tee -a "$LOG") 2>&1

echo "=== freedesktop.org contact diagnostic ==="
date
uname -a || true

# Resolve domain to IPs (use getent then dig as fallback)
echo
echo "[1] Resolving freedesktop.org -> IPs"
IPS=""
if command -v getent >/dev/null 2>&1; then
  IPS="$(getent ahosts freedesktop.org 2>/dev/null | awk '{print $1}' | sort -u)"
fi
if [[ -z "$IPS" ]] && command -v dig >/dev/null 2>&1; then
  IPS="$(dig +short freedesktop.org | grep -E '^[0-9.]+' || true)"
fi
if [[ -z "$IPS" ]]; then
  echo "Could not resolve freedesktop.org locally. Trying host command..."
  if command -v host >/dev/null 2>&1; then
    IPS="$(host freedesktop.org | awk '/has address/ {print $4}' | sort -u)"
  fi
fi
if [[ -z "$IPS" ]]; then
  echo "No IPs resolved for freedesktop.org; network/DNS may be blocked. Exiting."
  exit 0
fi
echo "Resolved IPs:"
echo "$IPS"

# helper to list connections to a remote IP via ss/netstat or /proc fallback
list_conns_to_ip() {
  ip="$1"
  echo
  echo "Connections to $ip :"
  if command -v ss >/dev/null 2>&1; then
    ss -tnpa | grep -E "$ip" || true
    ss -unpa | grep -E "$ip" || true
  elif command -v netstat >/dev/null 2>&1; then
    netstat -tanp 2>/dev/null | grep -E "$ip" || true
    netstat -uanp 2>/dev/null | grep -E "$ip" || true
  else
    echo "ss/netstat not available; falling back to /proc scan"
    # /proc/net/tcp column 2 = remote addr:port in hex
    # convert ip to hex
    IFS=. read -r a b c d <<< "$ip"
    iphex=$(printf "%02X%02X%02X%02X" "$d" "$c" "$b" "$a")
    # scan /proc/*/fd for sockets (expensive) - we limit to established tcp entries in /proc/net/tcp
    grep -i "$iphex" /proc/net/tcp 2>/dev/null || true
  fi
}

# list per-IP
for ip in $IPS; do
  list_conns_to_ip "$ip"
done

# Use lsof to map sockets to processes (if available)
if command -v lsof >/dev/null 2>&1; then
  echo
  echo "[2] lsof socket mappings (filtering for freedesktop.org IPs)"
  for ip in $IPS; do
    # show TCP/UDP owning processes connecting to ip
    lsof -nP -i @"$ip" 2>/dev/null || true
  done
else
  echo
  echo "[2] lsof not available; will attempt /proc scanning for PIDs with remote IPs"
  # Best-effort: scan /proc/*/fd symlinks pointing to socket:[inode] and cross-ref to /proc/net/tcp
  # Build map of remote iphex -> inode from /proc/net/tcp
  awk 'NR>1 {print $2,$10}' /proc/net/tcp 2>/dev/null | while read -r raddr inode; do
    echo "$raddr $inode"
  done | while read -r raddr inode; do
    # if raddr matches any freedesktop ip hex, record
    for ip in $IPS; do
      IFS=. read -r a b c d <<< "$ip"
      iphex=$(printf "%02X%02X%02X%02X" "$d" "$c" "$b" "$a")
      if echo "$raddr" | grep -iq "$iphex"; then
        echo "possible socket inode: $inode remote:$ip (raddr:$raddr)"
      fi
    done
  done
fi

# Search running commandlines for references to freedesktop.org or common desktop portal names
echo
echo "[3] Grep running processes and common files for 'freedesktop.org', 'xdg', 'portal', 'fontconfig'"
ps -eo pid,user,cmd --no-headers | grep -Ei 'freedesktop|xdg-|xdg_|portal|fontconfig|gvfs' || true
echo
echo "[4] Search common config and cache locations for 'freedesktop.org' (may be slow)"
for p in /etc /usr /var /home /root; do
  if [[ -d "$p" ]]; then
    grep -R --line-number --binary-files=without-match -I "freedesktop.org" "$p" 2>/dev/null | sed -n '1,200p' || true
  fi
done

# journalctl (if available)
echo
if command -v journalctl >/dev/null 2>&1; then
  echo "[5] journalctl recent messages mentioning freedesktop.org"
  journalctl -n 200 --no-pager | grep -i freedesktop || true
else
  echo "[5] journalctl not available"
fi

# Optional short tcpdump capture if requested
if [[ "${CAPTURE_SECONDS:-0}" -gt 0 ]]; then
  if command -v tcpdump >/dev/null 2>&1; then
    CAP="/tmp/fdo_capture_$(date +%s).pcap"
    echo
    echo "[6] Capturing traffic for ${CAPTURE_SECONDS}s to $CAP (requires root)"
    # build host filter from IPs
    FILTER=""
    for ip in $IPS; do
      if [[ -z "$FILTER" ]]; then FILTER="host $ip"; else FILTER="$FILTER or host $ip"; fi
    done
    tcpdump -i any -s 0 -w "$CAP" $FILTER & sleep "$CAPTURE_SECONDS"; kill $! 2>/dev/null || true
    echo "tcpdump saved to: $CAP"
  else
    echo "[6] tcpdump not found; skipping capture"
  fi
fi

echo
echo "=== End diagnostic ==="
echo "Log saved to: $LOG"