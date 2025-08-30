# Pull Request Conflict Resolution Guide

This document provides comprehensive guidance on resolving merge conflicts in the DevUtilityV2 project.

## Overview

The DevUtilityV2 project includes automated tools and workflows to detect and resolve merge conflicts in pull requests. This system helps maintain a clean merge history and reduces manual intervention required for common conflict scenarios.

## Automated Conflict Resolution System

### Components

1. **conflict-resolver.py** - Python script for automated conflict detection and resolution
2. **manual-conflict-resolver.sh** - Interactive bash script for complex conflicts
3. **GitHub Actions workflow** - Automated CI/CD integration for PR conflict handling

### Supported Conflict Types

#### ✅ Automatically Resolvable

| File Type | Resolution Strategy | Description |
|-----------|--------------------|-----------
| `.gitignore` | Merge unique entries | Combines all unique ignore patterns and comments |
| `build.gradle` | Intelligent merge | Merges dependencies, plugins, and configurations |
| `settings.gradle` | Configuration merge | Combines project settings and module configurations |
| `README.md` | Content combination | Merges documentation sections intelligently |

#### ⚠️ Manual Review Required

| File Type | Reason | Recommended Action |
|-----------|--------|--------------------|
| `*.kt`, `*.java` | Code logic conflicts | Use IDE merge tools or manual review |
| `*.py`, `*.js` | Business logic changes | Careful manual resolution required |
| Database migrations | Schema conflicts | Database expert review needed |
| Configuration files | Environment-specific | Context-aware resolution |

## Quick Start

### Automatic Resolution (Recommended)

The GitHub Actions workflow automatically triggers on PR creation/updates:

1. **Detection**: Automatically detects merge conflicts
2. **Resolution**: Attempts to resolve common conflicts
3. **Validation**: Tests that resolved branch merges cleanly
4. **Notification**: Comments on PR with results

### Manual Resolution

For complex conflicts or when automatic resolution fails:

```bash
# 1. Checkout your feature branch
git checkout your-feature-branch

# 2. Attempt merge with target branch
git merge origin/main

# 3. If conflicts occur, use the interactive resolver
./scripts/manual-conflict-resolver.sh -s your-feature-branch -t main -i

# 4. Follow the interactive prompts
# 5. Validate and commit
```

## Detailed Usage Guide

### Using the Automated Resolver

```bash
# Detect conflicts between branches
python3 scripts/conflict-resolver.py --source feature-branch --target main

# Generate detailed conflict report
python3 scripts/conflict-resolver.py --source feature-branch --target main --report conflicts.md

# Automatically resolve common conflicts
python3 scripts/conflict-resolver.py --source feature-branch --target main --resolve
```

### Using the Interactive Resolver

```bash
# Interactive mode with guided resolution
./scripts/manual-conflict-resolver.sh -i -s your-branch -t main

# List all conflicted files
./scripts/manual-conflict-resolver.sh -l

# Resolve specific file
./scripts/manual-conflict-resolver.sh -r build.gradle -v

# Validate resolution
./scripts/manual-conflict-resolver.sh -v
```

## Resolution Strategies by File Type

### Build Files (build.gradle, settings.gradle)

**Strategy**: Intelligent configuration merging

```gradle
// BEFORE (Conflict)
<<<<<<< HEAD
dependencies {
    implementation 'androidx.core:core-ktx:1.8.0'
}
=======
dependencies {
    implementation 'androidx.core:core-ktx:1.9.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.0'
}
>>>>>>> feature-branch

// AFTER (Resolved)
dependencies {
    implementation 'androidx.core:core-ktx:1.9.0'  // Latest version
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.0'  // Added dependency
}
```

### .gitignore Files

**Strategy**: Combine unique entries with intelligent organization

```gitignore
# BEFORE (Conflict)
<<<<<<< HEAD
/build
=======
# Android Studio / IntelliJ IDEA
*.iml
.idea/
.gradle/
build/
local.properties
>>>>>>> feature-branch

# AFTER (Resolved)
# DevUtilityV2 - Merged .gitignore
# Auto-resolved merge conflicts

# Android Studio / IntelliJ IDEA
*.iml
.idea/

# Gradle
.gradle/
build/

# Local Configuration
local.properties
```

### Documentation Files (README.md)

