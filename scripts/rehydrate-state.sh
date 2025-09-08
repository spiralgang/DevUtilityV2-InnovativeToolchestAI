#!/usr/bin/env bash
set -euo pipefail

mkdir -p logs .baseline

# Minimal env assertions (extend as needed)
echo "rehydrate: asserting env"
echo "ANDROID_HOME=${ANDROID_HOME:-unset}"
echo "JAVA_HOME=${JAVA_HOME:-unset}"

# Recreate expected directories and seed defaults
mkdir -p configs ai app logs

# Ensure activation rules present
if [[ ! -f configs/activation_rules.json ]]; then
  cat > configs/activation_rules.json <<'JSON'
{
  "branches": ["main", "copilot/fix-*"],
  "paths": [
    ".github/workflows/**",
    "scripts/**",
    "configs/activation_rules.json",
    "ai/**",
    "app/src/**"
  ],
  "agents": ["core", "validation", "android"]
}
JSON
fi

# Stamp rehydrate event
printf '%s\n' "rehydrate-state: ok"
