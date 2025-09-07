#!/bin/bash
# -*- coding: utf-8 -*-
# Master deployment script for all AI function calling tools

set -euo pipefail

TOOLS_DIR="/home/runner/work/DevUl-Army--__--Living-Sriracha-AGI/DevUl-Army--__--Living-Sriracha-AGI/ai_function_calling_tools/hosting_configs"
TOOLS=(langchain_tools openai_function_calling autogen_tools semantic_kernel_skills crewai_tools haystack_tools llamaindex_tools anthropic_tools litellm_tools instructor_tools)

echo "🚀 Deploying all AI function calling tools..."

for tool in "${TOOLS[@]}"; do
    if [ -d "$TOOLS_DIR/$tool" ]; then
        echo "📦 Deploying $tool..."
        cd "$TOOLS_DIR/$tool"
        ./deploy.sh
        echo "✅ $tool deployed"
    else
        echo "⚠️  Tool $tool directory not found"
    fi
done

echo "🎉 All tools deployed successfully!"
