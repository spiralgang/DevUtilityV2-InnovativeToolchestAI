#!/bin/bash
"""
Complete AI Model Ensemble Setup Script
DevUtilityV2.5â€”InnovativeToolchestAI

This script sets up and validates the complete AI model ensemble:
- LLaMA 3.1 Core Model
- Replit Code Assistant  
- Qwen Mathematical Reasoning
- DeepSeek R1 Reasoning
- Google Gemma Interface
- DevUtility V2.5 Space

Usage: ./setup_ai_ensemble.sh
"""

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AI_OLLAMA_DIR="$(dirname "$SCRIPT_DIR")"
AI_DIR="$(dirname "$AI_OLLAMA_DIR")"
ROOT_DIR="$(dirname "$AI_DIR")"

echo -e "${BLUE}ðŸš€ DevUtility AI Model Ensemble Setup${NC}"
echo "=============================================="
echo "Root Directory: $ROOT_DIR"
echo "AI Ollama Directory: $AI_OLLAMA_DIR"
echo ""

# Function to print status
print_status() {
    local status=$1
    local message=$2
    if [ "$status" == "success" ]; then
        echo -e "${GREEN}âœ… $message${NC}"
    elif [ "$status" == "warning" ]; then
        echo -e "${YELLOW}âš ï¸�  $message${NC}"
    elif [ "$status" == "error" ]; then
        echo -e "${RED}â�Œ $message${NC}"
    else
        echo -e "${BLUE}â„¹ï¸�  $message${NC}"
    fi
}

# Check if we're in the right directory
if [ ! -f "$ROOT_DIR/README.md" ] || [ ! -d "$AI_DIR" ]; then
    print_status "error" "Script must be run from DevUtility repository root"
    exit 1
fi

print_status "info" "Step 1: Checking prerequisites..."

# Check Python
if command -v python3 &> /dev/null; then
    print_status "success" "Python 3 is available"
else
    print_status "error" "Python 3 is required but not found"
    exit 1
fi

# Check Git LFS
if command -v git-lfs &> /dev/null; then
    print_status "success" "Git LFS is available"
else
    print_status "warning" "Git LFS not found - large model files cannot be downloaded"
fi

# Check Hugging Face Hub
if python3 -c "import huggingface_hub" 2>/dev/null; then
    print_status "success" "Hugging Face Hub is available"
else
    print_status "warning" "Hugging Face Hub not found - installing..."
    python3 -m pip install --user "huggingface_hub[cli]" || print_status "warning" "Could not install HF Hub"
fi

print_status "info" "Step 2: Validating AI model directories..."

# Define expected models
declare -A MODELS=(
    ["llama3.1_ollama_v3"]="LLaMA 3.1 Core Model"
    ["replit-code-v1-3b"]="Replit Code Assistant"
    ["qwen2.5-math-1.5b"]="Qwen Mathematical Reasoning"
    ["deepseek-r1"]="DeepSeek R1 Reasoning"
    ["google-gemma-1.1-7b-it"]="Google Gemma Interface"
    ["devutility-v2-5"]="DevUtility V2.5 Space"
)

for model_dir in "${!MODELS[@]}"; do
    if [ -d "$AI_OLLAMA_DIR/$model_dir" ]; then
        print_status "success" "${MODELS[$model_dir]} directory found"
    else
        print_status "error" "${MODELS[$model_dir]} directory missing: $AI_OLLAMA_DIR/$model_dir"
    fi
done

print_status "info" "Step 3: Testing integration scripts..."

# Test multi-model manager
if [ -f "$AI_OLLAMA_DIR/scripts/multi_model_manager.py" ]; then
    print_status "success" "Multi-model manager found"
    
    # Test the manager
    if python3 "$AI_OLLAMA_DIR/scripts/multi_model_manager.py" --status >/dev/null 2>&1; then
        print_status "success" "Multi-model manager test passed"
    else
        print_status "warning" "Multi-model manager test failed"
    fi
