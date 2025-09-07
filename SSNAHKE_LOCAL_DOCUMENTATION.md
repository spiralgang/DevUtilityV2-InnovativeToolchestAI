# SSnaHke-Local Security Scanner System

## 🐍 Overview

SSnaHke-Local is a comprehensive local security scanning system integrated with the Living Code Environment and hardened security infrastructure. It provides automated vulnerability detection, system monitoring, and security analysis with zero performance overhead.

## 🚀 Key Features

### Core Security Scanning
- **System Information Gathering**: Complete hardware and software analysis
- **Network Security Analysis**: Interface enumeration and connection monitoring  
- **Process Security Monitoring**: Suspicious activity detection and analysis
- **File Integrity Scanning**: Permission analysis and vulnerability detection
- **Living Code Integration**: Environment-aware security validation

### Installation & Deployment
- **Automatic Python 3.9+ Installation**: Multi-distro support via deadsnakes PPA
- **One-Command Setup**: Complete installation and initial scan
- **Cross-Platform Compatibility**: Ubuntu, Fedora, Arch, Alpine Linux support
- **Dependency Management**: Automatic package installation and verification

### Integration Benefits
- **Living Code Environment**: Seamless integration with existing infrastructure
- **Hardened Security Mode**: Compatible with maximum security configurations
- **Container Support**: TOYBOX, CHISEL, and Android container integration
- **Zero Overhead Operation**: Environment-level operation without performance impact

## 📋 Installation

### Quick Installation
```bash
# Install and run initial scan
./scripts/ssnahke-local-installer.sh
```

### Living Code Environment Integration
```bash
# Activate living environment
source ./.activate_living_environment

# Install SSnaHke-Local
ssnahke_install

# Check status
ssnahke_status
```

## 🔧 Usage

### Direct Commands
```bash
# Run security scan
~/ssnahke_launcher.sh

# Python direct execution
python3 ~/ssnahke_local.py
```

### Living Code Environment Commands
```bash
# After activating living environment
ssnahke_scan           # Run complete security scan
ssnahke_status         # Check installation status
ssnahke_demo           # Show demonstration
security_scan          # Alias for ssnahke_scan
snake                  # Quick alias
```

## 📊 Scan Capabilities

### System Information Scan
- Hostname and platform detection
- CPU and memory analysis  
- Disk usage monitoring across all filesystems
- Architecture and Python environment analysis

### Network Security Scan
- Network interface enumeration
- Active connection monitoring
- Listening port detection and analysis
- Suspicious network activity identification

### Process Security Scan
- Running process analysis
- Memory usage monitoring
- Suspicious process name detection (backdoor, rootkit, malware patterns)
- Command line argument analysis

### File Integrity Scan
- World-writable file detection in critical directories
- SUID binary identification
- Large file monitoring (100MB+ files)
- Hidden file discovery

### Living Code Integration Check
- Environment wrapper verification
- Security component status validation
- Hardening level detection
- Integration database verification

## 🛡️ Security Features

### Vulnerability Detection
- Real-time security issue identification
- Severity-based classification (HIGH, MEDIUM, LOW)
- Automated security recommendations
- Comprehensive reporting with JSON output

### System Hardening Integration
- Compatible with hardened security mode
- PGP integration support
- XML-based permission validation
- Container security verification

### Monitoring & Alerting
- Continuous system monitoring capabilities
- Automated threat detection
- Security violation logging
- Integration with existing audit systems

## 📁 File Structure

```
scripts/
├── ssnahke-local-installer.sh      # Main installer script
├── demonstrate-ssnahke-local.sh    # System demonstration
└── ssnahke-local-status.sh         # Integration status check

$HOME/
├── ssnahke_local.py                # Main scanner (after installation)
├── ssnahke_launcher.sh             # Quick launcher (after installation)
└── ssnahke_scan_report_*.json      # Security reports (after scans)
```

## 🔍 Sample Output

