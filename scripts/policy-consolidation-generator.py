#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Policy Consolidation Summary Generator
Consolidates scope enforcement and naming policy outputs into repository documentation

This script generates a comprehensive summary of policy enforcement status
and consolidates all policy-related outputs into standardized documentation.
"""

import os
import json
import sys
from pathlib import Path
from datetime import datetime

class PolicyConsolidator:
    """Consolidates policy enforcement and naming policy outputs"""
    
    def __init__(self, repo_root: str):
        self.repo_root = Path(repo_root)
        self.timestamp = datetime.utcnow().isoformat()
        
    def load_latest_policy_report(self) -> dict:
        """Load the most recent policy enforcement report"""
        logs_dir = self.repo_root / "logs"
        if not logs_dir.exists():
            return {}
            
        # Find the most recent policy enforcement report
        policy_reports = list(logs_dir.glob("policy_enforcement_report_*.json"))
        if not policy_reports:
            return {}
            
        latest_report = max(policy_reports, key=lambda p: p.stat().st_mtime)
        
        try:
            with open(latest_report, 'r') as f:
                return json.load(f)
        except Exception:
            return {}
            
    def generate_policy_status_summary(self) -> str:
        """Generate a comprehensive policy status summary"""
        report = self.load_latest_policy_report()
        
        if not report:
            return "No policy enforcement report available."
            
        summary = f"""# Policy Enforcement Status Summary

Generated: {self.timestamp}
Report Source: {report.get('timestamp', 'Unknown')}

## Overall Status: {report.get('overall_status', 'UNKNOWN')}

### Scope Enforcement
- **Status**: {report.get('scope_enforcement', {}).get('status', 'UNKNOWN')}
- **Violations**: {len(report.get('scope_enforcement', {}).get('violations', []))}

### Naming Policy
- **Status**: {report.get('naming_policy', {}).get('status', 'UNKNOWN')}
- **Violations**: {len(report.get('naming_policy', {}).get('violations', []))}

### Code Naming Conventions
- **Status**: {report.get('code_naming', {}).get('status', 'UNKNOWN')}
- **Violations**: {len(report.get('code_naming', {}).get('violations', []))}

### Directory Structure
- **Status**: {report.get('directory_structure', {}).get('status', 'UNKNOWN')}
- **Violations**: {len(report.get('directory_structure', {}).get('violations', []))}

### File Extension Binding
- **Status**: {report.get('file_binding', {}).get('status', 'UNKNOWN')}
- **Violations**: {len(report.get('file_binding', {}).get('violations', []))}

## Policy Documentation References

### Core Policy Documents
- [`docs/SCOPE_ENFORCEMENT_POLICY.md`](docs/SCOPE_ENFORCEMENT_POLICY.md) - Comprehensive scope enforcement rules
- [`docs/NAMING_POLICY.md`](docs/NAMING_POLICY.md) - Consolidated naming conventions
- [`reference/VAULT.md`](reference/VAULT.md) - Safety policy and protected resources
- [`reference/standards/DevUtilityAgenticStandards.md`](reference/standards/DevUtilityAgenticStandards.md) - Agentic development standards

### Configuration Files
- [`configs/activation_rules.json`](configs/activation_rules.json) - Primary policy configuration
- [`configs/move_manifest.json`](configs/move_manifest.json) - File move/delete operations manifest

### Enforcement Tools
- [`scripts/policy-enforcement-validator.py`](scripts/policy-enforcement-validator.py) - Automated policy validation
- [`scripts/validate-system.sh`](scripts/validate-system.sh) - System validation including policy checks
- [`tools/assimilation_audit.py`](tools/assimilation_audit.py) - Assimilation and integration auditing

## Recommendations

