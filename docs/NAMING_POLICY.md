# Naming Policy and Conventions

## Overview

This document consolidates all naming policies and conventions for the DevUl Army : Living Sriracha AGI repository. It serves as the canonical reference for consistent naming across all file types, code elements, and organizational structures.

## Core Naming Principles

### 1. DevUtility Agentic Standards Alignment (@GDA)
All naming must align with established agentic standards:
- **@GDA** (Guided-Development-Approach): Hierarchical, logical naming
- **@UFUIC-O** (User-Frontend-UI-Interface-Customizations Options): User-discoverable names
- **@PIPI** (Preview-Implement-Push-Implement): Version-aware naming
- **@LDU** (Linear-Development-Updates): Traceable naming evolution
- **@EG** (Easy-to-Grasp): Intuitive, clear names
- **@GATT** (Guided-AI-Tutorial-Tips): Self-documenting names
- **@SWT** (Structure Walk-Through): Explicitly documented naming logic

### 2. Consistency Across Languages
- Maintain consistent naming patterns across Kotlin, Java, Python, Shell, JavaScript
- Follow language-specific conventions while maintaining cross-language recognizability
- Use standard industry conventions as baseline

### 3. Hierarchical Organization
- Names should reflect structural relationships
- Parent-child relationships should be evident in naming
- Module boundaries should be clear from names

## Language-Specific Naming Conventions

### Kotlin/Java (@UFUIC-O)

#### Classes
- **Format**: PascalCase
- **Pattern**: `[Purpose][Type]`
- **Examples**: 
  - `CodeReviewService`
  - `ConflictResolver`
  - `AICollabManager`
  - `DevUtilityMainActivity`

#### Functions/Methods
- **Format**: camelCase
- **Pattern**: `[action][Object]`
- **Examples**:
  - `analyzeNamingConventions()`
  - `commitChanges()`
  - `createBranch()`
  - `updateSelectedAIModels()`

#### Variables
- **Format**: camelCase
- **Pattern**: `[descriptor][Type]`
- **Examples**:
  - `functionPattern`
  - `hardcodedStrings`
  - `targetLanguage`
  - `currentCode`

#### Constants
- **Format**: SCREAMING_SNAKE_CASE
- **Pattern**: `[MODULE]_[PURPOSE]`
- **Examples**:
  - `DEFAULT_MODEL_PATH`
  - `MAX_RETRY_ATTEMPTS`
  - `CONFLICT_RESOLUTION_TIMEOUT`

#### Packages
- **Format**: lowercase.with.dots
- **Pattern**: `com.spiralgang.srirachaarmy.[module].[submodule]`
- **Examples**:
  - `com.spiralgang.srirachaarmy.devutility.ai`
  - `com.spiralgang.srirachaarmy.devutility.conflict`
  - `com.spiralgang.srirachaarmy.devutility.storage`

### Python (@LDU)

#### Classes
- **Format**: PascalCase
- **Pattern**: `[Purpose][Type]`
- **Examples**:
  - `ConflictResolver`
  - `AssimilationAudit`
  - `OrganizationManager`

#### Functions
- **Format**: snake_case
- **Pattern**: `[action]_[object]`
- **Examples**:
  - `analyze_naming_conventions()`
  - `validate_scope_enforcement()`
  - `organize_repository()`

#### Variables
- **Format**: snake_case
- **Pattern**: `[descriptor]_[type]`
- **Examples**:
  - `file_moves`
  - `organization_rules`
  - `validation_results`

#### Constants
- **Format**: SCREAMING_SNAKE_CASE
- **Pattern**: `[MODULE]_[PURPOSE]`
- **Examples**:
  - `DEFAULT_LOG_FILE`
  - `REPOSITORY_ROOT`
  - `VALIDATION_TIMEOUT`

#### Modules
- **Format**: snake_case
- **Pattern**: `[purpose]_[type]`
- **Examples**:
  - `conflict_resolver`
  - `assimilation_audit`
  - `organization_manager`

### Shell Scripts (@EG)

#### Files
- **Format**: kebab-case.sh
- **Pattern**: `[action]-[object].sh`
- **Examples**:
  - `validate-system.sh`
  - `manual-conflict-resolver.sh`
  - `organize-root-cleanup.sh`

#### Functions
- **Format**: snake_case
- **Pattern**: `[action]_[object]`
- **Examples**:
  - `setup_auth()`
  - `move_file()`
  - `validate_android_build()`

#### Variables
- **Format**: SCREAMING_SNAKE_CASE
- **Pattern**: `[PURPOSE]_[TYPE]`
- **Examples**:
  - `LOG_FILE`
  - `BLUE`
  - `MOVE_RULES`

### Documentation (@GATT)

#### Files
- **Format**: SCREAMING_SNAKE_CASE.md
- **Pattern**: `[PURPOSE]_[TYPE].md`
- **Examples**:
  - `SCOPE_ENFORCEMENT_POLICY.md`
  - `NAMING_POLICY.md`
  - `ISSUE_42_IMPLEMENTATION.md`