**Strategy**: Preserve comprehensive content

- Merge feature descriptions
- Combine installation instructions
- Preserve all usage examples
- Maintain consistent formatting

### Source Code Files

**Strategy**: Manual review required

These conflicts require human judgment:
- Method implementations
- Business logic changes
- API modifications
- Class structure changes

## Common Conflict Scenarios

### Scenario 1: Multiple Feature Branches

**Problem**: Two feature branches modify the same configuration files

**Solution**:
```bash
# Resolve feature-1 first
git checkout feature-1
git merge main
./scripts/manual-conflict-resolver.sh -i

# Then resolve feature-2
git checkout feature-2
git merge main
./scripts/manual-conflict-resolver.sh -i
```

### Scenario 2: Long-Running Feature Branch

**Problem**: Feature branch diverged significantly from main

**Solution**:
```bash
# Rebase onto latest main
git checkout feature-branch
git rebase origin/main

# Resolve conflicts interactively
./scripts/manual-conflict-resolver.sh -i
```

### Scenario 3: Build Configuration Conflicts

**Problem**: Multiple PRs modify Gradle build files

**Solution**: Automated resolution handles this well:
- Dependencies are merged intelligently
- Plugin configurations are combined
- Version conflicts prefer higher versions

## Best Practices

### For Developers

1. **Keep branches updated**: Regularly merge main into feature branches
2. **Small, focused PRs**: Easier to resolve conflicts automatically
3. **Coordinate changes**: Communicate about overlapping modifications
4. **Test after resolution**: Always validate functionality after conflict resolution

### For Maintainers

1. **Review automated resolutions**: Check that automatic merges are correct
2. **Merge frequently**: Reduce conflict accumulation
3. **Use merge queues**: For high-traffic repositories
4. **Document patterns**: Update resolution strategies based on common conflicts

## GitHub Actions Integration

The workflow automatically:

1. **Triggers** on PR events (opened, synchronized, reopened)
2. **Detects** conflicts using merge-tree analysis
3. **Resolves** common conflicts automatically
4. **Validates** that resolution was successful
5. **Comments** on PR with results and next steps

### Workflow Configuration

Enable automatic conflict resolution by ensuring:

```yaml
# .github/workflows/conflict-resolution.yml is present
# Repository has necessary permissions
# Python 3.9+ is available
# Git is properly configured
```

## Troubleshooting

### Common Issues

#### "No conflicts detected but merge fails"

**Cause**: Semantic conflicts or build issues
**Solution**: 
```bash
# Check build after merge
./gradlew build --dry-run

# Review semantic changes
git log --oneline --graph origin/main..HEAD
```

#### "Automatic resolution fails"

**Cause**: Complex code conflicts
**Solution**:
```bash
# Use manual resolver
./scripts/manual-conflict-resolver.sh -i

# Or use IDE merge tools
git mergetool
```

#### "Scripts not executable"

**Solution**:
```bash
chmod +x scripts/*.sh
chmod +x scripts/*.py
```

### Getting Help

1. **Check logs**: Review GitHub Actions workflow logs
2. **Use interactive mode**: Run manual resolver with `-i` flag
3. **Generate reports**: Use `--report` flag to analyze conflicts
4. **Ask for review**: Tag maintainers for complex conflicts

## Development

### Adding New Resolution Strategies

To add support for new file types:

1. Update `conflict-resolver.py`:
```python
def resolve_new_file_type(self, file_path: str) -> bool:
    """Resolve conflicts in new file type"""
    # Implementation here
    pass
```

2. Add to resolution strategies:
```python
resolution_strategies = {
    'new_file.ext': self.resolve_new_file_type,
    # existing strategies...
}
```

3. Update documentation and tests

### Testing Resolution Strategies

```bash
# Create test conflicts
git checkout -b test-conflicts
# Make conflicting changes
git commit -am "Test changes"

# Test resolution
python3 scripts/conflict-resolver.py --source test-conflicts --target main --resolve
```

## References

- [Git Merge Documentation](https://git-scm.com/docs/git-merge)
- [GitHub Actions Workflow Syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [Automated Merge Strategies](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/configuring-pull-request-merges)

---

**Last Updated**: Generated automatically by conflict resolution system
**Version**: 1.0.0
**Maintainers**: DevUtilityV2 Development Team