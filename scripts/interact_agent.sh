#!/usr/bin/env bash
# CODE-REAVER: Comprehensive Local Agent Interaction Script
# Interact with DeepSeek, Phi-2, Ollama agents locally - NO COPILOT
# Usage: ./interact_agent.sh [agent] [mode] [prompt/file]

set -euo pipefail

# Supported agents and their local paths/models
AGENTS=("deepseek" "phi2" "ollama" "qwen" "mixtral")
MODEL_PATHS=(
    "models/deepseek-r1"
    "models/Phi-2"
    "models/ollama"
    "models/Qwen2.5-0.5B"
    "models/Mixtral-8x7B"
)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

usage() {
    echo -e "${BLUE}CODE-REAVER Local Agent Interaction${NC}"
    echo "Usage: $0 [agent: deepseek|phi2|ollama|qwen|mixtral] [mode: prompt|file|status|info] [STRING or FILE]"
    echo ""
    echo "Examples:"
    echo "  $0 deepseek prompt 'Write a TypeScript registry module for integrations'"
    echo "  $0 phi2 file ./app/src/components/AgentDashboard.vue"
    echo "  $0 ollama status"
    echo "  $0 qwen info"
    echo "  $0 mixtral prompt 'Optimize this React component for performance'"
    exit 1
}

log_event() {
    local event="$1"
    local details="$2"
    local timestamp=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    echo "{\"timestamp\": \"$timestamp\", \"event\": \"$event\", \"details\": \"$details\", \"agent\": \"${AGENT:-unknown}\"}" >> logs/agent_interactions.jsonl
}

# --- Argument Parsing ---
AGENT="${1:-}"
MODE="${2:-}"
INPUT="${3:-}"

if [[ -z "$AGENT" ]]; then
    echo -e "${RED}❌ Agent required${NC}"
    usage
fi

if [[ " ${AGENTS[*]} " != *" $AGENT "* ]]; then
    echo -e "${RED}❌ Invalid agent: $AGENT${NC}"
    echo -e "${YELLOW}Available agents: ${AGENTS[*]}${NC}"
    usage
fi

if [[ -z "$MODE" ]]; then
    echo -e "${RED}❌ Mode required${NC}"
    usage
fi

if [[ "$MODE" != "prompt" && "$MODE" != "file" && "$MODE" != "status" && "$MODE" != "info" ]]; then
    echo -e "${RED}❌ Invalid mode: $MODE${NC}"
    usage
fi

if [[ "$MODE" == "prompt" || "$MODE" == "file" ]] && [[ -z "$INPUT" ]]; then
    echo -e "${RED}❌ Input required for prompt/file mode${NC}"
    usage
fi

# --- Agent Environment (Offline, local-only) ---
export TRANSFORMERS_OFFLINE=1
export HF_HUB_OFFLINE=1
export GIT_TERMINAL_PROMPT=0

# Create necessary directories
mkdir -p logs scratch models

# --- Select model path ---
case "$AGENT" in
    deepseek)
        MODEL="${MODEL_PATHS[0]}"
        SCRIPT_PATH="ai/ollama/scripts/deepseek_r1_integration.py"
        ;;
    phi2)
        MODEL="${MODEL_PATHS[1]}" 
        SCRIPT_PATH=""
        ;;
    ollama)
        MODEL="${MODEL_PATHS[2]}"
        SCRIPT_PATH="ai/ollama/scripts/local_model_manager.py"
        ;;
    qwen)
        MODEL="${MODEL_PATHS[3]}"
        SCRIPT_PATH=""
        ;;
    mixtral)
        MODEL="${MODEL_PATHS[4]}"
        SCRIPT_PATH=""
        ;;
    *)
        echo -e "${RED}❌ Unsupported agent: $AGENT${NC}"
        exit 1
        ;;
esac

echo -e "${BLUE}🤖 CODE-REAVER Agent: $AGENT${NC}"
echo -e "${BLUE}📍 Mode: $MODE${NC}"
echo -e "${BLUE}🎯 Model: $MODEL${NC}"

