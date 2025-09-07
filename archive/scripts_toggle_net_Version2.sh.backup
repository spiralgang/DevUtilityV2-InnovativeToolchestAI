#!/usr/bin/env bash
# scripts/toggle_net.sh
# cross-toolkit network toggles: nmcli preferred (NetworkManager), fallback to ip/ifconfig.
# Also includes hotspot toggle (nmcli) and simple wifi scan helper.
#
# Usage:
#   ./toggle_net.sh status
#   ./toggle_net.sh down wlan0
#   ./toggle_net.sh up wlan0
#   ./toggle_net.sh hotspot start SSID PASSWORD
#   ./toggle_net.sh scan wlan0
set -euo pipefail
CMD=${1:-status}
ARG=${2:-}

has() { command -v "$1" >/dev/null 2>&1; }

show_status(){
  echo "=== Network status ==="
  if has nmcli; then
    nmcli general status
    nmcli device status
  else
    ip -c addr show
    echo
    route -n 2>/dev/null || ip route show
  fi
}

if [[ "$CMD" == "status" ]]; then
  show_status
  exit 0
fi

if [[ "$CMD" == "down" ]]; then
  IFACE="$ARG"
  if has nmcli; then sudo nmcli device disconnect "$IFACE"; else sudo ip link set "$IFACE" down; fi
  echo "Interface $IFACE down"
  exit 0
fi

if [[ "$CMD" == "up" ]]; then
  IFACE="$ARG"
  if has nmcli; then sudo nmcli device connect "$IFACE"; else sudo ip link set "$IFACE" up; fi
  echo "Interface $IFACE up"
  exit 0
fi

if [[ "$CMD" == "scan" ]]; then
  IFACE="${ARG:-wlan0}"
  if has iw; then sudo iw dev "$IFACE" scan | egrep "SSID|signal|freq" -A1 -B0; elif has iwlist; then sudo iwlist "$IFACE" scanning | egrep "ESSID|Signal level"; else echo "No wifi scan tool (iw/iwlist) available"; fi
  exit 0
fi

if [[ "$CMD" == "hotspot" ]]; then
  ACT=${2:-start}
  SSID=${3:-ubulite-hotspot}
  PASS=${4:-ubulitepass}
  if ! has nmcli; then echo "hotspot: nmcli required"; exit 2; fi
  if [[ "$ACT" == "start" ]]; then
    sudo nmcli device wifi hotspot ifname wlan0 ssid "$SSID" password "$PASS"
    echo "Hotspot started: $SSID"
  else
    # nmcli will tear down automatically; attempt to disconnect
    sudo nmcli connection show --active | grep hotspot >/dev/null 2>&1 && sudo nmcli connection down Hotspot || echo "No active hotspot connection"
  fi
  exit 0
fi

echo "Unknown command: $CMD"
exit 2