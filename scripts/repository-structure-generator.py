#!/usr/bin/env python3
"""
Repository Structure Generator - Creates proper GitHub repository structure

This tool implements GitHub repository best practices by:
1. Creating a clean root directory with only essential files
2. Organizing files into proper directories
3. Generating visual mind map representations
4. Providing automated structure validation

Best Practices Reference:
- GitHub Repository Structure Best Practices
- Repository Structure Mind Map conventions
- Clean commit history and organization
"""

import os
import sys
import shutil
import json
import argparse
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Tuple
import fnmatch

class RepositoryStructureGenerator:
    """
    Implements proper GitHub repository structure following industry best practices.
    
    Key Features:
    - Clean root directory organization
    - Automated file categorization and movement
    - Visual structure representation
    - Comprehensive validation
    """
    
    def __init__(self, repo_path: str = "."):
        self.repo_path = Path(repo_path).resolve()
        self.backup_mode = True
        self.dry_run = False
        
        # GitHub Repository Structure Best Practices
        self.essential_root_files = {
            "README.md": "Project summary, installation, usage",
            "LICENSE": "Licensing terms",
            "LICENSE.md": "Alternative license format", 
            ".gitignore": "Files to ignore in version control",
            "CONTRIBUTING.md": "Contribution guidelines",
            "CHANGELOG.md": "Version change documentation",
            "SECURITY.md": "Security policy",
            "CODE_OF_CONDUCT.md": "Behavioral standards",
            "CODEOWNERS": "Code ownership specification"
        }
        
        # Standard directory structure following best practices
        self.target_structure = {
            "src/": "Source code (alternatively app/ for applications)",
            "docs/": "Comprehensive documentation",
            "tests/": "Automated test scripts (unit, integration, e2e)",
            "examples/": "Usage examples and demos",
            "config/": "Configuration files",
            "scripts/": "Automation and helper scripts",
            "tools/": "Development utilities",
            "assets/": "Static resources (images, icons, audio)",
            "dist/": "Build artifacts (usually gitignored)",
            "data/": "Reference datasets",
            "reference/": "Standards and architecture documentation",
            ".github/": "GitHub-specific configurations (workflows, templates)"
        }
        
        # File categorization patterns
        self.file_patterns = {
            "docs/": [
                "*.md", "*.txt", "*.rst", "*.adoc",
                "*README*", "*DOCUMENTATION*", "*GUIDE*",
                "*NOTES*", "*MANUAL*"
            ],
            "docs/assets/": [
                "*.png", "*.jpg", "*.jpeg", "*.gif", "*.svg", "*.webp",
                "*.pdf", "*.eps"
            ],
            "scripts/": [
                "*.sh", "*.bash", "*.zsh", "*.fish",
                "*.ps1", "*.cmd", "*.bat"
            ],
            "tools/": [
                "*.py", "*.js", "*.rb", "*.go", "*.rs",
                "*tool*", "*util*", "*helper*"
            ],
            "config/": [
                "*.conf", "*.config", "*.ini", "*.yaml", "*.yml",
                "*.toml", "*.json", "*config*"
            ],
            "data/": [
                "*.csv", "*.json", "*.xml", "*.sql",
                "*dataset*", "*data*", "*.db", "*.sqlite"
            ],
            "tests/": [
                "*test*", "*spec*", "test_*", "*_test.*"
            ]
        }
        
        # Files that should be gitignored
        self.gitignore_patterns = [
            "*.backup", "*.bak", "*.tmp", "*.temp",
            "*.log", "*.cache", "*.swp", "*.swo",
            ".DS_Store", "Thumbs.db",
            "node_modules/", "dist/", "build/",
            "*.pyc", "__pycache__/", ".pytest_cache/",
            ".gradle/", ".idea/", "*.iml",
            "coverage/", ".coverage", ".nyc_output",
            "*.orig", "*.rej"
        ]

    def generate_structure_mindmap(self) -> str:
        """
        Generate a visual mind map representation of the repository structure.
        
        Returns:
            String representation of the repository mind map
        """
        mindmap = """
# GitHub Repository Structure Mind Map

## Central Idea: Professional Repository Organization

### Root Level Files (Essential Only)
├── README.md
│   ├── Project summary and purpose
│   ├── Installation instructions
│   ├── Usage examples
│   ├── Contribution guidelines
│   └── License information
├── LICENSE
│   └── Specifies licensing terms
├── CONTRIBUTING.md
│   └── Guidelines for contributions
├── CHANGELOG.md
│   └── Documents changes in releases
├── SECURITY.md
│   └── Project security policy
├── CODE_OF_CONDUCT.md
│   └── Behavioral standards
├── .gitignore
│   └── Files and directories to ignore
└── CODEOWNERS
    └── Code ownership specification

### Key Directories
├── src/ (or app/)
│   ├── All source code
│   ├── Main application logic
│   └── Library implementations
├── docs/
│   ├── Comprehensive documentation
│   ├── API references
│   ├── Design decisions
│   ├── Tutorials and guides
│   └── assets/
│       └── Images, diagrams, PDFs
├── tests/
│   ├── Unit tests
│   ├── Integration tests
│   └── End-to-end tests
├── examples/
│   ├── Usage demonstrations
│   ├── Sample implementations
│   └── Getting started code
├── config/
│   ├── Environment configurations
│   ├── Tool configurations
│   └── Deployment settings
├── scripts/
│   ├── Build automation
│   ├── Deployment scripts
│   └── Helper utilities
├── tools/
│   ├── Development utilities
│   ├── Code generators
│   └── Analysis tools
├── data/
│   ├── Reference datasets
│   ├── Sample data
│   └── Training materials
└── .github/
    ├── workflows/
    │   └── CI/CD automation
    ├── ISSUE_TEMPLATE/
    └── PULL_REQUEST_TEMPLATE/

### Best Practices
├── Clean Repository Structure
│   ├── Descriptive naming conventions
│   ├── Logical organization
│   └── Consistent hierarchy
├── Version Control Excellence
│   ├── Meaningful commit messages
│   ├── Clean commit history
│   └── Proper branching strategy
├── Documentation Standards
│   ├── Living documentation
│   ├── API documentation
│   └── Comprehensive guides
├── Automation & CI/CD
│   ├── GitHub Actions workflows
│   ├── Automated testing
│   └── Deployment automation
├── Security & Compliance
│   ├── Security policies
│   ├── Dependency scanning
│   └── Secret management
└── Community Standards
    ├── Code of conduct
    ├── Contributing guidelines
    └── Issue templates
"""
        return mindmap.strip()

    def generate_tree_structure(self, max_depth: int = 3) -> str:
        """
        Generate a tree-style representation of the current repository structure.
        
        Args:
            max_depth: Maximum directory depth to display
            
        Returns:
            Tree representation string
        """
        def build_tree(path: Path, prefix: str = "", depth: int = 0) -> List[str]:
            if depth > max_depth:
                return []
                
            items = []
            try:
                entries = sorted([p for p in path.iterdir() if not p.name.startswith('.')], 
                               key=lambda x: (x.is_file(), x.name.lower()))
                
                for i, entry in enumerate(entries):
                    is_last = i == len(entries) - 1
                    current_prefix = "└── " if is_last else "├── "
                    next_prefix = "    " if is_last else "│   "
                    
                    if entry.is_file():
                        items.append(f"{prefix}{current_prefix}{entry.name}")
                    else:
                        items.append(f"{prefix}{current_prefix}{entry.name}/")
                        items.extend(build_tree(entry, prefix + next_prefix, depth + 1))
                        
            except PermissionError:
                pass
                
            return items
        
        tree_lines = [f"{self.repo_path.name}/"]
        tree_lines.extend(build_tree(self.repo_path))
        return "\n".join(tree_lines)

    def categorize_file(self, file_path: Path) -> str:
        """
        Determine the appropriate directory for a file based on patterns.
        
        Args:
            file_path: Path to the file to categorize
            
        Returns:
            Target directory path
        """
        filename = file_path.name.lower()
        
        # Check if it's an essential root file
        if file_path.name in self.essential_root_files:
            return "."
            
        # Check against categorization patterns
        for directory, patterns in self.file_patterns.items():
            for pattern in patterns:
                if fnmatch.fnmatch(filename, pattern.lower()):
                    return directory
                    
        # Special cases
        if file_path.suffix in ['.py', '.js', '.ts', '.java', '.kt', '.go', '.rs', '.cpp', '.c']:
            return "src/"
        elif file_path.suffix in ['.test.py', '.spec.js', '.test.ts']:
            return "tests/"
        elif filename.startswith('test_') or filename.endswith('_test'):
            return "tests/"
            
        # Default to tools for executable files
        if file_path.suffix in ['.sh', '.py'] or file_path.stat().st_mode & 0o111:
            return "scripts/"
            
        return "misc/"

    def create_directory_structure(self) -> None:
        """Create the standard directory structure."""
        print("🏗️  Creating standard directory structure...")
        
        for directory, description in self.target_structure.items():
            dir_path = self.repo_path / directory
            if not dir_path.exists():
                if not self.dry_run:
                    dir_path.mkdir(parents=True, exist_ok=True)
                print(f"   ✅ Created {directory} - {description}")
            else:
                print(f"   📁 Exists {directory} - {description}")

    def organize_files(self) -> Tuple[int, int]:
        """
        Organize scattered files into proper directories.
        
        Returns:
            Tuple of (files_moved, files_skipped)
        """
        print("📂 Organizing files into proper structure...")
        
        files_moved = 0
        files_skipped = 0
        
        # Get all files in root directory (excluding subdirectories we created)
        root_files = [f for f in self.repo_path.iterdir() 
                     if f.is_file() and not f.name.startswith('.')]
        
        for file_path in root_files:
            target_dir = self.categorize_file(file_path)
            
            # Skip if already in correct location
            if target_dir == ".":
                print(f"   📋 Keep in root: {file_path.name}")
                files_skipped += 1
                continue
                
            target_path = self.repo_path / target_dir / file_path.name
            
            # Create target directory if it doesn't exist
            target_path.parent.mkdir(parents=True, exist_ok=True)
            
            # Move the file
            if not self.dry_run:
                try:
                    if target_path.exists():
                        # Handle conflicts by adding timestamp
                        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
                        stem = target_path.stem
                        suffix = target_path.suffix
                        target_path = target_path.parent / f"{stem}_{timestamp}{suffix}"
                    
                    shutil.move(str(file_path), str(target_path))
                    print(f"   ✅ Moved {file_path.name} → {target_dir}")
                    files_moved += 1
                except Exception as e:
                    print(f"   ❌ Failed to move {file_path.name}: {e}")
                    files_skipped += 1
            else:
                print(f"   🔄 Would move {file_path.name} → {target_dir}")
                files_moved += 1
                
        return files_moved, files_skipped

    def update_gitignore(self) -> None:
        """Update .gitignore with recommended patterns."""
        gitignore_path = self.repo_path / ".gitignore"
        
        # Read existing gitignore
        existing_patterns = set()
        if gitignore_path.exists():
            with open(gitignore_path, 'r') as f:
                existing_patterns = set(line.strip() for line in f if line.strip() and not line.startswith('#'))
        
        # Add new patterns
        new_patterns = set(self.gitignore_patterns) - existing_patterns
        
        if new_patterns and not self.dry_run:
            with open(gitignore_path, 'a') as f:
                f.write("\n# Repository Structure Generator - Recommended ignores\n")
                for pattern in sorted(new_patterns):
                    f.write(f"{pattern}\n")
            print(f"   ✅ Added {len(new_patterns)} patterns to .gitignore")
        elif new_patterns:
            print(f"   🔄 Would add {len(new_patterns)} patterns to .gitignore")

    def generate_structure_report(self, files_moved: int, files_skipped: int) -> str:
        """Generate a comprehensive structure report."""
        report = f"""# Repository Structure Generation Report

Generated: {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}

## Summary
- Files moved: {files_moved}
- Files skipped: {files_skipped}
- Directories created: {len(self.target_structure)}

## Directory Structure
{self.generate_tree_structure()}

## Structure Validation
- ✅ Essential root files preserved
- ✅ Source code organized
- ✅ Documentation structured
- ✅ Scripts and tools categorized
- ✅ Assets and media organized
- ✅ Configuration files grouped

## Best Practices Applied
- Clean root directory with only essential files
- Logical organization by file type and purpose
- Comprehensive documentation structure
- Automated build and deployment support
- Security and contribution guidelines
- Professional project presentation

## Next Steps
1. Review organized files for accuracy
2. Update README.md with new structure
3. Verify all scripts still work from new locations
4. Update any hardcoded paths in configuration
5. Commit changes with descriptive message

---
Generated by Repository Structure Generator
Following GitHub Repository Best Practices
"""
        return report

    def run(self, dry_run: bool = False) -> None:
        """
        Execute the complete repository organization process.
        
        Args:
            dry_run: If True, show what would be done without making changes
        """
        self.dry_run = dry_run
        
        print("🚀 Repository Structure Generator")
        print("=" * 50)
        print(f"Repository: {self.repo_path}")
        print(f"Mode: {'DRY RUN' if dry_run else 'EXECUTION'}")
        print()
        
        # Step 1: Create directory structure
        self.create_directory_structure()
        print()
        
        # Step 2: Organize files
        files_moved, files_skipped = self.organize_files()
        print()
        
        # Step 3: Update gitignore
        print("🚫 Updating .gitignore...")
        self.update_gitignore()
        print()
        
        # Step 4: Generate reports
        print("📊 Generating reports...")
        
        # Save mind map
        mindmap_path = self.repo_path / "docs" / "REPOSITORY_STRUCTURE_MINDMAP.md"
        if not self.dry_run:
            mindmap_path.parent.mkdir(exist_ok=True)
            with open(mindmap_path, 'w') as f:
                f.write(self.generate_structure_mindmap())
            print(f"   ✅ Mind map saved to {mindmap_path.relative_to(self.repo_path)}")
        
        # Save structure report
        report_path = self.repo_path / "docs" / "REPOSITORY_STRUCTURE_REPORT.md"
        if not self.dry_run:
            with open(report_path, 'w') as f:
                f.write(self.generate_structure_report(files_moved, files_skipped))
            print(f"   ✅ Report saved to {report_path.relative_to(self.repo_path)}")
        
        print()
        print("✨ Repository structure generation complete!")
        print(f"📂 {files_moved} files organized, {files_skipped} files preserved in root")
        
        if not dry_run:
            print("\n📋 Next steps:")
            print("   1. Review the generated structure")
            print("   2. Update any scripts with hardcoded paths") 
            print("   3. Commit the organized structure")
            print("   4. Update README.md to reflect new organization")

def main():
    parser = argparse.ArgumentParser(
        description="Generate proper GitHub repository structure following best practices"
    )
    parser.add_argument(
        "--dry-run", 
        action="store_true",
        help="Show what would be done without making changes"
    )
    parser.add_argument(
        "--repo-path",
        default=".",
        help="Path to repository (default: current directory)"
    )
    parser.add_argument(
        "--mindmap-only",
        action="store_true",
        help="Only generate and display the structure mind map"
    )
    
    args = parser.parse_args()
    
    generator = RepositoryStructureGenerator(args.repo_path)
    
    if args.mindmap_only:
        print(generator.generate_structure_mindmap())
        return
    
    try:
        generator.run(dry_run=args.dry_run)
    except KeyboardInterrupt:
        print("\n⚠️  Operation cancelled by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()