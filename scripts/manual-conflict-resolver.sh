#!/bin/bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi


# Manual Conflict Resolution Helper Script
# Provides guided assistance for resolving merge conflicts

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Function to show help
show_help() {
    cat << EOF
Manual Conflict Resolution Helper

USAGE:
    $0 [OPTIONS]

OPTIONS:
    -s, --source BRANCH     Source branch name (required)
    -t, --target BRANCH     Target branch name (default: main)
    -i, --interactive       Interactive mode with guided resolution
    -l, --list-conflicts    List all conflicted files
    -r, --resolve-file FILE Resolve specific file
    -v, --validate          Validate resolution before commit
    -h, --help              Show this help message

EXAMPLES:
    $0 -s feature-branch -t main -i
    $0 --source copilot/fix-6 --target main --list-conflicts
    $0 --resolve-file build.gradle --validate

WORKFLOW:
    1. Checkout your feature branch
    2. Run: git merge origin/main
    3. If conflicts occur, run this script with -i flag
    4. Follow the interactive prompts
    5. Validate and commit the resolution

EOF
}

# Function to list conflicted files
list_conflicts() {
    print_step "Checking for merge conflicts..."
    
    if ! git diff --name-only --diff-filter=U 2>/dev/null; then
        print_status "No active merge conflicts found."
        
        # Check if we can detect potential conflicts
        if [ -n "$SOURCE_BRANCH" ] && [ -n "$TARGET_BRANCH" ]; then
            print_step "Checking potential conflicts between $SOURCE_BRANCH and $TARGET_BRANCH..."
            git merge-tree "origin/$TARGET_BRANCH" "origin/$SOURCE_BRANCH" 2>/dev/null | grep -E "^\+\+\+|^---" | sort -u || true
        fi
        return 0
    fi
    
    print_warning "Found conflicted files:"
    git diff --name-only --diff-filter=U | while read -r file; do
        echo "  ❌ $file"
        
        # Show conflict markers count
        if [ -f "$file" ]; then
            conflicts=$(grep -c "<<<<<<< " "$file" 2>/dev/null || echo "0")
            echo "     └─ $conflicts conflict markers"
        fi
    done
}

# Function to resolve a specific file interactively
resolve_file() {
    local file="$1"
    
    if [ ! -f "$file" ]; then
        print_error "File '$file' not found"
        return 1
    fi
    
    if ! grep -q "<<<<<<< " "$file"; then
        print_status "No conflicts found in '$file'"
        return 0
    fi
    
    print_step "Resolving conflicts in: $file"
    
    # Show file type and suggest resolution strategy
    case "$file" in
        *.gradle)
            print_warning "Gradle build file detected. Strategy: Merge dependencies and configurations"
            ;;
        .gitignore)
            print_warning ".gitignore file detected. Strategy: Merge unique entries"
            ;;
        *.md)
            print_warning "Markdown file detected. Strategy: Combine documentation sections"
            ;;
        *.kt|*.java|*.py|*.js)
            print_warning "Source code file detected. Strategy: Manual review recommended"
            ;;
        *)
            print_warning "Generic file. Strategy: Choose appropriate version"
            ;;
    esac
    
    # Count conflicts
    conflicts=$(grep -c "<<<<<<< " "$file")
    print_status "Found $conflicts conflict(s) in $file"
    
    # Offer resolution options
    echo ""
    echo "Resolution options:"
    echo "  1) Use automated resolution (if supported)"
    echo "  2) Accept all incoming changes (theirs)"
    echo "  3) Accept all current changes (ours)"
    echo "  4) Open in merge tool (if available)"
    echo "  5) Manual edit"
    echo "  6) Skip this file"
    
    read -p "Choose option (1-6): " choice
    
    case $choice in
        1)
            print_step "Attempting automated resolution..."
            if python3 scripts/conflict-resolver.py --resolve --source "$SOURCE_BRANCH" --target "$TARGET_BRANCH" 2>/dev/null; then
                print_status "✅ Automated resolution successful"
            else
                print_error "Automated resolution failed, try another option"
                resolve_file "$file"
            fi
            ;;
        2)
            print_step "Accepting incoming changes..."
            git checkout --theirs "$file"
            git add "$file"
            print_status "✅ Accepted incoming changes for $file"
            ;;
        3)
            print_step "Accepting current changes..."
            git checkout --ours "$file"
            git add "$file"
            print_status "✅ Accepted current changes for $file"
            ;;
        4)
            print_step "Opening merge tool..."
            if command -v git-mergetool &> /dev/null; then
                git mergetool "$file"
            else
                print_error "No merge tool configured. Set up with: git config merge.tool <tool>"
                resolve_file "$file"
            fi
            ;;
        5)
            print_step "Opening for manual edit..."
            "${EDITOR:-nano}" "$file"
            
            # Verify resolution
            if grep -q "<<<<<<< " "$file"; then
                print_warning "Conflict markers still present. Resolution incomplete."
                read -p "Continue anyway? (y/N): " continue_choice
                if [[ ! $continue_choice =~ ^[Yy]$ ]]; then
                    resolve_file "$file"
                    return
                fi
            fi
            
            git add "$file"
            print_status "✅ Manual resolution completed for $file"
            ;;
        6)
            print_warning "Skipping $file (conflicts remain)"
            return 0
            ;;
        *)
            print_error "Invalid option. Please choose 1-6."
            resolve_file "$file"
            ;;
    esac
}

