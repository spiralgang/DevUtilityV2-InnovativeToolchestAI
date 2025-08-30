#!/usr/bin/env python3
"""
Active Merge Conflict Resolver
Resolves conflicts in an active merge state
"""

import os
import sys
import subprocess
import re
from typing import List

def resolve_gitignore_conflicts(file_path: str = ".gitignore") -> bool:
    """Resolve conflicts in .gitignore files by merging unique entries"""
    print(f"🔧 Resolving .gitignore conflicts in {file_path}")
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Extract all unique gitignore entries
        entries = set()
        comments = []
        
        lines = content.split('\n')
        in_conflict = False
        
        for line in lines:
            if line.startswith('<<<<<<<') or line.startswith('=======') or line.startswith('>>>>>>>'):
                in_conflict = not in_conflict if line.startswith('=======') else in_conflict
                continue
            
            stripped = line.strip()
            if stripped:
                if stripped.startswith('#'):
                    if stripped not in [c.strip() for c in comments]:
                        comments.append(stripped)
                else:
                    entries.add(stripped)
        
        # Create merged .gitignore
        merged_content = []
        
        # Add header comment
        merged_content.extend([
            "# DevUtilityV2 - Merged .gitignore",
            "# Auto-resolved merge conflicts",
            ""
        ])
        
        # Add all comments
        for comment in comments:
            merged_content.append(comment)
        
        merged_content.append("")
        
        # Add all unique entries sorted
        for entry in sorted(entries):
            merged_content.append(entry)
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write('\n'.join(merged_content))
        
        print(f"✅ Successfully resolved .gitignore conflicts")
        return True
        
    except Exception as e:
        print(f"❌ Failed to resolve .gitignore conflicts: {e}")
        return False

def resolve_gradle_conflicts(file_path: str) -> bool:
    """Resolve conflicts in Gradle files by intelligent merging"""
    print(f"🔧 Resolving Gradle conflicts in {file_path}")
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        lines = content.split('\n')
        resolved_lines = []
        in_conflict = False
        head_section = []
        incoming_section = []
        conflict_section = ""
        
        for line in lines:
            if line.startswith('<<<<<<<'):
                in_conflict = True
                conflict_section = "head"
                continue
            elif line.startswith('======='):
                conflict_section = "incoming"
                continue
            elif line.startswith('>>>>>>>'):
                # Merge the sections intelligently
                merged = merge_gradle_sections(head_section, incoming_section)
                resolved_lines.extend(merged)
                
                # Reset state
                in_conflict = False
                head_section = []
                incoming_section = []
                continue
            
            if in_conflict:
                if conflict_section == "head":
                    head_section.append(line)
                else:
                    incoming_section.append(line)
            else:
                resolved_lines.append(line)
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write('\n'.join(resolved_lines))
        
        print(f"✅ Successfully resolved Gradle conflicts in {file_path}")
        return True
        
    except Exception as e:
        print(f"❌ Failed to resolve Gradle conflicts in {file_path}: {e}")
        return False

def merge_gradle_sections(head_lines: List[str], incoming_lines: List[str]) -> List[str]:
    """Intelligently merge Gradle configuration sections"""
    # For Gradle files, combine both sections and prefer incoming when conflicts
    merged = []
    
    # If incoming section is more comprehensive (more lines), prefer it
    if len(incoming_lines) > len(head_lines):
        primary = incoming_lines
        secondary = head_lines
    else:
        primary = head_lines
        secondary = incoming_lines
    
    # Add all lines from primary section
    for line in primary:
        if line.strip():
            merged.append(line)
    
    # Add unique lines from secondary section
    primary_content = '\n'.join(primary)
    for line in secondary:
        if line.strip() and line.strip() not in primary_content:
            merged.append(line)
    
    return merged

def resolve_readme_conflicts(file_path: str = "README.md") -> bool:
    """Resolve conflicts in README files by combining content"""
    print(f"🔧 Resolving README conflicts in {file_path}")
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        lines = content.split('\n')
        resolved_lines = []
        in_conflict = False
        head_section = []
        incoming_section = []
        conflict_section = ""
        
        for line in lines:
            if line.startswith('<<<<<<<'):
                in_conflict = True
                conflict_section = "head"
                continue
            elif line.startswith('======='):
                conflict_section = "incoming"
                continue
            elif line.startswith('>>>>>>>'):
                # For README, prefer the more comprehensive version (usually incoming)
                if len(incoming_section) > len(head_section):
                    resolved_lines.extend(incoming_section)
                else:
                    resolved_lines.extend(head_section)
                
                # Reset state
                in_conflict = False
                head_section = []
                incoming_section = []
                continue
            
            if in_conflict:
                if conflict_section == "head":
                    head_section.append(line)
                else:
                    incoming_section.append(line)
            else:
                resolved_lines.append(line)
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write('\n'.join(resolved_lines))
        
        print(f"✅ Successfully resolved README conflicts")
        return True
        
    except Exception as e:
        print(f"❌ Failed to resolve README conflicts: {e}")
        return False

def get_conflicted_files() -> List[str]:
    """Get list of conflicted files in active merge"""
    try:
        result = subprocess.run(
            ["git", "diff", "--name-only", "--diff-filter=U"],
            capture_output=True, text=True, check=True
        )
        return [f.strip() for f in result.stdout.split('\n') if f.strip()]
    except subprocess.CalledProcessError:
        return []

def main():
    """Main function to resolve active merge conflicts"""
    print("🚀 Active Merge Conflict Resolver")
    print("=" * 40)
    
    # Check if we're in a merge state
    if not os.path.exists('.git/MERGE_HEAD'):
        print("❌ No active merge detected. Run 'git merge <branch>' first.")
        sys.exit(1)
    
    # Get conflicted files
    conflicted_files = get_conflicted_files()
    
    if not conflicted_files:
        print("✅ No conflicts found!")
        sys.exit(0)
    
    print(f"📋 Found {len(conflicted_files)} conflicted files:")
    for f in conflicted_files:
        print(f"  ❌ {f}")
    print()
    
    # Resolution strategies
    strategies = {
        '.gitignore': resolve_gitignore_conflicts,
        'build.gradle': resolve_gradle_conflicts,
        'settings.gradle': resolve_gradle_conflicts,
        'README.md': resolve_readme_conflicts,
    }
    
    resolved_count = 0
    
    # Resolve each file
    for file_path in conflicted_files:
        if file_path in strategies:
            if strategies[file_path](file_path):
                # Stage the resolved file
                subprocess.run(["git", "add", file_path], check=True)
                resolved_count += 1
                print(f"✅ Staged resolved file: {file_path}")
            else:
                print(f"⚠️  Could not auto-resolve: {file_path}")
        else:
            print(f"⚠️  No strategy for: {file_path} (manual resolution needed)")
    
    print()
    print(f"📊 Resolution Summary:")
    print(f"  Total files: {len(conflicted_files)}")
    print(f"  Auto-resolved: {resolved_count}")
    print(f"  Manual needed: {len(conflicted_files) - resolved_count}")
    
    if resolved_count == len(conflicted_files):
        print("\n🎉 All conflicts resolved automatically!")
        print("💡 You can now commit the merge:")
        print("   git commit -m 'Resolve merge conflicts automatically'")
    else:
        print(f"\n⚠️  {len(conflicted_files) - resolved_count} files need manual resolution")
        print("💡 Resolve manually, then:")
        print("   git add <resolved-files>")
        print("   git commit -m 'Resolve merge conflicts'")

if __name__ == "__main__":
    main()