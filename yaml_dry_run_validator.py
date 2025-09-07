#!/usr/bin/env python3
"""
YAML dry-run validator and syntax checker.
"""

import os
import yaml
import subprocess
import sys

def validate_yaml_syntax():
    """Validate all YAML files for syntax correctness."""
    os.chdir('/home/runner/work/DevUl-Army--__--Living-Sriracha-AGI/DevUl-Army--__--Living-Sriracha-AGI/.github/workflows')
    
    print("🔍 YAML Syntax Validation")
    print("=" * 50)
    
    yaml_files = [f for f in os.listdir('.') if f.endswith('.yml')]
    valid_files = 0
    total_files = len(yaml_files)
    
    for yaml_file in sorted(yaml_files):
        try:
            with open(yaml_file, 'r') as f:
                yaml.safe_load(f)
            print(f"✅ {yaml_file}: VALID YAML syntax")
            valid_files += 1
        except yaml.YAMLError as e:
            print(f"❌ {yaml_file}: YAML syntax error - {e}")
        except Exception as e:
            print(f"❌ {yaml_file}: Error reading file - {e}")
    
    print(f"\n📊 Summary: {valid_files}/{total_files} files have valid YAML syntax")
    
    if valid_files == total_files:
        print("✅ All YAML files have valid syntax!")
    else:
        print("❌ Some YAML files have syntax errors!")
        return False
    
    return True

def validate_yaml_linting():
    """Run yamllint to check for linting issues."""
    print("\n🔍 YAML Linting Validation")
    print("=" * 50)
    
    try:
        result = subprocess.run(['yamllint', '*.yml'], 
                              capture_output=True, text=True, shell=True)
        
        if result.returncode == 0:
            print("✅ All YAML files pass linting!")
            return True
        else:
            print("❌ YAML linting issues found:")
            print(result.stdout)
            return False
    except Exception as e:
        print(f"❌ Error running yamllint: {e}")
        return False

def check_github_actions_structure():
    """Basic check for GitHub Actions workflow structure."""
    print("\n🔍 GitHub Actions Structure Validation")
    print("=" * 50)
    
    yaml_files = [f for f in os.listdir('.') if f.endswith('.yml')]
    valid_workflows = 0
    
    for yaml_file in sorted(yaml_files):
        try:
            with open(yaml_file, 'r') as f:
                content = yaml.safe_load(f)
            
            # Check basic workflow structure
            if isinstance(content, dict):
                has_name = 'name' in content
                has_on = 'on' in content
                has_jobs = 'jobs' in content
                
                if has_name and has_on and has_jobs:
                    print(f"✅ {yaml_file}: Valid GitHub Actions workflow structure")
                    valid_workflows += 1
                else:
                    missing = []
                    if not has_name: missing.append('name')
                    if not has_on: missing.append('on')
                    if not has_jobs: missing.append('jobs')
                    print(f"⚠️  {yaml_file}: Missing required fields: {', '.join(missing)}")
            else:
                print(f"❌ {yaml_file}: Not a valid YAML object")
                
        except Exception as e:
            print(f"❌ {yaml_file}: Error validating structure - {e}")
    
    print(f"\n📊 Summary: {valid_workflows}/{len(yaml_files)} files are valid GitHub Actions workflows")
    return valid_workflows == len(yaml_files)

def dry_run_validation():
    """Perform comprehensive dry-run validation."""
    print("🧪 COMPREHENSIVE YAML DRY-RUN VALIDATION")
    print("=" * 60)
    
    syntax_ok = validate_yaml_syntax()
    structure_ok = check_github_actions_structure()
    linting_ok = validate_yaml_linting()
    
    print("\n" + "=" * 60)
    print("📋 VALIDATION SUMMARY")
    print("=" * 60)
    
    if syntax_ok:
        print("✅ YAML Syntax: PASSED")
    else:
        print("❌ YAML Syntax: FAILED")
    
    if structure_ok:
        print("✅ GitHub Actions Structure: PASSED")
    else:
        print("❌ GitHub Actions Structure: FAILED")
    
    if linting_ok:
        print("✅ YAML Linting: PASSED")
    else:
        print("⚠️  YAML Linting: HAS WARNINGS")
    
    if syntax_ok and structure_ok:
        print("\n🎉 WORKFLOWS CAN BE BUILT AND EXECUTED!")
        print("✅ All YAML files are syntactically valid and structurally correct")
        
        if not linting_ok:
            print("⚠️  Note: There are linting warnings (line length, spacing) but workflows will still run")
        
        return True
    else:
        print("\n❌ WORKFLOWS CANNOT BE BUILT!")
        print("🚫 Fix syntax and structure issues before deployment")
        return False

if __name__ == '__main__':
    success = dry_run_validation()
    sys.exit(0 if success else 1)