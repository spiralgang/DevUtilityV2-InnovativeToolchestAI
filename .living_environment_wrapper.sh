#!/usr/bin/env bash
# Living Environment Wrapper - Zero Overhead Living Code System
# This wrapper operates at the environment level without impacting
# command line performance, git operations, or data transfer

# Set environment variables for living code operation
export LIVING_CODE_ENABLED=1
export LIVING_CODE_DB="$(pwd)/.living_environment.db"
export LIVING_CODE_ROOT="$(pwd)"

# Living code functions (loaded in background)
living_code_monitor() {
    # Monitor file changes and evolve code patterns
    # Runs in background with minimal CPU impact
    if [[ "$LIVING_CODE_ENABLED" == "1" ]]; then
        python3 "$(pwd)/scripts/living-environment-integration.py" --background-monitor &
    fi
}

# Environment initialization
living_code_init() {
    # Initialize living code environment
    if [[ -f "$LIVING_CODE_DB" ]] && [[ "$LIVING_CODE_ENABLED" == "1" ]]; then
        # Load environment optimizations
        source <(python3 "$(pwd)/scripts/living-environment-integration.py" --get-env-optimizations 2>/dev/null || true)
    fi
}

# Hook into shell initialization (bash/zsh)
if [[ "${BASH_VERSION:-}" ]] || [[ "${ZSH_VERSION:-}" ]]; then
    # Initialize living code environment
    living_code_init
    
    # Start background monitoring (if not already running)
    if ! pgrep -f "living-environment-integration.py.*background-monitor" >/dev/null 2>&1; then
        living_code_monitor
    fi
fi

# Export functions for use in scripts
export -f living_code_monitor living_code_init
