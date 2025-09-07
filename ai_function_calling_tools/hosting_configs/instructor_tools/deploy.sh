#!/bin/bash
# -*- coding: utf-8 -*-
# Deployment script for instructor_tools

set -euo pipefail

TOOL_NAME="instructor_tools"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "🚀 Deploying $TOOL_NAME..."

# Build Docker image
echo "🏗️  Building Docker image..."
docker build -t "$TOOL_NAME:latest" "$SCRIPT_DIR"

# Stop existing container if running
echo "🛑 Stopping existing container..."
docker stop "$TOOL_NAME" 2>/dev/null || true
docker rm "$TOOL_NAME" 2>/dev/null || true

# Start new container
echo "▶️  Starting new container..."
docker-compose -f "$SCRIPT_DIR/docker-compose.yml" up -d

# Wait for service to be ready
echo "⏳ Waiting for service to be ready..."
sleep 10

# Health check
echo "🏥 Performing health check..."
curl -f http://localhost:8000/health || {
    echo "❌ Health check failed"
    docker-compose -f "$SCRIPT_DIR/docker-compose.yml" logs
    exit 1
}

echo "✅ $TOOL_NAME deployed successfully!"
echo "🌐 API available at: http://localhost:8000"
echo "📚 Documentation at: http://localhost:8000/docs"
