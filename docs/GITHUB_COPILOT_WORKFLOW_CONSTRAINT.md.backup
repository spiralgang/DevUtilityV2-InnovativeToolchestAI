# GitHub Copilot Single-Instance Constraint Implementation

## Overview

This implementation enforces a **single-instance constraint for GitHub Copilot workflows**, ensuring that only one Copilot operation can execute at any given time across the entire repository. This addresses resource conflicts, prevents race conditions, and ensures stable Copilot operations.

## Problem Statement

The user specifically requested: *"make the goddamn copilot limited to one at a time maximum"* referring to **GitHub Copilot workflow operations**, not application-level AI operations.

Multiple concurrent Copilot workflows could cause:
- Race conditions in branch operations
- Conflicting PR creation/merging
- Resource contention in GitHub Actions
- Inconsistent repository state

## Solution Architecture

### 🔒 Concurrency Groups

All Copilot-related workflows now use the same concurrency group:

```yaml
concurrency:
  group: copilot-operations
  cancel-in-progress: false  # Queue operations instead of canceling
```

### 🎯 Affected Workflows

1. **`copilot-idempotent-pr.yml`** - PR creation operations
2. **`auto-merge-prune.yml`** - Branch merging and pruning
3. **`copilot-constraint-manager.yml`** - Central operation coordinator

### 🚦 Operation Flow

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ Copilot         │    │ Concurrency      │    │ Execute         │
│ Operation       │───▶│ Group Queue      │───▶│ Single          │
│ Triggered       │    │ "copilot-ops"    │    │ Operation       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ Additional      │    │ Operations       │    │ Next Operation  │
│ Operations      │    │ Wait in Queue    │    │ Starts          │
│ Queue Up        │    │ Until Slot Free  │    │ Automatically   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Implementation Details

### Concurrency Configuration

```yaml
# Applied to all Copilot workflows
concurrency:
  group: copilot-operations
  cancel-in-progress: false  # CRITICAL: Queue instead of cancel
```

**Key Settings:**
- `group: copilot-operations` - All Copilot workflows share this group
- `cancel-in-progress: false` - Operations queue rather than being canceled
- This ensures **sequential execution** of all Copilot operations

### Constraint Manager Workflow

The new `copilot-constraint-manager.yml` provides:

1. **Manual Operation Triggering**
   ```yaml
   workflow_dispatch:
     inputs:
       operation_type:
         type: choice
         options:
           - 'pr-creation'
           - 'branch-pruning'
           - 'merge-operations'
           - 'conflict-resolution'
   ```

2. **Timeout Protection**
   ```yaml
   timeout-minutes: 45  # Prevents infinite waits
   max_wait_time: '30'  # Configurable wait time
   ```

3. **Queue Management**
   - Checks for running Copilot workflows
   - Waits for operation slot availability
   - Executes operation when slot is free
   - Releases slot for next operation

## Benefits

### 🔒 **Guaranteed Single Instance**
- Only one Copilot workflow can run at any time system-wide
- No concurrent operations competing for GitHub resources

### 🧵 **Thread Safety at GitHub Level**
- Concurrency groups prevent race conditions
- GitHub Actions handles queuing automatically

### 📈 **Better Resource Management**
- Eliminates GitHub Actions resource conflicts
- Prevents API rate limiting issues
- Reduces runner usage costs

### 👤 **Improved Workflow Reliability**
- Sequential operations prevent state conflicts
- Predictable execution order
- Better error isolation

### 🛡️ **System Stability**
- No conflicting branch operations
- Consistent repository state
- Reduced workflow failures

## Usage Examples

### Automatic Triggering

```bash
# When pushing to copilot/fix-* branches
git push origin copilot/fix-feature-xyz
# → Automatically queues in copilot-operations group
```

### Manual Operation Triggering

```bash
# Via GitHub Actions UI or CLI
gh workflow run copilot-constraint-manager.yml \
  -f operation_type=pr-creation \
  -f max_wait_time=30
```

### Operation Types

1. **`pr-creation`** - Create/update Copilot PRs
2. **`branch-pruning`** - Clean up old Copilot branches  
3. **`merge-operations`** - Merge Copilot branches
4. **`conflict-resolution`** - Resolve merge conflicts

## Monitoring

### Workflow Status

```bash
# Check running Copilot operations
gh run list --status in_progress --json workflowName,databaseId

# Check queued operations
gh run list --status queued --json workflowName,databaseId
```

### Queue Length

The constraint manager reports:
- Current queue position
- Estimated wait time
- Operation type being executed
- Slot release notifications

## Configuration

### Adjusting Wait Times

Modify `max_wait_time` in workflow dispatch inputs:

```yaml
max_wait_time:
  description: 'Maximum wait time in minutes'
  default: '30'  # Adjust as needed
```

### Adding New Copilot Workflows

To include new workflows in the constraint:

```yaml
name: New Copilot Workflow

concurrency:
  group: copilot-operations  # REQUIRED
  cancel-in-progress: false  # REQUIRED
```

## Testing

### Validate Constraint

1. Trigger multiple Copilot operations simultaneously
2. Verify only one executes at a time
3. Confirm queuing behavior
4. Check timeout handling

### Performance Testing

```bash
# Test concurrent triggers
for i in {1..5}; do
  gh workflow run copilot-constraint-manager.yml \
    -f operation_type=pr-creation &
done
```

## Troubleshooting

### Common Issues

1. **Workflow Stuck in Queue**
   - Check for hung Copilot workflows
   - Increase `max_wait_time`
   - Cancel stuck workflows manually

2. **Timeout Errors**
   - Reduce operation complexity
   - Increase timeout values
   - Check GitHub Actions status

3. **Concurrency Not Working**
   - Verify `group: copilot-operations` in all workflows
   - Ensure `cancel-in-progress: false`
   - Check workflow syntax

### Debug Commands

```bash
# List all workflow runs
gh run list --limit 20

# Get specific run details
gh run view <run-id>

# Cancel stuck workflow
gh run cancel <run-id>
```

## Migration Notes

### From Application-Level to Workflow-Level

This implementation specifically targets **GitHub workflow operations**, not application AI operations. The constraint applies to:

- ✅ GitHub Copilot workflows
- ✅ Branch operations via Actions
- ✅ PR creation/merging workflows
- ❌ Application AI modules (not required)

### Backward Compatibility

- Existing workflows continue to function
- No breaking changes to application code
- Additional safety through queuing

## Future Enhancements

1. **Priority Queuing** - Critical operations get priority
2. **Load Balancing** - Distribute operations across runners
3. **Metrics Collection** - Track operation performance
4. **Auto-scaling** - Adjust based on queue length

---

## Summary

This implementation provides **true single-instance constraint for GitHub Copilot operations** through GitHub Actions concurrency groups, ensuring only one Copilot workflow executes at any time while maintaining reliability and preventing resource conflicts.