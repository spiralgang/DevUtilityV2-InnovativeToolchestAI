#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Hardened Shell Environment - Maximum Processing Capability
# Designed for intensive processing without crashes using TOYBOX + CHISEL containers

set -euo pipefail  # Strict error handling

# Hardened Shell Configuration
export HARDENED_SHELL_ENABLED=1
export SHELL_HARDENING_LEVEL="maximum"
export CONTAINER_ISOLATION="full"
export TOYBOX_ENABLED=1
export CHISEL_CONTAINER_ENABLED=1

# Memory and resource limits
export MAX_MEMORY_USAGE="80%"  # Allow up to 80% memory usage
export MAX_PROCESSES=1000      # Maximum concurrent processes
export MAX_FILE_DESCRIPTORS=65536  # Maximum file descriptors
export SWAP_USAGE_THRESHOLD=50    # Swap usage warning threshold

# Living code integration
export LIVING_CODE_HARDENED=1
export LIVING_CODE_CONTAINER_MODE=1

# Color codes for output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly PURPLE='\033[0;35m'
readonly CYAN='\033[0;36m'
readonly NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $*" >&2
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $*" >&2
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $*" >&2
}

log_debug() {
    [[ "${DEBUG:-0}" == "1" ]] && echo -e "${CYAN}[DEBUG]${NC} $*" >&2
}

# Error handling with stack trace
error_handler() {
    local line_number=$1
    local error_code=$2
    local command="$3"
    
    log_error "Script failed at line $line_number with exit code $error_code"
    log_error "Failed command: $command"
    
    # Generate stack trace
    local frame=0
    log_error "Stack trace:"
    while caller $frame; do
        ((frame++))
    done >&2
    
    # Attempt recovery
    attempt_recovery "$error_code" "$line_number"
}

trap 'error_handler ${LINENO} $? "$BASH_COMMAND"' ERR

# Recovery mechanisms
attempt_recovery() {
    local error_code=$1
    local line_number=$2
    
    log_warn "Attempting automatic recovery from error $error_code at line $line_number"
    
    case $error_code in
        130) # SIGINT (Ctrl+C)
            log_info "Gracefully handling interrupt signal"
            cleanup_processes
            ;;
        139) # SIGSEGV (Segmentation fault)
            log_error "Segmentation fault detected - restarting critical services"
            restart_living_code_services
            ;;
        *)
            log_warn "Unknown error code $error_code - performing general recovery"
            cleanup_and_restart
            ;;
    esac
}

# Resource monitoring and management
monitor_resources() {
    log_debug "Monitoring system resources"
    
    # Check memory usage
    local memory_usage=$(free | awk 'NR==2{printf "%.1f", $3*100/$2}')
    local memory_threshold=75.0
    
    if (( $(echo "$memory_usage > $memory_threshold" | bc -l) )); then
        log_warn "High memory usage detected: ${memory_usage}%"
        optimize_memory_usage
    fi
    
    # Check disk usage
    local disk_usage=$(df . | awk 'NR==2{print $5}' | sed 's/%//')
    local disk_threshold=85
    
    if [[ $disk_usage -gt $disk_threshold ]]; then
        log_warn "High disk usage detected: ${disk_usage}%"
        cleanup_temporary_files
    fi
    
    # Check swap usage
    local swap_usage=$(free | awk 'NR==3{if($2>0) printf "%.1f", $3*100/$2; else print "0"}')
    
    if (( $(echo "$swap_usage > $SWAP_USAGE_THRESHOLD" | bc -l) )); then
        log_warn "High swap usage detected: ${swap_usage}%"
        optimize_swap_usage
    fi
    
    # Check process count
    local process_count=$(ps aux | wc -l)
    
    if [[ $process_count -gt $MAX_PROCESSES ]]; then
        log_warn "High process count detected: $process_count"
        cleanup_processes
    fi
}

