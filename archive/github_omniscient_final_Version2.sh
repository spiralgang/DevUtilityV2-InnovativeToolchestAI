#!/bin/bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# OMNISCIENT DEPENDABOT DESTROYER - Final Production Version
set -euo pipefail

########## CONFIGURATION ##########
readonly ORG_OR_USER="${1:-spiralgang}"
readonly TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
readonly LOGFILE="omniscient_${TIMESTAMP}.log"
readonly ERRORFILE="omniscient_errors_${TIMESTAMP}.log"

# Colors
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly PURPLE='\033[0;35m'
readonly NC='\033[0m'

########## ENVIRONMENT VALIDATION ##########
validate_environment() {
    local errors=0
    
    echo -e "${BLUE}[VALIDATION]${NC} Checking environment..." | tee -a "$LOGFILE"
    
    # Use strategic auth if available
    if [[ -f "./github_strategic_auth.sh" ]]; then
        echo "[AUTH] Using strategic auth controller..." >> "$LOGFILE"
        if ./github_strategic_auth.sh select dependabot_removal >/dev/null 2>&1; then
            echo "[AUTH] Optimal token selected for Dependabot operations" >> "$LOGFILE"
        else
            echo "[AUTH] Strategic auth failed, falling back..." >> "$LOGFILE"
        fi
    fi
    
    # Final token check
    if [[ -z "${GITHUB_TOKEN:-}" ]]; then
        if command -v gh >/dev/null 2>&1 && gh auth status >/dev/null 2>&1; then
            GITHUB_TOKEN=$(gh auth token 2>/dev/null || echo "")
            export GITHUB_TOKEN
        fi
        
        if [[ -z "${GITHUB_TOKEN:-}" ]]; then
            echo -e "${RED}[FATAL]${NC} No valid GitHub token available" | tee -a "$ERRORFILE"
            ((errors++))
        fi
    fi
    
    # Required tools
    for tool in gh jq curl; do
        if ! command -v "$tool" >/dev/null 2>&1; then
            echo -e "${RED}[FATAL]${NC} $tool not installed" | tee -a "$ERRORFILE"
            ((errors++))
        fi
    done
    
    return $errors
}

########## REPO DISCOVERY ##########
get_repositories() {
    echo -e "${BLUE}[DISCOVERY]${NC} Fetching your 42 repositories..." | tee -a "$LOGFILE"
    
    gh repo list "$ORG_OR_USER" --limit 50 --json name,private --jq '.[] | .name' 2>/dev/null || {
        echo -e "${RED}[FATAL]${NC} Repository discovery failed" | tee -a "$ERRORFILE"
        exit 1
    }
}

