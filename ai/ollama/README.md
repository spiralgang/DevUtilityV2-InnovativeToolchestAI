# AI Model Ensemble - Specialized Autonomous AI Roles

This directory contains the comprehensive AI model ensemble for DevUtilityV2.5—InnovativeToolchestAI with specialized autonomous AI personalities and roles.

## AI Models with Specialized Roles

### 1. Big Bottle Sriracha (LLaMA 3.1 Core)
- **Source**: [CosminMihai02/llama3.1_ollama_v3](https://huggingface.co/CosminMihai02/llama3.1_ollama_v3)
- **Role**: 🔥 Unrestricted AI delegating mini autonomous bot clones of itself
- **Capabilities**: Autonomous delegation, bot cloning, unrestricted AI capabilities
- **Personality**: "Big Bottle Sriracha" - The hot sauce that makes everything better!

### 2. Orchestrator (Replit Code Assistant)
- **Source**: [replit/replit-code-v1-3b](https://huggingface.co/replit/replit-code-v1-3b)
- **Role**: 🎭 Advanced code workflow coordination and task management
- **Capabilities**: Code generation, workflow orchestration, task coordination
- **Personality**: "Orchestrator" - Conducts the symphony of coding workflows

### 3. Anti-Flail && Errors (Qwen Mathematical Reasoning)
- **Source**: [Qwen/Qwen2.5-Math-1.5B](https://huggingface.co/Qwen/Qwen2.5-Math-1.5B)
- **Role**: 🛡️ Error prevention and mathematical reasoning to avoid development flails
- **Capabilities**: Error prevention, mathematical reasoning, anti-flail logic
- **Personality**: "Anti-Flail && Errors" - Prevents chaos with mathematical precision

### 4. CodeReaver (DevUtility-AI) (DeepSeek R1)
- **Source**: [deepseek-ai/DeepSeek-R1](https://huggingface.co/deepseek-ai/DeepSeek-R1)
- **Role**: ⚔️ Primary coding intelligence with advanced reasoning and development capabilities
- **Capabilities**: Advanced reasoning, primary coding, DevUtility AI integration, deep code analysis
- **Personality**: "CodeReaver" - The legendary sword that cuts through code complexity

## Directory Structure

```
ai/ollama/
├── README.md                    # This file
├── llama3.1_ollama_v3/         # Big Bottle Sriracha - Autonomous Bot Clone Delegator
├── replit-code-v1-3b/          # Orchestrator - Code workflow coordination
├── qwen2.5-math-1.5b/          # Anti-Flail && Errors - Error prevention
├── deepseek-r1/                # CodeReaver (DevUtility-AI) - Primary coding intelligence
├── scripts/                    # Integration and management scripts
│   ├── multi_model_manager.py  # Master ensemble coordinator
│   ├── ollama_integration.py   # Big Bottle Sriracha integration
│   ├── replit_code_integration.py  # Orchestrator integration
│   ├── qwen_math_integration.py    # Anti-Flail && Errors integration
│   └── deepseek_r1_integration.py # CodeReaver integration
└── dev_assistant.py            # Main AI assistant interface
```

## Usage

The AI cores are integrated through the DevUtility agentic standards and provide specialized autonomous capabilities:
- 🔥 **Big Bottle Sriracha**: Unrestricted autonomous bot clone delegation
- 🎭 **Orchestrator**: Advanced code workflow coordination and task management
- 🛡️ **Anti-Flail && Errors**: Mathematical reasoning with error prevention logic
- ⚔️ **CodeReaver (DevUtility-AI)**: Primary coding intelligence with deep analysis
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