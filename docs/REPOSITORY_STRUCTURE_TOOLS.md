# Repository Structure Tools

This directory contains tools to address the questions:
- "Is there a plugin that will generate a tree out of my folder?"
- "Is there a tool that creates a folder structure?"

## 🛠️ Available Tools

### 1. Repository Structure Generator (`repository-structure-generator.py`)

**Purpose**: Comprehensive repository organization following GitHub best practices

**Features**:
- Automatically organizes scattered files into proper directories
- Creates clean root directory with only essential files
- Generates visual mind map representations
- Updates .gitignore with recommended patterns
- Provides detailed reports of changes

**Usage**:
```bash
# See what would be organized (dry run)
python3 scripts/repository-structure-generator.py --dry-run

# Organize the repository
python3 scripts/repository-structure-generator.py

# Generate only the mind map
python3 scripts/repository-structure-generator.py --mindmap-only
```

### 2. Folder Structure Generator (`folder-structure-generator.sh`)

**Purpose**: Generate visual tree representations of any directory structure

**Features**:
- ASCII tree structure generation
- Multiple output formats (tree, mindmap, json, markdown)
- Customizable depth and filtering
- File size information
- Color-coded output by file type

**Usage**:
```bash
# Generate tree for current directory
./scripts/folder-structure-generator.sh

# Generate with custom depth
./scripts/folder-structure-generator.sh --depth 3 /path/to/directory

# Generate mind map format
./scripts/folder-structure-generator.sh --format mindmap

# Generate and save to file
./scripts/folder-structure-generator.sh --format json --output structure.json

# Show GitHub best practices mind map
./scripts/folder-structure-generator.sh --mindmap-only
```

## 📋 GitHub Repository Best Practices

Based on industry standards and community best practices:

### Essential Root Files
- `README.md` - Project overview, installation, usage
- `LICENSE` - Licensing terms
- `.gitignore` - Files to exclude from version control
- `CONTRIBUTING.md` - Contribution guidelines
- `CHANGELOG.md` - Version change documentation
- `SECURITY.md` - Security policy
- `CODE_OF_CONDUCT.md` - Community standards

### Standard Directory Structure
- `src/` or `app/` - Source code
- `docs/` - Documentation with `assets/` subdirectory
- `tests/` - Automated tests
- `examples/` - Usage demonstrations
- `config/` - Configuration files
- `scripts/` - Build and deployment scripts
- `tools/` - Development utilities
- `data/` - Reference datasets
- `.github/` - GitHub-specific configurations

## 🎯 Results

After running the repository structure generator, this repository has been transformed from:
- **120+ scattered files** in the root directory
- Multiple backup files and temporary files cluttering the space
- No clear organization or navigation

To:
- **Clean root directory** with only essential files (README.md, LICENSE)
- **Properly organized directories** following GitHub best practices
- **Categorized files** by type and purpose
- **Professional structure** ready for collaboration

## 📊 Structure Visualization

The tools generate several types of visualizations:

### Tree Structure
```
repository/
├── README.md
├── LICENSE
├── src/
│   └── source code files
├── docs/
│   ├── documentation files
│   └── assets/
│       └── images and media
├── tests/
│   └── test files
├── scripts/
│   └── automation scripts
└── tools/
    └── development utilities
```

### Mind Map Format
```
# Repository Structure
## Central Idea: Professional Organization
### Root Files
├── README.md
│   ├── Project summary
│   └── Usage instructions
### Directories
├── src/ - Source code
├── docs/ - Documentation
└── tests/ - Test files
```

## 🚀 Quick Start

1. **Analyze current structure**:
   ```bash
   ./scripts/folder-structure-generator.sh --depth 2
   ```

2. **Preview organization** (dry run):
   ```bash
   python3 scripts/repository-structure-generator.py --dry-run
   ```

3. **Organize repository**:
   ```bash
   python3 scripts/repository-structure-generator.py
   ```

4. **Generate documentation**:
   ```bash
   ./scripts/folder-structure-generator.sh --format markdown --output STRUCTURE.md
   ```

## 📚 Generated Documentation

The tools automatically create:
- `docs/REPOSITORY_STRUCTURE_MINDMAP.md` - Visual mind map
- `docs/REPOSITORY_STRUCTURE_REPORT.md` - Detailed organization report
- Updated `.gitignore` with recommended patterns

## 🔧 Customization

Both tools can be customized for different project types:
- **Web applications**: Focus on `src/`, `public/`, `build/`
- **Python libraries**: Use package name instead of `src/`
- **Documentation projects**: Emphasize `docs/` structure
- **Data science**: Include `notebooks/`, `data/`, `models/`

## 📞 Support

These tools address common questions from the development community:
- "How should I organize my GitHub repository?"
- "What's the best practice for folder structure?"
- "Is there a tool to visualize my directory structure?"
- "How do I clean up a messy repository?"

The implementation follows GitHub's own recommendations and industry best practices for repository organization.