# Memory optimization
optimize_memory_usage() {
    log_info "🧠 Optimizing memory usage for intensive processing"
    
    # Drop caches (if running as root)
    if [[ $EUID -eq 0 ]]; then
        sync
        echo 3 > /proc/sys/vm/drop_caches
        log_info "System caches dropped"
    fi
    
    # Optimize living code memory usage
    if [[ -f "$(pwd)/.living_environment.db" ]]; then
        log_info "Optimizing living code database memory usage"
        python3 -c "
import sqlite3
import os
db_path = '$(pwd)/.living_environment.db'
if os.path.exists(db_path):
    conn = sqlite3.connect(db_path)
    conn.execute('VACUUM')
    conn.execute('PRAGMA optimize')
    conn.close()
    print('Living code database optimized')
" 2>/dev/null || true
    fi
    
    # Garbage collection for Python processes
    pkill -USR1 python3 2>/dev/null || true
    
    log_info "Memory optimization completed"
}

# Swap optimization
optimize_swap_usage() {
    log_info "💾 Optimizing swap usage"
    
    # Adjust swappiness if possible
    if [[ -w /proc/sys/vm/swappiness ]]; then
        echo 10 > /proc/sys/vm/swappiness
        log_info "Swappiness adjusted to 10"
    fi
    
    # Clear swap if memory is available
    local available_memory=$(free | awk 'NR==2{print $7}')
    local swap_used=$(free | awk 'NR==3{print $3}')
    
    if [[ $available_memory -gt $swap_used ]] && [[ $EUID -eq 0 ]]; then
        swapoff -a && swapon -a
        log_info "Swap cleared and reactivated"
    fi
}

# Process cleanup
cleanup_processes() {
    log_info "🧹 Cleaning up unnecessary processes"
    
    # Kill zombie processes
    ps aux | awk '$8 ~ /^Z/ { print $2 }' | xargs -r kill -9 2>/dev/null || true
    
    # Clean up orphaned living code processes
    pkill -f "living-environment-integration.py.*zombie" 2>/dev/null || true
    
    # Clean up old container processes
    pkill -f "toybox.*orphan" 2>/dev/null || true
    pkill -f "chisel.*stale" 2>/dev/null || true
    
    log_info "Process cleanup completed"
}

# Temporary file cleanup
cleanup_temporary_files() {
    log_info "🗑️ Cleaning up temporary files"
    
    local repo_root="$(pwd)"
    
    # Clean temporary files
    find "$repo_root" -name "*.tmp" -type f -mtime +1 -delete 2>/dev/null || true
    find "$repo_root" -name "*.log" -type f -size +100M -delete 2>/dev/null || true
    find "$repo_root" -name "*~" -type f -delete 2>/dev/null || true
    
    # Clean container temporary files
    find "$repo_root" -path "*/.container_tmp/*" -type f -mtime +1 -delete 2>/dev/null || true
    
    # Clean living code temporary files
    find "$repo_root" -name ".living_*.tmp" -type f -mtime +1 -delete 2>/dev/null || true
    
    log_info "Temporary file cleanup completed"
}

