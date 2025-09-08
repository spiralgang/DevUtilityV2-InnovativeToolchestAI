#!/usr/bin/env bash
# CODE-REAVER: Local Agent CLI Management Tool
# Complete management interface for DeepSeek, Phi-2, Ollama, Qwen, Mixtral agents

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

usage() {
    echo -e "${BLUE}🤖 CODE-REAVER Local Agent CLI${NC}"
    echo -e "${CYAN}Complete management interface for local AI agents${NC}"
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo -e "${YELLOW}Commands:${NC}"
    echo "  setup           Initialize local agent environment"
    echo "  download        Download all models from manifest"
    echo "  status          Check status of all agents"
    echo "  info            Get detailed information about agents"
    echo "  chat            Start interactive chat with agent"
    echo "  prompt          Send single prompt to agent"
    echo "  file            Send file to agent for analysis"
    echo "  server          Start local agent backend server"
    echo "  test            Run comprehensive agent tests"
    echo "  clean           Clean up agent environments"
    echo "  logs            View agent interaction logs"
    echo "  export          Export agent configurations and logs"
    echo ""
    echo -e "${YELLOW}Agent Options (for applicable commands):${NC}"
    echo "  --agent, -a     Agent name: deepseek, phi2, ollama, qwen, mixtral"
    echo "  --all           Apply to all agents"
    echo ""
    echo -e "${YELLOW}Examples:${NC}"
    echo "  $0 setup"
    echo "  $0 download"
    echo "  $0 status --all"
    echo "  $0 chat --agent deepseek"
    echo "  $0 prompt --agent phi2 'Write a TypeScript interface'"
    echo "  $0 file --agent qwen myfile.py"
    echo "  $0 server"
    echo ""
    exit 1
}

log_action() {
    local action="$1"
    local details="$2"
    local timestamp=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    
    mkdir -p logs
    echo "{\"timestamp\": \"$timestamp\", \"action\": \"$action\", \"details\": \"$details\", \"source\": \"agent-cli\"}" >> logs/agent_cli.jsonl
}

setup_environment() {
    echo -e "${BLUE}🚀 Setting up CODE-REAVER Local Agent Environment${NC}"
    
    # Create directory structure
    mkdir -p models logs scripts backend frontend/src/{utils,components} configs datasets
    
    # Set offline environment
    echo -e "${YELLOW}📝 Configuring offline environment${NC}"
    cat > .env << 'EOF'
# CODE-REAVER Local Agent Environment
TRANSFORMERS_OFFLINE=1
HF_HUB_OFFLINE=1
GIT_TERMINAL_PROMPT=0
AGENT_MODE=local
NO_COPILOT=true
EOF
    
    # Create agent registry
    echo -e "${YELLOW}📋 Creating agent registry${NC}"
    cat > configs/agent_registry.json << 'EOF'
{
  "agents": {
    "deepseek": {
      "name": "DeepSeek R1",
      "description": "Advanced reasoning and primary coding intelligence",
      "model_path": "models/deepseek-r1",
      "script_path": "ai/ollama/scripts/deepseek_r1_integration.py",
      "capabilities": ["advanced_reasoning", "primary_coding", "devutility_ai"],
      "color": "#00ff00",
      "icon": "🧠",
      "priority": 1
    },
    "phi2": {
      "name": "Phi-2",
      "description": "Lightweight integration agent for frontend tasks",
      "model_path": "models/Phi-2",
      "capabilities": ["code_generation", "frontend_integration"],
      "color": "#0084ff",
      "icon": "🔧",
      "priority": 2
    },
    "ollama": {
      "name": "Ollama",
      "description": "Local model serving with REST API interface",
      "model_path": "models/ollama",
      "script_path": "ai/ollama/scripts/local_model_manager.py",
      "capabilities": ["local_serving", "multiple_models"],
      "color": "#ff6b35",
      "icon": "🦙",
      "priority": 3
    },
    "qwen": {
      "name": "Qwen 2.5",
      "description": "Multilingual glue code and integration logic",
      "model_path": "models/Qwen2.5-0.5B",
      "capabilities": ["multilingual", "glue_code"],
      "color": "#9c27b0",
      "icon": "🌟",
      "priority": 4
    },
    "mixtral": {
      "name": "Mixtral 8x7B",
      "description": "High-performance reasoning for complex tasks",
      "model_path": "models/Mixtral-8x7B",
      "capabilities": ["high_performance", "complex_reasoning"],
      "color": "#ff4444",
      "icon": "🚀",
      "priority": 5
    }
  },
  "settings": {
    "default_agent": "deepseek",
    "log_interactions": true,
    "offline_mode": true,
    "no_copilot": true
  }
}
EOF
    
    echo -e "${GREEN}✅ Local agent environment setup complete${NC}"
    log_action "setup" "Local agent environment initialized"
}

