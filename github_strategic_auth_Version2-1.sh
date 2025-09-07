#!/bin/bash
# Strategic GitHub Authentication Controller
# Handles multiple tokens with intelligent scope-based switching

set -euo pipefail

########## STRATEGIC CONFIGURATION ##########
readonly AUTH_CONFIG_DIR="$HOME/.config/github_strategic_auth"
readonly TOKENS_FILE="$AUTH_CONFIG_DIR/tokens.json"
readonly ACTIVE_TOKEN_FILE="$AUTH_CONFIG_DIR/active_token"
readonly SCOPE_CACHE_FILE="$AUTH_CONFIG_DIR/scope_cache.json"

# Colors for tactical feedback
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly PURPLE='\033[0;35m'
readonly NC='\033[0m'

########## TOKEN SCOPE ANALYSIS ##########
analyze_token_scopes() {
    local token="$1"
    local token_name="${2:-unknown}"
    
    echo -e "${BLUE}[SCOPE ANALYSIS]${NC} Analyzing $token_name..." >&2
    
    # Get token info directly from GitHub API
    local response
    response=$(curl -s -H "Authorization: token $token" \
        -H "Accept: application/vnd.github.v3+json" \
        -I "https://api.github.com/user" 2>/dev/null || echo "HTTP/1.1 401")
    
    # Extract scopes from X-OAuth-Scopes header
    local scopes
    scopes=$(echo "$response" | grep -i "x-oauth-scopes:" | cut -d: -f2- | tr -d '\r\n ' || echo "")
    
    # Get rate limit info
    local rate_info
    rate_info=$(curl -s -H "Authorization: token $token" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/rate_limit" 2>/dev/null || echo '{}')
    
    local remaining
    remaining=$(echo "$rate_info" | jq -r '.resources.core.remaining // 0' 2>/dev/null || echo "0")
    
    # Determine token capabilities
    local capabilities=()
    [[ "$scopes" == *"repo"* ]] && capabilities+=("repo_access")
    [[ "$scopes" == *"admin:org"* ]] && capabilities+=("org_admin")
    [[ "$scopes" == *"workflow"* ]] && capabilities+=("actions_workflow")
    [[ "$scopes" == *"delete_repo"* ]] && capabilities+=("repo_deletion")
    [[ "$scopes" == *"admin:repo_hook"* ]] && capabilities+=("webhooks")
    [[ "$scopes" == *"admin:public_key"* ]] && capabilities+=("deploy_keys")
    
    # Create analysis result
    jq -n \
        --arg token_name "$token_name" \
        --arg scopes "$scopes" \
        --argjson remaining "$remaining" \
        --argjson capabilities "$(printf '%s\n' "${capabilities[@]}" | jq -R . | jq -s . 2>/dev/null || echo '[]')" \
        --arg token_preview "${token:0:8}***${token: -4}" \
        '{
            token_name: $token_name,
            token_preview: $token_preview,
            scopes: ($scopes | split(",") | map(. | ltrimstr(" ") | rtrimstr(" ")) | map(select(length > 0))),
            raw_scopes: $scopes,
            capabilities: $capabilities,
            rate_limit_remaining: $remaining,
            valid: ($remaining > 0),
            timestamp: now
        }'
}

########## TOKEN MANAGEMENT ##########
register_token() {
    local token="$1"
    local token_name="$2"
    
    mkdir -p "$AUTH_CONFIG_DIR"
    
    echo -e "${PURPLE}[REGISTER]${NC} Adding token: $token_name"
    
    # Analyze token
    local analysis
    analysis=$(analyze_token_scopes "$token" "$token_name")
    
    if [[ $(echo "$analysis" | jq -r '.valid') != "true" ]]; then
        echo -e "${RED}[ERROR]${NC} Token $token_name is invalid or rate limited"
        return 1
    fi
    
    # Load existing tokens or create empty structure
    local tokens
    if [[ -f "$TOKENS_FILE" ]]; then
        tokens=$(cat "$TOKENS_FILE")
    else
        tokens='{"tokens":{}}'
    fi
    
    # Add/update token
    tokens=$(echo "$tokens" | jq \
        --arg name "$token_name" \
        --arg token "$token" \
        --argjson analysis "$analysis" \
        '.tokens[$name] = {token: $token, analysis: $analysis}')
    
    echo "$tokens" > "$TOKENS_FILE"
    chmod 600 "$TOKENS_FILE"
    
    echo -e "${GREEN}[SUCCESS]${NC} Token $token_name registered"
    echo "  Scopes: $(echo "$analysis" | jq -r '.raw_scopes')"
    echo "  Rate limit: $(echo "$analysis" | jq -r '.rate_limit_remaining')"
}