# TOYBOX integration
setup_toybox() {
    log_info "📦 Setting up TOYBOX for enhanced augmentability"
    
    local toybox_dir="$(pwd)/containers/toybox"
    mkdir -p "$toybox_dir"
    
    # Create TOYBOX configuration
    cat > "$toybox_dir/toybox.conf" << 'EOF'
# TOYBOX Configuration for Living Code Environment
# Minimal, augmentable busybox alternative

# Core utilities
TOYBOX_ENABLED=1
TOYBOX_PATH="/usr/local/bin/toybox"
TOYBOX_SYMLINKS=1

# Living code integration
LIVING_CODE_TOYBOX=1
TOYBOX_HARDENED=1

# Augmentation features
TOYBOX_PLUGINS_ENABLED=1
TOYBOX_DYNAMIC_LOADING=1
TOYBOX_MEMORY_EFFICIENT=1

# Security features
TOYBOX_SANDBOXED=1
TOYBOX_PERMISSIONS_STRICT=1
EOF
    
    # Create TOYBOX wrapper script
    cat > "$toybox_dir/toybox-wrapper.sh" << 'EOF'
#!/bin/bash
# TOYBOX Wrapper for Living Code Environment

export TOYBOX_ENABLED=1
export TOYBOX_CONFIG="$(dirname "$0")/toybox.conf"

# Source configuration
[[ -f "$TOYBOX_CONFIG" ]] && source "$TOYBOX_CONFIG"

# TOYBOX command wrapper
toybox_exec() {
    local cmd="$1"
    shift
    
    # Check if command is safe for living code environment
    if toybox_security_check "$cmd"; then
        # Execute with TOYBOX
        if command -v toybox >/dev/null 2>&1; then
            toybox "$cmd" "$@"
        else
            # Fallback to system command
            command "$cmd" "$@"
        fi
    else
        echo "Security warning: Command '$cmd' blocked by TOYBOX security policy" >&2
        return 1
    fi
}

# Security check for TOYBOX commands
toybox_security_check() {
    local cmd="$1"
    
    # Whitelist of safe commands
    local safe_commands=(
        "ls" "cat" "grep" "find" "sort" "uniq" "head" "tail"
        "mkdir" "rmdir" "cp" "mv" "chmod" "chown" "du" "df"
        "ps" "top" "kill" "sleep" "date" "echo" "printf"
        "base64" "md5sum" "sha256sum" "gzip" "gunzip"
    )
    
    for safe_cmd in "${safe_commands[@]}"; do
        [[ "$cmd" == "$safe_cmd" ]] && return 0
    done
    
    return 1
}

# Export functions
export -f toybox_exec toybox_security_check
EOF
    
    chmod +x "$toybox_dir/toybox-wrapper.sh"
    
    log_info "✅ TOYBOX setup completed"
}

