#!/usr/bin/env python3
"""
Pull Request Conflict Resolution System
Automated tools for detecting and resolving merge conflicts in Git repositories.
"""

import os
import sys
import subprocess
import argparse
import re
from typing import List, Tuple, Optional
from pathlib import Path
import logging
from datetime import datetime

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class ConflictResolver:
    """Main class for handling pull request conflicts"""
    
    def __init__(self, repo_path: str = "."):
        self.repo_path = Path(repo_path)
        self.conflicts_detected = []
        
    def detect_conflicts(self, source_branch: str, target_branch: str) -> List[str]:
        """Detect merge conflicts between two branches"""
        logger.info(f"Detecting conflicts between {source_branch} and {target_branch}")
        
        try:
            # Try a test merge
            result = subprocess.run([
                "git", "merge-tree", 
                f"origin/{target_branch}", 
                f"origin/{source_branch}"
            ], cwd=self.repo_path, capture_output=True, text=True)
            
            if result.returncode == 0 and result.stdout.strip():
                # Parse merge-tree output for conflicts
                conflicts = self._parse_merge_tree_output(result.stdout)
                self.conflicts_detected = conflicts
                return conflicts
            
        except subprocess.CalledProcessError as e:
            logger.error(f"Failed to detect conflicts: {e}")
            
        return []
    
    def _parse_merge_tree_output(self, output: str) -> List[str]:
        """Parse git merge-tree output to identify conflicted files"""
        conflicted_files = []
        lines = output.split('\n')
        
        # git merge-tree output: look for lines like "changed in both X", "added in both X", etc.
        conflict_patterns = [
            re.compile(r'^(changed|added|removed) in both (.+)$'),
        ]
        for line in lines:
            for pattern in conflict_patterns:
                match = pattern.match(line.strip())
                if match:
                    file_path = match.group(2)
                    if file_path not in conflicted_files:
                        conflicted_files.append(file_path)
        return conflicted_files
    
    def resolve_build_file_conflicts(self, file_path: str) -> bool:
        """Resolve conflicts in build files (build.gradle, settings.gradle)"""
        logger.info(f"Resolving build file conflicts in {file_path}")
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            if '.gradle' in file_path:
                resolved_content = self._resolve_gradle_conflicts(content)
            else:
                resolved_content = self._resolve_generic_conflicts(content)
            
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(resolved_content)
            
            logger.info(f"Successfully resolved conflicts in {file_path}")
            return True
            
        except Exception as e:
            logger.error(f"Failed to resolve conflicts in {file_path}: {e}")
            return False
    
    def _resolve_gradle_conflicts(self, content: str) -> str:
        """Resolve conflicts in Gradle build files"""
        lines = content.split('\n')
        resolved_lines = []
        in_conflict = False
        conflict_section = ""
        head_section = []
        incoming_section = []
        
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
                merged = self._merge_gradle_sections(head_section, incoming_section)
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
        
        return '\n'.join(resolved_lines)
    
    def _merge_gradle_sections(self, head_lines: List[str], incoming_lines: List[str]) -> List[str]:
        """Intelligently merge Gradle configuration sections"""
        # For Gradle files, prefer the more comprehensive configuration
        # Typically the incoming branch has more features
        
        # Combine dependencies and plugins
        merged = []
        
        # Add unique lines from both sections
        all_lines = head_lines + incoming_lines
        seen_lines = set()
        
        for line in all_lines:
            stripped = line.strip()
            if stripped and stripped not in seen_lines:
                merged.append(line)
                seen_lines.add(stripped)
        
        return merged
    
    def _resolve_generic_conflicts(self, content: str) -> str:
        """Resolve conflicts in generic files"""
        lines = content.split('\n')
        resolved_lines = []
        in_conflict = False
        head_section = []
        incoming_section = []
        
        for line in lines:
            if line.startswith('<<<<<<<'):
                in_conflict = True
                continue
            elif line.startswith('======='):
                continue
            elif line.startswith('>>>>>>>'):
                # For generic conflicts, prefer incoming changes (usually more recent)
                resolved_lines.extend(incoming_section)
                in_conflict = False
                head_section = []
                incoming_section = []
                continue
            
            if in_conflict:
                incoming_section.append(line)
            else:
                resolved_lines.append(line)
        
        return '\n'.join(resolved_lines)
    
    def resolve_gitignore_conflicts(self, file_path: str = ".gitignore") -> bool:
        """Resolve conflicts in .gitignore files by merging unique entries"""
        logger.info("Resolving .gitignore conflicts")
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Extract all unique gitignore entries
            entries = set()
            comments = []
            
            lines = content.split('\n')
            for line in lines:
                if line.startswith('<<<<<<<') or line.startswith('=======') or line.startswith('>>>>>>>'):
                    continue
                
                stripped = line.strip()
                if stripped:
                    if stripped.startswith('#'):
                        if stripped not in comments:
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
            
            logger.info("Successfully resolved .gitignore conflicts")
            return True
            
        except Exception as e:
            logger.error(f"Failed to resolve .gitignore conflicts: {e}")
            return False
    
    def create_conflict_report(self, conflicts: List[str]) -> str:
        """Generate a detailed conflict resolution report"""
        report = [
            "# Pull Request Conflict Resolution Report",
            f"Generated at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            "## Detected Conflicts",
        ]
        
        if not conflicts:
            report.append("✅ No conflicts detected!")
            return '\n'.join(report)
        
        for i, conflict in enumerate(conflicts, 1):
            report.append(f"{i}. `{conflict}`")
        
        report.extend([
            "",
            "## Resolution Strategy",
            "- **Build files**: Merge dependencies and configurations intelligently",
            "- **Documentation**: Prefer more comprehensive version",
            "- **Configuration files**: Combine unique entries",
            "- **Code files**: Manual review recommended",
            "",
            "## Actions Taken",
        ])
        
        return '\n'.join(report)
    
    def auto_resolve_conflicts(self) -> bool:
        """Automatically resolve common conflicts"""
        success = True
        
        # Common conflicted files and their resolution strategies
        resolution_strategies = {
            '.gitignore': self.resolve_gitignore_conflicts,
            'build.gradle': lambda: self.resolve_build_file_conflicts('build.gradle'),
            'settings.gradle': lambda: self.resolve_build_file_conflicts('settings.gradle'),
        }
        
        for file_path, resolver in resolution_strategies.items():
            if os.path.exists(file_path):
                try:
                    if not resolver():
                        success = False
                except Exception as e:
                    logger.error(f"Failed to resolve {file_path}: {e}")
                    success = False
        
        return success

