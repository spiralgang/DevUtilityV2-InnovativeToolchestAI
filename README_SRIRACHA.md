# 🌶️ SrirachaArmy DevUtility Android IDE

A comprehensive Android development environment featuring AI-powered coding assistance, terminal integration, and seamless cross-platform development capabilities.

*Based on DevUtilityV2.5 / DevUtilityAndroidV2.5 with complete SrirachaArmy bot integration and DeepSeek AI enhancement*

## 🚀 Features

### 🤖 DeepSeek AI Integration
- **Contextual Code Analysis**: AI-powered code suggestions based on terminal environment
- **Real-time Learning**: Adaptive AI that learns from your coding patterns  
- **SSH Context Awareness**: AI suggestions consider your terminal state and environment

### 🌶️ SrirachaArmy Bot Ecosystem
- **AI Think Module**: Core reasoning and analysis engine
- **Learning Bot**: Adaptive learning and pattern recognition
- **Sriracha Guidance System**: Contextual guidance and recommendations
- **WebNetCaste AI**: Web intelligence and content analysis
- **Float Window Watcher**: Floating UI elements for enhanced development experience

### 🖥️ Terminal Integration
- **SSH Terminal Support**: Full SSH client integration for UserLand compatibility
- **Interactive Command Execution**: Real-time command execution with output streaming
- **Environment Context Sharing**: Terminal state influences AI suggestions

### 🎯 Accessibility & Automation
- **SrirachaScreenHop Service**: Advanced screen automation capabilities
- **Float Window System**: Persistent development assistant overlay
- **Cross-app Integration**: Seamless integration with other development tools

## 📋 Requirements

### Hardware Requirements
- **Minimum**: Android 10 (API 29), 3GB RAM
- **Recommended**: Samsung Galaxy S9+ or equivalent (4GB+ RAM)
- ARM64 or ARMv7 processor

### Software Requirements
- Android SDK 29+
- Java 8+
- Gradle 8.2+

## 🛠️ Installation

### Quick Setup
```bash
# Clone repository
git clone https://github.com/spiralgang/DevUtilityV2-InnovativeToolchestAI.git
cd DevUtilityV2-InnovativeToolchestAI

# Setup environment
chmod +x scripts/setup.sh
./scripts/setup.sh

# Deploy to device
chmod +x scripts/deploy.sh
./scripts/deploy.sh
```

### Manual Installation
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

## ⚙️ Configuration

### Post-Installation Setup
1. **Enable Accessibility Service**:
   - Settings → Accessibility → SrirachaScreenHop Service → Enable

2. **Grant Display Over Other Apps Permission**:
   - Settings → Apps → SrirachaArmy DevUtility → Advanced → Display over other apps → Allow

3. **Configure File Access Permissions**:
   - Grant all file access permissions when prompted for full functionality

### SSH Configuration
```kotlin
val sshConfig = SSHConfiguration(
    host = "your-server.com",
    port = 22,
    username = "your-username",
    keyPath = "/path/to/private/key" // or use password
)
```

## 🏗️ Architecture

### Project Structure
```
DevUtilitySrirachaArmyIDE/
├── app/
│   ├── build.gradle
│   └── src/main/java/com/spiralgang/srirachaarmy/devutility/
│       ├── MainActivity.kt
│       ├── SrirachaArmyApplication.kt
│       ├── core/
│       │   ├── DeepSeekEngine.kt
│       │   └── SrirachaArmyOrchestrator.kt
│       ├── ai/
│       │   ├── DeepSeekAIService.kt
│       │   ├── AIThinkModule.kt
│       │   ├── LearningBot.kt
│       │   └── SrirachaGuidanceSystem.kt
│       ├── terminal/
│       │   └── SSHClient.kt
│       ├── execution/
│       │   └── FloatWindowWatcher.kt
│       └── accessibility/
│           └── SrirachaScreenHopService.kt
└── scripts/
    ├── setup.sh
    └── deploy.sh
```

### Core Components

#### 🧠 DeepSeek Engine
- Integrates AI capabilities with terminal environment
- Provides contextual code suggestions
- Validates suggestions against terminal environment

#### 🤖 SrirachaArmy Orchestrator
- Coordinates all bot activities
- Manages inter-bot communication
- Synthesizes intelligence from multiple sources

#### 📡 SSH Client
- Full SSH terminal integration
- Interactive command execution
- Environment context collection

## 🎮 Usage

### Basic Usage
1. Launch SrirachaArmy DevUtility
2. Initialize SSH connection to your development environment
3. Start coding with AI assistance
4. Use floating window for quick access to development tools

### AI-Powered Development
```kotlin
// AI analyzes your code in context of terminal environment
val result = deepSeekEngine.processCodeWithContext(
    code = "def hello_world():\n    print('Hello, World!')",
    language = "python",
    terminalContext = currentTerminalContext
)
```

### Bot Collaboration
```kotlin
// Multiple bots work together on complex tasks
val collaborationResult = srirachaArmyOrchestrator.orchestrateBotCollaboration(
    CollaborativeTask(
        type = TaskType.CODE_DEVELOPMENT,
        description = "Optimize Python function performance",
        estimatedDuration = 5000L,
        priority = Priority.HIGH
    )
)
```

## 🔧 Development

### Building from Source
```bash
# Clean build
./gradlew clean

# Build debug version
./gradlew assembleDebug

# Build release version
./gradlew assembleRelease
```

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

### Code Style
- Follows Kotlin official style guide
- Uses Timber for logging
- Implements Hilt for dependency injection

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🌶️ SrirachaArmy

Part of the SrirachaArmy ecosystem - bringing spicy development tools to developers worldwide!

### Key Features:
- **Version**: 2.5.0-sriracha
- **Build**: Android IDE Integration  
- **AI Engine**: DeepSeek Integration
- **Terminal**: SSH-enabled UserLand compatibility
- **Bots**: 5 specialized AI agents working in harmony

---

## 📜 Original DevUtility Credits

*The following app-code-runscript information-collective is legally owned as proprietary company data by Spiral Ganglion Neuron Labyrinths™ (sgNeuronLabs™) & formulated from the intellectual property of it's stakeholders.*

### Third-Party Collaborator Credits: 
- **xAI** - V2.5 Update
- **Inflection AI** - V2.5 Update  
- **Anthropic** - Only V2, Initial-Collaboration-Credit

### Contact Information:
- Email: spiralgang@outlook.com
- GitHub: https://github.com/spiralgang

---

**🚨 Important**: This is a comprehensive development environment. Ensure you have proper permissions and understand the security implications of the accessibility and overlay permissions before use.

**📱 Device Compatibility**: Optimized for Samsung Galaxy S9+ and similar devices with 4GB+ RAM for best performance.

**🔗 Integration**: Seamlessly integrates with UserLand, Termux, and other Android development environments.