### Immediate Actions Required
"""
        
        # Add specific recommendations based on violations
        total_violations = 0
        for category in ['scope_enforcement', 'naming_policy', 'code_naming', 'directory_structure', 'file_binding']:
            violations = report.get(category, {}).get('violations', [])
            total_violations += len(violations)
            
        if total_violations == 0:
            summary += "\n✅ **All policy checks are passing** - No immediate actions required.\n"
        else:
            if report.get('scope_enforcement', {}).get('status') == 'FAIL':
                summary += "\n❌ **Scope Enforcement Issues** - Review protected resources and operational boundaries\n"
            if report.get('naming_policy', {}).get('status') == 'FAIL':
                summary += "\n❌ **Naming Policy Issues** - Update file and directory names to match conventions\n"
            if report.get('code_naming', {}).get('status') == 'FAIL':
                summary += "\n❌ **Code Naming Issues** - Review Kotlin/Java naming conventions in source code\n"
            if report.get('directory_structure', {}).get('status') == 'FAIL':
                summary += "\n❌ **Directory Structure Issues** - Ensure required directories exist and follow naming conventions\n"
            if report.get('file_binding', {}).get('status') == 'FAIL':
                summary += "\n❌ **File Binding Issues** - Move files to appropriate directories based on extension rules\n"
                
        summary += f"""

### Long-term Improvements
- Continue migration of legacy naming patterns to new conventions
- Regular policy compliance auditing using automated tools
- Integration of policy checks into CI/CD pipeline
- Documentation updates to reflect policy changes

## Usage

### Running Policy Validation
```bash
# Full policy validation
python3 scripts/policy-enforcement-validator.py --repo-root . --verbose

# Scope enforcement only
python3 scripts/policy-enforcement-validator.py --repo-root . --scope-only

# Naming policy only  
python3 scripts/policy-enforcement-validator.py --repo-root . --naming-only

# System validation (includes policy checks)
./scripts/validate-system.sh
```

### Policy Configuration
- Edit `configs/activation_rules.json` for scope and organizational rules
- Update `docs/SCOPE_ENFORCEMENT_POLICY.md` for scope enforcement changes
- Update `docs/NAMING_POLICY.md` for naming convention changes
- All policy changes require PR review and approval

---

*This summary consolidates scope enforcement and naming policy outputs as requested.*
*Generated by Policy Consolidation Summary Generator - part of DevUl Army : Living Sriracha AGI*
"""
        
        return summary
        
    def save_consolidated_summary(self) -> None:
        """Save the consolidated policy summary to documentation"""
        summary = self.generate_policy_status_summary()
        
        # Save to docs directory
        summary_path = self.repo_root / "docs" / "POLICY_CONSOLIDATION_SUMMARY.md"
        with open(summary_path, 'w') as f:
            f.write(summary)
            
        print(f"✅ Policy consolidation summary saved to: {summary_path}")
        
        # Also save to logs for historical tracking
        logs_dir = self.repo_root / "logs"
        logs_dir.mkdir(exist_ok=True)
        
        timestamp_str = datetime.utcnow().strftime('%Y%m%d_%H%M%S')
        log_path = logs_dir / f"policy_consolidation_{timestamp_str}.md"
        with open(log_path, 'w') as f:
            f.write(summary)
            
        print(f"✅ Historical copy saved to: {log_path}")
        
    def run_consolidation(self) -> None:
        """Run complete policy consolidation"""
        print("🔄 Starting policy consolidation...")
        print(f"Repository: {self.repo_root}")
        print(f"Timestamp: {self.timestamp}")
        print()
        
        self.save_consolidated_summary()
        
        print()
        print("✅ Policy consolidation complete!")
        print()
        print("📋 Generated Documents:")
        print("  - docs/POLICY_CONSOLIDATION_SUMMARY.md (current status)")
        print("  - logs/policy_consolidation_*.md (historical record)")
        print()
        print("🔗 Related Documents:")
        print("  - docs/SCOPE_ENFORCEMENT_POLICY.md")
        print("  - docs/NAMING_POLICY.md")
        print("  - reference/VAULT.md")
        print("  - configs/activation_rules.json")

def main():
    """Main entry point"""
    import argparse
    
    parser = argparse.ArgumentParser(description="Policy Consolidation Summary Generator")
    parser.add_argument("--repo-root", default=".", help="Repository root directory")
    
    args = parser.parse_args()
    
    # Resolve repository root
    repo_root = os.path.abspath(args.repo_root)
    if not os.path.exists(repo_root):
        print(f"Error: Repository root does not exist: {repo_root}")
        sys.exit(1)
        
    # Create consolidator and run
    consolidator = PolicyConsolidator(repo_root)
    consolidator.run_consolidation()

if __name__ == "__main__":
    main()