def main():
    """Main entry point for the conflict resolver"""
    parser = argparse.ArgumentParser(description="Pull Request Conflict Resolution System")
    parser.add_argument("--source", required=True, help="Source branch name")
    parser.add_argument("--target", default="main", help="Target branch name")
    parser.add_argument("--resolve", action="store_true", help="Automatically resolve conflicts")
    parser.add_argument("--report", help="Generate conflict report file")
    
    args = parser.parse_args()
    
    resolver = ConflictResolver()
    
    # Detect conflicts
    conflicts = resolver.detect_conflicts(args.source, args.target)
    
    if conflicts:
        logger.info(f"Found {len(conflicts)} conflicts")
        for conflict in conflicts:
            logger.info(f"  - {conflict}")
    else:
        logger.info("No conflicts detected")
    
    # Generate report if requested
    if args.report:
        report = resolver.create_conflict_report(conflicts)
        with open(args.report, 'w') as f:
            f.write(report)
        logger.info(f"Conflict report saved to {args.report}")
    
    # Auto-resolve if requested
    if args.resolve and conflicts:
        logger.info("Attempting automatic conflict resolution...")
        if resolver.auto_resolve_conflicts():
            logger.info("✅ Conflicts resolved successfully!")
            return 0
        else:
            logger.error("❌ Some conflicts could not be resolved automatically")
            return 1
    
    return len(conflicts)

if __name__ == "__main__":
    sys.exit(main())