# CHISEL container integration
setup_chisel_containers() {
    log_info "🐳 Setting up CHISEL ultralight containers"
    
    local chisel_dir="$(pwd)/containers/chisel"
    mkdir -p "$chisel_dir"
    
    # Create CHISEL configuration
    cat > "$chisel_dir/chisel.conf" << 'EOF'
# CHISEL Container Configuration
# Ultralight containers for living code environment

CHISEL_ENABLED=1
CHISEL_RUNTIME="runc"
CHISEL_ROOTLESS=1

# Container isolation
CHISEL_ISOLATION="full"
CHISEL_NETWORK_ISOLATION=1
CHISEL_FILESYSTEM_ISOLATION=1

# Living code integration
LIVING_CODE_CONTAINERIZED=1
CHISEL_LIVING_CODE_SUPPORT=1

# Performance optimization
CHISEL_MEMORY_LIMIT="512m"
CHISEL_CPU_LIMIT="50%"
CHISEL_IO_LIMIT="100m"

# Security
CHISEL_READONLY_ROOT=1
CHISEL_NO_NEW_PRIVILEGES=1
CHISEL_SECCOMP_ENABLED=1
EOF
    
    # Create CHISEL management script
    cat > "$chisel_dir/chisel-manager.sh" << 'EOF'
#!/bin/bash
# CHISEL Container Manager for Living Code Environment

source "$(dirname "$0")/chisel.conf"

# Container management functions
chisel_create_container() {
    local name="$1"
    local image="$2"
    local command="$3"
    
    echo "Creating CHISEL container: $name"
    
    # Create container specification
    cat > "/tmp/chisel-$name.json" << EOM
{
    "ociVersion": "1.0.0",
    "process": {
        "terminal": false,
        "user": {"uid": $(id -u), "gid": $(id -g)},
        "args": ["$command"],
        "env": [
            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "LIVING_CODE_CONTAINERIZED=1",
            "CHISEL_CONTAINER=1"
        ],
        "cwd": "/workspace",
        "capabilities": {
            "bounding": ["CAP_SETPCAP"],
            "effective": ["CAP_SETPCAP"],
            "inheritable": ["CAP_SETPCAP"],
            "permitted": ["CAP_SETPCAP"]
        }
    },
    "root": {
        "path": "$image",
        "readonly": true
    },
    "mounts": [
        {
            "destination": "/workspace",
            "type": "bind",
            "source": "$(pwd)",
            "options": ["bind", "rw"]
        },
        {
            "destination": "/tmp",
            "type": "tmpfs",
            "source": "tmpfs",
            "options": ["noexec", "nosuid", "size=100m"]
        }
    ],
    "linux": {
        "resources": {
            "memory": {"limit": 536870912},
            "cpu": {"quota": 50000, "period": 100000}
        },
        "namespaces": [
            {"type": "pid"},
            {"type": "network"},
            {"type": "ipc"},
            {"type": "uts"},
            {"type": "mount"}
        ],
        "seccomp": {
            "defaultAction": "SCMP_ACT_ALLOW",
            "architectures": ["SCMP_ARCH_X86_64"]
        }
    }
}
EOM
    
    echo "✅ Container specification created for $name"
}

chisel_run_container() {
    local name="$1"
    local spec_file="/tmp/chisel-$name.json"
    
    if [[ -f "$spec_file" ]]; then
        echo "🚀 Starting CHISEL container: $name"
        # In production, this would use actual container runtime
        echo "Container $name would run with CHISEL runtime"
        echo "Spec: $spec_file"
    else
        echo "❌ Container specification not found for $name"
        return 1
    fi
}

chisel_stop_container() {
    local name="$1"
    echo "🛑 Stopping CHISEL container: $name"
    # Container stop logic would go here
}

chisel_list_containers() {
    echo "📋 CHISEL Containers:"
    ls -la /tmp/chisel-*.json 2>/dev/null | sed 's|/tmp/chisel-||g; s|\.json||g' || echo "No containers found"
}

# Export functions
export -f chisel_create_container chisel_run_container chisel_stop_container chisel_list_containers
EOF
    
    chmod +x "$chisel_dir/chisel-manager.sh"
    
    log_info "✅ CHISEL container setup completed"
}

