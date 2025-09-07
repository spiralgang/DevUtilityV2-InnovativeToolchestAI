#!/bin/bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# Repository Discovery Fix - Clean, Direct, No Recursion

# GitHub tokens
TOKEN1="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"
TOKEN2="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"

# Colors
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly BLUE='\033[0;34m'
readonly PURPLE='\033[0;35m'
readonly NC='\033[0m'

# Clean auth setup - no environment conflicts
setup_auth() {
    local token="$1"
    export GITHUB_TOKEN="$token"
    echo "[AUTH] Using token: ${token:0:8}***"
}

# Direct API call - no gh repo list bullshit  
get_repos_direct() {
    local user="spiralgang"
    echo -e "${BLUE}[DIRECT]${NC} Fetching repos via API..."
    
    # Direct curl to GitHub API - bypasses all gh CLI nonsense
    curl -s \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/users/$user/repos?per_page=100&type=all" \
    | jq -r '.[].name' 2>/dev/null | grep -v '^null$' | sort -u
}

# Process each repository 
process_repo() {
    local repo="$1"
    echo -e "${PURPLE}[PROCESS]${NC} $repo"
    
    # Your existing PR checkout or other operations here
    gh pr checkout "${2:-26}" 2>/dev/null || echo "  No PR ${2:-26} in $repo"
}

# Main execution
main() {
    echo -e "${PURPLE}╔═══════════════════════╗${NC}"
    echo -e "${PURPLE}║   REPO DISCOVERY FIX  ║${NC}"
    echo -e "${PURPLE}╚═══════════════════════╝${NC}"
    
    # Setup auth with Token1
    setup_auth "$TOKEN1"
    
    # Get repos directly - no function name confusion
    local repos_array=()
    while IFS= read -r repo_name; do
        if [[ -n "$repo_name" && "$repo_name" != "get_repos_direct" && "$repo_name" != *"function"* ]]; then
            repos_array+=("$repo_name")
        fi
    done < <(get_repos_direct)
    
    echo -e "${GREEN}[FOUND]${NC} ${#repos_array[@]} repositories"
    
    # Process each repo
    local pr_number="${1:-26}"
    for repo in "${repos_array[@]}"; do
        process_repo "$repo" "$pr_number"
    done
    
    echo -e "${GREEN}[COMPLETE]${NC} Repository processing finished"
}

# Execute with PR number as argument
main "$@"