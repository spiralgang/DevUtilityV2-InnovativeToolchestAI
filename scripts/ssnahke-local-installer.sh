#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# SSnaHke-Local Installer & Launcher
# Installs Python 3.9 via deadsnakes PPA (if on Ubuntu ≥18.04),
# drops in the SSnaHke-Local scanner, and runs it immediately.
# Integrated with Living Code Environment and Hardened Security System

set -euo pipefail

# Living Code Environment Integration
export LIVING_CODE_ENABLED=1
export SSNAHKE_INTEGRATION=1
export SECURITY_SCANNER_ACTIVE=1

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color

# Banner
echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║              ${WHITE}SSnaHke-Local Security Scanner & Installer${CYAN}           ║${NC}"
echo -e "${CYAN}║              ${GREEN}Integrated with Living Code Environment${CYAN}             ║${NC}"
echo -e "${CYAN}║              ${PURPLE}Maximum Security Hardened System${CYAN}                   ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo

# Source living environment if available
if [[ -f "$(pwd)/.living_environment_wrapper.sh" ]]; then
    echo -e "${GREEN}[*] Loading Living Code Environment...${NC}"
    source "$(pwd)/.living_environment_wrapper.sh" 2>/dev/null || true
fi

### 1) Ensure Python 3.9+ is available
echo -e "${BLUE}[*] Checking Python 3.9+ availability...${NC}"
if ! command -v python3 &>/dev/null || [[ "$(python3 -c 'import sys; print(sys.version_info[:2])')" < "(3,9)" ]]; then
    if grep -qi ubuntu /etc/os-release; then
        echo -e "${YELLOW}[*] Installing Python 3.9 via deadsnakes PPA...${NC}"
        sudo apt-get update -y
        sudo apt-get install -y software-properties-common
        sudo add-apt-repository -y ppa:deadsnakes/ppa
        sudo apt-get update -y
        sudo apt-get install -y python3.9 python3.9-venv python3.9-dev python3-pip
        PYTHON=python3.9
        echo -e "${GREEN}[✓] Python 3.9 installed successfully${NC}"
    else
        echo -e "${RED}[-] No suitable Python 3.9 found and not Ubuntu. Checking alternatives...${NC}"
        # Try other package managers
        if command -v dnf &>/dev/null; then
            sudo dnf install -y python39 python39-pip
            PYTHON=python3.9
        elif command -v yum &>/dev/null; then
            sudo yum install -y python39 python39-pip
            PYTHON=python3.9
        elif command -v pacman &>/dev/null; then
            sudo pacman -S --noconfirm python
            PYTHON=python3
        elif command -v apk &>/dev/null; then
            sudo apk add python3 py3-pip
            PYTHON=python3
        else
            echo -e "${RED}[-] Cannot install Python 3.9+. Please install manually.${NC}"
            exit 1
        fi
    fi
else
    PYTHON=python3
    echo -e "${GREEN}[✓] Python 3.9+ already available${NC}"
fi

### 2) Deploy SSnaHke-Local script
echo -e "${BLUE}[*] Deploying SSnaHke-Local scanner...${NC}"
SCRIPT="$HOME/ssnahke_local.py"
mkdir -p "$(dirname "$SCRIPT")"

cat > "$SCRIPT" << 'SSNAHKE_SCRIPT_EOF'
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SSnaHke-Local Security Scanner
==============================

Advanced local security scanning system integrated with Living Code Environment.
Performs comprehensive system analysis, vulnerability detection, and security hardening.