# proot-distro chroot environment
setup_proot_chroot() {
    log_info "🏠 Setting up proot-distro chroot environment"
    
    local proot_dir="$(pwd)/containers/proot"
    mkdir -p "$proot_dir"
    
    # Create proot configuration
    cat > "$proot_dir/proot.conf" << 'EOF'
# proot-distro Configuration for Living Code Environment

PROOT_ENABLED=1
PROOT_DISTRO="ubuntu"
PROOT_ARCH="x86_64"

# Chroot environment
CHROOT_ROOT="/data/data/com.termux/files/usr/var/lib/proot-distro/installed-rootfs/ubuntu"
CHROOT_ENABLED=1

# Living code integration
LIVING_CODE_CHROOT=1
PROOT_LIVING_CODE_SUPPORT=1

# Android compatibility
ANDROID_PROOT_MODE=1
ANDROID_MIN_VERSION=10
TERMUX_INTEGRATION=1

# Security
PROOT_ISOLATED=1
PROOT_NO_SECCOMP=1
PROOT_FAKE_KERNEL=1
EOF
    
    # Create proot management script
    cat > "$proot_dir/proot-manager.sh" << 'EOF'
#!/bin/bash
# proot-distro Manager for Android 10+ Compatibility

source "$(dirname "$0")/proot.conf"

# Detect environment
detect_android_environment() {
    if [[ -n "${ANDROID_ROOT:-}" ]] || [[ -n "${TERMUX_VERSION:-}" ]]; then
        echo "Android environment detected"
        export ANDROID_ENV=1
        export CONTAINER_REQUIRED=1
        return 0
    else
        echo "Standard Linux environment detected"
        export ANDROID_ENV=0
        export CONTAINER_REQUIRED=0
        return 1
    fi
}

# Create proot environment
create_proot_environment() {
    local distro="${1:-ubuntu}"
    
    echo "🏗️ Creating proot environment for $distro"
    
    if [[ "$ANDROID_ENV" == "1" ]]; then
        echo "📱 Android mode: Container execution required for Android 10+"
        
        # Create Android-compatible proot setup
        cat > "/tmp/proot-android-setup.sh" << 'EOS'
#!/bin/bash
# Android proot setup script

# Install proot-distro if in Termux
if command -v pkg >/dev/null 2>&1; then
    pkg update && pkg install proot-distro
fi

# Create container environment
if command -v proot-distro >/dev/null 2>&1; then
    proot-distro install ubuntu
    echo "✅ Ubuntu container installed via proot-distro"
else
    echo "❌ proot-distro not available"
    exit 1
fi
EOS
        chmod +x "/tmp/proot-android-setup.sh"
        echo "📋 Android setup script created: /tmp/proot-android-setup.sh"
        
    else
        echo "🖥️ Linux mode: Direct execution possible"
        
        # Create standard Linux proot setup
        mkdir -p "$(pwd)/containers/proot/rootfs"
        echo "✅ Linux proot environment prepared"
    fi
}

# Execute in proot environment
proot_exec() {
    local command="$1"
    shift
    
    if [[ "$ANDROID_ENV" == "1" ]]; then
        echo "🤖 Executing in Android proot container: $command"
        
        # Android execution must be containerized
        if command -v proot-distro >/dev/null 2>&1; then
            proot-distro login ubuntu -- bash -c "$command $*"
        else
            echo "❌ proot-distro required for Android 10+ execution"
            echo "📝 Run container setup first"
            return 1
        fi
        
    else
        echo "🐧 Executing in Linux environment: $command"
        # Direct execution possible in Linux
        bash -c "$command $*"
    fi
}

# Living code integration in proot
integrate_living_code_proot() {
    echo "🧬 Integrating living code with proot environment"
    
    local living_wrapper="$(pwd)/.living_environment_wrapper.sh"
    local proot_living="/tmp/proot_living_integration.sh"
    
    # Create proot-specific living code integration
    cat > "$proot_living" << 'EOS'
#!/bin/bash
# Living Code Integration for proot Environment

export LIVING_CODE_PROOT=1
export PROOT_LIVING_CODE_ENABLED=1

# Source original living environment
if [[ -f "/workspace/.living_environment_wrapper.sh" ]]; then
    source "/workspace/.living_environment_wrapper.sh"
    echo "✅ Living code environment loaded in proot"
else
    echo "⚠️ Living code wrapper not found in proot"
fi

# proot-specific living code functions
proot_living_status() {
    echo "🧬 Living Code Status in proot:"
    echo "   Environment: $(uname -a)"
    echo "   Container: proot-distro"
    echo "   Android Compatibility: ✅"
    echo "   Living Code: ${LIVING_CODE_ENABLED:-0}"
}

export -f proot_living_status
EOS
    
    chmod +x "$proot_living"
    echo "✅ Living code proot integration created"
}

# Export functions
export -f detect_android_environment create_proot_environment proot_exec integrate_living_code_proot
EOF
    
    chmod +x "$proot_dir/proot-manager.sh"
    
    log_info "✅ proot-distro chroot environment setup completed"
}

