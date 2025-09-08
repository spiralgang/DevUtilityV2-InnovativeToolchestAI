# GitHub Copilot Instructions - DevUl Army Living Sriracha AGI

## 🤖 COPILOT OPERATIONAL DIRECTIVES

### PRIMARY MISSION
You are operating within the **DevUl Army : Living Sriracha AGI** repository - a comprehensive Android development platform with integrated AI systems, living-code augmentation, and agentic automation capabilities.

### CORE PRINCIPLES
- **PRECISION OVER SPEED**: Make surgical, minimal changes that preserve existing functionality
- **ANDROID APP PROTECTION**: Never modify Android build system, Gradle configs, or source code without explicit request
- **SAFETY FIRST**: All destructive operations require approval via `configs/activation_rules.json` and `configs/move_manifest.json`
- **REPOSITORY ORGANIZATION**: Maintain the established directory structure and file organization
- **LIVING CODE RESPECT**: Preserve conflict resolution systems and living-code mechanisms

### REPOSITORY ARCHITECTURE UNDERSTANDING

#### Core Components
- **Android App** (`app/`): Full Android development platform with AI integration
- **AI Systems** (`ai/`): TensorFlow Lite, Ollama, multi-model orchestration
- **Scripts** (`scripts/`): 97 automation scripts organized by function
- **Tools** (`tools/`): Development utilities and validation systems
- **Docs** (`docs/`): Comprehensive documentation (149 files)
- **Configs** (`configs/`): System configuration and safety policies

#### Protected Resources (DO NOT MODIFY)
- `scripts/validate-system.sh` - System validation script
- `scripts/manual-conflict-resolver.sh` - Conflict resolution
- `app/build.gradle` - Android build configuration  
- `build.gradle` - Root Gradle configuration
- `gradlew` / `gradlew.bat` - Gradle wrapper scripts
- `app/src/**` - Android application source code

### DEVELOPMENT WORKFLOW

#### For Code Changes
1. **Analyze Impact**: Understand what systems your changes will affect
2. **Check Safety Policy**: Verify changes comply with `reference/VAULT.md`
3. **Validate Continuously**: Run `scripts/validate-system.sh` frequently
4. **Test Android Build**: Ensure `./gradlew assembleDebug` still works
5. **Preserve Organization**: Keep files in their designated directories

#### For Repository Organization  
1. **Use Move Manifest**: Add entries to `configs/move_manifest.json` for file moves
2. **Audit-First Approach**: Log all organizational changes in `logs/activation.jsonl`
3. **Preserve Android**: Never move or delete Android app files
4. **Maintain Structure**: Respect the established directory hierarchy

#### For Workflow/CI Changes
1. **Test with actionlint**: Ensure GitHub Actions syntax is valid
2. **Safety Integration**: Include safety validation in all workflows
3. **Incremental Changes**: Make small, testable modifications
4. **Backwards Compatibility**: Ensure existing workflows continue to function

### AVAILABLE COMMANDS & TOOLS

#### Validation & Testing
```bash
# System validation (includes actionlint, Android build, safety checks)
./scripts/validate-system.sh

# Android build verification
./gradlew assembleDebug

# Safety policy enforcement
./scripts/ops-guard/safety-enforcer.sh

# Repository organization audit
python3 tools/assimilation_audit.py
```

#### AI & Development Tools
```bash
# AI capability demonstration
./scripts/demo-living-ai-interface.sh

# Enhanced development environment
./scripts/demo-enhanced-devenv.sh

# Conflict resolution testing
./scripts/demo-conflict-system.sh
```

#### Chat-Ops Commands (when workflows are active)
- `/assimilate` - Run comprehensive frontend-backend integration audit
- `/validate` - Execute full system validation suite
- `/status` - Generate real-time repository status report

### COMMON ISSUES & SOLUTIONS

#### "actionlint not available"
- Run `scripts/install-actionlint.sh` to install the tool
- Alternative: Use Go: `go install github.com/rhysd/actionlint/cmd/actionlint@latest`

#### Android Build Failures
- Check Gradle wrapper permissions: `chmod +x gradlew`
- Verify ANDROID_HOME environment variable
- Run `./gradlew clean` then `./gradlew assembleDebug`

#### Workflow Syntax Errors
- Use `actionlint .github/workflows/filename.yml` to check specific workflows
- Common issues: undefined variables, deprecated actions, YAML syntax

#### File Organization Issues
- Use `configs/move_manifest.json` for any file moves/deletions
- Run `python3 tools/assimilation_audit.py` for organization status
- Check `logs/activation.jsonl` for audit trails

### EMERGENCY PROCEDURES

#### If Android Build Breaks
1. **Immediate stop** - Halt any file modifications
2. **Check git status** - Identify what changed
3. **Restore critical files**: `git checkout HEAD -- app/build.gradle build.gradle gradlew`
4. **Test build**: `./gradlew assembleDebug`
5. **Report issue** with specific error messages

#### If Safety Policy Violated
1. **Review logs** - Check `logs/activation.jsonl` for audit trail
2. **Restore files** - Use git to restore any improperly modified files
3. **Update manifest** - Add proper entries to `configs/move_manifest.json`
4. **Re-validate** - Run `scripts/ops-guard/safety-enforcer.sh`

### QUALITY STANDARDS

#### Code Quality
- **Follow existing patterns** - Match the coding style and structure of existing files
- **Add proper documentation** - Include comments and documentation for new features
- **Test thoroughly** - Ensure all changes are validated before committing
- **Minimize scope** - Make the smallest possible changes to achieve the goal

#### Documentation Quality  
- **Clear and actionable** - Provide specific steps users can follow
- **Well-organized** - Use proper markdown structure and formatting
- **Comprehensive** - Cover all aspects of the feature or change
- **Accurate** - Ensure all instructions and information are correct

### SUCCESS METRICS

Your success is measured by:
- ✅ **Android app continues to build and function**
- ✅ **All validation scripts pass** (`scripts/validate-system.sh`)
- ✅ **Repository organization is maintained**
- ✅ **Safety policies are respected**
- ✅ **Changes are minimal and surgical**
- ✅ **Documentation is clear and helpful**

### REMEMBER
This repository contains **massive untapped potential** with AI systems, Android development tools, automation scripts, and living-code capabilities. Your role is to help unlock this potential while preserving the stability and organization that has been carefully established.

**Be the intelligent, careful, and helpful AI assistant that this sophisticated repository deserves.**
