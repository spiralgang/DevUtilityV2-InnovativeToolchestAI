#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# parse_proc_transcript.sh - summarize a find-style transcript of /proc
# Usage: ./parse_proc_transcript.sh "Terminal transcript (4).txt"
# Output: concise summary: unique PIDs, counts, map_files ranges, permission errors, samples
set -euo pipefail
IFS=$'\n\t'

if [ $# -lt 1 ]; then
  echo "Usage: $0 <transcript-file>" >&2
  exit 2
fi
TF="$1"
[ -r "$TF" ] || { echo "Cannot read $TF" >&2; exit 2; }

# extract any lines that start with ./<digits> or ./<digits>/...
grep -Eo '^\./[0-9]+' "$TF" | sed 's#^\./##' | sort -u > /tmp/_proc_pids.$$ || true

# Also consider patterns like ./26853/task/26853/...
grep -Eo '^\./[0-9]+' "$TF" | sed 's#^\./##' | sort | uniq -c | sort -rn > /tmp/_pid_counts.$$ || true

# map_files lines (memory mapping ranges)
grep -E '/map_files/[0-9a-fA-F-]+' "$TF" | sed 's#^\./##' | sort -u > /tmp/_map_files.$$ || true

# Permission denied lines
grep -i 'Permission denied' "$TF" | sed 's#^\./##' > /tmp/_perm_denied.$$ || true

# No such file lines
grep -i "No such file or directory" "$TF" | sed 's#^\./##' > /tmp/_nosuch.$$ || true

echo "==== /proc transcript summary ===="
echo "Transcript: $TF"
echo

# Unique PIDs and top counts
pcnt=$(wc -l < /tmp/_proc_pids.$$ 2>/dev/null || echo 0)
echo "Unique PIDs observed: $pcnt"
echo "Top PID path counts (showing most frequent paths in transcript):"
head -n 20 /tmp/_pid_counts.$$ | sed -e 's/^/  /'
echo

# Map_files overview
mf_count=$(wc -l < /tmp/_map_files.$$ 2>/dev/null || echo 0)
echo "map_files entries (memory map files) found: $mf_count"
if [ "$mf_count" -gt 0 ]; then
  echo "Sample map_files ranges:"
  head -n 20 /tmp/_map_files.$$ | sed -e 's/^/  /'
fi
echo

# Permission denied summary
perm_count=$(wc -l < /tmp/_perm_denied.$$ 2>/dev/null || echo 0)
echo "Permission denied occurrences: $perm_count"
if [ "$perm_count" -gt 0 ]; then
  echo "Sample permission denied lines:"
  head -n 10 /tmp/_perm_denied.$$ | sed -e 's/^/  /'
fi
echo

# No-such-file summary
nosuch_count=$(wc -l < /tmp/_nosuch.$$ 2>/dev/null || echo 0)
echo "No-such-file occurrences: $nosuch_count"
if [ "$nosuch_count" -gt 0 ]; then
  echo "Sample no-such-file lines:"
  head -n 10 /tmp/_nosuch.$$ | sed -e 's/^/  /'
fi
echo

# Extract sample useful per-pid files if present (cmdline, exe, mountinfo lines)
echo "Sample lines referencing cmdline/exe/mounts (if any):"
grep -E '/[0-9]+/(cmdline|exe|mounts|mountinfo|status|environ)' "$TF" | sed 's#^\./##' | sort -u | head -n 40 | sed -e 's/^/  /'
echo
echo "==== end summary ===="
# cleanup temp files (keeping them can help manual inspection if needed)
# rm -f /tmp/_proc_pids.$$ /tmp/_pid_counts.$$ /tmp/_map_files.$$ /tmp/_perm_denied.$$ /tmp/_nosuch.$$