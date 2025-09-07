#!/bin/bash
# OMNISCIENT DEPLOYMENT SCRIPT - Complete setup automation

echo "ðŸš€ OMNISCIENT ANDROID DEVELOPMENT POWERHOUSE"
echo "============================================="

# Create directory structure
mkdir -p "$OMNI_ROOT"/{core,dev,network,build,optimization,workspace}

# Deploy all components
echo "[DEPLOY] Installing core system..."
detect_hardware_limits
setup_gui_environment

echo "[DEPLOY] Setting up development environment..."
install_code_server
setup_jupyter_powerhouse
setup_dev_containers

echo "[DEPLOY] Configuring networking..."
setup_tunneling_stack
configure_advanced_networking

echo "[DEPLOY] Installing build system..."
setup_mobile_cicd

echo "[DEPLOY] Optimizing performance..."
optimize_cpu_performance
optimize_memory_usage
optimize_storage

# Create unified startup script
cat > "$OMNI_ROOT/start-omniscient.sh" << 'EOF'
#!/bin/bash
echo "ðŸ§  Starting OMNISCIENT Android Development Environment"

# Start core services
code-server --bind-addr 0.0.0.0:3000 &
jupyter lab --ip=0.0.0.0 --port=8888 --no-browser &
python3 ~/.omniscient/build/build-orchestrator.py &

# Start tunnels
~/.omniscient/network/start-tunnels.sh &

# Start desktop environment
~/.omniscient/start-desktop.sh &

echo "ðŸŽ¯ OMNISCIENT Environment Ready!"
echo "ðŸ“� Code Server: http://localhost:3000"  
echo "ðŸ“Š Jupyter Lab: http://localhost:8888"
echo "ðŸ–¥ï¸�  X11 Desktop: Launch Termux-X11 app"
echo "ðŸŒ� Check tunnel output above for public URLs"
EOF

chmod +x "$OMNI_ROOT/start-omniscient.sh"

echo ""
echo "ðŸŽ¯ DEPLOYMENT COMPLETE!"
echo "Run: ~/.omniscient/start-omniscient.sh"
echo ""
echo "Access your development environment:"
echo "â€¢ Code Editor: Browser â†’ http://localhost:3000"
echo "â€¢ Data Science: Browser â†’ http://localhost:8888" 
echo "â€¢ GUI Apps: Termux-X11 app"
echo "â€¢ Remote Access: Check tunnel URLs in output"