#!/bin/bash
# -*- coding: utf-8 -*-
# Status checking script for all AI function calling tools

set -euo pipefail

TOOLS=(langchain_tools openai_function_calling autogen_tools semantic_kernel_skills crewai_tools haystack_tools llamaindex_tools anthropic_tools litellm_tools instructor_tools)

echo "📊 Checking status of all AI function calling tools..."

for tool in "${TOOLS[@]}"; do
    echo "🔍 Checking $tool..."
    
    # Check if container is running
    if docker ps | grep -q "$tool"; then
        echo "  ✅ Container running"
        
        # Health check
        if curl -f "http://localhost:8000/health" >/dev/null 2>&1; then
            echo "  ✅ Health check passed"
        else
            echo "  ❌ Health check failed"
        fi
    else
        echo "  ❌ Container not running"
    fi
    
    echo ""
done