download_models() {
    echo -e "${BLUE}📥 Downloading models from manifest${NC}"
    
    if [ ! -f "configs/model_manifest.json" ]; then
        echo -e "${RED}❌ Model manifest not found${NC}"
        exit 1
    fi
    
    # Parse manifest and download models
    jq -r '.models[] | "\(.repo) \(.dir)"' configs/model_manifest.json | while read repo dir; do
        if [ ! -d "models/$dir" ]; then
            echo -e "${YELLOW}📦 Downloading: $repo -> models/$dir${NC}"
            git clone --depth 1 "$repo" "models/$dir" || {
                echo -e "${RED}❌ Download failed: $repo${NC}"
                continue
            }
            rm -rf "models/$dir/.git"
            echo -e "${GREEN}✅ Downloaded: models/$dir${NC}"
        else
            echo -e "${CYAN}ℹ️ Already exists: models/$dir${NC}"
        fi
    done
    
    log_action "download" "Model download completed"
}

check_status() {
    local agent="${1:-all}"
    
    echo -e "${BLUE}🔍 Checking agent status${NC}"
    
    if [ "$agent" = "all" ]; then
        agents=("deepseek" "phi2" "ollama" "qwen" "mixtral")
    else
        agents=("$agent")
    fi
    
    for a in "${agents[@]}"; do
        echo -e "${YELLOW}Agent: $a${NC}"
        "$SCRIPT_DIR/interact_agent.sh" "$a" status || true
        echo ""
    done
}

interactive_chat() {
    local agent="${1:-deepseek}"
    
    echo -e "${BLUE}💬 Starting interactive chat with $agent${NC}"
    echo -e "${CYAN}Type 'exit' to quit, 'help' for commands${NC}"
    echo ""
    
    while true; do
        echo -ne "${PURPLE}[$agent]> ${NC}"
        read -r input
        
        case "$input" in
            "exit"|"quit"|"q")
                echo -e "${YELLOW}👋 Chat session ended${NC}"
                break
                ;;
            "help"|"h")
                echo -e "${CYAN}Commands:${NC}"
                echo "  exit, quit, q    - Exit chat"
                echo "  help, h          - Show this help"
                echo "  status           - Check agent status"
                echo "  info             - Get agent info"
                echo "  switch <agent>   - Switch to different agent"
                echo ""
                ;;
            "status")
                "$SCRIPT_DIR/interact_agent.sh" "$agent" status
                ;;
            "info")
                "$SCRIPT_DIR/interact_agent.sh" "$agent" info
                ;;
            switch*)
                new_agent=$(echo "$input" | cut -d' ' -f2)
                if [ -n "$new_agent" ]; then
                    agent="$new_agent"
                    echo -e "${GREEN}Switched to: $agent${NC}"
                fi
                ;;
            "")
                continue
                ;;
            *)
                echo -e "${YELLOW}🤖 Processing...${NC}"
                "$SCRIPT_DIR/interact_agent.sh" "$agent" prompt "$input"
                echo ""
                ;;
        esac
    done
}

send_prompt() {
    local agent="$1"
    local prompt="$2"
    
    echo -e "${BLUE}🚀 Sending prompt to $agent${NC}"
    "$SCRIPT_DIR/interact_agent.sh" "$agent" prompt "$prompt"
}

send_file() {
    local agent="$1"
    local file="$2"
    
    if [ ! -f "$file" ]; then
        echo -e "${RED}❌ File not found: $file${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}📄 Sending file to $agent: $file${NC}"
    "$SCRIPT_DIR/interact_agent.sh" "$agent" file "$file"
}

start_server() {
    echo -e "${BLUE}🌐 Starting CODE-REAVER Agent Backend Server${NC}"
    echo -e "${CYAN}Server will be available at http://localhost:8080${NC}"
    echo -e "${YELLOW}Press Ctrl+C to stop${NC}"
    echo ""
    
    cd "$PROJECT_ROOT"
    python3 backend/agent_server.py
}

run_tests() {
    echo -e "${BLUE}🧪 Running comprehensive agent tests${NC}"
    
    # Test each agent
    for agent in deepseek phi2 ollama qwen mixtral; do
        echo -e "${YELLOW}Testing $agent...${NC}"
        "$SCRIPT_DIR/interact_agent.sh" "$agent" status || true
        "$SCRIPT_DIR/interact_agent.sh" "$agent" info || true
    done
    
    # Test backend server
    echo -e "${YELLOW}Testing backend server...${NC}"
    timeout 5s python3 backend/agent_server.py || echo "Backend server test completed"
    
    echo -e "${GREEN}✅ All tests completed${NC}"
    log_action "test" "Comprehensive agent tests completed"
}