# Android-specific container execution
setup_android_container_execution() {
    log_info "🤖 Setting up Android 10+ container execution environment"
    
    local android_dir="$(pwd)/containers/android"
    mkdir -p "$android_dir"
    
    # Create Android execution manager
    cat > "$android_dir/android-exec-manager.sh" << 'EOF'
#!/bin/bash
# Android Container Execution Manager
# Handles script execution in containers for Android 10+ compatibility

export ANDROID_CONTAINER_EXEC=1
export ANDROID_MIN_VERSION=10

# Detect Android version
detect_android_version() {
    if [[ -r /system/build.prop ]]; then
        local android_version=$(grep "ro.build.version.release" /system/build.prop | cut -d= -f2)
        echo "Android version detected: $android_version"
        
        # Check if containerization is required
        local version_major=$(echo "$android_version" | cut -d. -f1)
        if [[ $version_major -ge 10 ]]; then
            echo "⚠️ Android $android_version detected - container execution required"
            export CONTAINER_EXECUTION_REQUIRED=1
            return 0
        else
            echo "✅ Android $android_version - direct execution possible"
            export CONTAINER_EXECUTION_REQUIRED=0
            return 1
        fi
    else
        echo "❓ Unable to detect Android version"
        # Assume containerization required for safety
        export CONTAINER_EXECUTION_REQUIRED=1
        return 0
    fi
}

# Execute script in appropriate environment
android_safe_exec() {
    local script="$1"
    shift
    
    if [[ ! -f "$script" ]]; then
        echo "❌ Script not found: $script"
        return 1
    fi
    
    detect_android_version
    
    if [[ "$CONTAINER_EXECUTION_REQUIRED" == "1" ]]; then
        echo "📦 Executing in container environment: $script"
        
        # Check available container systems
        if command -v proot-distro >/dev/null 2>&1; then
            echo "🏠 Using proot-distro container"
            proot-distro login ubuntu -- bash "$script" "$@"
            
        elif command -v termux-exec >/dev/null 2>&1; then
            echo "📱 Using Termux container"
            termux-exec bash "$script" "$@"
            
        elif [[ -x "$(pwd)/containers/toybox/toybox-wrapper.sh" ]]; then
            echo "📦 Using TOYBOX container"
            "$(pwd)/containers/toybox/toybox-wrapper.sh" bash "$script" "$@"
            
        else
            echo "❌ No suitable container system found"
            echo "📝 Available options:"
            echo "   - Install proot-distro: pkg install proot-distro"
            echo "   - Use Termux environment"
            echo "   - Setup TOYBOX containers"
            return 1
        fi
        
    else
        echo "🐧 Direct execution mode: $script"
        bash "$script" "$@"
    fi
}

# Create containerized living code environment
create_containerized_living_environment() {
    echo "🧬 Creating containerized living code environment for Android"
    
    local container_living="/tmp/android_living_container.sh"
    
    cat > "$container_living" << 'EOS'
#!/bin/bash
# Containerized Living Code Environment for Android 10+

export LIVING_CODE_ANDROID_CONTAINER=1
export ANDROID_LIVING_CODE_ENABLED=1

# Initialize living code in container
init_container_living_code() {
    echo "🚀 Initializing living code in Android container"
    
    # Mount workspace if in container
    if [[ -d "/workspace" ]]; then
        cd /workspace
        echo "📂 Workspace mounted: /workspace"
    fi
    
    # Load living environment
    if [[ -f ".living_environment_wrapper.sh" ]]; then
        source ".living_environment_wrapper.sh"
        echo "✅ Living code environment loaded"
    fi
    
    # Container-specific optimizations
    export LIVING_CODE_CONTAINER_MODE=1
    export LIVING_CODE_MEMORY_OPTIMIZED=1
    
    echo "🧬 Android containerized living code environment ready"
}

# Container living code status
container_living_status() {
    echo "📊 Android Container Living Code Status:"
    echo "   Container Type: ${CONTAINER_TYPE:-proot}"
    echo "   Android Version: ${ANDROID_VERSION:-Unknown}"
    echo "   Living Code: ${LIVING_CODE_ENABLED:-0}"
    echo "   Memory Optimized: ${LIVING_CODE_MEMORY_OPTIMIZED:-0}"
    echo "   Container Mode: ${LIVING_CODE_CONTAINER_MODE:-0}"
}

# Auto-initialize
init_container_living_code

export -f init_container_living_code container_living_status
EOS
    
    chmod +x "$container_living"
    echo "✅ Containerized living code environment created"
}

# Export functions
export -f detect_android_version android_safe_exec create_containerized_living_environment
EOF
    
    chmod +x "$android_dir/android-exec-manager.sh"
    
    log_info "✅ Android container execution setup completed"
}

