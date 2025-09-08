# CODE-REAVER Local Agent Integration System

🤖 **Complete local AI agent system - NO COPILOT dependency**

## Overview

This comprehensive system provides extensive tooling for interacting with local AI agents (DeepSeek, Phi-2, Ollama, Qwen, Mixtral) directly, bypassing any Copilot dependencies. Built for the CODE-REAVER philosophy of local autonomy and anti-corporate AI control.

## 🚀 Quick Start

```bash
# Initialize the environment
./scripts/agent_cli.sh setup

# Download models
./scripts/agent_cli.sh download

# Check agent status
./scripts/agent_cli.sh status --all

# Start interactive chat
./scripts/agent_cli.sh chat --agent deepseek

# Send a prompt
./scripts/agent_cli.sh prompt --agent phi2 "Write a TypeScript interface for user management"

# Analyze a file
./scripts/agent_cli.sh file --agent qwen ./src/components/App.vue

# Start backend server
./scripts/agent_cli.sh server
```

## 🎯 System Architecture

### Core Components

1. **CLI Interface** (`scripts/agent_cli.sh`)
   - Complete management interface
   - Interactive chat sessions
   - File analysis capabilities
   - Agent orchestration

2. **Agent Interaction Script** (`scripts/interact_agent.sh`)
   - Direct agent communication
   - Multiple interaction modes
   - Comprehensive logging
   - Status monitoring

3. **Backend Server** (`backend/agent_server.py`)
   - REST API for agents
   - Local-only communication
   - Multi-agent coordination
   - Request logging

4. **Frontend Interface** (`frontend/src/components/AgentDashboard.vue`)
   - Web-based agent interaction
   - Multi-agent consensus
   - Visual status monitoring
   - Interaction history

5. **API Module** (`frontend/src/utils/agentApi.js`)
   - Frontend-backend communication
   - Agent capability mapping
   - Local storage logging
   - Error handling

## 🤖 Available Agents

### DeepSeek R1 🧠
- **Capabilities**: Advanced reasoning, primary coding, deep analysis
- **Use Cases**: Complex coding tasks, architectural decisions
- **Priority**: Primary agent for development work

### Phi-2 🔧
- **Capabilities**: Code generation, frontend integration
- **Use Cases**: UI components, lightweight scripting
- **Priority**: Secondary agent for frontend tasks

### Ollama 🦙
- **Capabilities**: Local serving, multiple models, REST API
- **Use Cases**: Model serving, API endpoints
- **Priority**: Infrastructure agent

### Qwen 2.5 🌟
- **Capabilities**: Multilingual support, glue code, integration logic
- **Use Cases**: Code integration, language processing
- **Priority**: Integration specialist

### Mixtral 8x7B 🚀
- **Capabilities**: High-performance reasoning, complex tasks
- **Use Cases**: Performance optimization, complex algorithms
- **Priority**: Advanced processing agent

## 📋 CLI Commands

### Environment Management
```bash
# Setup environment
./scripts/agent_cli.sh setup

# Download all models
./scripts/agent_cli.sh download

# Clean environment
./scripts/agent_cli.sh clean
```

### Agent Interaction
```bash
# Check status of all agents
./scripts/agent_cli.sh status --all

# Check specific agent
./scripts/agent_cli.sh status --agent deepseek

# Get agent information
./scripts/agent_cli.sh info deepseek

# Interactive chat session
./scripts/agent_cli.sh chat --agent deepseek

# Send single prompt
./scripts/agent_cli.sh prompt --agent phi2 "Create a Vue component"

# Analyze file
./scripts/agent_cli.sh file --agent qwen myfile.py
```

### System Management
```bash
# Start backend server
./scripts/agent_cli.sh server

# Run comprehensive tests
./scripts/agent_cli.sh test

# View interaction logs
./scripts/agent_cli.sh logs

# Export configurations and logs
./scripts/agent_cli.sh export
```

## 🌐 Web Interface

### Starting the Web Interface

1. **Start Backend Server**:
   ```bash
   ./scripts/agent_cli.sh server
   ```

2. **Access Frontend**: Open `frontend/src/components/AgentDashboard.vue` in your web framework

### Features

- **Agent Selection**: Choose from 5 available agents
- **Multiple Interaction Modes**: Prompt, file upload, status check, info
- **Multi-Agent Consensus**: Send prompts to multiple agents simultaneously
- **Interaction History**: Track all agent communications
- **Real-time Status**: Monitor agent availability
- **Export Capabilities**: Download conversation history

## 🔧 Advanced Features

### Multi-Agent Consensus

Get responses from multiple agents for the same prompt:

```javascript
import agentApi from './utils/agentApi.js';

const consensus = await agentApi.getMultiAgentConsensus(
  ['deepseek', 'phi2', 'qwen'],
  'How should I structure a React application?'
);
```

### File Processing

Analyze code files with specific agents:

```bash
# Frontend files with Phi-2
./scripts/agent_cli.sh file --agent phi2 src/components/Header.vue

# Backend files with DeepSeek
./scripts/agent_cli.sh file --agent deepseek api/controllers/user.py

# Integration logic with Qwen
./scripts/agent_cli.sh file --agent qwen utils/database.js
```

