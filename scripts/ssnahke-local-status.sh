#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# SSnaHke-Local Status and Integration Verification
# Quick verification that SSnaHke-Local is properly integrated

set -euo pipefail

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'

echo -e "${CYAN}🐍 SSnaHke-Local Integration Status${NC}"
echo -e "${CYAN}====================================${NC}"
echo

# Check installer
if [[ -f "scripts/ssnahke-local-installer.sh" ]]; then
    echo -e "${GREEN}✅ SSnaHke-Local Installer: Available${NC}"
    echo -e "   📁 Location: scripts/ssnahke-local-installer.sh"
    if [[ -x "scripts/ssnahke-local-installer.sh" ]]; then
        echo -e "   🔧 Executable: Yes"
    else
        echo -e "   🔧 Executable: No (run: chmod +x scripts/ssnahke-local-installer.sh)"
    fi
else
    echo -e "${YELLOW}❌ SSnaHke-Local Installer: Missing${NC}"
fi

echo

# Check demonstration script
if [[ -f "scripts/demonstrate-ssnahke-local.sh" ]]; then
    echo -e "${GREEN}✅ SSnaHke-Local Demo: Available${NC}"
    echo -e "   📁 Location: scripts/demonstrate-ssnahke-local.sh"
    if [[ -x "scripts/demonstrate-ssnahke-local.sh" ]]; then
        echo -e "   🔧 Executable: Yes"
    else
        echo -e "   🔧 Executable: No (run: chmod +x scripts/demonstrate-ssnahke-local.sh)"
    fi
else
    echo -e "${YELLOW}❌ SSnaHke-Local Demo: Missing${NC}"
fi

echo

# Check Living Code Environment integration
if [[ -f ".activate_living_environment" ]]; then
    echo -e "${GREEN}✅ Living Code Environment: Available${NC}"
    
    # Check for SSnaHke integration in the file
    if grep -q "SSNAHKE_LOCAL_ENABLED" ".activate_living_environment" 2>/dev/null; then
        echo -e "   🧬 SSnaHke Integration: ✅ Embedded"
    else
        echo -e "   🧬 SSnaHke Integration: ❌ Not embedded"
    fi
    
    if grep -q "ssnahke_scan()" ".activate_living_environment" 2>/dev/null; then
        echo -e "   🔧 SSnaHke Commands: ✅ Available"
    else
        echo -e "   🔧 SSnaHke Commands: ❌ Missing"
    fi
else
    echo -e "${YELLOW}❌ Living Code Environment: Missing${NC}"
fi

echo

# Check if already installed
SSNAHKE_SCANNER_PATH="$HOME/ssnahke_local.py"
SSNAHKE_LAUNCHER_PATH="$HOME/ssnahke_launcher.sh"

echo -e "${BLUE}🏠 Installation Status (in $HOME):${NC}"
if [[ -f "$SSNAHKE_SCANNER_PATH" ]]; then
    echo -e "${GREEN}✅ Scanner: $SSNAHKE_SCANNER_PATH${NC}"
else
    echo -e "${YELLOW}❌ Scanner: Not installed${NC}"
fi

if [[ -f "$SSNAHKE_LAUNCHER_PATH" ]]; then
    echo -e "${GREEN}✅ Launcher: $SSNAHKE_LAUNCHER_PATH${NC}"
else
    echo -e "${YELLOW}❌ Launcher: Not installed${NC}"
fi

echo

# Show quick commands
echo -e "${BLUE}🚀 Quick Commands:${NC}"
echo -e "   ${WHITE}./scripts/ssnahke-local-installer.sh${NC}     # Install SSnaHke-Local"
echo -e "   ${WHITE}./scripts/demonstrate-ssnahke-local.sh${NC}   # Show demonstration"
echo -e "   ${WHITE}source ./.activate_living_environment${NC}    # Activate living environment"
echo -e "   ${WHITE}ssnahke_status${NC}                           # Check status (after activation)"
echo -e "   ${WHITE}ssnahke_install${NC}                          # Install (after activation)"
echo -e "   ${WHITE}ssnahke_demo${NC}                             # Demo (after activation)"

echo

# Show integration benefits
echo -e "${CYAN}🌟 Integration Benefits:${NC}"
echo -e "   🧬 Living Code Environment integration"
echo -e "   🛡️  Hardened security system compatibility"
echo -e "   📊 Comprehensive local security scanning"
echo -e "   🐍 Automatic Python 3.9+ installation"
echo -e "   🔧 One-command setup and deployment"
echo -e "   📋 JSON security reports and analysis"

echo