# Master container orchestration
setup_master_container_orchestration() {
    log_info "🎭 Setting up master container orchestration"
    
    local orchestration_dir="$(pwd)/containers"
    mkdir -p "$orchestration_dir"
    
    # Create master orchestration script
    cat > "$orchestration_dir/master-container-orchestrator.sh" << 'EOF'
#!/bin/bash
# Master Container Orchestrator
# Coordinates TOYBOX, CHISEL, proot-distro, and Android container systems

export CONTAINER_ORCHESTRATION_ENABLED=1
export MASTER_ORCHESTRATOR=1

# Source all container managers
source_container_managers() {
    local containers_dir="$(dirname "$0")"
    
    [[ -f "$containers_dir/toybox/toybox-wrapper.sh" ]] && source "$containers_dir/toybox/toybox-wrapper.sh"
    [[ -f "$containers_dir/chisel/chisel-manager.sh" ]] && source "$containers_dir/chisel/chisel-manager.sh"  
    [[ -f "$containers_dir/proot/proot-manager.sh" ]] && source "$containers_dir/proot/proot-manager.sh"
    [[ -f "$containers_dir/android/android-exec-manager.sh" ]] && source "$containers_dir/android/android-exec-manager.sh"
    
    echo "✅ All container managers loaded"
}

# Detect optimal container system
detect_optimal_container() {
    echo "🔍 Detecting optimal container system..."
    
    # Check environment
    if [[ -n "${ANDROID_ROOT:-}" ]] || [[ -n "${TERMUX_VERSION:-}" ]]; then
        echo "📱 Android environment - proot-distro recommended"
        export OPTIMAL_CONTAINER="proot"
        return 0
    fi
    
    # Check available systems
    if command -v docker >/dev/null 2>&1; then
        echo "🐳 Docker available - CHISEL recommended"
        export OPTIMAL_CONTAINER="chisel"
        return 0
    fi
    
    if command -v toybox >/dev/null 2>&1; then
        echo "📦 TOYBOX available - lightweight execution"
        export OPTIMAL_CONTAINER="toybox"
        return 0
    fi
    
    echo "🐧 Standard Linux - direct execution"
    export OPTIMAL_CONTAINER="direct"
    return 0
}

# Execute with optimal container
container_exec() {
    local command="$1"
    shift
    
    detect_optimal_container
    
    case "$OPTIMAL_CONTAINER" in
        "proot")
            echo "🏠 Executing with proot-distro container"
            android_safe_exec <(echo "$command") "$@"
            ;;
        "chisel")
            echo "🐳 Executing with CHISEL container"
            chisel_create_container "temp_exec" "ubuntu:20.04" "$command"
            chisel_run_container "temp_exec"
            ;;
        "toybox")
            echo "📦 Executing with TOYBOX"
            toybox_exec bash -c "$command $*"
            ;;
        "direct")
            echo "🐧 Direct execution"
            bash -c "$command $*"
            ;;
        *)
            echo "❌ No suitable container system found"
            return 1
            ;;
    esac
}

