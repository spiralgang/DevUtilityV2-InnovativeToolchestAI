# DevUtilityV2 SrirachaArmy - Developer Instructions

Always follow these instructions first and only search for additional information if these instructions are incomplete or found to be in error.

## Project Overview
DevUtilityV2 (SrirachaArmy) is a sophisticated Android development utility that integrates AI-driven coding interfaces with comprehensive developer tools. It features Kotlin + Jetpack Compose UI, TensorFlow Lite AI integration, advanced conflict resolution automation, and multi-window development capabilities.

## Working Effectively

### Repository Validation and Setup
Always start by validating the working components:
```bash
# Validate all systems - takes ~15 seconds, NEVER CANCEL
cd /home/runner/work/DevUtilityV2-InnovativeToolchestAI/DevUtilityV2-InnovativeToolchestAI
chmod +x scripts/*.sh
./scripts/validate-system.sh
# Expected output: All validation tests pass in ~15 seconds
```

### Conflict Resolution System (Fully Functional)
The conflict resolution system is the most reliable component - always test it first:
```bash
# Demo the complete system - runs in 0.18 seconds
./scripts/demo-conflict-system.sh

# Test individual components
python3 scripts/conflict_resolver.py --help                    # ~0.044s
./scripts/manual-conflict-resolver.sh --help                   # ~0.003s
python3 scripts/resolve-active-conflicts.py --help             # sub-second
```

### Environment Prerequisites
Verify required environment (these are already configured in CI):
```bash
echo $ANDROID_HOME                    # Should show: /usr/local/lib/android/sdk
java -version                         # Should show: OpenJDK 17
python3 --version                     # Should show: Python 3.x
which gradle                          # Should show: /usr/bin/gradle
```

## Android Application Build System

### **CRITICAL BUILD ISSUES - KNOWN LIMITATIONS**
The Android build system has compatibility issues that require manual intervention:

**Problem**: Gradle 9.0.0 is incompatible with Android Gradle Plugin 8.2.0
**Status**: Build fails with plugin resolution and API compatibility errors
**Impact**: Cannot currently build the Android application without significant refactoring

**Workarounds**:
1. **DO NOT attempt to run `gradle build` or `./gradlew` commands** - they will fail
2. Focus development on the conflict resolution scripts and documentation
3. For Android development, use alternative build approaches or downgrade Gradle

### Build Configuration Analysis
Current configuration issues identified:
```bash
# Root build.gradle - Fixed but still has compatibility issues
# app/build.gradle - Modern Kotlin/Compose configuration (correct)
# Missing gradlew wrapper - generation fails due to plugin conflicts
```

**If you must attempt Android builds**:
```bash
# NEVER CANCEL: These commands will fail but log useful debug info
# Set timeout to 600+ seconds for diagnostic purposes only
gradle clean build --no-daemon --stacktrace
# Expected: Failure with plugin compatibility errors
```

## Validation Scenarios

### Manual Validation Requirements
After making any changes, ALWAYS run these validation steps:

#### 1. Script System Validation
```bash
# Test all conflict resolution components - 15 seconds, NEVER CANCEL
./scripts/validate-system.sh
# Must show: "🎉 All validation tests passed!"
```

#### 2. Conflict Resolution Workflow Testing
```bash
# Test conflict detection on actual repository data
python3 scripts/conflict_resolver.py --source copilot/fix-13 --target main
# Should complete without errors in sub-second time

# Test interactive help systems
./scripts/manual-conflict-resolver.sh --help
python3 scripts/conflict_resolver.py --help
# Both should return usage information instantly
```

#### 3. GitHub Workflows Validation
```bash
# Check workflow syntax (if actionlint available)
ls -la .github/workflows/
# Should show 4 workflow files: conflict-resolution.yml, auto-merge-prune.yml, copilot-idempotent-pr.yml, crda.yml
```

### Functional Testing Scenarios
When testing changes to the conflict resolution system:

1. **Create a test conflict scenario**:
   ```bash
   git checkout -b test-conflict-branch
   echo "# Test change" >> README.md
   git add README.md && git commit -m "Test conflict"
   ```

2. **Test conflict detection**:
   ```bash
   python3 scripts/conflict_resolver.py --source test-conflict-branch --target main
   ```

3. **Test resolution workflows**:
   ```bash
   ./scripts/manual-conflict-resolver.sh -s test-conflict-branch -t main -l
   ```

## Common Tasks Reference