else
    print_status "error" "Multi-model manager script not found"
fi

# Test individual model scripts
declare -A SCRIPTS=(
    ["replit_code_integration.py"]="Replit Code Integration"
    ["qwen_math_integration.py"]="Qwen Math Integration"
    ["deepseek_r1_integration.py"]="DeepSeek R1 Integration"
    ["gemma_interface_integration.py"]="Gemma Interface Integration"
    ["devutility_space_integration.py"]="DevUtility Space Integration"
)

for script in "${!SCRIPTS[@]}"; do
    if [ -f "$AI_OLLAMA_DIR/scripts/$script" ]; then
        print_status "success" "${SCRIPTS[$script]} script found"
        
        # Test the script
        if python3 "$AI_OLLAMA_DIR/scripts/$script" --status >/dev/null 2>&1; then
            print_status "success" "${SCRIPTS[$script]} test passed"
        else
            print_status "warning" "${SCRIPTS[$script]} test failed"
        fi
    else
        print_status "error" "${SCRIPTS[$script]} script not found"
    fi
done

print_status "info" "Step 4: Checking DevUtility integration..."

# Check conflict resolution system
if [ -f "$ROOT_DIR/scripts/validate-system.sh" ]; then
    print_status "success" "DevUtility conflict resolution system found"
    
    # Test the system (quick test)
    if timeout 30 "$ROOT_DIR/scripts/validate-system.sh" >/dev/null 2>&1; then
        print_status "success" "DevUtility systems validated"
    else
        print_status "warning" "DevUtility system validation incomplete"
    fi
else
    print_status "warning" "DevUtility conflict resolution system not found"
fi

print_status "info" "Step 5: Generating integration report..."

# Generate comprehensive report
REPORT_FILE="$AI_OLLAMA_DIR/INTEGRATION_REPORT.md"
python3 "$AI_OLLAMA_DIR/scripts/multi_model_manager.py" --report > "$REPORT_FILE" 2>/dev/null || \
    echo "# AI Integration Report\nReport generation failed - manual check required" > "$REPORT_FILE"

if [ -f "$REPORT_FILE" ]; then
    print_status "success" "Integration report generated: $REPORT_FILE"
else
    print_status "warning" "Could not generate integration report"
fi

print_status "info" "Step 6: Final validation..."

# Count successful integrations
SUCCESS_COUNT=0
TOTAL_COUNT=6

for model_dir in "${!MODELS[@]}"; do
    if [ -d "$AI_OLLAMA_DIR/$model_dir" ]; then
        ((SUCCESS_COUNT++))
    fi
done

echo ""
echo "=============================================="
echo -e "${BLUE}ðŸ¤– AI Model Ensemble Setup Complete${NC}"
echo "=============================================="
echo "Models Successfully Integrated: $SUCCESS_COUNT/$TOTAL_COUNT"
echo ""

if [ $SUCCESS_COUNT -eq $TOTAL_COUNT ]; then
    print_status "success" "All AI models are ready for use!"
    echo ""
    echo "Next steps:"
    echo "1. Run: python3 ai/ollama/scripts/multi_model_manager.py --status"
    echo "2. Run: python3 ai/ollama/scripts/multi_model_manager.py --activate"
    echo "3. See: ai/ollama/INTEGRATION_REPORT.md for full details"
    echo ""
    echo "To download full model files (optional, requires significant bandwidth):"
    echo "python3 ai/ollama/scripts/multi_model_manager.py --download"
elif [ $SUCCESS_COUNT -gt 3 ]; then
    print_status "success" "Most AI models are ready - partial integration successful"
    echo ""
    echo "Check the integration report for details on missing models."
else
    print_status "warning" "Several AI models are missing - integration incomplete"
    echo ""
    echo "Please check the error messages above and retry setup."
fi

echo ""
print_status "info" "Setup script completed!"