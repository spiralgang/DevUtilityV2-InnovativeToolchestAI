#!/usr/bin/env bash
# free_space_cleanup.sh
# Find large candidate files (by default >100MB) in specific dirs and delete them safely.
# Usage:
#   bash free_space_cleanup.sh            # interactive
#   bash free_space_cleanup.sh --yes      # delete without confirmation
#   bash free_space_cleanup.sh --min 50   # set minimum MB threshold
#
set -euo pipefail

MIN_MB=100
AUTO_YES=false
CANDIDATES=( "/tmp/recovery_output" "/tmp" "/storage/internal" "/storage/emulated/0" "/data/media/0" )

usage(){
  cat <<USAGE
Usage: $0 [--yes] [--min N]
  --yes     : non-interactive (delete automatically)
  --min N   : minimum size (MB) to consider for deletion (default ${MIN_MB}MB)
USAGE
  exit 1
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --yes) AUTO_YES=true; shift ;;
    --min) MIN_MB="$2"; shift 2 ;;
    -h|--help) usage ;;
    *) echo "Unknown arg: $1"; usage ;;
  esac
done

printf 'Cleanup run: %s\n' "$(date)"
printf 'Threshold: %s MB\n' "$MIN_MB"
printf '\nChecking candidate directories:\n'
for d in "${CANDIDATES[@]}"; do
  [ -d "$d" ] || continue
  echo " - $d"
done

echo
echo "Finding files >= ${MIN_MB}MB in candidate directories..."
# Build list of files (full paths) into array
mapfile -t FILES < <(
  for d in "${CANDIDATES[@]}"; do
    [ -d "$d" ] || continue
    # only search one level deeper for speed; adjust find path if you want deeper
    find "$d" -type f -size +"${MIN_MB}"M -printf '%s\t%p\n' 2>/dev/null
  done | sort -nr
)

if [ "${#FILES[@]}" -eq 0 ]; then
  echo "No files >= ${MIN_MB}MB found in candidates."
  exit 0
fi

echo
echo "Candidates (size_bytes TAB path):"
printf '%s\n' "${FILES[@]}" | awk '{printf "%10s  %s\n", $1, $2}'
echo

# Present summary (sizes in human)
echo "Summary (human-readable):"
printf '%s\n' "${FILES[@]}" | while IFS=$'\t' read -r bytes path; do
  human=$(awk -v b="$bytes" 'function human(x){
      s="B KB MB GB TB"; split(s,u);
      for(i=5;i>=1;i--){ p=1024^(i-1); if(x>=p){ printf("%.1f %s", x/p, u[i+0]); return } }
      printf("%d B", x)
    } BEGIN{ human(b) }')
  printf "%10s  %s\n" "$human" "$path"
done

if ! $AUTO_YES; then
  echo
  read -p "Type YES to delete ALL listed files now (irreversible): " confirm
  if [ "$confirm" != "YES" ]; then
    echo "Aborted by user. No files deleted."
    exit 0
  fi
fi

# Delete the files
echo "Deleting..."
printf '%s\n' "${FILES[@]}" | while IFS=$'\t' read -r bytes path; do
  echo "rm -v \"$path\""
  rm -vf "$path" || echo "Failed to remove $path"
done

echo "Syncing and reporting free space..."
sync
df -h /tmp /storage /data 2>/dev/null || df -h
echo "Done."