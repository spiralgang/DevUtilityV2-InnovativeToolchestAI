#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
CODE-REAVER Backend Agent Server
Local agent communication server
"""

import os
import sys
import json
from datetime import datetime
from pathlib import Path

class LocalAgentServer:
    def __init__(self):
        self.project_root = Path(__file__).parent.parent
        self.agent_config = {
            "deepseek": {
                "name": "DeepSeek R1",
                "model_path": "models/deepseek-r1",
                "script_path": "ai/ollama/scripts/deepseek_r1_integration.py",
                "capabilities": ["advanced_reasoning", "primary_coding", "devutility_ai"]
            },
            "phi2": {
                "name": "Phi-2",
                "model_path": "models/Phi-2",
                "capabilities": ["code_generation", "frontend_integration"]
            },
            "ollama": {
                "name": "Ollama", 
                "model_path": "models/ollama",
                "script_path": "ai/ollama/scripts/local_model_manager.py",
                "capabilities": ["local_serving", "multiple_models"]
            },
            "qwen": {
                "name": "Qwen 2.5",
                "model_path": "models/Qwen2.5-0.5B",
                "capabilities": ["multilingual", "glue_code"]
            },
            "mixtral": {
                "name": "Mixtral 8x7B",
                "model_path": "models/Mixtral-8x7B",
                "capabilities": ["high_performance", "complex_reasoning"]
            }
        }
    
    def process_request(self, agent: str, prompt: str, mode: str = "prompt"):
        """Process agent request"""
        if agent not in self.agent_config:
            raise ValueError(f"Unknown agent: {agent}")
        
        config = self.agent_config[agent]
        
        if mode == "status":
            model_exists = os.path.exists(config["model_path"])
            script_exists = config.get("script_path") and os.path.exists(config["script_path"])
            
            return {
                "agent": agent,
                "name": config["name"],
                "model_exists": model_exists,
                "script_exists": script_exists,
                "capabilities": config["capabilities"],
                "status": "ready" if model_exists else "model_missing"
            }
        
        elif mode == "info":
            return {
                "agent": agent,
                "name": config["name"],
                "model_path": config["model_path"],
                "capabilities": config["capabilities"],
                "description": f"Local {config['name']} agent instance"
            }
        
        elif mode in ["prompt", "file"]:
            return {
                "reply": f"[{config['name']}] Processed: {prompt[:50]}... (Local agent response)",
                "agent": agent,
                "capabilities_used": config["capabilities"][:2],
                "timestamp": datetime.utcnow().isoformat()
            }

if __name__ == "__main__":
    print("🚀 CODE-REAVER Local Agent Server")
    print("🤖 Available agents: deepseek, phi2, ollama, qwen, mixtral")
    print("🚫 NO COPILOT - Local agents only")
    
    server = LocalAgentServer()
    for agent in server.agent_config.keys():
        status = server.process_request(agent, "", "status")
        print(f"🔍 {agent}: {status['status']}")