### Interactive Chat Sessions

Start persistent chat sessions with agents:

```bash
./scripts/agent_cli.sh chat --agent deepseek
```

Chat commands:
- `status` - Check agent status
- `info` - Get agent information
- `switch <agent>` - Switch to different agent
- `exit` - End chat session

## 📊 Logging and Monitoring

### Log Files

- `logs/agent_interactions.jsonl` - All agent interactions
- `logs/agent_cli.jsonl` - CLI command history
- `logs/agent_backend.jsonl` - Backend server logs
- `logs/model_sync.jsonl` - Model download/sync logs

### Log Analysis

```bash
# View recent interactions
./scripts/agent_cli.sh logs

# Export all logs
./scripts/agent_cli.sh export
```

## 🚫 Anti-Copilot Features

### Copilot Blocking

- **Disabled Workflows**: Copilot memory workflows disabled
- **Blocked Directories**: `.github/copilot-memory/` blocked in `.gitignore`
- **Local-Only**: All processing happens locally
- **No External Calls**: Offline mode enforced

### Environment Variables

```bash
TRANSFORMERS_OFFLINE=1
HF_HUB_OFFLINE=1
GIT_TERMINAL_PROMPT=0
AGENT_MODE=local
NO_COPILOT=true
```

## 🛠️ Configuration

### Agent Registry (`configs/agent_registry.json`)

Configure agent priorities, capabilities, and settings:

```json
{
  "agents": {
    "deepseek": {
      "name": "DeepSeek R1",
      "priority": 1,
      "capabilities": ["advanced_reasoning", "primary_coding"],
      "color": "#00ff00",
      "icon": "🧠"
    }
  },
  "settings": {
    "default_agent": "deepseek",
    "offline_mode": true,
    "no_copilot": true
  }
}
```

### Model Manifest (`configs/model_manifest.json`)

Define which models to download:

```json
{
  "models": [
    { "repo": "https://huggingface.co/Qwen/Qwen2.5-0.5B", "dir": "Qwen2.5-0.5B" },
    { "repo": "https://huggingface.co/microsoft/phi-2", "dir": "Phi-2" },
    { "repo": "https://huggingface.co/mistralai/Mixtral-8x7B-Instruct-v0.1", "dir": "Mixtral-8x7B" }
  ]
}
```

## 🚀 Workflow Integration

### Updated Workflow (`clone-hf-models.yml`)

Now named "CODE-REAVER Cyberforge Sync and Train":

- **Local Agent Priority**: Agents take precedence over Copilot
- **Comprehensive Testing**: All agents tested during workflow
- **Offline Mode**: No external dependencies
- **Forensic Logging**: Complete audit trail

### Workflow Modes

```bash
# Local mode (default)
gh workflow run clone-hf-models.yml -f agent_mode=local

# Comprehensive testing
gh workflow run clone-hf-models.yml -f agent_mode=comprehensive

# Fine-tuning mode
gh workflow run clone-hf-models.yml -f agent_mode=fine-tune
```

## 🔍 Troubleshooting

### Common Issues

1. **Models Not Found**
   ```bash
   ./scripts/agent_cli.sh download
   ```

2. **Permission Errors**
   ```bash
   chmod +x scripts/*.sh
   ```

3. **Python Dependencies**
   ```bash
   pip install torch transformers datasets
   ```

4. **Backend Server Issues**
   ```bash
   ./scripts/agent_cli.sh test
   ```

### Debug Mode

Enable verbose logging:

```bash
export DEBUG=1
./scripts/agent_cli.sh status --all
```

## 📈 Performance Optimization

### Model Loading

- **Caching**: Models cached between runs
- **Lazy Loading**: Models loaded on demand
- **Memory Management**: Automatic cleanup

### Request Optimization

- **Batching**: Multiple requests batched when possible
- **Timeouts**: Reasonable timeouts to prevent hanging
- **Retry Logic**: Automatic retry for failed requests

## 🎓 Best Practices

### Agent Selection

1. **DeepSeek** for complex reasoning and architecture
2. **Phi-2** for lightweight frontend tasks
3. **Qwen** for multilingual and integration work
4. **Mixtral** for performance-critical tasks
5. **Ollama** for serving and API tasks

### Prompt Engineering

- Be specific about the task
- Provide context about the codebase
- Use agent-specific capabilities
- Test with multiple agents for consensus

### File Organization

- Keep model files in `models/` directory
- Store logs in `logs/` directory
- Use `configs/` for configuration files
- Organize scripts in `scripts/` directory

## 🔮 Future Enhancements

### Planned Features

- **Voice Interface**: Speech-to-text agent interaction
- **Code Generation**: Automated code generation pipelines
- **Integration Testing**: Automated testing of agent responses
- **Performance Metrics**: Detailed performance analysis
- **Custom Models**: Support for custom trained models

### Extensibility

The system is designed to be easily extended with new agents, capabilities, and interaction modes. All components are modular and follow consistent interfaces.

---

**🚫 NO COPILOT REQUIRED - CODE-REAVER APPROVED 🤖**

This system provides complete autonomy from corporate AI services while maintaining advanced capabilities through local agent orchestration.