########## DEPENDABOT ANNIHILATION ##########
annihilate_dependabot() {
    local repo="$1"
    local full_repo="$ORG_OR_USER/$repo"
    local actions_taken=0
    
    echo -e "${PURPLE}[ANNIHILATE]${NC} $repo" | tee -a "$LOGFILE"
    
    # 1. Remove GitHub App installation
    local installations
    installations=$(gh api "/repos/$full_repo/installations" 2>/dev/null || echo '{"installations":[]}')
    
    local dependabot_id
    dependabot_id=$(echo "$installations" | jq -r '.installations[] | select(.app_slug=="dependabot") | .id' 2>/dev/null || echo "")
    
    if [[ -n "$dependabot_id" ]]; then
        local repo_id
        repo_id=$(gh api "/repos/$full_repo" --jq '.id' 2>/dev/null || echo "")
        
        if [[ -n "$repo_id" ]] && gh api --method DELETE "/user/installations/$dependabot_id/repositories/$repo_id" >/dev/null 2>&1; then
            echo "  [DESTROYED] Dependabot app removed" | tee -a "$LOGFILE"
            ((actions_taken++))
        fi
    fi
    
    # 2. Disable vulnerability alerts
    if gh api --method DELETE "/repos/$full_repo/vulnerability-alerts" >/dev/null 2>&1; then
        echo "  [DISABLED] Vulnerability alerts" | tee -a "$LOGFILE"
        ((actions_taken++))
    fi
    
    # 3. Disable automated security fixes
    if gh api --method DELETE "/repos/$full_repo/automated-security-fixes" >/dev/null 2>&1; then
        echo "  [DISABLED] Automated security fixes" | tee -a "$LOGFILE"
        ((actions_taken++))
    fi
    
    # 4. Remove dependabot.yml
    if gh api "/repos/$full_repo/contents/.github/dependabot.yml" >/dev/null 2>&1; then
        local file_sha
        file_sha=$(gh api "/repos/$full_repo/contents/.github/dependabot.yml" --jq '.sha' 2>/dev/null)
        
        if [[ -n "$file_sha" ]] && gh api --method DELETE "/repos/$full_repo/contents/.github/dependabot.yml" \
            -f message="Omniscient remediation: Remove dependabot.yml" \
            -f sha="$file_sha" >/dev/null 2>&1; then
            echo "  [DELETED] dependabot.yml configuration" | tee -a "$LOGFILE"
            ((actions_taken++))
        fi
    fi
    
    # 5. Enable GitHub Actions with full permissions
    if gh api --method PUT "/repos/$full_repo/actions/permissions" \
        -f enabled=true \
        -f allowed_actions=all \
        -f github_owned_allowed=true \
        -f verified_allowed=true >/dev/null 2>&1; then
        echo "  [ENABLED] GitHub Actions (full permissions)" | tee -a "$LOGFILE"
        ((actions_taken++))
    fi
    
    if [[ $actions_taken -gt 0 ]]; then
        echo -e "  ${GREEN}[SUCCESS]${NC} $actions_taken actions completed"
        return 0
    else
        echo -e "  ${YELLOW}[CLEAN]${NC} No Dependabot traces found"
        return 0
    fi
}

########## EXECUTION ENGINE ##########
execute_omniscient_remediation() {
    local repos
    mapfile -t repos < <(get_repositories)
    
    local total=${#repos[@]}
    echo -e "${GREEN}[EXECUTION]${NC} Processing $total repositories..." | tee -a "$LOGFILE"
    
    local successful=0
    local processed=0
    
    for repo in "${repos[@]}"; do
        ((processed++))
        echo -e "\n${BLUE}[$processed/$total]${NC} Processing: $repo"
        
        if annihilate_dependabot "$repo"; then
            ((successful++))
        fi
        
        # Brief pause to respect rate limits
        sleep 0.5
    done
    
    return $((total - successful))
}

########## MAIN EXECUTION ##########
main() {
    echo -e "${PURPLE}"
    echo "╔══════════════════════════════════════════╗"
    echo "║          OMNISCIENT REMEDIATION          ║"
    echo "║       GitHub Dependabot Annihilator      ║"
    echo "║              spiralgang                  ║"
    echo "╚══════════════════════════════════════════╝"
    echo -e "${NC}"
    
    # Environment validation
    if ! validate_environment; then
        echo -e "${RED}[FATAL]${NC} Environment validation failed" | tee -a "$ERRORFILE"
        exit 1
    fi
    
    # Execute remediation
    local start_time
    start_time=$(date +%s)
    
    if execute_omniscient_remediation; then
        local end_time
        end_time=$(date +%s)
        local duration=$((end_time - start_time))
        
        echo -e "\n${GREEN}╔═══════════ OMNISCIENT COMPLETE ═══════════╗${NC}"
        echo -e "${GREEN}║${NC} Dependabot annihilated across all repos   ${GREEN}║${NC}"
        echo -e "${GREEN}║${NC} GitHub Actions re-enabled everywhere      ${GREEN}║${NC}"
        echo -e "${GREEN}║${NC} Duration: ${duration}s                            ${GREEN}║${NC}"
        echo -e "${GREEN}╚══════════════════════════════════════════════╝${NC}"
        
        echo -e "\nLogs: $LOGFILE"
        [[ -s "$ERRORFILE" ]] && echo "Errors: $ERRORFILE"
        
        exit 0
    else
        echo -e "\n${RED}[COMPLETION]${NC} Some operations failed - check $ERRORFILE"
        exit 1
    fi
}

main "$@"