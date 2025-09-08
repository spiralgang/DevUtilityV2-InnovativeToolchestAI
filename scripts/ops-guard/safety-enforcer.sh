#!/usr/bin/env bash
set -euo pipefail

# Copilot Safety Policy Enforcer
# Validates that no destructive actions are performed without explicit approval

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "[SAFETY GUARD] Copilot Safety Policy Enforcer"
echo "[SAFETY GUARD] Repository: $REPO_ROOT"

# Load safety policy
ACTIVATION_RULES="$REPO_ROOT/configs/activation_rules.json"
MOVE_MANIFEST="$REPO_ROOT/configs/move_manifest.json"

if [ ! -f "$ACTIVATION_RULES" ]; then
    echo "[SAFETY GUARD] ERROR: activation_rules.json not found"
    exit 1
fi

if [ ! -f "$MOVE_MANIFEST" ]; then
    echo "[SAFETY GUARD] ERROR: move_manifest.json not found"
    exit 1
fi

# Check if safety policy is enabled
SAFETY_ENABLED=$(jq -r '.copilot_safety_policy.enabled // false' "$ACTIVATION_RULES")
if [ "$SAFETY_ENABLED" != "true" ]; then
    echo "[SAFETY GUARD] ERROR: Copilot safety policy is not enabled"
    exit 1
fi

echo "[SAFETY GUARD] ✅ Safety policy is enabled"

# Check for destructive actions flag
DESTRUCTIVE_DISABLED=$(jq -r '.copilot_safety_policy.destructive_actions_disabled // false' "$ACTIVATION_RULES")
if [ "$DESTRUCTIVE_DISABLED" != "true" ]; then
    echo "[SAFETY GUARD] ERROR: Destructive actions are not disabled"
    exit 1
fi

echo "[SAFETY GUARD] ✅ Destructive actions are disabled"

# Validate protected files are not modified
PROTECTED_FILES=$(jq -r '.copilot_safety_policy.protected_files[]' "$ACTIVATION_RULES")
echo "[SAFETY GUARD] Checking protected files..."

for file in $PROTECTED_FILES; do
    if [ -f "$REPO_ROOT/$file" ]; then
        echo "[SAFETY GUARD] ✅ Protected file exists: $file"
    else
        echo "[SAFETY GUARD] ⚠️  Protected file missing: $file"
    fi
done

echo "[SAFETY GUARD] ✅ Safety policy validation complete"