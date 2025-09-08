# VAULT.md - Copilot Safety Policy Reference

## Repository Safety Standards

This document defines the comprehensive safety policy for GitHub Copilot operations within this repository to prevent destructive or unauthorized actions.

## Safety Policy Overview

The Copilot Safety Policy ensures all automated actions are:
- **Audit-First**: All operations are logged and reviewable
- **Non-Destructive**: No file deletions or destructive modifications without explicit approval
- **Android 10 Respect**: Preserves Android app integrity and Gradle build system

## Core Safety Guarantees

### Non-Destructive Operations
- ✅ **No writes to main branch** - All changes arrive as draft PRs on `copilot/fix-*` branches
- ✅ **No deletes/moves without manifest** - File operations require explicit `configs/move_manifest.json` entries
- ✅ **Android app protection** - No modification of Android app, Gradle, or build system
- ✅ **Conflict resolution preservation** - Scripts like `validate-system.sh` and `manual-conflict-resolver.sh` are protected
- ✅ **No branch deletion** - Branch guards run in audit-only mode unless explicitly enabled

### Operational Discipline
- ✅ **Precision triggers** - Operations limited to curated paths: `.github/workflows/**`, `scripts/ops-guard/**`, `configs/**`, `ai/**`, `app/src/**`
- ✅ **Ephemeral state** - CI state rehydration is local and temporary, no repository mutation
- ✅ **Forensic logging** - Append-only logs to `logs/activation.jsonl` artifacts
- ✅ **Drift detection** - SHA256 comparison of ops-layer files, fails CI but never edits files

### Enablement Controls
- ✅ **Workflows ship disabled** - All destructive workflows are disabled by default
- ✅ **Manual activation required** - Destructive actions require explicit config toggle in `configs/activation_rules.json`
- ✅ **PR review gates** - Manual review required for any policy changes

## Protected Resources

### Protected Files
- `scripts/validate-system.sh` - System validation script
- `scripts/manual-conflict-resolver.sh` - Manual conflict resolution
- `app/build.gradle` - Android app build configuration
- `build.gradle` - Root Gradle configuration
- `gradlew` / `gradlew.bat` - Gradle wrapper scripts
- `app/src/**/*.kt` - Android Kotlin source files
- `app/src/**/*.java` - Android Java source files

### Protected Directories
- `app/src/` - Android application source code
- `.gradle/` - Gradle cache and configuration
- `logs/` - Forensic audit logs (append-only)

## Configuration Files

### `configs/activation_rules.json`
Contains the comprehensive safety policy configuration including:
- `copilot_safety_policy.enabled` - Master safety toggle
- `copilot_safety_policy.non_destructive_guarantees` - File protection rules
- `copilot_safety_policy.operational_discipline` - Operational constraints
- `copilot_safety_policy.protected_files` - List of protected resources

### `configs/move_manifest.json`
Explicit manifest for any file move or deletion operations:
- `approved_moves` - Pre-approved file relocations
- `approved_deletions` - Pre-approved file removals
- `protected_patterns` - Glob patterns for protected resources
- `audit_log` - History of all move/delete operations

## Safety Validation

### Automated Checks
- `scripts/ops-guard/safety-enforcer.sh` - Validates safety policy compliance
- Integrated into CI/CD workflows for continuous validation
- Blocks any operations that violate safety guarantees

### Manual Overrides
- Destructive actions can be enabled via `configs/activation_rules.json`
- Requires explicit PR review and approval
- All overrides are logged in forensic audit trails

## Emergency Procedures

### If Destructive Action Detected
1. **Immediate halt** - Stop any running workflows
2. **Forensic analysis** - Review `logs/activation.jsonl` for audit trail
3. **Restore from backup** - Use git history to restore any damaged files
4. **Policy review** - Investigate how the destructive action bypassed safety controls

### Policy Updates
1. **PR-based changes only** - All policy updates must go through pull request process
2. **Required reviews** - Multiple maintainer approvals required
3. **Testing required** - Validate policy changes in isolated environment
4. **Audit logging** - Document all policy modifications

## Living Code Integration

The safety policy integrates with the existing Living Code system described in `README.md`:
- Preserves conflict resolution mechanisms
- Maintains Android 10 build compatibility
- Respects existing agentic standards (@GDA @UFUIC-O @PIPI @LDU @EG @GATT @SWT)
- Supports mobile development workflows

## Version History

- **v1.0.0** - Initial safety policy implementation (2024-12-19)
  - Core non-destructive guarantees established
  - Protected file system implemented
  - Forensic logging framework created
  - Emergency procedures defined

---

**CRITICAL**: This safety policy is mandatory for all automated operations. Bypassing or disabling safety controls without proper authorization is prohibited and will result in immediate investigation and remediation.