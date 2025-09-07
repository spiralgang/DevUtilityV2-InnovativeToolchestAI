#!/bin/bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# Token Manager - Handle your two tokens intelligently

readonly AUTH_DIR="$HOME/.github_tokens"
readonly TOKENS_FILE="$AUTH_DIR/tokens"

setup_tokens() {
    mkdir -p "$AUTH_DIR"
    chmod 700 "$AUTH_DIR"
    
    echo "Enter your generic token (ghp_...):"
    read -s generic_token
    
    echo "Enter your GitHub App token (github_pat_...):"
    read -s app_token
    
    cat > "$TOKENS_FILE" << EOF
GENERIC_TOKEN=$generic_token
APP_TOKEN=$app_token
EOF
    
    chmod 600 "$TOKENS_FILE"
    echo "Tokens saved securely"
}

use_token() {
    local token_type="${1:-generic}"
    
    if [[ ! -f "$TOKENS_FILE" ]]; then
        echo "No tokens configured. Run: $0 setup"
        exit 1
    fi
    
    source "$TOKENS_FILE"
    
    case "$token_type" in
        "generic"|"repo")
            export GITHUB_TOKEN="$GENERIC_TOKEN"
            echo "Using generic token for repo operations"
            ;;
        "app"|"actions")
            export GITHUB_TOKEN="$APP_TOKEN"
            echo "Using GitHub App token for actions/model operations"
            ;;
        *)
            echo "Usage: $0 use [generic|app]"
            exit 1
            ;;
    esac
}

case "${1:-help}" in
    "setup") setup_tokens ;;
    "use") use_token "$2" ;;
    *) echo "Commands: setup, use [generic|app]" ;;
esac