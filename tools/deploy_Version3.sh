#!/bin/bash
# Direct APK deployment script

PROJECT_NAME="ashlar-native-terminal"
APK_OUTPUT="app/build/outputs/apk/debug/app-debug.apk"

create_project_structure() {
    mkdir -p $PROJECT_NAME/{app/src/main/{cpp,java/com/spiralgang/ashlar,res/values}}
    cd $PROJECT_NAME
}

setup_build_files() {
    # Copy implementation files
    cp ../native_terminal_core.cpp app/src/main/cpp/
    cp ../jni_bridge.cpp app/src/main/cpp/
    cp ../Android.mk app/src/main/cpp/
    cp ../Application.mk app/src/main/cpp/
    cp ../MainActivity.kt app/src/main/java/com/spiralgang/ashlar/
    
    # Create manifest
    cat > app/src/main/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.spiralgang.ashlar">
    
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application
        android:label="Ashlar Terminal"
        android:icon="@android:drawable/star_on"
        android:theme="@android:style/Theme.Black.NoTitleBar.Fullscreen">
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
    </application>
</manifest>
EOF

    # Create strings resource
    cat > app/src/main/res/values/strings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Ashlar Terminal</string>
</resources>
EOF
}

build_and_deploy() {
    if command -v gradle >/dev/null; then
        gradle assembleDebug
        
        if [ -f "$APK_OUTPUT" ]; then
            echo "✅ APK built successfully: $APK_OUTPUT"
            
            if command -v adb >/dev/null && adb devices | grep -q "device$"; then
                adb install "$APK_OUTPUT"
                echo "✅ APK installed on connected device"
            else
                echo "📱 Connect device and run: adb install $APK_OUTPUT"
            fi
        fi
    else
        echo "⚠️ Gradle not found. Use Android Studio or upload to GitHub Actions"
    fi
}

create_project_structure
setup_build_files
build_and_deploy