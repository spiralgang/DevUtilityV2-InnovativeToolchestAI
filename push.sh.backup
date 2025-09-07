#!/usr/bin/env bash
# push.sh — helper to commit and create remote using GitHub CLI (gh).
# Usage: ./push.sh [repo-name]
set -euo pipefail

REPO="${1:-pgp-public-keys}"
# ensure files present
if [ ! -f public_key.asc ]; then
  echo "public_key.asc not found — place the key file here first" >&2
  exit 2
fi

git init 2>/dev/null || true
git add public_key.asc README.md 2>/dev/null || true
if git rev-parse --verify HEAD >/dev/null 2>&1; then
  git commit -m "Update PGP public key" || true
else
  git commit -m "Add PGP public key" || true
fi

if command -v gh >/dev/null 2>&1; then
  # create remote and push (interactive if needed)
  gh repo create "$REPO" --public --source=. --remote=origin --push
  echo "Created and pushed to GitHub repository: $REPO"
else
  echo "GitHub CLI (gh) not found. Create a repo on github.com and then run:"
  echo "  git branch -M main"
  echo "  git remote add origin https://github.com/<youruser>/$REPO.git"
  echo "  git push -u origin main"
fi