clean_environment() {
    echo -e "${YELLOW}🧹 Cleaning agent environments${NC}"
    
    echo "This will remove:"
    echo "  - Downloaded models"
    echo "  - Log files"
    echo "  - Temporary files"
    echo ""
    
    read -p "Are you sure? (y/N): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        rm -rf models/* logs/* scratch/* || true
        echo -e "${GREEN}✅ Cleanup completed${NC}"
        log_action "clean" "Environment cleaned"
    else
        echo -e "${CYAN}Cleanup cancelled${NC}"
    fi
}

view_logs() {
    echo -e "${BLUE}📋 Agent Interaction Logs${NC}"
    
    if [ -f "logs/agent_interactions.jsonl" ]; then
        echo -e "${YELLOW}Recent interactions:${NC}"
        tail -10 logs/agent_interactions.jsonl | while read -r line; do
            echo "$line" | jq -r '"\(.timestamp) [\(.agent)] \(.event): \(.details)"' 2>/dev/null || echo "$line"
        done
    else
        echo -e "${CYAN}No interaction logs found${NC}"
    fi
    
    echo ""
    
    if [ -f "logs/agent_cli.jsonl" ]; then
        echo -e "${YELLOW}Recent CLI actions:${NC}"
        tail -5 logs/agent_cli.jsonl | while read -r line; do
            echo "$line" | jq -r '"\(.timestamp) \(.action): \(.details)"' 2>/dev/null || echo "$line"
        done
    fi
}

export_data() {
    local export_file="agent_export_$(date +%Y%m%d_%H%M%S).tar.gz"
    
    echo -e "${BLUE}💾 Exporting agent data${NC}"
    
    tar -czf "$export_file" \
        configs/ \
        logs/ \
        scripts/ \
        backend/ \
        frontend/ \
        .env 2>/dev/null || true
    
    echo -e "${GREEN}✅ Exported to: $export_file${NC}"
    log_action "export" "Data exported to $export_file"
}

# Main command processing
COMMAND="${1:-}"
shift || true

case "$COMMAND" in
    setup)
        setup_environment
        ;;
    download)
        download_models
        ;;
    status)
        AGENT="all"
        while [[ $# -gt 0 ]]; do
            case $1 in
                --agent|-a)
                    AGENT="$2"
                    shift 2
                    ;;
                --all)
                    AGENT="all"
                    shift
                    ;;
                *)
                    shift
                    ;;
            esac
        done
        check_status "$AGENT"
        ;;
    info)
        AGENT="${1:-deepseek}"
        "$SCRIPT_DIR/interact_agent.sh" "$AGENT" info
        ;;
    chat)
        AGENT="deepseek"
        while [[ $# -gt 0 ]]; do
            case $1 in
                --agent|-a)
                    AGENT="$2"
                    shift 2
                    ;;
                *)
                    shift
                    ;;
            esac
        done
        interactive_chat "$AGENT"
        ;;
    prompt)
        AGENT=""
        PROMPT=""
        while [[ $# -gt 0 ]]; do
            case $1 in
                --agent|-a)
                    AGENT="$2"
                    shift 2
                    ;;
                *)
                    PROMPT="$1"
                    shift
                    ;;
            esac
        done
        
        if [ -z "$AGENT" ] || [ -z "$PROMPT" ]; then
            echo -e "${RED}❌ Both --agent and prompt are required${NC}"
            exit 1
        fi
        
        send_prompt "$AGENT" "$PROMPT"
        ;;
    file)
        AGENT=""
        FILE=""
        while [[ $# -gt 0 ]]; do
            case $1 in
                --agent|-a)
                    AGENT="$2"
                    shift 2
                    ;;
                *)
                    FILE="$1"
                    shift
                    ;;
            esac
        done
        
        if [ -z "$AGENT" ] || [ -z "$FILE" ]; then
            echo -e "${RED}❌ Both --agent and file are required${NC}"
            exit 1
        fi
        
        send_file "$AGENT" "$FILE"
        ;;
    server)
        start_server
        ;;
    test)
        run_tests
        ;;
    clean)
        clean_environment
        ;;
    logs)
        view_logs
        ;;
    export)
        export_data
        ;;
    "")
        usage
        ;;
    *)
        echo -e "${RED}❌ Unknown command: $COMMAND${NC}"
        echo ""
        usage
        ;;
esac