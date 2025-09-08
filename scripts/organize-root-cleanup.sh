#!/bin/bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🧹 Repository Organization Cleanup${NC}"
echo -e "${BLUE}Following DevUtility agentic standards (@GDA @UFUIC-O @PIPI @LDU @EG @GATT @SWT)${NC}"
printf '=%.0s' {1..60}; echo

# Create organization log
mkdir -p logs
LOG_FILE="logs/organization_cleanup.jsonl"
echo "{\"ts\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",\"phase\":\"start\",\"action\":\"cleanup_root_directory\"}" >> "$LOG_FILE"

# Define organization rules based on existing structure
declare -A MOVE_RULES=(
    # Images and assets to docs/assets
    ["*.png"]="docs/assets/"
    ["*.jpg"]="docs/assets/"
    ["*.jpeg"]="docs/assets/"
    
    # Reports and documentation to docs/
    ["*REPORT*.md"]="docs/"
    ["*SUMMARY*.md"]="docs/"
    ["*DOCUMENTATION*.md"]="docs/"
    ["AUDIT_SUMMARY.md"]="docs/"
    ["PROTECTION_STATUS.md"]="docs/"
    ["SSNAHKE_LOCAL_DOCUMENTATION.md"]="docs/"
    
    # Enhanced/backup documentation to docs/legacy
    ["README_ENHANCED.md"]="docs/legacy/"
    ["*_ENHANCED.md"]="docs/legacy/"
    
    # Core agent Python files to ai/
    ["agent_core.py"]="ai/"
    
    # Utility/performance scripts to tools/
    ["*performance*.sh"]="tools/"
    ["*optimization*.sh"]="tools/"
    ["*omniscient*.sh"]="tools/"
    ["*bootstrap*.sh"]="tools/"
    ["*tunnel*.sh"]="tools/"
    ["demo-*.sh"]="tools/"
    
    # System/environment scripts to scripts/
    ["01_env_tell_all.sh"]="scripts/"
    ["02_space_init_and_sync.sh"]="scripts/"
    ["generate_profile_env.sh"]="scripts/"
    ["install_ca.sh"]="scripts/"
    
    # Build/deployment scripts to tools/
    ["move_apks_to_shared.sh"]="tools/"
    ["prepare_transfer.sh"]="tools/"
    ["extract_and_copy_apk.sh"]="tools/"
    ["direct-apk-assembly.sh"]="tools/"
    
    # Reports and JSON files to logs/ or docs/
    ["*.json"]="logs/"
    ["utf8_conversion_report.json"]="logs/"
    
    # Large binaries to archive/
    ["COMPREHENSIVE_AUDIT_REPORT.json"]="archive/"
    ["DevUl Army : Living Sriracha AGI"]="archive/"
)

# Function to move file with logging
move_file() {
    local src="$1"
    local dest_dir="$2"
    
    if [[ ! -f "$src" ]]; then
        return 0
    fi
    
    echo -e "${YELLOW}Moving:${NC} $src -> $dest_dir"
    
    # Create destination directory
    mkdir -p "$dest_dir"
    
    # Move the file
    mv "$src" "$dest_dir"
    
    # Log the move
    echo "{\"ts\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",\"action\":\"move\",\"src\":\"$src\",\"dest\":\"$dest_dir\",\"type\":\"cleanup\"}" >> "$LOG_FILE"
    
    return 0
}

# Process each rule
for pattern in "${!MOVE_RULES[@]}"; do
    dest_dir="${MOVE_RULES[$pattern]}"
    
    # Handle exact matches first
    if [[ "$pattern" != *"*"* ]] && [[ -f "$pattern" ]]; then
        move_file "$pattern" "$dest_dir"
        continue
    fi
    
    # Handle glob patterns
    if [[ "$pattern" == *"*"* ]]; then
        while IFS= read -r -d '' file; do
            if [[ -f "$file" ]]; then
                move_file "$file" "$dest_dir"
            fi
        done < <(find . -maxdepth 1 -name "$pattern" -type f -print0 2>/dev/null)
    fi
done

# Move remaining shell scripts that match specific patterns
echo -e "${BLUE}Moving remaining utility scripts...${NC}"
for script in *.sh; do
    [[ ! -f "$script" ]] && continue
    
    case "$script" in
        *proot*|*inspector*|*diagnostics*|*audit*)
            move_file "$script" "tools/"
            ;;
        *knows*|*map*|*parse*|*find*)
            move_file "$script" "tools/"
            ;;
        *github*|*git*|*push*|*merge*)
            move_file "$script" "scripts/"
            ;;
        *cleanup*|*space*|*free*)
            move_file "$script" "scripts/"
            ;;
        *problem*|*execute*|*poller*)
            move_file "$script" "tools/"
            ;;
        *agentics*|*agent*)
            move_file "$script" "ai/"
            ;;
        SCRIPTTHESE*|DEMONSTRATE*)
            move_file "$script" "tools/"
            ;;
        *)
            # Keep essential build/config scripts in root
            if [[ ! "$script" =~ ^(gradlew|build|setup|validate).*\.sh$ ]]; then
                move_file "$script" "tools/"
            fi
            ;;
    esac
done

# Clean up any remaining loose files that should be organized
echo -e "${BLUE}Moving remaining miscellaneous files...${NC}"
for file in CODEREAVER-* HARDENED_* IMPLEMENTATION_* LICENSE_ENHANCED; do
    [[ -f "$file" ]] && move_file "$file" "docs/legacy/"
done

# Move any remaining .txt files to docs/legacy
for txt_file in *.txt; do
    [[ -f "$txt_file" ]] && move_file "$txt_file" "docs/legacy/"
done

# Final log entry
echo "{\"ts\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",\"phase\":\"complete\",\"action\":\"cleanup_root_directory\"}" >> "$LOG_FILE"

echo ""
echo -e "${GREEN}✅ Repository cleanup complete!${NC}"
echo ""

# Show final root directory state
echo -e "${BLUE}📁 Root directory after cleanup:${NC}"
ls -la | head -20

echo ""
echo -e "${BLUE}📊 Organization statistics:${NC}"
root_files=$(find . -maxdepth 1 -type f | wc -l)
echo "  • Root directory files: $root_files"
echo "  • Organization log: $LOG_FILE"

if [[ $root_files -gt 30 ]]; then
    echo -e "${YELLOW}⚠️  Still $root_files files in root - consider further organization${NC}"
else
    echo -e "${GREEN}✅ Root directory is well-organized${NC}"
fi