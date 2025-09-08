#!/usr/bin/env sh
# 01_env_tell_all.sh — v0.7 (CLI-only Puter integration)
# Human summary + JSONL at ~/.unify/env_report.jsonl
# Optional Puter sync via puter-cli (enable with PUTER_ENABLED=1)

set -eu

OUTDIR="${OUTDIR:-$HOME/.unify}"
JSONL="$OUTDIR/env_report.jsonl"
mkdir -p "$OUTDIR"

ts() { date -u +'%Y-%m-%dT%H:%M:%SZ'; }
j() { printf '%s\n' "$1" >> "$JSONL"; }
say() { printf '%s\n' "$*"; }
kv() { printf '%-22s %s\n' "$1" "$2"; }
has() { command -v "$1" >/dev/null 2>&1; }
json_escape() { printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g; s/\t/\\t/g; s/\r/\\r/g; s/\n/\\n/g'; }

detect_storage_root() {
  for cand in /storage/self/primary /storage/emulated/0 /sdcard /storage/internal; do
    [ -d "$cand" ] || continue
    if [ -d "$cand/Android/data/tech.ula/files" ]; then echo "$cand"; return 0; fi
    sel="$cand"
  done
  [ -n "${sel:-}" ] && { echo "$sel"; return 0; }
  echo ""; return 1
}

detect_android_facts() {
  ANDROID="no"; ANDROID_REL=""; ANDROID_SDK=""; ANDROID_SELINUX=""
  TERMUX="no"; USERLAND="no"; APP_HOME=""
  if has getprop; then
    ANDROID="yes"
    ANDROID_REL="$(getprop ro.build.version.release 2>/dev/null || true)"
    ANDROID_SDK="$(getprop ro.build.version.sdk 2>/dev/null || true)"
  fi
  if has getenforce; then ANDROID_SELINUX="$(getenforce 2>/dev/null || true)"; fi
  if [ "${PREFIX:-}" = "/data/data/com.termux/files/usr" ] || [ -d "/data/data/com.termux/files/usr" ]; then
    TERMUX="yes"; APP_HOME="/data/data/com.termux/files/home"
  fi
  if [ -d "/sdcard/Android/data/tech.ula/files" ] || [ -d "/storage/emulated/0/Android/data/tech.ula/files" ]; then
    USERLAND="yes"; [ -z "$APP_HOME" ] && APP_HOME="/sdcard/Android/data/tech.ula/files"
  fi
}

check_path_lpp() {
  p="$1"; [ -n "$p" ] || return 0
  if [ ! -e "$p" ]; then kv "missing" "$p"; return 0; fi
  perm="$(ls -ld "$p" 2>/dev/null | awk '{print $1,$3":"$4}')" || perm="unknown"
  w="no"; [ -w "$p" ] && w="yes"
  x="no"; [ -x "$p" ] && x="yes"
  kv "$p" "$perm w=$w x=$x"
}

# — Collect facts —
UID="$(id -u 2>/dev/null || echo 0)"
GID="$(id -g 2>/dev/null || echo 0)"
USERN="${USER:-$(whoami 2>/dev/null || echo user)}"
GRPS="$(id -nG 2>/dev/null || echo '')"
UNAME="$(uname -a 2>/dev/null || echo '')"

OSPRETTY=""
if [ -r /etc/os-release ]; then . /etc/os-release 2>/dev/null || true; OSPRETTY="${PRETTY_NAME:-}"; fi

detect_android_facts

STOR="$(detect_storage_root || true)"
TECH=""; [ -n "$STOR" ] && [ -d "$STOR/Android/data/tech.ula/files" ] && TECH="$STOR/Android/data/tech.ula/files"

CAPS=""
for t in s3fs rclone fusermount3 fusermount mount inotifywait curl jq python3 sha256sum toybox busybox ip netstat ping getprop getenforce puter; do
  has "$t" && CAPS="$CAPS $t"
done
CAPS="$(printf '%s' "$CAPS" | sed 's/^ //')"

FUSE_DEV="no"; [ -c /dev/fuse ] && FUSE_DEV="yes"

MNT_STORAGE="$(mount 2>/dev/null | grep -E '/storage|/sdcard|fuse|sdcardfs' || true)"
MNT_FUSE="$(mount 2>/dev/null | grep -E ' fuse|fuse\.' || true)"

DFH="$(df -h 2>/dev/null || true)"
DFI="$(df -i 2>/dev/null || true)"

UMASK="$(umask 2>/dev/null || echo '')"
PATHV="$PATH"
RESOLV=""; [ -r /etc/resolv.conf ] && RESOLV="$(sed -n '1,8p' /etc/resolv.conf 2>/dev/null || true)"

WARNINGS=""
append_warn() { WARNINGS="${WARNINGS}${1}\n"; }
if [ "$FUSE_DEV" = "no" ] && printf '%s' "$CAPS" | grep -q 'fusermount'; then
  append_warn "FUSE tools present but /dev/fuse missing → use non-FUSE modes on Android 10."
fi
if [ "$USERLAND" = "yes" ] && [ -n "$STOR" ] && [ ! -w "$STOR" ]; then
  append_warn "Storage root $STOR not writable; prefer TECH ($TECH)."
fi

# — Human output —
say "=== Identity ==="; kv "user" "$USERN ($UID:$GID)"; kv "groups" "$GRPS"; kv "uname" "$UNAME"; kv "os" "$OSPRETTY"; say
say "=== Android context ==="; kv "android" "$ANDROID rel=${ANDROID_REL:-} sdk=${ANDROID_SDK:-} selinux=${ANDROID_SELINUX:-}"; kv "termux" "$TERMUX"; kv "userland (tech.ula)" "$USERLAND"; kv "app_home" "${APP_HOME:-}"; say
say "=== Storage mapping (Android ↔ chroot) ==="; kv "storage_root" "${STOR:-not-detected}"; kv "tech.ula files" "${TECH:-n/a}"; check_path_lpp "${STOR:-/sdcard}"; check_path_lpp "$TECH"; say
say "=== Mounts of interest ==="; [ -n "$MNT_STORAGE" ] && printf '%s\n' "$MNT_STORAGE" | sed 's/^/  /' || say "  (none)"; say
say "=== FUSE ==="; kv "/dev/fuse" "$FUSE_DEV"; [ -n "$MNT_FUSE" ] && printf '%s\n' "$MNT_FUSE" | sed 's/^/  /' || say "  (no fuse mounts)"; say
say "=== Tools detected ==="; kv "tools" "$CAPS"; say
say "=== Space (df -h) ==="; printf '%s\n' "$DFH" | sed 's/^/  /'; say
say "=== Inodes (df -i) ==="; printf '%s\n' "$DFI" | sed 's/^/  /'; say
say "=== L.P.P++ (paths & perms) ==="
for p in / /home /usr/local /opt /var/lib "$HOME" "$STOR" "$TECH" "$HOME/.unify" "${APP_HOME:-}"; do [ -n "$p" ] && check_path_lpp "$p"; done
say; kv "PATH" "$PATHV"; kv "umask" "$UMASK"; say
say "=== DNS (/etc/resolv.conf) ==="; [ -n "$RESOLV" ] && printf '%s\n' "$RESOLV" | sed 's/^/  /' || say "  (none)"; say
[ -n "$WARNINGS" ] && { say "=== Warnings ==="; printf '%s' "$WARNINGS" | sed 's/^/  - /'; say; }

say "(JSONL written to $JSONL)"

# — JSONL line —
BEGIN="{\"ts\":\"$(ts)\",\"event\":\"env_tell_all\",\"data\":{"
END="}}"
WARN_ESC="$(json_escape "$(printf '%s' "$WARNINGS")")"
LINE="$BEGIN\"identity\":{\"user\":\"$(json_escape "$USERN")\",\"uid\":$UID,\"gid\":$GID,\"groups\":\"$(json_escape "$GRPS")\",\"uname\":\"$(json_escape "$UNAME")\",\"os\":\"$(json_escape "$OSPRETTY")\"},\"android\":{\"present\":\"$ANDROID\",\"rel\":\"$(json_escape "$ANDROID_REL")\",\"sdk\":\"$(json_escape "$ANDROID_SDK")\",\"selinux\":\"$(json_escape "$ANDROID_SELINUX")\",\"termux\":\"$TERMUX\",\"userland\":\"$USERLAND\",\"app_home\":\"$(json_escape "${APP_HOME:-}")\"},\"storage\":{\"root\":\"$(json_escape "${STOR:-}")\",\"tech\":\"$(json_escape "${TECH:-}")\"},\"tools\":\"$(json_escape "$CAPS")\",\"fuse\":{\"dev\":\"$FUSE_DEV\"},\"mounts\":{\"storage\":\"$(json_escape "$MNT_STORAGE")\",\"fuse\":\"$(json_escape "$MNT_FUSE")\"},\"dfh\":\"$(json_escape "$DFH")\",\"dfi\":\"$(json_escape "$DFI")\",\"dns\":\"$(json_escape "$RESOLV")\",\"env\":{\"PATH\":\"$(json_escape "$PATHV")\",\"umask\":\"$(json_escape "$UMASK")\"},\"warnings\":\"$WARN_ESC\"$END"
j "$LINE"

# — Puter CLI sync —
if [ "${PUTER_ENABLED:-0}" = "1" ]; then
  HN="$( (hostname 2>/dev/null || echo localhost) | tr ' /' '--')"
  D="$(date -u +%Y%m%d)"
  REMOTE_DIR="${PUTER_DIR:-/unify/env}"
  REMOTE_NAME="${REMOTE_NAME:-env-$HN-$D.jsonl}"
  PLUG="${PLUGIN_PUTER_CLI_SYNC:-$(dirname "$0")/plugins/puter_cli_sync.sh}"
  if [ -x "$PLUG" ]; then
    "$PLUG" --jsonl "$JSONL" --name "$REMOTE_NAME" --dir "$REMOTE_DIR" --append-last || say "[puter] sync failed"
  else
    say "[puter] plugin missing or not executable: $PLUG"
  fi
fi