#### Headers
- **Format**: Title Case
- **Pattern**: Hierarchical structure with clear purpose
- **Examples**:
  - `# Core Naming Principles`
  - `## Language-Specific Naming Conventions`
  - `### Kotlin/Java (@UFUIC-O)`

### Configuration Files (@PIPI)

#### JSON Files
- **Format**: snake_case.json
- **Pattern**: `[purpose]_[type].json`
- **Examples**:
  - `activation_rules.json`
  - `move_manifest.json`
  - `frontend_to_backend_map.json`

#### Keys
- **Format**: snake_case
- **Pattern**: `[module]_[purpose]`
- **Examples**:
  - `copilot_safety_policy`
  - `non_destructive_guarantees`
  - `operational_discipline`

## Directory Naming Conventions (@SWT)

### Primary Directories
```
app/                    # Android application (fixed convention)
ai/                     # AI system components
scripts/                # Automation and system scripts
docs/                   # Documentation and guides
configs/                # Configuration files by category
tools/                  # Development utilities and helpers
reference/              # Canonical standards and architecture
logs/                   # Forensic audit logs
archive/                # Legacy and deprecated content
datasets/               # Training and reference data
```

### Subdirectory Patterns
- **Format**: snake_case
- **Purpose-based organization**: Group by function, not file type
- **Examples**:
  - `docs/specifications/`
  - `docs/architecture/`
  - `configs/android/`
  - `configs/security/`

## File Naming Patterns

### Script Files
- **Shell**: `[action]-[object].sh`
- **Python**: `[purpose]_[type].py`
- **Examples**:
  - `validate-system.sh`
  - `conflict_resolver.py`
  - `assimilation_audit.py`

### Documentation Files
- **User guides**: `[TOPIC]_GUIDE.md`
- **Implementation docs**: `[FEATURE]_IMPLEMENTATION.md`
- **Reference docs**: `[TOPIC]_REFERENCE.md`
- **Examples**:
  - `INSTALLATION_GUIDE.md`
  - `ISSUE_42_IMPLEMENTATION.md`
  - `API_REFERENCE.md`

### Configuration Files
- **JSON configs**: `[purpose]_[scope].json`
- **YAML workflows**: `[action]-[trigger].yml`
- **Examples**:
  - `activation_rules.json`
  - `pr-assimilation-audit.yml`

## Naming Validation Rules

### Automated Checks
1. **Code Review Service**: Validates Kotlin/Java naming conventions
2. **Organization Scripts**: Enforces directory and file naming
3. **Validation Pipeline**: Checks naming compliance during CI/CD

### Enforcement Patterns
```regex
# Kotlin/Java Classes
^[A-Z][a-zA-Z0-9]*$

# Kotlin/Java Functions
^[a-z][a-zA-Z0-9]*$

# Python Functions
^[a-z][a-z0-9_]*$

# Shell Scripts
^[a-z][a-z0-9-]*\.sh$

# Documentation
^[A-Z][A-Z0-9_]*\.md$
```

### Legacy Naming Migration

#### Current Inconsistencies
- Mixed naming conventions in archive/ directory
- Some shell scripts use underscore instead of hyphen
- Documentation files have inconsistent casing

#### Migration Strategy
1. **Phase 1**: Document current state and exceptions
2. **Phase 2**: Migrate new files to standard conventions
3. **Phase 3**: Gradually migrate legacy files during updates
4. **Phase 4**: Enforce strict compliance for all new additions

## Integration with Existing Systems

### Code Review Service Integration
The `CodeReviewService.kt` already implements basic naming convention validation:
- Function name validation (camelCase enforcement)
- Class name validation (PascalCase enforcement)
- Variable naming suggestions

### Organization Script Integration
The `organize-repository.py` already implements file organization based on naming patterns:
- Extension-based organization
- Pattern-based directory assignment
- Agentic standards compliance

### Validation Pipeline Integration
All naming policy validation integrates with:
- `scripts/validate-system.sh`
- `tools/assimilation_audit.py`
- GitHub Actions workflows

## Exceptions and Special Cases

### Android Conventions
- Follow Android naming conventions for app/ directory
- Maintain Gradle naming requirements
- Preserve AndroidManifest.xml structure

### Third-Party Integration
- Maintain compatibility with external tool naming requirements
- Document deviations with clear justification
- Isolate non-compliant naming to specific modules

### Legacy Support
- Archive directory maintains historical naming
- Gradual migration for active legacy files
- Clear documentation of naming evolution

## Version History

- **v1.0.0** - Initial naming policy consolidation (2024-12-19)
  - Consolidated naming rules from DevUtility standards and CodeReviewService
  - Defined language-specific conventions
  - Established validation patterns
  - Integrated with existing enforcement systems

---

**Note**: This naming policy evolves with the repository while maintaining backward compatibility. All changes must go through the standard PR review process and be reflected in this canonical document.