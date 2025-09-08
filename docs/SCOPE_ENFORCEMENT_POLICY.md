# Scope Enforcement Policy

## Overview

This document consolidates all scope enforcement policies for the DevUl Army : Living Sriracha AGI repository. It serves as the canonical reference for operational boundaries, protected resources, and automated enforcement mechanisms.

## Core Scope Enforcement Principles

### 1. Non-Destructive Operations (@GDA)
- ✅ **No writes to main branch** - All changes arrive as draft PRs on `copilot/fix-*` branches
- ✅ **No deletes/moves without manifest** - File operations require explicit `configs/move_manifest.json` entries
- ✅ **Android app protection** - No modification of Android app, Gradle, or build system
- ✅ **Conflict resolution preservation** - Core system scripts are protected
- ✅ **No branch deletion** - Branch guards run in audit-only mode unless explicitly enabled

### 2. Operational Discipline (@PIPI)
- ✅ **Precision triggers** - Operations limited to curated paths only
- ✅ **Ephemeral state** - CI state rehydration is local and temporary
- ✅ **Forensic logging** - Append-only logs to `logs/activation.jsonl`
- ✅ **Drift detection** - SHA256 comparison with CI failure but no file edits

### 3. Enablement Controls (@LDU)
- ✅ **Workflows ship disabled** - All destructive workflows are disabled by default
- ✅ **Manual activation required** - Destructive actions require explicit config toggle
- ✅ **PR review gates** - Manual review required for any policy changes

## Protected Resource Scopes

### Protected Files (@SWT)
```
scripts/validate-system.sh           # System validation script
scripts/manual-conflict-resolver.sh  # Manual conflict resolution
app/build.gradle                     # Android app build configuration
build.gradle                         # Root Gradle configuration
gradlew                             # Gradle wrapper scripts
gradlew.bat                         # Gradle wrapper scripts
app/src/**/*.kt                     # Android Kotlin source files
app/src/**/*.java                   # Android Java source files
```

### Protected Directories (@UFUIC-O)
```
app/src/                            # Android application source code
.gradle/                            # Gradle cache and configuration
logs/                               # Forensic audit logs (append-only)
```

### Curated Operation Paths (@EG)
```
.github/workflows/**                # GitHub Actions workflows
scripts/ops-guard/**               # Operations guard scripts
configs/**                         # Configuration files
ai/**                              # AI system components
app/src/**                         # Android source (read-only for analysis)
tools/**                           # Development utilities
docs/**                            # Documentation
```

## Scope Validation Rules

### File Extension Binding (@GATT)
```json
{
  "from_ext": ".sh",   "bind_hint": "scripts/"
  "from_ext": ".py",   "bind_hint": "ai/"
  "from_ext": ".md",   "bind_hint": "docs/"
  "from_ext": ".yml",  "bind_hint": ".github/workflows/"
  "from_ext": ".yaml", "bind_hint": ".github/workflows/"
  "from_ext": ".json", "bind_hint": "configs/"
}
```

### Organization Limits
- **Root file limit**: 50 files maximum
- **Allowed root extensions**: .md, .gradle, .yml, .json, .sh
- **Auto-organize threshold**: 10 files trigger automatic organization
- **Frontend globs**: ./, .github/, scripts/, configs/, docs/, .ops/
- **Backend globs**: app/src/, ai/, logs/, scratch/

## Enforcement Mechanisms

### Automated Validation
1. **System Validation**: `scripts/validate-system.sh`
2. **Safety Enforcer**: `scripts/ops-guard/safety-enforcer.sh`
3. **Assimilation Audit**: `tools/assimilation_audit.py`
4. **Drift Guard**: `scripts/drift-guard.sh`

### Manual Overrides
- Destructive actions enabled via `configs/activation_rules.json`
- Requires explicit PR review and approval
- All overrides logged in forensic audit trails

### Emergency Procedures
1. **Immediate halt** - Stop any running workflows
2. **Forensic analysis** - Review `logs/activation.jsonl` for audit trail
3. **Restore from backup** - Use git history to restore damaged files
4. **Policy review** - Investigate how action bypassed safety controls

## Integration with DevUtility Standards

This scope enforcement policy integrates with all DevUtility agentic standards:
- **@GDA** (Guided-Development-Approach): Structured operation paths
- **@UFUIC-O** (User-Frontend-UI-Interface-Customizations Options): Protected user-facing resources
- **@PIPI** (Preview-Implement-Push-Implement): Controlled change deployment
- **@LDU** (Linear-Development-Updates): Traceable incremental operations
- **@EG** (Easy-to-Grasp): Clear operational boundaries
- **@GATT** (Guided-AI-Tutorial-Tips): Documented enforcement procedures
- **@SWT** (Structure Walk-Through): Explicit scope documentation

## Configuration References

- **Primary Config**: `configs/activation_rules.json`
- **Move Manifest**: `configs/move_manifest.json`
- **Safety Policy**: `reference/VAULT.md`
- **Standards**: `reference/standards/DevUtilityAgenticStandards.md`

## Version History

- **v1.0.0** - Initial scope enforcement policy consolidation (2024-12-19)
  - Consolidated scope rules from activation_rules.json and VAULT.md
  - Defined protected resource scopes
  - Established enforcement mechanisms
  - Integrated with DevUtility agentic standards

---

**CRITICAL**: This scope enforcement policy is mandatory for all automated operations. Bypassing scope controls without proper authorization is prohibited and will result in immediate investigation and remediation.