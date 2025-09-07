#!/usr/bin/env bash
# scripts/merge_main_into_pr.sh
# Merge origin/main into a PR branch and push. Stops on conflicts.
#
# Usage:
#   ./scripts/merge_main_into_pr.sh <branch>
#
set -euo pipefail
BRANCH="${1:-}"
if [[ -z "$BRANCH" ]]; then
  echo "Usage: $0 <branch>"
  exit 2
fi

echo "[1/5] Fetching origin..."
git fetch origin --prune

echo "[2/5] Updating local main..."
git checkout main
git pull origin main

echo "[3/5] Fetching and checking out head branch: $BRANCH"
# create local branch tracking origin/<branch> if not present
if git show-ref --verify --quiet "refs/heads/$BRANCH"; then
  git checkout "$BRANCH"
else
  git fetch origin "$BRANCH":"$BRANCH" || git checkout -b "$BRANCH" "origin/$BRANCH"
fi

echo "[4/5] Merging origin/main into $BRANCH..."
set +e
git merge origin/main
MERGE_EXIT=$?
set -e

if [[ $MERGE_EXIT -ne 0 ]]; then
  echo "[!] Merge finished with conflicts or errors (exit $MERGE_EXIT)."
  echo "Conflicted files:"
  git diff --name-only --diff-filter=U || true
  echo
  echo "To continue:"
  echo "  - Resolve conflicts in each file, then 'git add <file>'"
  echo "  - When done: git commit -m \"Merge main into $BRANCH — resolve conflicts\""
  echo "  - Then: git push -u origin $BRANCH"
  echo
  echo "If you want to abort merge: git merge --abort"
  exit 1
fi

echo "[5/5] Merge succeeded, pushing branch to origin..."
git push -u origin "$BRANCH"

echo "[+] Done. Branch '$BRANCH' is updated with origin/main and pushed."