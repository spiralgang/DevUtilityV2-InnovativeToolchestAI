#!/bin/bash

echo "🚀 SrirachaArmy DevUtility Deployment Script 🚀"
echo "=============================================="

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

# Check if APK exists
if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK not found. Run setup.sh first to build the project."
    exit 1
fi

# Check if device is connected
adb devices | grep -q "device$"
if [ $? -ne 0 ]; then
    echo "❌ No Android device connected. Please connect a device and enable USB debugging."
    echo "   Run 'adb devices' to verify connection."
    exit 1
fi

echo "📱 Android device detected"

# Install APK
echo "📦 Installing SrirachaArmy DevUtility..."
adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    echo "✅ Installation successful!"
    echo ""
    echo "🔧 Post-installation setup:"
    echo "1. Enable Accessibility Service:"
    echo "   Settings > Accessibility > SrirachaScreenHop Service > Enable"
    echo ""
    echo "2. Grant Display Over Other Apps permission:"
    echo "   Settings > Apps > SrirachaArmy DevUtility > Advanced > Display over other apps > Allow"
    echo ""
    echo "3. Grant file access permissions when prompted"
    echo ""
    echo "🌶️ SrirachaArmy DevUtility deployed successfully! 🌶️"
    echo "🚀 Launch the app and start your endless DevUtility experience!"
else
    echo "❌ Installation failed. Check the error messages above."
    exit 1
fi