```
🐍 SSnaHke-Local v1.0.0-Living - Security Scanner
============================================================
✅ Living Code Environment: ACTIVE
🛡️ Hardened Security: ENABLED

📊 Gathering system information...
✅ System information collected
🌐 Scanning network configuration...
✅ Network scan completed
⚙️ Scanning running processes...
✅ Process scan completed (127 processes)
📁 Scanning file system security...
✅ File security scan completed
🧬 Checking Living Code Environment integration...
✅ Living Code integration verified
🔒 Generating security recommendations...
  ✅ No immediate security issues detected
  🛡️ Hardened security mode already enabled

📋 Generating security report...
✅ Security report saved: ssnahke_scan_report_20241231_143022.json

🎯 Scan Summary:
   Duration: 2.34 seconds
   Security Issues: 0
   Report: ssnahke_scan_report_20241231_143022.json

🧬 Living Code Environment: ACTIVE
   All scans integrated with living code monitoring

✅ SSnaHke-Local scan completed successfully!
```

## 📊 Report Format

### JSON Security Report Structure
```json
{
  "scan_metadata": {
    "scanner_version": "1.0.0-Living",
    "scan_time": "2024-12-31T14:30:22",
    "duration_seconds": 2.34,
    "living_code_active": true,
    "hardened_security": true
  },
  "scan_results": {
    "system": { "hostname": "...", "platform": "..." },
    "network": { "interfaces": [...], "listening_ports": [...] },
    "processes": [...],
    "file_security": { "world_writable": [], "suid_files": [] },
    "living_code": { "integration_status": "active" }
  },
  "security_issues": [
    {
      "type": "world_writable_file",
      "severity": "MEDIUM", 
      "details": "World-writable file: /tmp/example"
    }
  ],
  "summary": {
    "total_issues": 0,
    "high_severity": 0,
    "medium_severity": 0,
    "low_severity": 0
  }
}
```

## 🌟 Integration Architecture

### Living Code Environment Integration
- **Environment Detection**: Automatic CHROOT/BUSYBOX/TERMUX/Android detection
- **Zero Performance Impact**: Background whisper processes
- **Seamless Commands**: Native command integration without CLI overhead
- **Status Monitoring**: Real-time integration verification

### Security Infrastructure Integration  
- **Hardened Permissions**: XML-based permission compatibility
- **PGP Integration**: Cryptographic security support
- **Container Support**: TOYBOX, CHISEL, proot-distro integration
- **Android Compatibility**: Container-based execution for Android 10+

### Automation Features
- **Multi-Distro Support**: Ubuntu, Fedora, Arch, Alpine, others
- **Dependency Resolution**: Automatic package management
- **Fallback Methods**: Multiple installation approaches
- **Cross-Platform**: Windows, Linux, macOS compatibility

## 🔧 Advanced Configuration

### Custom Scan Parameters
```bash
# Quick scan mode
python3 ~/ssnahke_local.py --quick

# Verbose output
python3 ~/ssnahke_local.py --verbose

# Custom report location
python3 ~/ssnahke_local.py --output /custom/path/
```

### Integration with Monitoring Systems
```bash
# Automated periodic scanning
crontab -e
# Add: 0 2 * * * /home/user/ssnahke_launcher.sh --quiet

# Integration with log monitoring
tail -f ssnahke_scan_report_*.json | jq '.security_issues'
```

## 🎯 Benefits Summary

### Security Enhancement
- ✅ Real-time vulnerability detection
- ✅ Comprehensive system monitoring  
- ✅ Automated security recommendations
- ✅ Integration with existing security infrastructure

### Operational Efficiency
- ✅ One-command installation and setup
- ✅ Zero configuration required
- ✅ Automatic dependency management
- ✅ Cross-platform compatibility

### Living Code Integration
- ✅ Environment-level operation
- ✅ Perfect symmetrical integration
- ✅ Zero performance overhead
- ✅ Native command availability

### Reporting & Analysis
- ✅ JSON-formatted security reports
- ✅ Timestamp-based tracking
- ✅ Severity-based classification
- ✅ Integration with audit systems

---

**SSnaHke-Local** represents the next generation of local security scanning, combining comprehensive vulnerability detection with seamless Living Code Environment integration for maximum security with zero performance impact.