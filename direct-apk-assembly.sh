#!/bin/bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# DIRECT APK ASSEMBLY - Bypass Gradle/Build Tools Entirely

APK_NAME="NeuronLabs.apk"

# Create APK structure manually
create_direct_apk() {
    mkdir -p manual-apk/{META-INF,res,classes}
    
    # Create manifest
    cat > manual-apk/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.spiralgang.neuronlabs">
    <application android:label="NeuronLabs">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
EOF

    # Create basic resources
    mkdir -p manual-apk/res/values
    cat > manual-apk/res/values/strings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">NeuronLabs</string>
</resources>
EOF

    # Use aapt to package (if available)
    if command -v aapt >/dev/null; then
        aapt package -f -m -J manual-apk/gen -S manual-apk/res \
             -M manual-apk/AndroidManifest.xml -I $ANDROID_HOME/platforms/android-33/android.jar
    fi
    
    # Create unsigned APK
    cd manual-apk
    zip -r "../$APK_NAME" .
    cd ..
    
    echo "Created: $APK_NAME (unsigned)"
}