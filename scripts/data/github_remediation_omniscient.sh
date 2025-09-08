#!/bin/bash
# OMNISCIENT GITHUB REMEDIATION ENGINE
# Dynamically adapts to any GitHub org/repo state with surgical precision
# Handles: Dependabot removal, Actions enablement, permission escalation, API failures

set -euo pipefail

########## OMNISCIENT CONFIGURATION ##########
readonly ORG_OR_USER="${1:-spiralgang}"
readonly REPO_LIMIT="${2:-100}"
readonly PARALLEL_JOBS="${3:-5}"
readonly TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
readonly LOGFILE="github_omniscient_${TIMESTAMP}.log"
readonly ERRORFILE="github_errors_${TIMESTAMP}.log"
readonly SUMMARYFILE="github_summary_${TIMESTAMP}.json"

# Color codes for omniscient output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly PURPLE='\033[0;35m'
readonly NC='\033[0m' # No Color

########## OMNISCIENT ENVIRONMENT VALIDATION ##########
validate_environment() {
    local errors=0
    
    echo -e "${BLUE}[OMNISCIENT]${NC} Validating environment..." | tee -a "$LOGFILE"
    
    # GitHub token validation with scope checking
    if [[ -z "${GITHUB_TOKEN:-}" ]]; then
        echo -e "${RED}[FATAL]${NC} GITHUB_TOKEN not set" | tee -a "$ERRORFILE"
        ((errors++))
    else
        # Validate token scopes
        local scopes
        scopes=$(gh api /user -H "Accept: application/vnd.github.v3+json" --include 2>/dev/null | grep -i "x-oauth-scopes" | cut -d: -f2 | tr -d ' \r\n' || echo "unknown")
        echo "[TOKEN] Scopes: $scopes" >> "$LOGFILE"
        
        if [[ "$scopes" != *"repo"* ]] && [[ "$scopes" != *"admin:org"* ]]; then
            echo -e "${YELLOW}[WARNING]${NC} Token may lack sufficient permissions" | tee -a "$LOGFILE"
        fi
    fi
    
    # CLI tools validation
    for tool in gh jq curl; do
        if ! command -v "$tool" >/dev/null 2>&1; then
            echo -e "${RED}[FATAL]${NC} $tool not installed" | tee -a "$ERRORFILE"
            ((errors++))
        fi
    done
    
    # Authentication status
    if ! gh auth status >/dev/null 2>&1; then
        echo -e "${RED}[FATAL]${NC} gh CLI not authenticated" | tee -a "$ERRORFILE"
        ((errors++))
    fi
    
    # Rate limit check
    local rate_info
    rate_info=$(gh api /rate_limit 2>/dev/null || echo '{"resources":{"core":{"remaining":0}}}')
    local remaining
    remaining=$(echo "$rate_info" | jq -r '.resources.core.remaining // 0')
    echo "[RATE_LIMIT] Remaining: $remaining" >> "$LOGFILE"
    
    if [[ "$remaining" -lt 100 ]]; then
        echo -e "${YELLOW}[WARNING]${NC} Low API rate limit remaining: $remaining" | tee -a "$LOGFILE"
    fi
    
    return $errors
}

