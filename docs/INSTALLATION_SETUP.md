# Installation & Setup Guide - DevUl Army Living Sriracha AGI

## 🎯 Complete Installation Instructions

This comprehensive guide covers all installation methods and setup procedures for the **DevUl Army : Living Sriracha AGI** system.

## 📋 Pre-Installation Checklist

### System Requirements
- [ ] **Operating System**: Linux (Ubuntu 18.04+), macOS (10.14+), Windows 10/11
- [ ] **Java Development Kit**: JDK 11 or higher
- [ ] **Android SDK**: API level 24+ (Android 7.0+) 
- [ ] **Git**: Version 2.20+ for repository operations
- [ ] **Storage**: 5GB free space for complete development environment
- [ ] **Network**: Internet connection for downloads and updates

### Android Device Requirements  
- [ ] **Android Version**: 7.0 (API 24) or higher
- [ ] **RAM**: 4GB minimum, 8GB recommended for optimal performance
- [ ] **Storage**: 2GB available space for app and data
- [ ] **Developer Options**: Enabled for ADB installation
- [ ] **USB Debugging**: Enabled for development features

## 🚀 Installation Methods

### Method 1: Quick Install (Recommended for Users)

#### Download Pre-built APK
```bash
# Create installation directory
mkdir -p ~/sriracha-agi && cd ~/sriracha-agi

# Download latest stable release
curl -L "https://github.com/spiralgang/DevUl-Army--__--Living-Sriracha-AGI/releases/latest/download/app-release.apk" -o sriracha-agi.apk

# Verify download
ls -la sriracha-agi.apk
```

#### Install on Android Device
```bash
# Method A: ADB Installation (if ADB available)
adb install sriracha-agi.apk

# Method B: Manual Installation
# 1. Transfer APK to device via USB/email/cloud
# 2. Enable "Install from Unknown Sources" in Android Settings
# 3. Open APK file on device and install
```

### Method 2: Build from Source (Recommended for Developers)

#### Step 1: Environment Setup
```bash
# Clone repository
git clone https://github.com/spiralgang/DevUl-Army--__--Living-Sriracha-AGI.git
cd DevUl-Army--__--Living-Sriracha-AGI

# Set permissions for scripts
chmod +x gradlew
chmod +x scripts/*.sh
find scripts/ -name "*.sh" -exec chmod +x {} \;
```

#### Step 2: Android SDK Configuration
```bash
# Linux/macOS - Add to ~/.bashrc or ~/.zshrc
export ANDROID_HOME="/opt/android-sdk"  # Adjust path as needed
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools

# Windows - Add to environment variables
# ANDROID_HOME = C:\Android\Sdk
# PATH += %ANDROID_HOME%\platform-tools;%ANDROID_HOME%\tools

# Reload environment
source ~/.bashrc  # Linux/macOS
# or restart terminal/PowerShell on Windows
```

#### Step 3: Validate Environment
```bash
# Install validation tools
./scripts/install-actionlint.sh

# Run comprehensive environment check
./scripts/validate-system.sh

# Expected output should show all green checkmarks
# ✅ Java JDK 11+ available
# ✅ Android SDK configured
# ✅ Gradle wrapper functional
# ✅ GitHub Actions workflow syntax valid
```

#### Step 4: Build Application
```bash
# Clean any previous builds
./gradlew clean

# Build debug version (for development)
./gradlew assembleDebug

# Build release version (for production)
./gradlew assembleRelease

# Verify build outputs
ls -la app/build/outputs/apk/debug/
ls -la app/build/outputs/apk/release/
```

#### Step 5: Install on Device
```bash
# Connect Android device via USB
# Ensure USB debugging is enabled

# Verify device connection
adb devices
# Should show your device listed

# Install debug build
adb install app/build/outputs/apk/debug/app-debug.apk

# Or install release build
adb install app/build/outputs/apk/release/app-release.apk
```

### Method 3: Development Environment Setup

#### Complete Development Stack
```bash
# Navigate to repository
cd DevUl-Army--__--Living-Sriracha-AGI

# Install all development tools
./scripts/install-actionlint.sh

# Set up AI development environment
python3 -m pip install --upgrade pip
pip3 install -r requirements.txt  # If requirements file exists

# Initialize AI systems (if applicable)
if [ -f ai/ollama/scripts/ollama_integration.py ]; then
    python3 ai/ollama/scripts/ollama_integration.py --setup
fi

# Start enhanced development environment
./scripts/demo-enhanced-devenv.sh
```

#### IDE Integration (Android Studio)
```bash
# Open project in Android Studio
# File -> Open -> Select DevUl-Army--__--Living-Sriracha-AGI directory

# Sync Gradle project
# Tools -> Android -> Sync Project with Gradle Files

# Configure build variants
# Build -> Select Build Variant -> Choose debug/release
```

## 🔧 Advanced Configuration

### AI Integration Setup
```bash
# Check AI system status
if [ -f ai/ollama/scripts/ollama_integration.py ]; then
    python3 ai/ollama/scripts/ollama_integration.py --status
fi

# Download AI models (requires significant storage)
if [ -d ai/ollama/llama3.1_ollama_v3 ]; then
    cd ai/ollama/llama3.1_ollama_v3 && git lfs pull
fi
```

