#!/bin/bash
# MOBILE IDE SERVER - Full development environment on Android

IDE_PORT=8080
JUPYTER_PORT=8888
CODE_SERVER_PORT=3000

# Install and configure code-server (VS Code in browser)
install_code_server() {
    echo "[IDE] Installing code-server..."
    
    # Download and install code-server
    curl -fsSL https://code-server.dev/install.sh | sh
    
    # Configure code-server
    mkdir -p ~/.config/code-server
    cat > ~/.config/code-server/config.yaml << EOF
bind-addr: 0.0.0.0:${CODE_SERVER_PORT}
auth: password
password: omniscient2025
cert: false
EOF
    
    # Install essential extensions
    code-server --install-extension ms-python.python
    code-server --install-extension ms-vscode.cpptools
    code-server --install-extension golang.Go
    code-server --install-extension rust-lang.rust-analyzer
    code-server --install-extension bradlc.vscode-tailwindcss
    code-server --install-extension ms-vscode.vscode-typescript-next
}

# Advanced Jupyter Lab setup
setup_jupyter_powerhouse() {
    echo "[IDE] Configuring Jupyter Lab powerhouse..."
    
    pip3 install --user \
        jupyterlab \
        jupyterlab-git \
        jupyterlab-lsp \
        jupyter-ai \
        voila \
        plotly \
        bokeh \
        streamlit \
        dash \
        gradio
    
    # Configure Jupyter
    jupyter lab --generate-config
    
    # Custom Jupyter config for mobile optimization
    cat >> ~/.jupyter/jupyter_lab_config.py << EOF
c.ServerApp.ip = '0.0.0.0'
c.ServerApp.port = ${JUPYTER_PORT}
c.ServerApp.password = ''
c.ServerApp.token = 'omniscient'
c.ServerApp.open_browser = False
c.ServerApp.allow_remote_access = True
c.ServerApp.allow_root = True
EOF
}

# Container-based development environments
setup_dev_containers() {
    echo "[CONTAINERS] Setting up development containers..."
    
    # Docker containers for different development stacks
    cat > "$OMNI_ROOT/dev/docker-compose.yml" << 'EOF'
version: '3.8'
services:
  python-dev:
    image: python:3.11-slim
    ports:
      - "8001:8000"
    volumes:
      - ./workspace:/workspace
    working_dir: /workspace
    command: tail -f /dev/null
    
  node-dev:
    image: node:18-alpine
    ports:
      - "3001:3000"
    volumes:
      - ./workspace:/workspace
    working_dir: /workspace
    command: tail -f /dev/null
    
  go-dev:
    image: golang:1.21-alpine
    ports:
      - "8002:8000"
    volumes:
      - ./workspace:/workspace
    working_dir: /workspace
    command: tail -f /dev/null
    
  database-stack:
    image: postgres:15
    environment:
      POSTGRES_PASSWORD: omniscient
      POSTGRES_DB: devdb
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      
volumes:
  postgres_data:
EOF
}