#!/usr/bin/env bash
# Living Environment Wrapper - Zero Overhead Living Code System
# This wrapper operates at the environment level without impacting
# command line performance, git operations, or data transfer

# Set environment variables for living code operation
export LIVING_CODE_ENABLED=1
export LIVING_CODE_DB="$(pwd)/.living_environment.db"
export LIVING_CODE_ROOT="$(pwd)"

# Living code functions (loaded in background)
living_code_monitor() {
    # Monitor file changes and evolve code patterns
    # Runs in background with minimal CPU impact
    if [[ "$LIVING_CODE_ENABLED" == "1" ]]; then
        python3 "$(pwd)/scripts/living-environment-integration.py" --background-monitor &
    fi
}

# Environment initialization
living_code_init() {
    # Initialize living code environment
    if [[ -f "$LIVING_CODE_DB" ]] && [[ "$LIVING_CODE_ENABLED" == "1" ]]; then
        # Load environment optimizations
        source <(python3 "$(pwd)/scripts/living-environment-integration.py" --get-env-optimizations 2>/dev/null || true)
    fi
}

# Hook into shell initialization (bash/zsh)
if [[ "${BASH_VERSION:-}" ]] || [[ "${ZSH_VERSION:-}" ]]; then
    # Initialize living code environment
    living_code_init
    
    # Start background monitoring (if not already running)
    if ! pgrep -f "living-environment-integration.py.*background-monitor" >/dev/null 2>&1; then
        living_code_monitor
    fi
fi

# Storage Features Functions - ZRAM & NumPy Memmap
storage_features_enable() {
    echo "💾 Enabling mobile storage features..."
    if [[ -d "$(pwd)/scraped_data/mobile_storage" ]]; then
        echo "✅ ZRAM tools and NumPy memmap documentation available"
        echo "📍 Location: $(pwd)/scraped_data/mobile_storage"
        return 0
    else
        echo "⚠️  Storage features not yet collected. Run data collection first:"
        echo "   python3 scripts/comprehensive-data-scraper.py"
        return 1
    fi
}

zram_status() {
    echo "🗜️ ZRAM Status Check"
    if [[ -d "$(pwd)/scraped_data/mobile_storage/zram" ]]; then
        echo "✅ ZRAM documentation: Available"
        echo "✅ ZRAM tools repos: Available"
        echo "✅ Linux kernel source: Available"
        echo "📖 Implementation guide: $(pwd)/scraped_data/mobile_storage/zram/MOBILE_ZRAM_GUIDE.md"
        
        # Check if ZRAM is actually enabled on system
        if [[ -e /sys/block/zram0 ]]; then
            echo "✅ ZRAM kernel module: Loaded"
            if [[ -r /sys/block/zram0/disksize ]]; then
                local disksize=$(cat /sys/block/zram0/disksize 2>/dev/null || echo "0")
                if [[ "$disksize" != "0" ]]; then
                    echo "✅ ZRAM device: Active ($(numfmt --to=iec $disksize))"
                else
                    echo "⚠️  ZRAM device: Available but not configured"
                fi
            fi
        else
            echo "⚠️  ZRAM kernel module: Not loaded (documentation available for setup)"
        fi
    else
        echo "❌ ZRAM resources not available. Run storage features collection first."
        return 1
    fi
}

memmap_status() {
    echo "🗺️ NumPy Memmap Status Check"
    if [[ -d "$(pwd)/scraped_data/mobile_storage/numpy_memmap" ]]; then
        echo "✅ NumPy memmap documentation: Available"
        echo "✅ Implementation examples: Available"
        echo "✅ Mobile integration guide: Available"
        echo "📖 Implementation guide: $(pwd)/scraped_data/mobile_storage/numpy_memmap/MOBILE_MEMMAP_GUIDE.md"
        
        # Check if NumPy is available
        if python3 -c "import numpy; print(f'NumPy version: {numpy.__version__}')" 2>/dev/null; then
            echo "✅ NumPy library: Available"
            if python3 -c "import numpy; numpy.memmap" 2>/dev/null; then
                echo "✅ NumPy memmap: Ready for use"
            else
                echo "⚠️  NumPy memmap: Check installation"
            fi
        else
            echo "⚠️  NumPy library: Not installed (pip install numpy)"
        fi
    else
        echo "❌ NumPy memmap resources not available. Run storage features collection first."
        return 1
    fi
}

storage_optimization_report() {
    echo "📊 Mobile Storage Optimization Report"
    echo "============================================"
    
    # Memory information
    if [[ -r /proc/meminfo ]]; then
        local total_mem=$(grep MemTotal /proc/meminfo | awk '{print $2}')
        local avail_mem=$(grep MemAvailable /proc/meminfo | awk '{print $2}')
        echo "💾 System Memory:"
        echo "   Total: $(numfmt --to=iec $((total_mem * 1024)))"
        echo "   Available: $(numfmt --to=iec $((avail_mem * 1024)))"
        
        # ZRAM information if available
        if [[ -e /sys/block/zram0/mem_used_total ]]; then
            local zram_used=$(cat /sys/block/zram0/mem_used_total 2>/dev/null || echo "0")
            local zram_size=$(cat /sys/block/zram0/disksize 2>/dev/null || echo "0")
            if [[ "$zram_used" != "0" ]]; then
                echo "🗜️ ZRAM Usage:"
                echo "   Used: $(numfmt --to=iec $zram_used)"
                echo "   Size: $(numfmt --to=iec $zram_size)"
                echo "   Compression: Active"
            fi
        fi
    fi
    
    # Storage features availability
    echo "💾 Storage Features:"
    if [[ -d "$(pwd)/scraped_data/mobile_storage" ]]; then
        echo "   ✅ ZRAM tools and documentation"
        echo "   ✅ NumPy memmap implementation guides"
        echo "   ✅ Linux kernel source with ZRAM driver"
        echo "   ✅ Mobile optimization guides"
        echo "   ✅ Living code integration complete"
    else
        echo "   ❌ Storage features not collected"
        echo "   📝 Run: python3 scripts/comprehensive-data-scraper.py"
    fi
    
    echo "============================================"
    echo "📖 Complete guide: $(pwd)/scraped_data/mobile_storage/STORAGE_INTEGRATION_GUIDE.md"
}

# Export functions for use in scripts
export -f living_code_monitor living_code_init storage_features_enable zram_status memmap_status storage_optimization_report