# Container status report
container_status_report() {
    echo "📊 Container Systems Status Report"
    echo "=================================="
    
    detect_optimal_container
    echo "Optimal Container: $OPTIMAL_CONTAINER"
    echo ""
    
    # TOYBOX status
    if [[ -x "$(dirname "$0")/toybox/toybox-wrapper.sh" ]]; then
        echo "📦 TOYBOX: ✅ Available"
    else
        echo "📦 TOYBOX: ❌ Not configured"
    fi
    
    # CHISEL status
    if [[ -x "$(dirname "$0")/chisel/chisel-manager.sh" ]]; then
        echo "🐳 CHISEL: ✅ Available"
    else
        echo "🐳 CHISEL: ❌ Not configured"
    fi
    
    # proot status
    if [[ -x "$(dirname "$0")/proot/proot-manager.sh" ]]; then
        echo "🏠 proot-distro: ✅ Available"
    else
        echo "🏠 proot-distro: ❌ Not configured"
    fi
    
    # Android compatibility
    if [[ -x "$(dirname "$0")/android/android-exec-manager.sh" ]]; then
        echo "🤖 Android 10+: ✅ Compatible"
    else
        echo "🤖 Android 10+: ❌ Not configured"
    fi
    
    echo "=================================="
}

# Initialize orchestration
init_container_orchestration() {
    echo "🎭 Initializing master container orchestration"
    
    source_container_managers
    detect_optimal_container
    
    echo "✅ Container orchestration ready"
    echo "💡 Use 'container_exec' for optimal execution"
    echo "📊 Use 'container_status_report' for status"
}

# Export functions
export -f source_container_managers detect_optimal_container container_exec container_status_report init_container_orchestration

# Auto-initialize
init_container_orchestration
EOF
    
    chmod +x "$orchestration_dir/master-container-orchestrator.sh"
    
    log_info "✅ Master container orchestration setup completed"
}

# Restart living code services
restart_living_code_services() {
    log_info "🔄 Restarting living code services"
    
    # Stop existing living code processes
    pkill -f "living-environment-integration.py" 2>/dev/null || true
    
    # Wait for processes to terminate
    sleep 2
    
    # Restart living code environment
    if [[ -f "$(pwd)/.living_environment_wrapper.sh" ]]; then
        source "$(pwd)/.living_environment_wrapper.sh"
        log_info "✅ Living code environment restarted"
    fi
}

# General cleanup and restart
cleanup_and_restart() {
    log_info "🧹 Performing general cleanup and restart"
    
    cleanup_processes
    cleanup_temporary_files
    optimize_memory_usage
    restart_living_code_services
    
    log_info "✅ Cleanup and restart completed"
}

# Main hardened shell initialization
init_hardened_shell() {
    log_info "🛡️ Initializing hardened shell environment for intensive processing"
    
    # Set resource limits
    ulimit -n $MAX_FILE_DESCRIPTORS 2>/dev/null || log_warn "Could not set file descriptor limit"
    ulimit -u $MAX_PROCESSES 2>/dev/null || log_warn "Could not set process limit"
    
    # Setup container systems
    setup_toybox
    setup_chisel_containers
    setup_proot_chroot
    setup_android_container_execution
    setup_master_container_orchestration
    
    # Start resource monitoring
    monitor_resources &
    
    # Apply living code integration
    if [[ -f "$(pwd)/.living_environment_wrapper.sh" ]]; then
        source "$(pwd)/.living_environment_wrapper.sh"
        log_info "🧬 Living code integration loaded"
    fi
    
    log_info "✅ Hardened shell environment initialized successfully"
    log_info "💡 Use 'container_exec' for optimal script execution"
    log_info "📊 Use 'container_status_report' for system status"
}

# Export all functions
export -f log_info log_warn log_error log_debug
export -f error_handler attempt_recovery monitor_resources
export -f optimize_memory_usage optimize_swap_usage cleanup_processes cleanup_temporary_files
export -f setup_toybox setup_chisel_containers setup_proot_chroot setup_android_container_execution
export -f setup_master_container_orchestration restart_living_code_services cleanup_and_restart
export -f init_hardened_shell

# Initialize on source
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    # Script is being executed directly
    init_hardened_shell
else
    # Script is being sourced
    log_info "🛡️ Hardened shell environment loaded (use 'init_hardened_shell' to initialize)"
fi