### GitHub Actions Integration
```bash
# Validate all workflows
find .github/workflows -name "*.yml" -exec actionlint {} \;

# Test chat-ops commands (if repository has actions enabled)
# Available commands: /assimilate, /validate, /status
```

### Security Configuration
```bash
# Review safety policy
cat reference/VAULT.md

# Check safety enforcement
./scripts/ops-guard/safety-enforcer.sh

# Verify protected resources
cat configs/activation_rules.json
```

## 🧪 Testing & Validation

### Basic Functionality Tests
```bash
# Run system validation
./scripts/validate-system.sh

# Test Android build
./gradlew assembleDebug

# Test app installation (if device connected)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Advanced Testing
```bash
# Run unit tests
./gradlew test

# Run Android instrumentation tests (if available)
./gradlew connectedAndroidTest

# Test conflict resolution system
./scripts/demo-conflict-system.sh

# Test AI integration
./scripts/demo-living-ai-interface.sh
```

### Performance Validation
```bash
# Check build performance
time ./gradlew assembleDebug

# Memory usage analysis (if tools available)
./gradlew --profile assembleDebug

# App startup testing (if device connected)
adb shell am start -W com.spiralgang.srirachaarmy.devutility/.MainActivity
```

## 🛠️ Troubleshooting Guide

### Common Build Issues

#### Gradle Build Failures
```bash
# Issue: Gradle wrapper not executable
chmod +x gradlew

# Issue: Android SDK not found
export ANDROID_HOME="/path/to/android/sdk"
echo $ANDROID_HOME

# Issue: Java version incompatibility
java -version  # Should be 11+
update-alternatives --config java  # Linux
```

#### Dependency Issues
```bash
# Clear Gradle cache
./gradlew clean --refresh-dependencies

# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.0

# Check for conflicting dependencies
./gradlew dependencies
```

### Installation Problems

#### ADB Connection Issues
```bash
# Check ADB server status
adb kill-server && adb start-server

# List connected devices
adb devices -l

# Check device authorization
# Look for device in "Settings > Developer Options > USB Debugging"
```

#### APK Installation Failures
```bash
# Issue: INSTALL_FAILED_INSUFFICIENT_STORAGE
adb shell pm clear com.spiralgang.srirachaarmy.devutility

# Issue: INSTALL_FAILED_UPDATE_INCOMPATIBLE
adb uninstall com.spiralgang.srirachaarmy.devutility
adb install app/build/outputs/apk/debug/app-debug.apk

# Issue: INSTALL_PARSE_FAILED_NO_CERTIFICATES
# Use debug build or properly sign release build
```

### Environment Issues

#### Android SDK Problems
```bash
# Update SDK components
sdkmanager --update
sdkmanager "platform-tools" "platforms;android-30"

# Accept licenses
sdkmanager --licenses

# Check available packages
sdkmanager --list
```

#### Script Execution Problems
```bash
# Issue: Permission denied
find scripts/ -name "*.sh" -exec chmod +x {} \;

# Issue: Command not found
export PATH=$PATH:./scripts
source ~/.bashrc

# Issue: Missing dependencies
./scripts/install-actionlint.sh
apt update && apt install -y curl jq git  # Linux
brew install curl jq git  # macOS
```

## 📱 Post-Installation Setup

### First Launch Checklist
- [ ] App installs without errors
- [ ] App launches successfully
- [ ] UI elements load properly
- [ ] AI features are accessible
- [ ] No immediate crashes or errors

### Configuration Steps
1. **Grant Permissions**: Allow required Android permissions
2. **AI Setup**: Configure AI model preferences (if applicable)
3. **Development Settings**: Enable development features
4. **Sync Settings**: Configure cloud sync (if available)

### Verification Commands
```bash
# Check app is installed
adb shell pm list packages | grep spiralgang

# Check app version
adb shell dumpsys package com.spiralgang.srirachaarmy.devutility | grep versionName

# Monitor app logs
adb logcat | grep "SrirachaArmy"
```

## 🎯 Next Steps

### For End Users
1. Explore app features and AI capabilities
2. Review [Getting Started Guide](GETTING_STARTED.md)
3. Check out [User Documentation](README.md)

### For Developers
1. Set up development environment completely
2. Review [Development Standards](../reference/standards/DevUtilityAgenticStandards.md)
3. Explore codebase and contribute improvements
4. Test chat-ops commands and workflows

### For Contributors
1. Read [Contributing Guidelines](../CONTRIBUTING.md)
2. Set up development workflows
3. Run full test suite
4. Submit your first pull request

---

## 📊 Installation Success Criteria

✅ **Environment Ready**: All required tools installed and configured  
✅ **Build Success**: App builds without errors  
✅ **Installation Complete**: App installs and launches on device  
✅ **Validation Passed**: All system checks pass  
✅ **Features Functional**: Core app features work as expected  

**Installation complete! Ready to experience the future of Android development! 🚀**