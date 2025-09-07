# Ollama AI Core Integration

This directory contains the comprehensive Ollama AI core integration for DevUtilityV2.5—InnovativeToolchestAI with multiple specialized AI models.

## AI Models Collection

### 1. LLaMA 3.1 Core Model
- **Source**: [CosminMihai02/llama3.1_ollama_v3](https://huggingface.co/CosminMihai02/llama3.1_ollama_v3)
- **Purpose**: Primary open-source Ollama implementation for general AI capabilities

### 2. Replit Code Assistant
- **Source**: [replit/replit-code-v1-3b](https://huggingface.co/replit/replit-code-v1-3b)
- **Purpose**: Specialized code generation and completion

### 3. Qwen Mathematical Reasoning
- **Source**: [Qwen/Qwen2.5-Math-1.5B](https://huggingface.co/Qwen/Qwen2.5-Math-1.5B)
- **Purpose**: Advanced mathematical problem solving and reasoning

### 4. DeepSeek R1 Reasoning
- **Source**: [deepseek-ai/DeepSeek-R1](https://huggingface.co/deepseek-ai/DeepSeek-R1)
- **Purpose**: Advanced reasoning and complex problem solving

### 5. Google Gemma Interface
- **Source**: [SpiralGanglionNeuronLabyrinths/google-gemma-1.1-7b-it](https://huggingface.co/spaces/SpiralGanglionNeuronLabyrinths/google-gemma-1.1-7b-it)
- **Purpose**: Interactive Gemma model interface space

### 6. DevUtility V2.5 Space
- **Source**: [SpiralGanglionNeuronLabyrinths/devutility-v2-5](https://huggingface.co/spaces/SpiralGanglionNeuronLabyrinths/devutility-v2-5)
- **Purpose**: Integrated DevUtility space interface

## Directory Structure

```
ai/ollama/
├── README.md                    # This file
├── llama3.1_ollama_v3/         # Primary LLaMA 3.1 model
├── replit-code-v1-3b/          # Replit code generation model
├── qwen2.5-math-1.5b/          # Qwen mathematical reasoning model
├── deepseek-r1/                # DeepSeek reasoning model
├── google-gemma-1.1-7b-it/     # Google Gemma interface space
├── devutility-v2-5/            # DevUtility integrated space
├── scripts/                    # Integration and management scripts
└── dev_assistant.py            # Main AI assistant interface
```

## Usage

The AI cores are integrated through the DevUtility agentic standards and provide:
- Enhanced coding assistance and code generation
- Advanced mathematical reasoning and problem solving
- Complex reasoning and analytical capabilities
- Conflict resolution automation
- Development workflow optimization
- Multi-model AI ensemble capabilities

## Setup

All models are automatically cloned and configured. To activate full model capabilities:

```bash
# For each model directory, pull full model files (optional):
cd ai/ollama/[model-name] && git lfs pull
```

## Integration Scripts

- `setup_ollama.sh` - Complete Ollama installation and configuration
- `ollama_integration.py` - Python integration interface
- Model-specific integration scripts for each AI model