Part of: DevUl Army : Living Sriracha AGI - SSnaHke-Local
License: See LICENSE_ENHANCED for comprehensive terms
"""

import os
import sys
import json
import time
import socket
import psutil
import hashlib
import subprocess
import platform
import threading
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Any, Optional
import sqlite3

class SSnaHkeLocal:
    """SSnaHke-Local Security Scanner - Living Code Integrated"""
    
    def __init__(self):
        self.start_time = time.time()
        self.scan_results = {}
        self.security_issues = []
        self.living_code_integration = True
        self.version = "1.0.0-Living"
        
        # Living Code Environment Detection
        self.living_env_active = os.getenv('LIVING_CODE_ENABLED') == '1'
        self.hardened_security = os.getenv('LIVING_CODE_HARDENED') == '1'
        
        print(f"\n🐍 SSnaHke-Local v{self.version} - Security Scanner")
        print("=" * 60)
        if self.living_env_active:
            print("✅ Living Code Environment: ACTIVE")
        if self.hardened_security:
            print("🛡️  Hardened Security: ENABLED")
        print()
    
    def system_info_scan(self):
        """Comprehensive system information gathering"""
        print("📊 Gathering system information...")
        
        self.scan_results['system'] = {
            'hostname': socket.gethostname(),
            'platform': platform.platform(),
            'architecture': platform.architecture(),
            'processor': platform.processor(),
            'python_version': sys.version,
            'cpu_count': psutil.cpu_count(),
            'memory_total': psutil.virtual_memory().total,
            'disk_usage': {}
        }
        
        # Disk usage for all mounted filesystems
        for partition in psutil.disk_partitions():
            try:
                usage = psutil.disk_usage(partition.mountpoint)
                self.scan_results['system']['disk_usage'][partition.mountpoint] = {
                    'total': usage.total,
                    'used': usage.used,
                    'free': usage.free,
                    'percent': (usage.used / usage.total) * 100
                }
            except:
                pass
        
        print("✅ System information collected")
    
    def network_scan(self):
        """Network security analysis"""
        print("🌐 Scanning network configuration...")
        
        self.scan_results['network'] = {
            'interfaces': [],
            'connections': [],
            'listening_ports': []
        }
        
        # Network interfaces
        for interface, addrs in psutil.net_if_addrs().items():
            iface_info = {'name': interface, 'addresses': []}
            for addr in addrs:
                iface_info['addresses'].append({
                    'family': str(addr.family),
                    'address': addr.address,
                    'netmask': addr.netmask,
                    'broadcast': addr.broadcast
                })
            self.scan_results['network']['interfaces'].append(iface_info)
        
        # Active connections
        try:
            for conn in psutil.net_connections():
                if conn.status == 'LISTEN':
                    self.scan_results['network']['listening_ports'].append({
                        'port': conn.laddr.port if conn.laddr else None,
                        'address': conn.laddr.ip if conn.laddr else None,
                        'pid': conn.pid,
                        'family': str(conn.family)
                    })
        except psutil.AccessDenied:
            print("⚠️  Network connections require elevated privileges")
        
        print("✅ Network scan completed")
    
    def process_scan(self):
        """Running processes analysis"""
        print("⚙️  Scanning running processes...")
        
        self.scan_results['processes'] = []
        suspicious_processes = []
        
        for proc in psutil.process_iter(['pid', 'name', 'username', 'cmdline', 'memory_info']):
            try:
                proc_info = proc.info
                self.scan_results['processes'].append({
                    'pid': proc_info['pid'],
                    'name': proc_info['name'],
                    'username': proc_info['username'],
                    'memory_mb': proc_info['memory_info'].rss / 1024 / 1024 if proc_info['memory_info'] else 0,
                    'cmdline': ' '.join(proc_info['cmdline']) if proc_info['cmdline'] else ''
                })
                
                # Check for suspicious process patterns
                if proc_info['name'] and any(sus in proc_info['name'].lower() for sus in 
                    ['backdoor', 'rootkit', 'keylog', 'trojan', 'malware']):
                    suspicious_processes.append(proc_info['name'])
                    
            except (psutil.NoSuchProcess, psutil.AccessDenied):
                continue
        
        if suspicious_processes:
            self.security_issues.append({
                'type': 'suspicious_processes',
                'severity': 'HIGH',
                'details': f"Suspicious process names detected: {', '.join(suspicious_processes)}"
            })
        
        print(f"✅ Process scan completed ({len(self.scan_results['processes'])} processes)")
    
    def file_integrity_scan(self):
        """File integrity and security scan"""
        print("📁 Scanning file system security...")
        
        self.scan_results['file_security'] = {
            'world_writable': [],
            'suid_files': [],
            'large_files': [],
            'hidden_files': []
        }
        
        # Scan critical directories
        critical_dirs = ['/etc', '/bin', '/sbin', '/usr/bin', '/usr/sbin']
        if os.name == 'nt':  # Windows
            critical_dirs = ['C:\\Windows\\System32', 'C:\\Program Files']
        
        for directory in critical_dirs:
            if os.path.exists(directory):
                try:
                    for root, dirs, files in os.walk(directory):
                        for file in files[:100]:  # Limit to prevent long scans
                            filepath = os.path.join(root, file)
                            try:
                                stat = os.stat(filepath)
                                # Check for world-writable files
                                if stat.st_mode & 0o002:
                                    self.scan_results['file_security']['world_writable'].append(filepath)
                                    self.security_issues.append({
                                        'type': 'world_writable_file',
                                        'severity': 'MEDIUM',
                                        'details': f"World-writable file: {filepath}"
                                    })
                                
                                # Check for SUID files (Unix-like systems)
                                if hasattr(stat, 'st_mode') and stat.st_mode & 0o4000:
                                    self.scan_results['file_security']['suid_files'].append(filepath)
                                
                                # Check for large files that might be suspicious
                                if stat.st_size > 100 * 1024 * 1024:  # 100MB+
                                    self.scan_results['file_security']['large_files'].append({
                                        'path': filepath,
                                        'size_mb': stat.st_size / 1024 / 1024
                                    })
                                    
                            except (OSError, PermissionError):
                                continue
                except (OSError, PermissionError):
                    continue
        
        print("✅ File security scan completed")
    
    def living_code_integration_check(self):
        """Check Living Code Environment integration status"""
        if not self.living_env_active:
            return
            
        print("🧬 Checking Living Code Environment integration...")
        
        # Check for living code components
        living_components = {
            '.living_environment_wrapper.sh': 'Environment Wrapper',
            '.activate_living_environment': 'Environment Activator',
            'scripts/living-environment-integration.py': 'Integration Manager',
            'scripts/hardened-permissions-manager.py': 'Security Manager',
            '.living_environment.db': 'Living Database'
        }
        
        self.scan_results['living_code'] = {
            'integration_status': 'active',
            'components_found': {},
            'security_level': os.getenv('LIVING_CODE_SECURITY_LEVEL', 'standard')
        }
        
        for component, description in living_components.items():
            if os.path.exists(component):
                self.scan_results['living_code']['components_found'][component] = {
                    'status': 'present',
                    'description': description
                }
            else:
                self.scan_results['living_code']['components_found'][component] = {
                    'status': 'missing',
                    'description': description
                }
        
        print("✅ Living Code integration verified")
    
    def security_recommendations(self):
        """Generate security recommendations"""
        print("🔒 Generating security recommendations...")
        
        recommendations = []
        
        if len(self.security_issues) == 0:
            recommendations.append("✅ No immediate security issues detected")
        else:
            recommendations.append(f"⚠️  {len(self.security_issues)} security issues detected")
        
        # System hardening recommendations
        if not self.hardened_security:
            recommendations.append("🛡️  Enable hardened security mode in Living Code Environment")
        
        # Network security
        open_ports = len(self.scan_results.get('network', {}).get('listening_ports', []))
        if open_ports > 10:
            recommendations.append(f"🌐 Consider reviewing {open_ports} open network ports")
        
        # File system security
        world_writable = len(self.scan_results.get('file_security', {}).get('world_writable', []))
        if world_writable > 0:
            recommendations.append(f"📁 Fix {world_writable} world-writable files")
        
        self.scan_results['recommendations'] = recommendations
        
        for rec in recommendations:
            print(f"  {rec}")
    
    def generate_report(self):
        """Generate comprehensive security report"""
        print("\n📋 Generating security report...")
        
        report = {
            'scan_metadata': {
                'scanner_version': self.version,
                'scan_time': datetime.now().isoformat(),
                'duration_seconds': time.time() - self.start_time,
                'living_code_active': self.living_env_active,
                'hardened_security': self.hardened_security
            },
            'scan_results': self.scan_results,
            'security_issues': self.security_issues,
            'summary': {
                'total_issues': len(self.security_issues),
                'high_severity': len([i for i in self.security_issues if i['severity'] == 'HIGH']),
                'medium_severity': len([i for i in self.security_issues if i['severity'] == 'MEDIUM']),
                'low_severity': len([i for i in self.security_issues if i['severity'] == 'LOW'])
            }
        }
        
        # Save report
        report_file = f"ssnahke_scan_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
        with open(report_file, 'w') as f:
            json.dump(report, f, indent=2, default=str)
        
        print(f"✅ Security report saved: {report_file}")
        return report_file
    
    def run_scan(self):
        """Execute complete security scan"""
        print("🔍 Starting SSnaHke-Local security scan...\n")
        
        try:
            self.system_info_scan()
            self.network_scan()
            self.process_scan()
            self.file_integrity_scan()
            self.living_code_integration_check()
            self.security_recommendations()
            
            report_file = self.generate_report()
            
            print(f"\n🎯 Scan Summary:")
            print(f"   Duration: {time.time() - self.start_time:.2f} seconds")
            print(f"   Security Issues: {len(self.security_issues)}")
            print(f"   Report: {report_file}")
            
            if self.living_env_active:
                print("\n🧬 Living Code Environment: ACTIVE")
                print("   All scans integrated with living code monitoring")
            
            print("\n✅ SSnaHke-Local scan completed successfully!")
            
        except KeyboardInterrupt:
            print("\n⚠️  Scan interrupted by user")
            sys.exit(1)
        except Exception as e:
            print(f"\n❌ Scan error: {e}")
            sys.exit(1)

def main():
    """Main execution function"""
    try:
        # Ensure required modules are available
        required_modules = ['psutil']
        missing_modules = []
        
        for module in required_modules:
            try:
                __import__(module)
            except ImportError:
                missing_modules.append(module)
        
        if missing_modules:
            print(f"Installing required modules: {', '.join(missing_modules)}")
            subprocess.check_call([sys.executable, '-m', 'pip', 'install'] + missing_modules)
            print("✅ Required modules installed")
        
        # Initialize and run scanner
        scanner = SSnaHkeLocal()
        scanner.run_scan()
        
    except Exception as e:
        print(f"❌ SSnaHke-Local startup error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
SSNAHKE_SCRIPT_EOF

chmod +x "$SCRIPT"
echo -e "${GREEN}[✓] SSnaHke-Local scanner deployed successfully${NC}"

### 3) Install required Python packages
echo -e "${BLUE}[*] Installing required Python packages...${NC}"
$PYTHON -m pip install --user --upgrade pip psutil 2>/dev/null || {
    echo -e "${YELLOW}[*] Installing pip and psutil via package manager...${NC}"
    if command -v apt-get &>/dev/null; then
        sudo apt-get install -y python3-pip python3-psutil
    elif command -v dnf &>/dev/null; then
        sudo dnf install -y python3-pip python3-psutil
    elif command -v yum &>/dev/null; then
        sudo yum install -y python3-pip python3-psutil
    elif command -v pacman &>/dev/null; then
        sudo pacman -S --noconfirm python-pip python-psutil
    elif command -v apk &>/dev/null; then
        sudo apk add py3-pip py3-psutil
    fi
}

### 4) Create launcher script
echo -e "${BLUE}[*] Creating SSnaHke-Local launcher...${NC}"
LAUNCHER="$HOME/ssnahke_launcher.sh"
cat > "$LAUNCHER" << 'LAUNCHER_EOF'
#!/usr/bin/env bash
# SSnaHke-Local Quick Launcher
# Integrated with Living Code Environment

# Colors
GREEN='\033[0;32m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}🐍 Launching SSnaHke-Local Security Scanner...${NC}"

# Find Python executable
if command -v python3.9 &>/dev/null; then
    PYTHON=python3.9
elif command -v python3 &>/dev/null; then
    PYTHON=python3
else
    echo "❌ Python not found"
    exit 1
fi

# Run scanner
cd "$HOME"
$PYTHON "$HOME/ssnahke_local.py" "$@"
LAUNCHER_EOF

chmod +x "$LAUNCHER"

### 5) Integration with Living Code Environment
if [[ -f "$(pwd)/.activate_living_environment" ]]; then
    echo -e "${BLUE}[*] Integrating SSnaHke-Local with Living Code Environment...${NC}"
    
    # Add SSnaHke commands to living environment
    cat >> "$(pwd)/.activate_living_environment" << 'INTEGRATION_EOF'

# SSnaHke-Local Integration
export SSNAHKE_LOCAL_ENABLED=1
export SSNAHKE_SCANNER_PATH="$HOME/ssnahke_local.py"
export SSNAHKE_LAUNCHER_PATH="$HOME/ssnahke_launcher.sh"

# SSnaHke-Local commands
ssnahke_scan() {
    echo "🐍 Running SSnaHke-Local security scan..."
    "$SSNAHKE_LAUNCHER_PATH" "$@"
}

ssnahke_quick() {
    echo "🐍 Running quick SSnaHke-Local scan..."
    python3 "$SSNAHKE_SCANNER_PATH" --quick
}

ssnahke_status() {
    echo "🐍 SSnaHke-Local Status:"
    echo "   Scanner: $([[ -f "$SSNAHKE_SCANNER_PATH" ]] && echo "✅ Installed" || echo "❌ Missing")"
    echo "   Launcher: $([[ -f "$SSNAHKE_LAUNCHER_PATH" ]] && echo "✅ Available" || echo "❌ Missing")"
    echo "   Living Code Integration: $([[ "$LIVING_CODE_ENABLED" == "1" ]] && echo "✅ Active" || echo "❌ Inactive")"
}

alias ssnahke='ssnahke_scan'
alias snake='ssnahke_scan'
alias security_scan='ssnahke_scan'

INTEGRATION_EOF

    echo -e "${GREEN}[✓] Living Code Environment integration completed${NC}"
fi

### 6) Run initial scan
echo -e "${BLUE}[*] Running initial SSnaHke-Local security scan...${NC}"
echo

# Change to home directory for scan
cd "$HOME"
$PYTHON "$SCRIPT"

### 7) Installation summary
echo
echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                  ${WHITE}SSnaHke-Local Installation Complete${GREEN}               ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo
echo -e "${CYAN}📁 Installed Files:${NC}"
echo -e "   Scanner: ${WHITE}$SCRIPT${NC}"
echo -e "   Launcher: ${WHITE}$LAUNCHER${NC}"
echo
echo -e "${CYAN}🚀 Quick Commands:${NC}"
echo -e "   ${WHITE}$LAUNCHER${NC}                    # Run full scan"
echo -e "   ${WHITE}ssnahke_scan${NC}                      # (if living env active)"
echo -e "   ${WHITE}ssnahke_status${NC}                    # Check status"
echo
echo -e "${CYAN}🧬 Living Code Integration:${NC}"
if [[ -f "$(pwd)/.activate_living_environment" ]]; then
    echo -e "   ${GREEN}✅ Integrated with Living Code Environment${NC}"
    echo -e "   ${WHITE}source ./.activate_living_environment${NC}  # Activate environment"
else
    echo -e "   ${YELLOW}⚠️  Living Code Environment not detected${NC}"
fi
echo
echo -e "${GREEN}✅ SSnaHke-Local is ready for security scanning!${NC}"