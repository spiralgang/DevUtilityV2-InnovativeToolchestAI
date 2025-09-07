#!/system/bin/sh
# android_investigate_net_dropbear.sh
# Lightweight evidence collector for Android (works in adb shell / Termux)
# Usage: adb shell "sh /sdcard/android_investigate_net_dropbear.sh"  OR run directly in shell
LOG=/sdcard/investigate_$(date +%s).log
echo "=== investigation started $(date) ===" > "$LOG"

echo; echo "[A] System / identity" | tee -a "$LOG"
uname -a 2>/dev/null | tee -a "$LOG"
getprop ro.build.version.release 2>/dev/null | sed -n '1p' | tee -a "$LOG"
getprop ro.build.version.sdk 2>/dev/null | tee -a "$LOG"

echo; echo "[B] Who's running dropbear / ssh-like services" | tee -a "$LOG"
ps -A 2>/dev/null | grep -Ei 'dropbear|sshd|ssh' | tee -a "$LOG" || true
# list any binaries named dropbear
for p in /system/bin /system/xbin /sbin /vendor/bin /data/local /data/local/tmp /data/data; do
  find "$p" -maxdepth 3 -type f -iname '*dropbear*' 2>/dev/null | tee -a "$LOG" || true
done

echo; echo "[C] Resolve freedesktop.org" | tee -a "$LOG"
if command -v getprop >/dev/null 2>&1; then
  # prefer host tools if available
  if command -v getent >/dev/null 2>&1; then
    getent ahosts freedesktop.org 2>/dev/null | awk '{print $1}' | sort -u | tee -a "$LOG"
  elif command -v dig >/dev/null 2>&1; then
    dig +short freedesktop.org 2>/dev/null | grep -E '^[0-9.]+' | tee -a "$LOG"
  else
    echo "no dig/getent; will use /etc/hosts and DNS cache" | tee -a "$LOG"
    cat /etc/hosts 2>/dev/null | grep freedesktop.org | tee -a "$LOG" || true
  fi
else
  echo "getprop not available" | tee -a "$LOG"
fi

echo; echo "[D] Current network sockets (ss/netstat fallback)" | tee -a "$LOG"
if command -v ss >/dev/null 2>&1; then
  ss -tunp 2>/dev/null | tee -a "$LOG"
elif command -v netstat >/dev/null 2>&1; then
  netstat -tunp 2>/dev/null | tee -a "$LOG"
else
  echo "/proc/net/tcp (will print remote addr hex)" | tee -a "$LOG"
  awk 'NR>1 {print $2,$10}' /proc/net/tcp 2>/dev/null | tee -a "$LOG" || true
fi

echo; echo "[E] Map sockets to processes (best-effort without lsof)" | tee -a "$LOG"
# For each pid, print cmdline and /proc/<pid>/fd listing (may be large)
for pid in $(ls /proc | grep '^[0-9]' | head -n 2000); do
  if [ -r /proc/$pid/cmdline ]; then
    cmd="$(tr '\0' ' ' < /proc/$pid/cmdline 2>/dev/null)"
    if echo "$cmd" | grep -E -i 'dropbear|vmos|ssh|proot|termux|com.vmos' >/dev/null 2>&1; then
      echo "PID:$pid CMD:$cmd" | tee -a "$LOG"
      ls -l /proc/$pid/exe 2>/dev/null | tee -a "$LOG"
      ls -l /proc/$pid/fd 2>/dev/null | sed -n '1,200p' | tee -a "$LOG"
    fi
  fi
done

echo; echo "[F] Installed packages (grep for vmos, ssh, termux)" | tee -a "$LOG"
if command -v pm >/dev/null 2>&1; then
  pm list packages | grep -Ei 'vmos|ssh|termux|dropbear|proot|virtual' | tee -a "$LOG" || true
fi

echo; echo "[G] Check common persistence locations" | tee -a "$LOG"
ls -la /data/local /data/local/tmp /data/local/bin /data/local/xbin 2>/dev/null | tee -a "$LOG" || true
grep -R --line-number -I "dropbear" /data 2>/dev/null | sed -n '1,200p' | tee -a "$LOG" || true

echo; echo "[H] shell profile files (possible logging lines) - DO NOT PASTE PASSWORDS" | tee -a "$LOG"
for f in /data/data /root /sdcard /storage/emulated/0; do
  # check usual shell rc/profile
  if [ -f "$f/.profile" ]; then echo "FOUND $f/.profile"; sed -n '1,200p' "$f/.profile" | sed 's/^[^\n]*$/[REDACTED]/' 2>/dev/null | tee -a "$LOG"; fi
done

echo; echo "[I] Crash logs / logcat hints (grep VMOS/authorize)" | tee -a "$LOG"
if command -v logcat >/dev/null 2>&1; then
  logcat -d | egrep -i 'vmos|authorize|auth|dropbear|ssh' | tail -n 500 | tee -a "$LOG" || true
fi

echo "=== investigation finished $(date) ==="
echo "Log path: $LOG"