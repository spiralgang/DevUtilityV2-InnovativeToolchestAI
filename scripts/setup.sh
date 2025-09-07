#!/bin/bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi


echo "🌶️ SrirachaArmy DevUtility Setup Script 🌶️"
echo "=========================================="

# Check if Android SDK is available
if [ -z "$ANDROID_HOME" ]; then
    echo "❌ ANDROID_HOME not set. Please set up Android SDK first."
    echo "   Export ANDROID_HOME=/path/to/your/android/sdk"
    exit 1
fi

echo "✅ Android SDK found at: $ANDROID_HOME"

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
echo "☕ Java version: $JAVA_VERSION"

# Create local.properties if it doesn't exist
if [ ! -f "local.properties" ]; then
    echo "📝 Creating local.properties..."
    echo "sdk.dir=$ANDROID_HOME" > local.properties
fi

# Make gradlew executable
chmod +x gradlew

echo "🔧 Building SrirachaArmy DevUtility..."
./gradlew clean assembleDebug

if [ $? -eq 0 ]; then
    echo "🎉 Build successful!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "🚀 To install on device:"
    echo "   adb install app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📱 Required permissions to grant manually:"
    echo "   - Accessibility Service (for SrirachaScreenHop)"
    echo "   - Display over other apps (for FloatWindowWatcher)"
    echo "   - File access permissions"
else
    echo "❌ Build failed. Check error messages above."
    exit 1
fi

echo "🌶️ SrirachaArmy DevUtility setup complete! 🌶️"