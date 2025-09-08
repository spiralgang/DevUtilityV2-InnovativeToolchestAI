# CODE-REAVER Local Agent Scripts

🚫 **NO COPILOT - LOCAL AGENTS ONLY** 🤖

This directory contains the complete collection of scripts for the CODE-REAVER local agent integration system.

## 📜 Script Overview

### 🎯 Primary Scripts

#### `agent_cli.sh` - Complete Management Interface
The main CLI tool for managing all local agents.

**Usage:**
```bash
./scripts/agent_cli.sh [COMMAND] [OPTIONS]
```

**Key Commands:**
- `setup` - Initialize local agent environment
- `download` - Download all models from manifest
- `status --all` - Check status of all agents
- `chat --agent deepseek` - Start interactive chat
- `prompt --agent phi2 "prompt"` - Send single prompt
- `file --agent qwen myfile.py` - Analyze file
- `server` - Start backend API server
- `test` - Run comprehensive tests

#### `interact_agent.sh` - Direct Agent Communication
Low-level script for direct agent interaction.

**Usage:**
```bash
./scripts/interact_agent.sh [agent] [mode] [input]
```

**Supported Agents:**
- `deepseek` - DeepSeek R1 (Advanced reasoning)
- `phi2` - Phi-2 (Frontend integration)  
- `ollama` - Ollama (Local serving)
- `qwen` - Qwen 2.5 (Multilingual)
- `mixtral` - Mixtral 8x7B (High-performance)

**Modes:**
- `prompt` - Send text prompt
- `file` - Analyze file
- `status` - Check agent status
- `info` - Get agent information

### 🎪 Demonstration Script

#### `demo_comprehensive.sh` - Full System Demo
Comprehensive demonstration of all system capabilities.

**Features:**
- Environment setup
- Agent status checking
- Interaction testing
- File analysis demo
- Backend server testing
- Logging demonstration

**Usage:**
```bash
./scripts/demo_comprehensive.sh
```

## 🚀 Quick Start Examples

### Basic Usage
```bash
# Setup environment
./scripts/agent_cli.sh setup

# Check all agents
./scripts/agent_cli.sh status --all

# Interactive chat with DeepSeek
./scripts/agent_cli.sh chat --agent deepseek

# Send prompt to Phi-2
./scripts/agent_cli.sh prompt --agent phi2 "Create a Vue component for user profiles"

# Analyze file with Qwen
./scripts/agent_cli.sh file --agent qwen src/components/App.vue
```

### Advanced Usage
```bash
# Download models
./scripts/agent_cli.sh download

# Start backend server
./scripts/agent_cli.sh server

# Run comprehensive tests
./scripts/agent_cli.sh test

# View logs
./scripts/agent_cli.sh logs

# Export data
./scripts/agent_cli.sh export
```

### Direct Interaction
```bash
# Direct agent status check
./scripts/interact_agent.sh deepseek status

# Direct prompt to agent
./scripts/interact_agent.sh phi2 prompt "Help me optimize this React component"

# Direct file analysis
./scripts/interact_agent.sh qwen file ./backend/server.py
```

## 🔧 Configuration

### Environment Variables
Scripts automatically set:
```bash
TRANSFORMERS_OFFLINE=1
HF_HUB_OFFLINE=1
GIT_TERMINAL_PROMPT=0
AGENT_MODE=local
NO_COPILOT=true
```

### Configuration Files
- `configs/agent_registry.json` - Agent configuration
- `configs/model_manifest.json` - Model download list
- `.env` - Environment settings

## 📊 Logging

All scripts log interactions to:
- `logs/agent_interactions.jsonl` - Agent interactions
- `logs/agent_cli.jsonl` - CLI command history
- `logs/agent_backend.jsonl` - Backend server logs

## 🛡️ Anti-Copilot Features

- **Offline Mode**: All processing happens locally
- **No External Calls**: Blocked from making external API requests
- **Local Logging**: All data stored locally
- **Copilot Bypass**: Complete independence from Copilot services

## 🎯 Agent Specializations

### DeepSeek R1 🧠
**Best For:**
- Complex reasoning tasks
- Architectural decisions
- Advanced coding problems
- Deep code analysis

### Phi-2 🔧
**Best For:**
- Frontend components
- UI/UX related tasks
- Lightweight scripting
- Quick code generation

### Ollama 🦙
**Best For:**
- Model serving
- API endpoints
- Infrastructure tasks
- Local model management

### Qwen 2.5 🌟
**Best For:**
- Multilingual tasks
- Code integration
- Glue code generation
- Language processing

### Mixtral 8x7B 🚀
**Best For:**
- Performance optimization
- Complex algorithms
- High-performance tasks
- Advanced reasoning

## 🚨 Troubleshooting

### Common Issues

**Models Not Found:**
```bash
./scripts/agent_cli.sh download
```

**Permission Errors:**
```bash
chmod +x scripts/*.sh
```

**Backend Server Issues:**
```bash
./scripts/agent_cli.sh test
```

### Debug Mode
```bash
export DEBUG=1
./scripts/agent_cli.sh status --all
```

## 📚 Related Documentation

- [`docs/LOCAL_AGENT_SYSTEM.md`](../docs/LOCAL_AGENT_SYSTEM.md) - Complete system documentation
- [`backend/agent_server.py`](../backend/agent_server.py) - Backend API server
- [`frontend/src/utils/agentApi.js`](../frontend/src/utils/agentApi.js) - Frontend API module

## 🎉 Getting Started

1. **Run the Demo:**
   ```bash
   ./scripts/demo_comprehensive.sh
   ```

2. **Setup Environment:**
   ```bash
   ./scripts/agent_cli.sh setup
   ```

3. **Start Using Agents:**
   ```bash
   ./scripts/agent_cli.sh chat --agent deepseek
   ```

---

**🚫 NO COPILOT REQUIRED - CODE-REAVER APPROVED 🤖**

All scripts are designed for complete local autonomy with extensive capabilities as requested.