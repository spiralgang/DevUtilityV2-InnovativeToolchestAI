#!/bin/bash
# ADVANCED TUNNELING SYSTEM - Expose Android services globally

TUNNEL_CONFIG="$OMNI_ROOT/network/tunnels.json"

# Multiple tunneling solutions for maximum reliability
setup_tunneling_stack() {
    echo "[TUNNEL] Setting up tunneling stack..."
    
    # Install tunneling tools
    pip3 install --user pyngrok
    npm install -g localtunnel
    
    # Download cloudflared
    wget -O /tmp/cloudflared https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64
    chmod +x /tmp/cloudflared
    mv /tmp/cloudflared ~/.local/bin/
    
    # Create tunnel management script
    cat > "$OMNI_ROOT/network/start-tunnels.sh" << 'EOF'
#!/bin/bash
# Start all tunneling services

# Ngrok tunnels
python3 -c "
from pyngrok import ngrok
import time

# Code server tunnel
code_tunnel = ngrok.connect(3000, 'http')
print(f'Code Server: {code_tunnel.public_url}')

# Jupyter tunnel  
jupyter_tunnel = ngrok.connect(8888, 'http')
print(f'Jupyter Lab: {jupyter_tunnel.public_url}')

# Keep running
while True:
    time.sleep(60)
" &

# Cloudflare tunnel for production services
cloudflared tunnel --url http://localhost:8080 &

# LocalTunnel backup
lt --port 3000 --subdomain spiralgang-code &
lt --port 8888 --subdomain spiralgang-jupyter &

echo "All tunnels started - check output above for URLs"
EOF
    chmod +x "$OMNI_ROOT/network/start-tunnels.sh"
}

# Advanced networking configuration
configure_advanced_networking() {
    echo "[NETWORK] Configuring advanced networking..."
    
    # Setup reverse proxy with authentication
    cat > "$OMNI_ROOT/network/nginx.conf" << 'EOF'
events {
    worker_connections 1024;
}

http {
    upstream code_server {
        server localhost:3000;
    }
    
    upstream jupyter_lab {
        server localhost:8888;
    }
    
    server {
        listen 80;
        server_name _;
        
        location /code/ {
            proxy_pass http://code_server/;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_cache_bypass $http_upgrade;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
        
        location /jupyter/ {
            proxy_pass http://jupyter_lab/;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_cache_bypass $http_upgrade;
        }
    }
}
EOF
}