### Repository Structure
```
DevUtilityV2-InnovativeToolchestAI/
├── app/                                 # Android application source
│   ├── src/main/java/.../devutility/   # Kotlin source (20+ files)
│   └── build.gradle                    # Modern Android config
├── scripts/                            # Working automation scripts
│   ├── conflict_resolver.py            # Primary conflict detection
│   ├── manual-conflict-resolver.sh     # Interactive resolution
│   ├── resolve-active-conflicts.py     # Active merge handling
│   ├── validate-system.sh              # System validation
│   └── demo-conflict-system.sh         # Complete system demo
├── .github/workflows/                  # CI/CD automation
├── docs/                               # Comprehensive documentation
└── build.gradle                       # Project-level config (fixed)
```

### Key Source Files Locations
```bash
# Main Android application entry point
app/src/main/java/com/spiralgang/srirachaarmy/devutility/MainActivity.kt

# AI and core functionality
app/src/main/java/com/spiralgang/srirachaarmy/devutility/ai/
app/src/main/java/com/spiralgang/srirachaarmy/devutility/core/

# UI and theming
app/src/main/java/com/spiralgang/srirachaarmy/devutility/ui/

# Advanced features
app/src/main/java/com/spiralgang/srirachaarmy/devutility/accessibility/
app/src/main/java/com/spiralgang/srirachaarmy/devutility/terminal/
```

### Documentation Locations
- `docs/CONFLICT_RESOLUTION.md` - Complete conflict resolution guide
- `README.md` - Project overview and status
- `.github/copilot-instructions.md` - These instructions
- Multiple AI training datasets and documentation files in root

## Timing Expectations and Timeouts

**NEVER CANCEL any of these operations** - set appropriate timeouts:

| Operation | Expected Time | Timeout Setting | Status |
|-----------|---------------|----------------|---------|
| `./scripts/validate-system.sh` | ~15 seconds | 60 seconds | ✅ Working |
| `./scripts/demo-conflict-system.sh` | ~0.18 seconds | 30 seconds | ✅ Working |
| `python3 scripts/conflict_resolver.py --help` | ~0.044 seconds | 10 seconds | ✅ Working |
| `./scripts/manual-conflict-resolver.sh --help` | ~0.003 seconds | 10 seconds | ✅ Working |
| `gradle build` | NEVER COMPLETES | N/A | ❌ Broken |
| `./gradlew` commands | NEVER COMPLETES | N/A | ❌ Missing wrapper |

## Technology Stack Analysis

### Verified Working Components
- **Python Scripts**: All conflict resolution automation works perfectly
- **Bash Scripts**: Interactive and demo scripts fully functional  
- **GitHub Workflows**: Comprehensive CI/CD configuration available
- **Documentation**: Extensive markdown documentation system

### Android Application Stack (Build Issues)
- **UI Framework**: Jetpack Compose with Material 3
- **Language**: Kotlin with coroutines
- **DI**: Hilt dependency injection
- **AI**: TensorFlow Lite integration, DeepSeek API
- **Database**: Room with Kotlin extensions
- **Architecture**: MVVM with ViewModels and Compose navigation

### Key Dependencies (From app/build.gradle)
```groovy
// Core working dependencies that are properly configured:
androidx.compose:compose-bom:2024.02.00
org.tensorflow:tensorflow-lite:2.14.0
com.google.dagger:hilt-android:2.48.1
androidx.room:room-runtime:2.6.1
com.squareup.retrofit2:retrofit:2.9.0
```

## Development Workflow

### For Script Development
1. Always start with `./scripts/validate-system.sh`
2. Test individual components before integration
3. Use the demo script to verify end-to-end functionality
4. Validate timing remains under expected thresholds

### For Android Development  
1. **DO NOT attempt to build the Android app** - focus on source code analysis
2. Use IDE inspection of Kotlin files for understanding
3. Reference the manifest and build configuration for architecture understanding
4. Test any Android-related changes in isolation before attempting builds

### For Documentation Changes
1. Validate all links and references work
2. Test any command examples in the documentation
3. Ensure new instructions include appropriate timing expectations

## Troubleshooting

### If Scripts Fail
```bash
# Check script permissions
ls -la scripts/
# All .sh files should have +x permissions

# Validate Python syntax
python3 -m py_compile scripts/conflict_resolver.py
python3 -m py_compile scripts/resolve-active-conflicts.py

# Check system dependencies
which python3 && which bash && which git
```

### If You Need Android Build
**Current recommendation**: Do not attempt Android builds until build system is refactored.

**Alternative approaches**:
1. Focus on Kotlin source code analysis and review
2. Use conflict resolution system for any repository changes
3. Leverage documentation and training datasets for understanding
4. Consider build system refactoring as a separate major task

## References and External Standards
- Android development: Follow modern Kotlin + Compose patterns
- CI/CD: GitHub Actions best practices with comprehensive validation
- Conflict resolution: Git merge-tree analysis with automated resolution strategies
- Documentation: Markdown with comprehensive cross-linking and validation scenarios