########## OMNISCIENT REPO DISCOVERY ##########
discover_repositories() {
    echo -e "${BLUE}[DISCOVERY]${NC} Fetching repositories for $ORG_OR_USER..." | tee -a "$LOGFILE"
    
    local repos_json
    local page=1
    local all_repos=()
    
    while true; do
        repos_json=$(gh api "/users/$ORG_OR_USER/repos?per_page=$REPO_LIMIT&page=$page&sort=updated" 2>/dev/null || \
                    gh api "/orgs/$ORG_OR_USER/repos?per_page=$REPO_LIMIT&page=$page&sort=updated" 2>/dev/null || \
                    echo '[]')
        
        local batch_repos
        mapfile -t batch_repos < <(echo "$repos_json" | jq -r '.[].name // empty')
        
        if [[ ${#batch_repos[@]} -eq 0 ]]; then
            break
        fi
        
        all_repos+=("${batch_repos[@]}")
        echo "[DISCOVERY] Page $page: ${#batch_repos[@]} repos" >> "$LOGFILE"
        
        if [[ ${#batch_repos[@]} -lt $REPO_LIMIT ]]; then
            break
        fi
        ((page++))
    done
    
    echo "[DISCOVERY] Total discovered: ${#all_repos[@]}" >> "$LOGFILE"
    printf '%s\n' "${all_repos[@]}"
}

########## OMNISCIENT DEPENDABOT ANALYZER ##########
analyze_dependabot_state() {
    local repo="$1"
    local full_repo="$ORG_OR_USER/$repo"
    local state="{}"
    
    # Check GitHub App installation
    local installations
    installations=$(gh api "/repos/$full_repo/installations" 2>/dev/null || echo '{"installations":[]}')
    local dependabot_app
    dependabot_app=$(echo "$installations" | jq -r '.installations[] | select(.app_slug=="dependabot") | {id, app_id, app_slug}' 2>/dev/null || echo 'null')
    
    # Check vulnerability alerts (requires admin)
    local vuln_alerts="unknown"
    if gh api "/repos/$full_repo/vulnerability-alerts" >/dev/null 2>&1; then
        vuln_alerts="enabled"
    elif gh api "/repos/$full_repo/vulnerability-alerts" 2>&1 | grep -q "404"; then
        vuln_alerts="disabled"
    fi
    
    # Check security updates
    local security_updates="unknown"
    if gh api "/repos/$full_repo/automated-security-fixes" >/dev/null 2>&1; then
        security_updates="enabled"
    elif gh api "/repos/$full_repo/automated-security-fixes" 2>&1 | grep -q "404"; then
        security_updates="disabled"
    fi
    
    # Check dependabot.yml existence
    local config_exists="false"
    if gh api "/repos/$full_repo/contents/.github/dependabot.yml" >/dev/null 2>&1; then
        config_exists="true"
    fi
    
    state=$(jq -n \
        --argjson app "$dependabot_app" \
        --arg vuln_alerts "$vuln_alerts" \
        --arg security_updates "$security_updates" \
        --arg config_exists "$config_exists" \
        '{
            app: $app,
            vulnerability_alerts: $vuln_alerts,
            security_updates: $security_updates,
            config_file_exists: ($config_exists == "true")
        }')
    
    echo "$state"
}

########## OMNISCIENT DEPENDABOT REMOVER ##########
remove_dependabot_surgical() {
    local repo="$1"
    local full_repo="$ORG_OR_USER/$repo"
    local results=()
    
    echo -e "${PURPLE}[SURGICAL]${NC} Analyzing Dependabot state for $repo..." >> "$LOGFILE"
    
    local dependabot_state
    dependabot_state=$(analyze_dependabot_state "$repo")
    
    # Remove GitHub App if present
    local app_id
    app_id=$(echo "$dependabot_state" | jq -r '.app.id // empty')
    if [[ -n "$app_id" ]]; then
        local repo_id
        repo_id=$(gh api "/repos/$full_repo" --jq '.id' 2>/dev/null || echo "")
        
        if [[ -n "$repo_id" ]]; then
            if gh api --method DELETE "/user/installations/$app_id/repositories/$repo_id" >/dev/null 2>&1; then
                results+=("app_removed")
                echo "  [SUCCESS] Dependabot app removed" >> "$LOGFILE"
            else
                results+=("app_removal_failed")
                echo "  [FAILED] Could not remove Dependabot app" >> "$ERRORFILE"
            fi
        else
            results+=("repo_id_resolution_failed")
        fi
    else
        results+=("no_app_found")
    fi
    
    # Disable vulnerability alerts if enabled
    local vuln_status
    vuln_status=$(echo "$dependabot_state" | jq -r '.vulnerability_alerts')
    if [[ "$vuln_status" == "enabled" ]]; then
        if gh api --method DELETE "/repos/$full_repo/vulnerability-alerts" >/dev/null 2>&1; then
            results+=("vulnerability_alerts_disabled")
            echo "  [SUCCESS] Vulnerability alerts disabled" >> "$LOGFILE"
        else
            results+=("vulnerability_alerts_disable_failed")
            echo "  [FAILED] Could not disable vulnerability alerts" >> "$ERRORFILE"
        fi
    fi
    
    # Disable automated security fixes if enabled
    local security_status
    security_status=$(echo "$dependabot_state" | jq -r '.security_updates')
    if [[ "$security_status" == "enabled" ]]; then
        if gh api --method DELETE "/repos/$full_repo/automated-security-fixes" >/dev/null 2>&1; then
            results+=("security_fixes_disabled")
            echo "  [SUCCESS] Automated security fixes disabled" >> "$LOGFILE"
        else
            results+=("security_fixes_disable_failed")
            echo "  [FAILED] Could not disable automated security fixes" >> "$ERRORFILE"
        fi
    fi
    
    # Remove dependabot.yml if exists
    local config_exists
    config_exists=$(echo "$dependabot_state" | jq -r '.config_file_exists')
    if [[ "$config_exists" == "true" ]]; then
        local file_sha
        file_sha=$(gh api "/repos/$full_repo/contents/.github/dependabot.yml" --jq '.sha' 2>/dev/null || echo "")
        
        if [[ -n "$file_sha" ]]; then
            local delete_data
            delete_data=$(jq -n \
                --arg message "Remove dependabot.yml - omniscient remediation" \
                --arg sha "$file_sha" \
                '{message: $message, sha: $sha}')
            
            if echo "$delete_data" | gh api --method DELETE "/repos/$full_repo/contents/.github/dependabot.yml" --input - >/dev/null 2>&1; then
                results+=("config_file_removed")
                echo "  [SUCCESS] dependabot.yml removed" >> "$LOGFILE"
            else
                results+=("config_file_removal_failed")
                echo "  [FAILED] Could not remove dependabot.yml" >> "$ERRORFILE"
            fi
        fi
    fi
    
    printf '%s\n' "${results[@]}"
}

########## OMNISCIENT ACTIONS ENABLER ##########
enable_actions_omniscient() {
    local repo="$1"
    local full_repo="$ORG_OR_USER/$repo"
    
    echo -e "${GREEN}[ACTIONS]${NC} Enabling GitHub Actions for $repo..." >> "$LOGFILE"
    
    # Check current permissions
    local current_perms
    current_perms=$(gh api "/repos/$full_repo/actions/permissions" 2>/dev/null || echo '{"enabled":false}')
    local currently_enabled
    currently_enabled=$(echo "$current_perms" | jq -r '.enabled // false')
    
    if [[ "$currently_enabled" == "true" ]]; then
        echo "  [SKIP] Actions already enabled" >> "$LOGFILE"
        echo "already_enabled"
        return 0
    fi
    
    # Enable with comprehensive permissions
    local permissions_data
    permissions_data=$(jq -n '{
        enabled: true,
        allowed_actions: "all",
        github_owned_allowed: true,
        verified_allowed: true,
        patterns_allowed: ["*"]
    }')
    
    if echo "$permissions_data" | gh api --method PUT "/repos/$full_repo/actions/permissions" --input - >/dev/null 2>&1; then
        echo "  [SUCCESS] Actions enabled with full permissions" >> "$LOGFILE"
        echo "enabled_successfully"
    else
        echo "  [FAILED] Could not enable Actions" >> "$ERRORFILE"
        echo "enable_failed"
    fi
}

########## OMNISCIENT REPO PROCESSOR ##########
process_repository() {
    local repo="$1"
    local full_repo="$ORG_OR_USER/$repo"
    local repo_result="{}"
    
    echo -e "${BLUE}[PROCESSING]${NC} $full_repo" | tee -a "$LOGFILE"
    
    local start_time
    start_time=$(date +%s)
    
    # Get repo metadata
    local repo_info
    repo_info=$(gh api "/repos/$full_repo" 2>/dev/null || echo '{}')
    local is_private
    is_private=$(echo "$repo_info" | jq -r '.private // true')
    local default_branch
    default_branch=$(echo "$repo_info" | jq -r '.default_branch // "main"')
    
    # Process Dependabot removal
    local dependabot_results
    mapfile -t dependabot_results < <(remove_dependabot_surgical "$repo")
    
    # Process Actions enablement
    local actions_result
    actions_result=$(enable_actions_omniscient "$repo")
    
    local end_time
    end_time=$(date +%s)
    local processing_time
    processing_time=$((end_time - start_time))
    
    # Compile results
    repo_result=$(jq -n \
        --arg repo "$repo" \
        --arg full_repo "$full_repo" \
        --argjson is_private "$is_private" \
        --arg default_branch "$default_branch" \
        --argjson dependabot_results "$(printf '%s\n' "${dependabot_results[@]}" | jq -R . | jq -s .)" \
        --arg actions_result "$actions_result" \
        --arg processing_time "${processing_time}s" \
        --arg timestamp "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
        '{
            repository: $repo,
            full_name: $full_repo,
            private: $is_private,
            default_branch: $default_branch,
            dependabot_actions: $dependabot_results,
            actions_result: $actions_result,
            processing_time: $processing_time,
            timestamp: $timestamp,
            success: (
                ($dependabot_actions | length > 0 and ($dependabot_actions | map(select(. | endswith("_failed"))) | length == 0)) or
                ($dependabot_actions | map(select(. == "no_app_found")) | length > 0)
            ) and ($actions_result | IN("already_enabled", "enabled_successfully"))
        }')
    
    echo "$repo_result"
}

########## OMNISCIENT PARALLEL EXECUTOR ##########
execute_remediation() {
    local repos=("$@")
    local total=${#repos[@]}
    
    echo -e "${BLUE}[EXECUTOR]${NC} Processing $total repositories with $PARALLEL_JOBS parallel jobs..." | tee -a "$LOGFILE"
    
    # Create temp directory for parallel processing
    local temp_dir
    temp_dir=$(mktemp -d)
    trap "rm -rf '$temp_dir'" EXIT
    
    # Process repos in parallel
    printf '%s\n' "${repos[@]}" | xargs -I {} -P "$PARALLEL_JOBS" bash -c "
        result=\$(process_repository '{}')
        echo \"\$result\" > '$temp_dir/{}.json'
        if echo \"\$result\" | jq -e '.success' >/dev/null 2>&1; then
            echo -e \"${GREEN}[SUCCESS]${NC} {}\"
        else
            echo -e \"${RED}[FAILED]${NC} {}\"
        fi
    "
    
    # Collect results
    local all_results=()
    for repo in "${repos[@]}"; do
        if [[ -f "$temp_dir/$repo.json" ]]; then
            all_results+=("$(cat "$temp_dir/$repo.json")")
        fi
    done
    
    # Generate summary
    local summary
    summary=$(printf '%s\n' "${all_results[@]}" | jq -s '{
        timestamp: (now | strftime("%Y-%m-%dT%H:%M:%SZ")),
        total_repositories: length,
        successful: [.[] | select(.success)] | length,
        failed: [.[] | select(.success | not)] | length,
        repositories: .
    }')
    
    echo "$summary" > "$SUMMARYFILE"
    
    echo -e "\n${GREEN}[OMNISCIENT COMPLETE]${NC} Summary written to $SUMMARYFILE" | tee -a "$LOGFILE"
}

########## OMNISCIENT MAIN ##########
main() {
    echo -e "${PURPLE}╔══════════════════════════════════════╗${NC}"
    echo -e "${PURPLE}║        OMNISCIENT REMEDIATION        ║${NC}"
    echo -e "${PURPLE}║      GitHub Dependabot Destroyer     ║${NC}"
    echo -e "${PURPLE}╚══════════════════════════════════════╝${NC}"
    
    # Environment validation
    if ! validate_environment; then
        echo -e "${RED}[FATAL]${NC} Environment validation failed" | tee -a "$ERRORFILE"
        exit 1
    fi
    
    # Repository discovery
    local repos
    mapfile -t repos < <(discover_repositories)
    
    if [[ ${#repos[@]} -eq 0 ]]; then
        echo -e "${RED}[FATAL]${NC} No repositories found for $ORG_OR_USER" | tee -a "$ERRORFILE"
        exit 1
    fi
    
    echo -e "${GREEN}[DISCOVERED]${NC} ${#repos[@]} repositories" | tee -a "$LOGFILE"
    
    # Execute remediation
    execute_remediation "${repos[@]}"
    
    # Final summary
    local summary
    summary=$(jq -r '
        "
╔══════════ OMNISCIENT SUMMARY ═══════════╗
║ Total Repositories: " + (.total_repositories | tostring) + "
║ Successful: " + (.successful | tostring) + "
║ Failed: " + (.failed | tostring) + "
║ Success Rate: " + ((.successful / .total_repositories * 100) | floor | tostring) + "%
╚═══════════════════════════════════════════╝
        "
    ' "$SUMMARYFILE")
    
    echo -e "${PURPLE}$summary${NC}"
    
    # Exit with appropriate code
    local failed_count
    failed_count=$(jq -r '.failed' "$SUMMARYFILE")
    exit $((failed_count > 0 ? 1 : 0))
}

# Execute if run directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi

########## REFERENCES ##########
# Vault: /reference (GitHub Apps API, CLI patterns, parallel processing)
# - https://docs.github.com/en/rest/apps/installations
# - https://docs.github.com/en/rest/actions/permissions
# - https://docs.github.com/en/rest/repos/repos#enable-vulnerability-alerts
# - https://cli.github.com/manual/gh_api
# - Parallel processing: xargs -P for controlled concurrency
# - jq advanced JSON manipulation for state analysis