########## INTELLIGENT TOKEN SELECTION ##########
select_optimal_token() {
    local operation="${1:-general}"
    
    if [[ ! -f "$TOKENS_FILE" ]]; then
        echo -e "${RED}[ERROR]${NC} No tokens registered" >&2
        return 1
    fi
    
    local tokens
    tokens=$(cat "$TOKENS_FILE")
    
    # Define operation requirements
    local required_capabilities
    case "$operation" in
        "dependabot_removal")
            required_capabilities='["repo_access", "repo_deletion"]'
            ;;
        "actions_management")
            required_capabilities='["repo_access", "actions_workflow"]'
            ;;
        "org_operations")
            required_capabilities='["repo_access", "org_admin"]'
            ;;
        "general"|*)
            required_capabilities='["repo_access"]'
            ;;
    esac
    
    echo -e "${BLUE}[SELECTION]${NC} Finding optimal token for: $operation" >&2
    
    # Score tokens based on capabilities and rate limits
    local optimal_token
    optimal_token=$(echo "$tokens" | jq -r \
        --argjson required "$required_capabilities" \
        '
        .tokens | to_entries | 
        map({
            name: .key,
            token: .value.token,
            analysis: .value.analysis,
            score: (
                (.value.analysis.capabilities | map(select(. as $cap | $required | index($cap))) | length) * 100 +
                (.value.analysis.rate_limit_remaining / 10) +
                (if .value.analysis.valid then 1000 else 0 end)
            )
        }) |
        sort_by(-.score) |
        if length > 0 then .[0] else null end |
        if . then .token else empty end
        ')
    
    if [[ -n "$optimal_token" ]]; then
        echo "$optimal_token"
        return 0
    else
        echo -e "${RED}[ERROR]${NC} No suitable token found for operation: $operation" >&2
        return 1
    fi
}

########## STRATEGIC TOKEN SWITCHING ##########
set_active_token() {
    local operation="${1:-general}"
    
    local optimal_token
    if optimal_token=$(select_optimal_token "$operation"); then
        export GITHUB_TOKEN="$optimal_token"
        echo "$optimal_token" > "$ACTIVE_TOKEN_FILE"
        chmod 600 "$ACTIVE_TOKEN_FILE"
        
        local token_name
        token_name=$(cat "$TOKENS_FILE" | jq -r \
            --arg token "$optimal_token" \
            '.tokens | to_entries[] | select(.value.token == $token) | .key')
        
        echo -e "${GREEN}[ACTIVE]${NC} Switched to token: $token_name for operation: $operation"
        return 0
    else
        return 1
    fi
}

########## TOKEN INVENTORY ##########
list_tokens() {
    if [[ ! -f "$TOKENS_FILE" ]]; then
        echo -e "${YELLOW}[INFO]${NC} No tokens registered"
        return 0
    fi
    
    echo -e "${PURPLE}[TOKEN INVENTORY]${NC}"
    echo "═══════════════════════════════════════════════════════════════"
    
    cat "$TOKENS_FILE" | jq -r '
    .tokens | to_entries[] | 
    "Name: " + .key + "\n" +
    "Preview: " + .value.analysis.token_preview + "\n" +
    "Scopes: " + .value.analysis.raw_scopes + "\n" +
    "Capabilities: " + (.value.analysis.capabilities | join(", ")) + "\n" +
    "Rate Limit: " + (.value.analysis.rate_limit_remaining | tostring) + "\n" +
    "Valid: " + (.value.analysis.valid | tostring) + "\n" +
    "───────────────────────────────────────────────────────────────"
    '
}

########## MAIN CONTROLLER ##########
main() {
    local command="${1:-help}"
    
    case "$command" in
        "register"|"add")
            if [[ $# -lt 3 ]]; then
                echo "Usage: $0 register <token> <name>"
                exit 1
            fi
            register_token "$2" "$3"
            ;;
        "select"|"switch")
            local operation="${2:-general}"
            set_active_token "$operation"
            ;;
        "list"|"inventory")
            list_tokens
            ;;
        "analyze")
            if [[ $# -lt 3 ]]; then
                echo "Usage: $0 analyze <token> <name>"
                exit 1
            fi
            analyze_token_scopes "$2" "$3"
            ;;
        "current")
            if [[ -n "${GITHUB_TOKEN:-}" ]]; then
                echo "Current token: ${GITHUB_TOKEN:0:8}***${GITHUB_TOKEN: -4}"
                analyze_token_scopes "$GITHUB_TOKEN" "current" | jq -r '.raw_scopes'
            else
                echo "No active token"
            fi
            ;;
        "auto")
            # Auto-register your two tokens
            echo -e "${PURPLE}[AUTO SETUP]${NC} Setting up your token environment..."
            
            echo "Enter your generic token (ghp_...):"
            read -s generic_token
            echo "Enter your GitHub App/model token (github_pat_...):"
            read -s app_token
            
            register_token "$generic_token" "generic"
            register_token "$app_token" "github_app"
            
            echo -e "${GREEN}[SETUP COMPLETE]${NC} Tokens registered. Use 'select' to switch between them."
            ;;
        *)
            echo -e "${PURPLE}Strategic GitHub Authentication Controller${NC}"
            echo ""
            echo "Commands:"
            echo "  register <token> <name>  - Register a new token"
            echo "  select [operation]       - Switch to optimal token for operation"
            echo "  list                     - Show all registered tokens"
            echo "  current                  - Show active token info"
            echo "  auto                     - Interactive setup for your tokens"
            echo ""
            echo "Operations:"
            echo "  general, dependabot_removal, actions_management, org_operations"
            echo ""
            echo "Usage: $0 [command]"
            ;;
    esac
}

main "$@"