#!/bin/bash
# NEURONLABS BUILD SCRIPT - Functional APK Generator

APP_NAME="NeuronLabs"
PACKAGE="com.spiralgang.neuronlabs"
BUILD_TOOLS="34.0.0"

# Create project structure
mkdir -p neuronlabs-app/{app/src/main/{java/com/spiralgang/neuronlabs,res},gradle/wrapper}
cd neuronlabs-app

# Generate build files
cat > settings.gradle << EOF
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
rootProject.name = "NeuronLabs"
include ':app'
EOF

cat > build.gradle << EOF
buildscript {
    ext.kotlin_version = '1.9.10'
    dependencies {
        classpath 'com.android.tools.build:gradle:8.1.2'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:\$kotlin_version"
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
EOF

# Copy source files (assumes files are in current directory)
cp MainActivity.kt app/src/main/java/com/spiralgang/neuronlabs/
cp app/build.gradle app/
cp AndroidManifest.xml app/src/main/

# Build APK
if command -v ./gradlew >/dev/null; then
    ./gradlew assembleDebug
    echo "APK generated: app/build/outputs/apk/debug/app-debug.apk"
else
    echo "Download gradlew first:"
    echo "curl -L https://services.gradle.org/distributions/gradle-8.4-bin.zip -o gradle.zip"
    echo "unzip gradle.zip && ./gradle-8.4/bin/gradle wrapper"
fi