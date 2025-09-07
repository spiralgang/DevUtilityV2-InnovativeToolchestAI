#!/bin/bash
#
# Quick Repository Structure Tool
# Provides one-command solutions for common structure questions
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Color codes
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

show_banner() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                Repository Structure Tools                    ║"
    echo "║            Answering: 'How to organize my repo?'            ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

show_help() {
    show_banner
    cat << EOF
${YELLOW}Usage:${NC} $0 [COMMAND] [OPTIONS]

${YELLOW}Commands:${NC}
  tree            Generate ASCII tree of current directory
  mindmap         Show GitHub repository structure mind map
  organize        Organize repository following best practices
  analyze         Analyze current repository structure
  help            Show this help message

${YELLOW}Options:${NC}
  --depth N       Set maximum depth for tree generation (default: 3)
  --dry-run       Show what would be done without making changes
  --format FORMAT Output format: tree, mindmap, json, markdown
  --output FILE   Save output to file

${YELLOW}Examples:${NC}
  $0 tree                    # Generate tree for current directory
  $0 tree --depth 2          # Generate tree with max depth 2
  $0 mindmap                 # Show GitHub best practices mind map
  $0 organize --dry-run      # Preview repository organization
  $0 organize                # Actually organize the repository
  $0 analyze                 # Analyze current structure

${YELLOW}Quick Answers:${NC}
  "Is there a plugin that generates a tree?"     → $0 tree
  "How to create folder structure?"              → $0 mindmap
  "How to organize my GitHub repository?"        → $0 organize
  "What's the best practice structure?"          → $0 mindmap

EOF
}

generate_tree() {
    local depth="${1:-3}"
    local target="${2:-.}"
    
    echo -e "${GREEN}🌳 Generating directory tree...${NC}"
    echo
    
    if [[ -x "$SCRIPT_DIR/folder-structure-generator.sh" ]]; then
        "$SCRIPT_DIR/folder-structure-generator.sh" --depth "$depth" "$target"
    else
        echo "Error: folder-structure-generator.sh not found or not executable"
        return 1
    fi
}

show_mindmap() {
    echo -e "${GREEN}🧠 GitHub Repository Structure Mind Map${NC}"
    echo
    
    if [[ -x "$SCRIPT_DIR/folder-structure-generator.sh" ]]; then
        "$SCRIPT_DIR/folder-structure-generator.sh" --mindmap-only
    else
        echo "Error: folder-structure-generator.sh not found"
        return 1
    fi
}

organize_repository() {
    local dry_run="$1"
    
    echo -e "${GREEN}🏗️ Repository Organization Tool${NC}"
    echo
    
    if [[ -f "$SCRIPT_DIR/repository-structure-generator.py" ]]; then
        if [[ "$dry_run" == "--dry-run" ]]; then
            python3 "$SCRIPT_DIR/repository-structure-generator.py" --dry-run
        else
            python3 "$SCRIPT_DIR/repository-structure-generator.py"
        fi
    else
        echo "Error: repository-structure-generator.py not found"
        return 1
    fi
}

analyze_structure() {
    echo -e "${GREEN}📊 Repository Structure Analysis${NC}"
    echo
    
    cd "$REPO_ROOT"
    
    echo "📂 Root Directory Files:"
    find . -maxdepth 1 -type f | wc -l | xargs echo "   Files in root:"
    
    echo
    echo "📁 Directory Structure (depth 2):"
    generate_tree 2
    
    echo
    echo "🔍 File Type Distribution:"
    echo "   Scripts (.sh): $(find . -name "*.sh" | wc -l)"
    echo "   Documentation (.md): $(find . -name "*.md" | wc -l)"
    echo "   Python (.py): $(find . -name "*.py" | wc -l)"
    echo "   Images (.png/.jpg): $(find . -name "*.png" -o -name "*.jpg" -o -name "*.jpeg" | wc -l)"
    echo "   Backup files (.backup): $(find . -name "*.backup" | wc -l)"
    
    echo
    echo "💡 Recommendations:"
    local root_files=$(find . -maxdepth 1 -type f | wc -l)
    if [[ $root_files -gt 10 ]]; then
        echo "   ⚠️  Root directory has $root_files files - consider organizing"
        echo "   💡 Run: $0 organize --dry-run"
    else
        echo "   ✅ Root directory appears well organized"
    fi
    
    if [[ $(find . -name "*.backup" | wc -l) -gt 0 ]]; then
        echo "   ⚠️  Found backup files - consider adding to .gitignore"
    fi
}

# Parse command line arguments
COMMAND="${1:-help}"
shift || true

case "$COMMAND" in
    tree)
        DEPTH=3
        TARGET="."
        while [[ $# -gt 0 ]]; do
            case $1 in
                --depth)
                    DEPTH="$2"
                    shift 2
                    ;;
                *)
                    TARGET="$1"
                    shift
                    ;;
            esac
        done
        generate_tree "$DEPTH" "$TARGET"
        ;;
    
    mindmap)
        show_mindmap
        ;;
    
    organize)
        DRY_RUN=""
        while [[ $# -gt 0 ]]; do
            case $1 in
                --dry-run)
                    DRY_RUN="--dry-run"
                    shift
                    ;;
                *)
                    shift
                    ;;
            esac
        done
        organize_repository "$DRY_RUN"
        ;;
    
    analyze)
        analyze_structure
        ;;
    
    help|--help|-h)
        show_help
        ;;
    
    *)
        echo "Unknown command: $COMMAND"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac