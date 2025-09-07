#!/bin/bash
#
# Folder Structure Generator - Creates visual tree representations of directory structures
#
# This tool addresses the question: "Is there a plugin that will generate a tree out of my folder?"
#
# Features:
# - Generates ASCII tree structure of any directory
# - Creates mind map style visualizations
# - Supports various output formats
# - Follows GitHub repository best practices
#

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Default settings
MAX_DEPTH=4
SHOW_HIDDEN=false
OUTPUT_FORMAT="tree"
INCLUDE_SIZE=false
EXCLUDE_PATTERNS=".git node_modules dist build .gradle .cache"

show_help() {
    cat << EOF
${CYAN}Folder Structure Generator${NC}
===============================

Generate visual tree representations of directory structures.

${YELLOW}Usage:${NC}
    $0 [OPTIONS] [DIRECTORY]

${YELLOW}Options:${NC}
    -h, --help          Show this help message
    -d, --depth DEPTH   Maximum depth to traverse (default: $MAX_DEPTH)
    -a, --all           Include hidden files and directories
    -f, --format FORMAT Output format: tree, mindmap, json (default: $OUTPUT_FORMAT)
    -s, --size          Include file sizes
    -o, --output FILE   Save output to file
    -e, --exclude PATTERNS  Space-separated patterns to exclude (default: "$EXCLUDE_PATTERNS")
    --mindmap-only      Generate only the repository structure mind map

${YELLOW}Examples:${NC}
    $0                          # Generate tree for current directory
    $0 --depth 2 /path/to/dir   # Generate tree with max depth 2
    $0 --format mindmap         # Generate mind map format
    $0 --format json -o tree.json  # Save as JSON file
    $0 --mindmap-only           # Show GitHub best practices mind map

${YELLOW}Output Formats:${NC}
    tree     - ASCII tree structure (default)
    mindmap  - Mind map style representation  
    json     - JSON structure data
    markdown - Markdown formatted tree

This tool helps answer: "Is there a tool that creates a folder structure?"
EOF
}

generate_github_mindmap() {
    cat << 'EOF'
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
EOF
}

get_file_size() {
    local file="$1"
    if [[ "$INCLUDE_SIZE" == "true" && -f "$file" ]]; then
        local size=$(stat -c%s "$file" 2>/dev/null || stat -f%z "$file" 2>/dev/null || echo "0")
        echo " ($(numfmt --to=iec --suffix=B $size))"
    fi
}

should_exclude() {
    local item="$1"
    local basename=$(basename "$item")
    
    for pattern in $EXCLUDE_PATTERNS; do
        if [[ "$basename" == $pattern* ]] || [[ "$basename" == *$pattern ]]; then
            return 0  # Should exclude
        fi
    done
    return 1  # Should include
}

generate_tree() {
    local directory="${1:-.}"
    local prefix="${2:-}"
    local depth="${3:-0}"
    local is_last="${4:-true}"
    
    # Check depth limit
    if [[ $depth -gt $MAX_DEPTH ]]; then
        return
    fi
    
    # Check if directory exists and is readable
    if [[ ! -d "$directory" ]] || [[ ! -r "$directory" ]]; then
        echo "${prefix}${RED}[Permission Denied or Not Found]${NC}"
        return
    fi
    
    # Get directory contents
    local items=()
    while IFS= read -r -d '' item; do
        local basename=$(basename "$item")
        
        # Skip hidden files unless requested
        if [[ "$SHOW_HIDDEN" == "false" && "$basename" == .* ]]; then
            continue
        fi
        
        # Skip excluded patterns
        if should_exclude "$item"; then
            continue
        fi
        
        items+=("$item")
    done < <(find "$directory" -maxdepth 1 -print0 | sort -z)
    
    # Remove the directory itself from the list
    items=("${items[@]/$directory}")
    
    # Sort items: directories first, then files
    local dirs=()
    local files=()
    
    for item in "${items[@]}"; do
        if [[ -n "$item" ]]; then
            if [[ -d "$item" ]]; then
                dirs+=("$item")
            else
                files+=("$item")
            fi
        fi
    done
    
    # Combine sorted lists
    local all_items=("${dirs[@]}" "${files[@]}")
    local total=${#all_items[@]}
    
    # Generate tree for each item
    for i in "${!all_items[@]}"; do
        local item="${all_items[$i]}"
        local basename=$(basename "$item")
        local is_last_item=$((i == total - 1))
        
        # Determine tree characters
        local tree_char
        local new_prefix
        
        if [[ $is_last_item == 1 ]]; then
            tree_char="└── "
            new_prefix="${prefix}    "
        else
            tree_char="├── "
            new_prefix="${prefix}│   "
        fi
        
        # Display item with appropriate formatting
        if [[ -d "$item" ]]; then
            echo -e "${prefix}${tree_char}${BLUE}${basename}/${NC}"
            # Recursively process subdirectory
            generate_tree "$item" "$new_prefix" $((depth + 1)) $is_last_item
        else
            local size_info=""
            if [[ "$INCLUDE_SIZE" == "true" ]]; then
                size_info=$(get_file_size "$item")
            fi
            
            # Color code files by extension
            local color=""
            case "${basename##*.}" in
                md|txt|rst) color="$YELLOW" ;;
                sh|bash|zsh) color="$GREEN" ;;
                py|js|ts|java|kt) color="$CYAN" ;;
                json|yaml|yml|xml) color="$PURPLE" ;;
                png|jpg|jpeg|gif|svg) color="$RED" ;;
                *) color="$NC" ;;
            esac
            
            echo -e "${prefix}${tree_char}${color}${basename}${NC}${size_info}"
        fi
    done
}

generate_mindmap_format() {
    local directory="${1:-.}"
    local depth="${2:-0}"
    local indent=""
    
    # Create indentation
    for ((i=0; i<depth; i++)); do
        indent="  $indent"
    done
    
    # Check depth limit
    if [[ $depth -gt $MAX_DEPTH ]]; then
        return
    fi
    
    local basename=$(basename "$directory")
    if [[ $depth -eq 0 ]]; then
        echo "# Repository Structure Mind Map"
        echo ""
        echo "## Central Idea: $basename"
        echo ""
    fi
    
    # Get and sort directory contents
    local items=()
    while IFS= read -r -d '' item; do
        local basename=$(basename "$item")
        
        if [[ "$SHOW_HIDDEN" == "false" && "$basename" == .* ]]; then
            continue
        fi
        
        if should_exclude "$item"; then
            continue
        fi
        
        items+=("$item")
    done < <(find "$directory" -maxdepth 1 -print0 | sort -z)
    
    # Remove directory itself
    items=("${items[@]/$directory}")
    
    # Process items
    for item in "${items[@]}"; do
        if [[ -n "$item" ]]; then
            local basename=$(basename "$item")
            if [[ -d "$item" ]]; then
                echo "${indent}- **${basename}/**"
                generate_mindmap_format "$item" $((depth + 1))
            else
                echo "${indent}- ${basename}"
            fi
        fi
    done
}

generate_json_format() {
    local directory="${1:-.}"
    local depth="${2:-0}"
    
    # Check depth limit
    if [[ $depth -gt $MAX_DEPTH ]]; then
        echo "null"
        return
    fi
    
    local basename=$(basename "$directory")
    local first=true
    
    echo "{"
    echo "  \"name\": \"$basename\","
    echo "  \"type\": \"directory\","
    echo "  \"children\": ["
    
    # Get directory contents
    while IFS= read -r -d '' item; do
        local item_basename=$(basename "$item")
        
        if [[ "$SHOW_HIDDEN" == "false" && "$item_basename" == .* ]]; then
            continue
        fi
        
        if should_exclude "$item"; then
            continue
        fi
        
        if [[ "$item" == "$directory" ]]; then
            continue
        fi
        
        if [[ "$first" == "false" ]]; then
            echo ","
        fi
        first=false
        
        if [[ -d "$item" ]]; then
            echo -n "    "
            generate_json_format "$item" $((depth + 1))
        else
            local size=""
            if [[ "$INCLUDE_SIZE" == "true" ]]; then
                local file_size=$(stat -c%s "$item" 2>/dev/null || stat -f%z "$item" 2>/dev/null || echo "0")
                size=", \"size\": $file_size"
            fi
            echo "    {\"name\": \"$item_basename\", \"type\": \"file\"$size}"
        fi
    done < <(find "$directory" -maxdepth 1 -print0 | sort -z)
    
    echo ""
    echo "  ]"
    echo "}"
}

generate_markdown_format() {
    local directory="${1:-.}"
    
    echo "# Directory Structure: $(basename "$directory")"
    echo ""
    echo "\`\`\`"
    echo "$(basename "$directory")/"
    generate_tree "$directory" "" 0
    echo "\`\`\`"
    echo ""
    echo "*Generated on $(date) by Folder Structure Generator*"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -d|--depth)
            MAX_DEPTH="$2"
            shift 2
            ;;
        -a|--all)
            SHOW_HIDDEN=true
            shift
            ;;
        -f|--format)
            OUTPUT_FORMAT="$2"
            shift 2
            ;;
        -s|--size)
            INCLUDE_SIZE=true
            shift
            ;;
        -o|--output)
            OUTPUT_FILE="$2"
            shift 2
            ;;
        -e|--exclude)
            EXCLUDE_PATTERNS="$2"
            shift 2
            ;;
        --mindmap-only)
            generate_github_mindmap
            exit 0
            ;;
        -*)
            echo "Unknown option $1"
            show_help
            exit 1
            ;;
        *)
            TARGET_DIR="$1"
            shift
            ;;
    esac
done

# Set default target directory
TARGET_DIR="${TARGET_DIR:-.}"

# Validate target directory
if [[ ! -d "$TARGET_DIR" ]]; then
    echo -e "${RED}Error: '$TARGET_DIR' is not a directory${NC}" >&2
    exit 1
fi

# Generate output based on format
echo -e "${CYAN}Generating ${OUTPUT_FORMAT} structure for: ${TARGET_DIR}${NC}" >&2
echo "" >&2

case "$OUTPUT_FORMAT" in
    tree)
        echo "$(basename "$TARGET_DIR")/"
        generate_tree "$TARGET_DIR"
        ;;
    mindmap)
        generate_mindmap_format "$TARGET_DIR"
        ;;
    json)
        generate_json_format "$TARGET_DIR"
        ;;
    markdown)
        generate_markdown_format "$TARGET_DIR"
        ;;
    *)
        echo -e "${RED}Error: Unknown output format '$OUTPUT_FORMAT'${NC}" >&2
        echo "Available formats: tree, mindmap, json, markdown" >&2
        exit 1
        ;;
esac

# Save to file if specified
if [[ -n "$OUTPUT_FILE" ]]; then
    case "$OUTPUT_FORMAT" in
        tree)
            {
                echo "$(basename "$TARGET_DIR")/"
                generate_tree "$TARGET_DIR"
            } > "$OUTPUT_FILE"
            ;;
        mindmap)
            generate_mindmap_format "$TARGET_DIR" > "$OUTPUT_FILE"
            ;;
        json)
            generate_json_format "$TARGET_DIR" > "$OUTPUT_FILE"
            ;;
        markdown)
            generate_markdown_format "$TARGET_DIR" > "$OUTPUT_FILE"
            ;;
    esac
    echo -e "${GREEN}Output saved to: $OUTPUT_FILE${NC}" >&2
fi