# Interactive conflict resolution mode
interactive_mode() {
    print_step "Starting interactive conflict resolution..."
    
    # Check if we're in a merge state
    if [ ! -f .git/MERGE_HEAD ]; then
        print_warning "No active merge detected. Attempting to merge $SOURCE_BRANCH with $TARGET_BRANCH..."
        
        if [ -z "$SOURCE_BRANCH" ] || [ -z "$TARGET_BRANCH" ]; then
            print_error "Source and target branches must be specified"
            exit 1
        fi
        
        # Attempt merge
        if ! git merge "origin/$TARGET_BRANCH" --no-commit; then
            print_step "Merge conflicts detected, starting resolution..."
        else
            print_status "Merge completed without conflicts!"
            git commit -m "Merge branch '$TARGET_BRANCH' into $SOURCE_BRANCH"
            return 0
        fi
    fi
    
    # Get list of conflicted files
    conflicted_files=$(git diff --name-only --diff-filter=U)
    
    if [ -z "$conflicted_files" ]; then
        print_status "No conflicts found!"
        return 0
    fi
    
    print_step "Found conflicted files:"
    echo "$conflicted_files" | while read -r file; do
        echo "  ❌ $file"
    done
    
    echo ""
    echo "Resolution modes:"
    echo "  1) Resolve all files interactively"
    echo "  2) Use automatic resolution where possible"
    echo "  3) Resolve specific file"
    echo "  4) Show detailed conflict analysis"
    
    read -p "Choose mode (1-4): " mode
    
    case $mode in
        1)
            echo "$conflicted_files" | while read -r file; do
                resolve_file "$file"
            done
            ;;
        2)
            print_step "Attempting automatic resolution..."
            python3 scripts/conflict-resolver.py --resolve --source "$SOURCE_BRANCH" --target "$TARGET_BRANCH"
            ;;
        3)
            echo "Available files:"
            echo "$conflicted_files" | nl
            read -p "Enter file number: " file_num
            file=$(echo "$conflicted_files" | sed -n "${file_num}p")
            if [ -n "$file" ]; then
                resolve_file "$file"
            else
                print_error "Invalid file number"
            fi
            ;;
        4)
            print_step "Detailed conflict analysis:"
            echo "$conflicted_files" | while read -r file; do
                echo ""
                echo "=== $file ==="
                conflicts=$(grep -c "<<<<<<< " "$file" 2>/dev/null || echo "0")
                echo "Conflict markers: $conflicts"
                
                # Show context around first conflict
                if [ "$conflicts" -gt 0 ]; then
                    echo "First conflict:"
                    grep -n -A5 -B5 "<<<<<<< " "$file" | head -20
                fi
            done
            ;;
        *)
            print_error "Invalid mode"
            interactive_mode
            ;;
    esac
}

# Function to validate resolution
validate_resolution() {
    print_step "Validating conflict resolution..."
    
    # Check for remaining conflict markers
    if git grep "<<<<<<< \|>>>>>>> \|=======" -- . 2>/dev/null; then
        print_error "Conflict markers still found in repository!"
        return 1
    fi
    
    # Check if all conflicts are resolved
    if git diff --name-only --diff-filter=U | grep -q .; then
        print_error "Unresolved conflicts still exist!"
        git diff --name-only --diff-filter=U
        return 1
    fi
    
    # Try to compile/build if build files exist
    if [ -f "build.gradle" ]; then
        print_step "Validating Gradle build..."
        if command -v ./gradlew &> /dev/null; then
            if ! ./gradlew build --dry-run 2>/dev/null; then
                print_warning "Gradle build validation failed (non-critical)"
            else
                print_status "✅ Gradle build validation passed"
            fi
        fi
    fi
    
    print_status "✅ Conflict resolution validation passed!"
    return 0
}

# Parse command line arguments
SOURCE_BRANCH=""
TARGET_BRANCH="main"
INTERACTIVE=false
LIST_CONFLICTS=false
RESOLVE_FILE=""
VALIDATE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -s|--source)
            SOURCE_BRANCH="$2"
            shift 2
            ;;
        -t|--target)
            TARGET_BRANCH="$2"
            shift 2
            ;;
        -i|--interactive)
            INTERACTIVE=true
            shift
            ;;
        -l|--list-conflicts)
            LIST_CONFLICTS=true
            shift
            ;;
        -r|--resolve-file)
            RESOLVE_FILE="$2"
            shift 2
            ;;
        -v|--validate)
            VALIDATE=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Main execution logic
main() {
    print_status "Manual Conflict Resolution Helper"
    print_status "Repository: $(pwd)"
    print_status "Current branch: $(git branch --show-current)"
    
    if [ "$LIST_CONFLICTS" = true ]; then
        list_conflicts
    elif [ -n "$RESOLVE_FILE" ]; then
        resolve_file "$RESOLVE_FILE"
    elif [ "$INTERACTIVE" = true ]; then
        interactive_mode
    elif [ "$VALIDATE" = true ]; then
        validate_resolution
    else
        print_error "No action specified. Use --help for usage information."
        exit 1
    fi
    
    if [ "$VALIDATE" = true ] || [ "$INTERACTIVE" = true ]; then
        validate_resolution
    fi
}

# Run main function
main