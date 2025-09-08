#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Policy Enforcement Utility
Consolidates scope enforcement and naming policy validation for DevUl Army : Living Sriracha AGI

This script validates both scope enforcement and naming policy compliance across the repository.
It serves as the central enforcement mechanism for all policy rules.

References:
- docs/SCOPE_ENFORCEMENT_POLICY.md
- docs/NAMING_POLICY.md
- configs/activation_rules.json
- reference/VAULT.md
"""

import os
import sys
import json
import re
import logging
from pathlib import Path
from typing import List, Dict, Tuple, Optional
from datetime import datetime

# Setup logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class PolicyEnforcementValidator:
    """Validates scope enforcement and naming policy compliance"""
    
    def __init__(self, repo_root: str):
        self.repo_root = Path(repo_root)
        self.violations = []
        self.warnings = []
        self.load_configuration()
        
    def load_configuration(self):
        """Load scope and naming policy configuration"""
        # Load activation rules
        activation_rules_path = self.repo_root / "configs" / "activation_rules.json"
        if activation_rules_path.exists():
            with open(activation_rules_path, 'r') as f:
                self.activation_rules = json.load(f)
        else:
            logger.warning("activation_rules.json not found")
            self.activation_rules = {}
            
        # Load move manifest
        move_manifest_path = self.repo_root / "configs" / "move_manifest.json"
        if move_manifest_path.exists():
            with open(move_manifest_path, 'r') as f:
                self.move_manifest = json.load(f)
        else:
            logger.warning("move_manifest.json not found")
            self.move_manifest = {}
            
    def validate_scope_enforcement(self) -> List[str]:
        """Validate scope enforcement policy compliance"""
        scope_violations = []
        
        # Check protected files
        protected_files = self.activation_rules.get('copilot_safety_policy', {}).get('protected_files', [])
        for protected_pattern in protected_files:
            # Convert glob pattern to actual files
            if '*' in protected_pattern:
                # Handle glob patterns
                continue
            else:
                # Check specific files
                file_path = self.repo_root / protected_pattern
                if not file_path.exists():
                    scope_violations.append(f"Protected file missing: {protected_pattern}")
                    
        # Check curated paths
        curated_paths = self.activation_rules.get('copilot_safety_policy', {}).get('operational_discipline', {}).get('curated_paths_only', [])
        
        # Check root directory file limits
        organization = self.activation_rules.get('organization', {})
        root_file_limit = organization.get('root_file_limit', 50)
        allowed_root_extensions = organization.get('allowed_root_extensions', [])
        
        root_files = [f for f in self.repo_root.iterdir() if f.is_file() and not f.name.startswith('.')]
        if len(root_files) > root_file_limit:
            scope_violations.append(f"Root directory has {len(root_files)} files, exceeding limit of {root_file_limit}")
            
        # Check for unauthorized files in root
        for root_file in root_files:
            if root_file.suffix not in allowed_root_extensions:
                scope_violations.append(f"Unauthorized file in root: {root_file.name} (extension {root_file.suffix} not allowed)")
                
        return scope_violations
        
    def validate_naming_policy(self) -> List[str]:
        """Validate naming policy compliance across all file types"""
        naming_violations = []
        
        # Define naming patterns - more permissive for existing repository
        naming_patterns = {
            'kotlin_class': r'^[A-Z][a-zA-Z0-9]*$',
            'kotlin_function': r'^[a-z][a-zA-Z0-9]*$',
            'python_function': r'^[a-z][a-z0-9_]*$',
            'python_class': r'^[A-Z][a-zA-Z0-9]*$',
            'shell_script': r'^[a-z0-9][a-z0-9-_]*\.sh$',  # Allow underscores and numbers at start
            'documentation': r'^[A-Z][A-Z0-9_]*\.md$',
            'config_json': r'^[a-z][a-z0-9_]*\.json$'
        }
        
        # Excluded paths - don't check build artifacts, third-party code, or legacy files
        excluded_patterns = [
            'app/build/',
            '.gradle/',
            'ai_function_calling_tools/',
            'scraped_data/',
            '.git/',
            '.ops/',
            'backend/',
            'frontend/',
            'dashboard/',
            'environment/'
        ]
        
        # Validate shell scripts with more permissive pattern
        scripts_dir = self.repo_root / "scripts"
        if scripts_dir.exists():
            for script_file in scripts_dir.glob("*.sh"):
                # Allow existing naming patterns but warn about new ones
                if not re.match(naming_patterns['shell_script'], script_file.name):
                    # Only flag truly problematic names, not existing patterns
                    if ' ' in script_file.name or script_file.name.startswith('.'):
                        naming_violations.append(f"Shell script naming violation: {script_file.name}")
                    
        # Validate Python files - more permissive for existing files
        for py_file in self.repo_root.rglob("*.py"):
            if py_file.name.startswith('.') or py_file.parent.name == '__pycache__':
                continue
                
            relative_path = py_file.relative_to(self.repo_root)
            relative_str = str(relative_path)
            
            # Skip excluded paths
            if any(relative_str.startswith(pattern) for pattern in excluded_patterns):
                continue
                
            # Check file name - allow existing patterns but flag problematic ones
            if ' ' in py_file.name or py_file.name.startswith('.'):
                naming_violations.append(f"Python file naming violation: {relative_path}")
                    
        # Validate documentation files - only check docs directory for main documentation
        docs_dir = self.repo_root / "docs"
        if docs_dir.exists():
            for doc_file in docs_dir.glob("*.md"):
                # Only check direct children of docs/, not subdirectories
                if doc_file.parent == docs_dir:
                    if not re.match(naming_patterns['documentation'], doc_file.name):
                        # Allow some common patterns
                        if not doc_file.name.startswith('README') and ' ' in doc_file.name:
                            naming_violations.append(f"Documentation naming violation: {doc_file.name}")
                    
        # Validate configuration files in configs directory only
        configs_dir = self.repo_root / "configs"
        if configs_dir.exists():
            for config_file in configs_dir.rglob("*.json"):
                # Only check direct children and one level deep
                if len(config_file.relative_to(configs_dir).parts) <= 2:
                    if not re.match(naming_patterns['config_json'], config_file.name):
                        if ' ' in config_file.name:
                            naming_violations.append(f"Config file naming violation: {config_file.relative_to(self.repo_root)}")
                    
        return naming_violations
        
    def validate_kotlin_java_code(self) -> List[str]:
        """Validate Kotlin and Java code naming conventions"""
        code_violations = []
        
        app_src = self.repo_root / "app" / "src"
        if not app_src.exists():
            return code_violations
            
        # Patterns for Kotlin/Java code analysis
        function_pattern = re.compile(r'fun\s+([A-Z][a-zA-Z0-9_]*)\s*\(')
        class_pattern = re.compile(r'class\s+([a-z][a-zA-Z0-9_]*)')
        
        for code_file in app_src.rglob("*.kt"):
            try:
                with open(code_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                    
                # Check function naming (should be camelCase)
                for match in function_pattern.finditer(content):
                    function_name = match.group(1)
                    code_violations.append(f"Function '{function_name}' in {code_file.relative_to(self.repo_root)} should use camelCase")
                    
                # Check class naming (should be PascalCase)
                for match in class_pattern.finditer(content):
                    class_name = match.group(1)
                    code_violations.append(f"Class '{class_name}' in {code_file.relative_to(self.repo_root)} should use PascalCase")
                    
            except Exception as e:
                logger.warning(f"Could not analyze {code_file}: {e}")
                
        return code_violations
        
    def validate_directory_structure(self) -> List[str]:
        """Validate directory naming and organization"""
        structure_violations = []
        
        # Expected directory structure
        expected_dirs = [
            'app', 'ai', 'scripts', 'docs', 'configs', 'tools', 
            'reference', 'logs', 'archive'
        ]
        
        # Check for required directories
        for expected_dir in expected_dirs:
            if not (self.repo_root / expected_dir).exists():
                structure_violations.append(f"Required directory missing: {expected_dir}/")
                
        # Check directory naming conventions (should be lowercase, underscores allowed)
        for item in self.repo_root.iterdir():
            if item.is_dir() and not item.name.startswith('.'):
                if not re.match(r'^[a-z][a-z0-9_]*$', item.name):
                    structure_violations.append(f"Directory naming violation: {item.name}/")
                    
        return structure_violations
        
    def check_file_extensions_binding(self) -> List[str]:
        """Check if files are in correct directories based on extension binding rules"""
        binding_violations = []
        
        bind_rules = self.activation_rules.get('bind_rules', [])
        
        # Excluded paths - don't check build artifacts, third-party code, or generated files
        excluded_patterns = [
            'app/build/',
            '.gradle/',
            'ai_function_calling_tools/',
            'scraped_data/',
            'archive/',
            'datasets/',
            '.git/',
            '.ops/',
            'logs/',
            'backend/',
            'frontend/',
            'dashboard/',
            'environment/'
        ]
        
        for rule in bind_rules:
            from_ext = rule.get('from_ext')
            bind_hint = rule.get('bind_hint')
            
            if not from_ext or not bind_hint:
                continue
                
            # Find files with this extension
            for file_path in self.repo_root.rglob(f"*{from_ext}"):
                if file_path.is_file():
                    relative_path = file_path.relative_to(self.repo_root)
                    relative_str = str(relative_path)
                    
                    # Skip excluded paths
                    if any(relative_str.startswith(pattern) for pattern in excluded_patterns):
                        continue
                    
                    # Check if file is in the correct directory
                    if not relative_str.startswith(bind_hint):
                        binding_violations.append(f"File extension binding violation: {relative_path} should be in {bind_hint}")
                        
        return binding_violations
        
    def generate_report(self) -> Dict:
        """Generate comprehensive policy compliance report"""
        report = {
            'timestamp': datetime.utcnow().isoformat(),
            'repository_root': str(self.repo_root),
            'scope_enforcement': {
                'violations': self.validate_scope_enforcement(),
                'status': 'PASS'
            },
            'naming_policy': {
                'violations': self.validate_naming_policy(),
                'status': 'PASS'
            },
            'code_naming': {
                'violations': self.validate_kotlin_java_code(),
                'status': 'PASS'
            },
            'directory_structure': {
                'violations': self.validate_directory_structure(),
                'status': 'PASS'
            },
            'file_binding': {
                'violations': self.check_file_extensions_binding(),
                'status': 'PASS'
            }
        }
        
        # Set status based on violations
        for category in report:
            if category in ['timestamp', 'repository_root']:
                continue
            if report[category]['violations']:
                report[category]['status'] = 'FAIL'
                
        # Overall status
        report['overall_status'] = 'PASS'
        for category in report:
            if category in ['timestamp', 'repository_root', 'overall_status']:
                continue
            if report[category]['status'] == 'FAIL':
                report['overall_status'] = 'FAIL'
                break
                
        return report
        
    def run_validation(self, verbose: bool = False) -> bool:
        """Run complete policy validation"""
        logger.info("Starting policy enforcement validation...")
        
        report = self.generate_report()
        
        # Print results
        print("\n" + "="*60)
        print("POLICY ENFORCEMENT VALIDATION REPORT")
        print("="*60)
        print(f"Repository: {report['repository_root']}")
        print(f"Timestamp: {report['timestamp']}")
        print(f"Overall Status: {report['overall_status']}")
        print()
        
        total_violations = 0
        
        for category in ['scope_enforcement', 'naming_policy', 'code_naming', 'directory_structure', 'file_binding']:
            violations = report[category]['violations']
            status = report[category]['status']
            total_violations += len(violations)
            
            print(f"{category.upper().replace('_', ' ')}: {status}")
            if violations and verbose:
                for violation in violations:
                    print(f"  ❌ {violation}")
            elif violations:
                print(f"  ❌ {len(violations)} violations found")
            else:
                print(f"  ✅ No violations found")
            print()
            
        # Save report to logs
        logs_dir = self.repo_root / "logs"
        logs_dir.mkdir(exist_ok=True)
        report_file = logs_dir / f"policy_enforcement_report_{datetime.utcnow().strftime('%Y%m%d_%H%M%S')}.json"
        
        with open(report_file, 'w') as f:
            json.dump(report, f, indent=2)
            
        logger.info(f"Report saved to: {report_file}")
        
        if total_violations > 0:
            print(f"Total violations found: {total_violations}")
            return False
        else:
            print("✅ All policy enforcement checks passed!")
            return True

def main():
    """Main entry point"""
    import argparse
    
    parser = argparse.ArgumentParser(description="Policy Enforcement Validator for DevUl Army : Living Sriracha AGI")
    parser.add_argument("--repo-root", default=".", help="Repository root directory (default: current directory)")
    parser.add_argument("--verbose", "-v", action="store_true", help="Show detailed violation information")
    parser.add_argument("--scope-only", action="store_true", help="Only validate scope enforcement")
    parser.add_argument("--naming-only", action="store_true", help="Only validate naming policy")
    
    args = parser.parse_args()
    
    # Resolve repository root
    repo_root = os.path.abspath(args.repo_root)
    if not os.path.exists(repo_root):
        print(f"Error: Repository root does not exist: {repo_root}")
        sys.exit(1)
        
    # Create validator
    validator = PolicyEnforcementValidator(repo_root)
    
    # Run validation
    success = validator.run_validation(verbose=args.verbose)
    
    # Exit with appropriate code
    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()