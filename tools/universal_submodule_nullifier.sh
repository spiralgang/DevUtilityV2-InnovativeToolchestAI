#!/bin/bash
# Universal Submodule Nullifier for CI/PR/Agent workflows
# Reference: /reference/VAULT.md

set -e

REPO_ROOT="$(git rev-parse --show-toplevel)"

echo "🔍 Checking for broken submodule entries..."

# Remove any submodule from .gitmodules that lacks a valid url
if [ -f "$REPO_ROOT/.gitmodules" ]; then
    echo "⏳ Cleaning .gitmodules..."
    # Remove any entry without url=...
    awk '/^\[submodule /{s=1} s && /url =/{s=0} !s' "$REPO_ROOT/.gitmodules" > "$REPO_ROOT/.gitmodules.cleaned"
    mv "$REPO_ROOT/.gitmodules.cleaned" "$REPO_ROOT/.gitmodules"
fi

# Remove submodule config from .git/config
echo "⏳ Cleaning .git/config..."
git config --remove-section "submodule.ai/ollama/deepseek-r1" || true

# Remove submodule directories from .git/modules if present
if [ -d "$REPO_ROOT/.git/modules/ai/ollama/deepseek-r1" ]; then
    rm -rf "$REPO_ROOT/.git/modules/ai/ollama/deepseek-r1"
    echo "🧹 Removed .git/modules/ai/ollama/deepseek-r1"
fi

# Remove submodule from index and working tree
if git ls-files | grep -q "^ai/ollama/deepseek-r1"; then
    echo "🧹 Removing ai/ollama/deepseek-r1 from index..."
    git rm --cached -r "ai/ollama/deepseek-r1"
fi

# Remove folder if present
if [ -d "$REPO_ROOT/ai/ollama/deepseek-r1" ]; then
    rm -rf "$REPO_ROOT/ai/ollama/deepseek-r1"
    echo "🧹 Removed ai/ollama/deepseek-r1 directory"
fi

# Final safety commit
git add .gitmodules
git commit -am "Universal submodule nullifier: removed broken ollama submodule entries" || true

echo "✅ Submodule cleanup complete. Repo is safe for agent workflows."

# References:
# - Vault: /reference/VAULT.md
# - GitHub Actions: https://docs.github.com/en/actions