# --- Handle different modes ---
case "$MODE" in
    status)
        echo -e "${YELLOW}🔍 Checking agent status...${NC}"
        if [[ -d "$MODEL" ]]; then
            echo -e "${GREEN}✅ Model directory exists: $MODEL${NC}"
            if [[ -n "$SCRIPT_PATH" && -f "$SCRIPT_PATH" ]]; then
                echo -e "${GREEN}✅ Integration script available: $SCRIPT_PATH${NC}"
                if [[ "$AGENT" == "deepseek" ]]; then
                    python3 "$SCRIPT_PATH" --status || echo -e "${YELLOW}⚠️ Script execution failed${NC}"
                fi
            else
                echo -e "${YELLOW}⚠️ No integration script available${NC}"
            fi
        else
            echo -e "${RED}❌ Model directory not found: $MODEL${NC}"
            echo -e "${YELLOW}💡 Run: ./scripts/download_models.sh${NC}"
        fi
        log_event "status_check" "$AGENT status checked"
        ;;
        
    info)
        echo -e "${YELLOW}📋 Agent Information...${NC}"
        echo -e "${BLUE}Agent: $AGENT${NC}"
        echo -e "${BLUE}Model Path: $MODEL${NC}"
        echo -e "${BLUE}Script: ${SCRIPT_PATH:-'Direct Python integration'}${NC}"
        
        case "$AGENT" in
            deepseek)
                echo -e "${GREEN}🧠 DeepSeek R1 Reasoning Agent${NC}"
                echo -e "   • Advanced reasoning capabilities"
                echo -e "   • Primary coding intelligence"
                echo -e "   • Deep code analysis"
                if [[ -f "$SCRIPT_PATH" ]]; then
                    python3 "$SCRIPT_PATH" --info 2>/dev/null || echo -e "${YELLOW}⚠️ Script info unavailable${NC}"
                fi
                ;;
            phi2)
                echo -e "${GREEN}🔧 Phi-2 Integration Agent${NC}"
                echo -e "   • Lightweight language model"
                echo -e "   • Code generation and analysis"
                echo -e "   • Frontend integration"
                ;;
            ollama)
                echo -e "${GREEN}🦙 Ollama Local Agent${NC}"
                echo -e "   • Local model serving"
                echo -e "   • Multiple model support"
                echo -e "   • REST API interface"
                ;;
            qwen)
                echo -e "${GREEN}🌟 Qwen 2.5 Agent${NC}"
                echo -e "   • Multilingual support"
                echo -e "   • Glue code generation"
                echo -e "   • Integration logic"
                ;;
            mixtral)
                echo -e "${GREEN}🚀 Mixtral 8x7B Agent${NC}"
                echo -e "   • High-performance reasoning"
                echo -e "   • Complex task handling"
                echo -e "   • Advanced code generation"
                ;;
        esac
        log_event "info_request" "$AGENT info displayed"
        ;;
        
    prompt|file)
        # --- Prepare the query ---
        if [[ "$MODE" == "prompt" ]]; then
            PROMPT="$INPUT"
        else
            # Read file for context/task
            if [[ ! -f "$INPUT" ]]; then
                echo -e "${RED}❌ File not found: $INPUT${NC}"
                exit 1
            fi
            FILE_CONTENT=$(cat "$INPUT")
            PROMPT="You are the $AGENT agent. Process the following file: $(basename "$INPUT"). File content:\n$FILE_CONTENT\n\nPlease analyze and provide suggestions."
        fi
        
        echo -e "${YELLOW}🚀 Processing with $AGENT...${NC}"
        
        # --- Dispatch to agent ---
        if [[ "$AGENT" == "deepseek" && -f "$SCRIPT_PATH" ]]; then
            # Use existing DeepSeek integration
            echo -e "${BLUE}Using DeepSeek R1 Integration...${NC}"
            python3 "$SCRIPT_PATH" --reason "$PROMPT" --type analytical
            RESULT_CODE=$?
        else
            # --- Use Python + Transformers for direct model access ---
            echo -e "${BLUE}Using direct model access...${NC}"
            python3 <<PY
import sys
import os
sys.path.append('.')

try:
    from transformers import AutoModelForCausalLM, AutoTokenizer
    import torch
    
    model_dir = "$MODEL"
    if not os.path.exists(model_dir):
        print("❌ Model not found: $model_dir")
        print("💡 Run model download workflow first")
        sys.exit(1)
    
    print("🔄 Loading model and tokenizer...")
    try:
        tok = AutoTokenizer.from_pretrained(model_dir)
        model = AutoModelForCausalLM.from_pretrained(model_dir, torch_dtype=torch.float32)
        
        if tok.pad_token is None:
            tok.pad_token = tok.eos_token
            
        prompt = '''$PROMPT'''
        
        print("🧠 Processing prompt...")
        inputs = tok(prompt, return_tensors="pt", max_length=512, truncation=True)
        
        with torch.no_grad():
            outputs = model.generate(
                **inputs, 
                max_new_tokens=512,
                do_sample=True,
                temperature=0.7,
                top_p=0.9,
                pad_token_id=tok.eos_token_id
            )
        
        reply = tok.decode(outputs[0], skip_special_tokens=True)
        
        print("="*60)
        print(f"🤖 {agent.upper()} AGENT RESPONSE:")
        print("="*60)
        print(reply)
        print("="*60)
        
    except Exception as e:
        print(f"❌ Model execution error: {str(e)}")
        print("💡 Make sure model is properly downloaded and compatible")
        sys.exit(1)
        
except ImportError as e:
    print(f"❌ Missing dependencies: {str(e)}")
    print("💡 Install: pip install torch transformers")
    sys.exit(1)
PY
            RESULT_CODE=$?
        fi
        
        if [[ $RESULT_CODE -eq 0 ]]; then
            echo -e "${GREEN}✅ Agent processing completed successfully${NC}"
            log_event "successful_interaction" "$AGENT processed $MODE: ${INPUT:0:50}..."
        else
            echo -e "${RED}❌ Agent processing failed${NC}"
            log_event "failed_interaction" "$AGENT failed $MODE: ${INPUT:0:50}..."
        fi
        ;;
esac

echo -e "${BLUE}📊 Interaction logged to logs/agent_interactions.jsonl${NC}"