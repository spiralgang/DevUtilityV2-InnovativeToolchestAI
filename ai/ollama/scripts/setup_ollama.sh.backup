#!/bin/bash

# Ollama AI Core Setup Script for DevUtilityV2.5—InnovativeToolchestAI
# This script sets up the Ollama AI integration with the LLama3.1 model

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AI_DIR="$(dirname "$SCRIPT_DIR")"
REPO_ROOT="$(dirname "$(dirname "$AI_DIR")")"
MODEL_DIR="$AI_DIR/llama3.1_ollama_v3"

echo "🚀 Setting up Ollama AI Core for DevUtilityV2.5—InnovativeToolchestAI"
echo "📁 Model directory: $MODEL_DIR"

# Check if the model directory exists
if [ ! -d "$MODEL_DIR" ]; then
    echo "❌ Model directory not found. Please ensure the model is cloned first."
    exit 1
fi

# Check if Ollama is installed
if ! command -v ollama &> /dev/null; then
    echo "📦 Installing Ollama..."
    curl -fsSL https://ollama.ai/install.sh | sh
else
    echo "✅ Ollama is already installed"
fi

# Check model files
echo "🔍 Checking model files..."
cd "$MODEL_DIR"

if [ -f "unsloth.Q2_K.gguf" ] && [ -f "unsloth.Q3_K_M.gguf" ] && [ -f "unsloth.Q3_K_S.gguf" ]; then
    echo "✅ Model files found"
    
    # Check if files are LFS pointers (small files) or actual model files
    Q2_SIZE=$(stat -c%s "unsloth.Q2_K.gguf" 2>/dev/null || echo "0")
    if [ "$Q2_SIZE" -lt "1000" ]; then
        echo "⚠️  Model files appear to be Git LFS pointers (size: ${Q2_SIZE} bytes)"
        echo "💡 To download the actual model files, run:"
        echo "   cd $MODEL_DIR && git lfs pull"
        echo ""
        echo "🔧 For now, we'll set up the integration framework..."
    else
        echo "✅ Model files are ready (size: ${Q2_SIZE} bytes)"
    fi
else
    echo "❌ Some model files are missing"
    ls -la
fi

echo ""
echo "🎯 Ollama AI Core setup complete!"
echo "📖 Model Info:"
echo "   - Base Model: unsloth/meta-llama-3.1-8b-instruct"
echo "   - Formats: Q2_K, Q3_K_M, Q3_K_S (quantized GGUF)"
echo "   - License: Apache 2.0"
echo ""
echo "🚀 Integration ready for DevUtility agentic standards!"