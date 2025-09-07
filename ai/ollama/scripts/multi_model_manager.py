# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Multi-Model AI Integration Manager
DevUtilityV2.5—InnovativeToolchestAI

Master integration script for managing all AI models with specialized roles:
- LLaMA 3.1 Core Model → "Big Bottle Sriracha" (Autonomous Bot Clone Delegator)
- Replit Code Assistant → "Orchestrator" (Code workflow coordination)
- Qwen Mathematical Reasoning → "Anti-Flail && Errors" (Error prevention & handling)
- DeepSeek R1 Reasoning → "CodeReaver (DevUtility-AI)" (Primary coding intelligence)
"""

import os
import sys
import subprocess
import json
from pathlib import Path
import importlib.util

class MultiModelManager:
    def __init__(self):
        self.base_path = Path(__file__).parent.parent
        self.scripts_path = Path(__file__).parent
        
        self.models = {
            "big-bottle-sriracha": {
                "name": "Big Bottle Sriracha",
                "alias": "LLaMA 3.1 Core (Autonomous Bot Clone Delegator)",
                "path": self.base_path / "llama3.1_ollama_v3",
                "script": self.scripts_path / "ollama_integration.py",
                "type": "Autonomous Bot Clone Delegator",
                "role": "Unrestricted AI delegating mini autonomous bot clones",
                "capabilities": ["autonomous_delegation", "bot_cloning", "unrestricted_ai"],
                "status": "active"
            },
            "orchestrator": {
                "name": "Orchestrator",
                "alias": "Replit Code Assistant (Workflow Coordinator)",
                "path": self.base_path / "replit-code-v1-3b",
                "script": self.scripts_path / "replit_code_integration.py",
                "type": "Code Workflow Orchestrator",
                "role": "Orchestrates and coordinates all coding workflows",
                "capabilities": ["code_generation", "workflow_orchestration", "task_coordination"],
                "status": "active"
            },
            "anti-flail-errors": {
                "name": "Anti-Flail && Errors",
                "alias": "Qwen Mathematical Reasoning (Error Prevention)",
                "path": self.base_path / "qwen2.5-math-1.5b",
                "script": self.scripts_path / "qwen_math_integration.py",
                "type": "Error Prevention & Handling",
                "role": "Prevents errors and handles mathematical reasoning to avoid flails",
                "capabilities": ["error_prevention", "mathematical_reasoning", "anti_flail_logic"],
                "status": "active"
            },
            "codereaver": {
                "name": "CodeReaver (DevUtility-AI)",
                "alias": "DeepSeek R1 Reasoning (Primary Coding Intelligence)",
                "path": self.base_path / "deepseek-r1",
                "script": self.scripts_path / "deepseek_r1_integration.py",
                "type": "Primary Coding Intelligence",
                "role": "Main coding AI with advanced reasoning and development capabilities",
                "capabilities": ["advanced_reasoning", "primary_coding", "devutility_ai", "deep_code_analysis"],
                "status": "active"
            }
        }
    
    def check_all_models(self):
        """Check the status of all AI models."""
        results = {}
        
        for model_id, model_info in self.models.items():
            try:
                if model_info["path"].exists():
                    if model_info["script"] and model_info["script"].exists():
                        # Run the model's status check
                        result = subprocess.run(
                            [sys.executable, str(model_info["script"]), "--status"],
                            capture_output=True,
                            text=True,
                            timeout=30
                        )
                        status = "Ready" if result.returncode == 0 else "Error"
                        message = result.stdout.strip() if result.stdout else result.stderr.strip()
                    else:
                        status = "Ready (Basic)"
                        message = "Model directory exists"
                else:
                    status = "Missing"
                    message = "Model directory not found"
                    
                results[model_id] = {
                    "name": model_info["name"],
                    "alias": model_info["alias"],
                    "role": model_info["role"],
                    "capabilities": model_info["capabilities"],
                    "status": status,
                    "message": message,
                    "type": model_info["type"]
                }
            except Exception as e:
                results[model_id] = {
                    "name": model_info["name"],
                    "alias": model_info["alias"],
                    "role": model_info["role"],
                    "capabilities": model_info["capabilities"],
                    "status": "Error",
                    "message": str(e),
                    "type": model_info["type"]
                }
        
        return results
    
    def activate_all_models(self):
        """Activate all AI models."""
        results = {}
        
        for model_id, model_info in self.models.items():
            try:
                if model_info["script"] and model_info["script"].exists():
                    result = subprocess.run(
                        [sys.executable, str(model_info["script"]), "--activate"],
                        capture_output=True,
                        text=True,
                        timeout=30
                    )
                    status = "Activated" if result.returncode == 0 else "Failed"
                    message = result.stdout.strip() if result.stdout else result.stderr.strip()
                else:
                    status = "Basic Ready"
                    message = "No activation script needed"
                
                results[model_id] = {
                    "name": model_info["name"],
                    "status": status,
                    "message": message
                }
            except Exception as e:
                results[model_id] = {
                    "name": model_info["name"],
                    "status": "Error",
                    "message": str(e)
                }
        
        return results
    
    def get_model_ensemble_info(self):
        """Get comprehensive information about the AI model ensemble."""
        ensemble_info = {
            "total_models": len(self.models),
            "model_types": {},
            "capabilities": [],
            "models": []
        }
        
        for model_id, model_info in self.models.items():
            model_type = model_info["type"]
            ensemble_info["model_types"][model_type] = ensemble_info["model_types"].get(model_type, 0) + 1
            
            model_data = {
                "id": model_id,
                "name": model_info["name"],
                "alias": model_info["alias"],
                "role": model_info["role"],
                "capabilities": model_info["capabilities"],
                "type": model_type,
                "path": str(model_info["path"]),
                "has_script": model_info["script"] is not None,
                "status": model_info["status"]
            }
            
            ensemble_info["models"].append(model_data)
        
        # Define ensemble capabilities based on new AI roles
        ensemble_info["capabilities"] = [
            "🔥 Big Bottle Sriracha: Unrestricted autonomous bot clone delegation",
            "🎭 Orchestrator: Advanced code workflow coordination and task management",
            "🛡️ Anti-Flail && Errors: Mathematical reasoning with error prevention logic",
            "⚔️ CodeReaver (DevUtility-AI): Primary coding intelligence with deep analysis",
            "🤖 Integrated autonomous AI development ecosystem",
            "💻 Advanced code generation and completion across all models",
            "🧠 Complex reasoning and analytical thinking coordination",
            "⚡ Specialized task routing and multi-model optimization"
        ]
        
        return ensemble_info
    
    def download_all_models(self):
        """Download full model files using git-lfs."""
        download_results = {}
        
        for model_id, model_info in self.models.items():
            if model_info["path"].exists():
                try:
                    print(f"🔄 Downloading full model for {model_info['name']}...")
                    
                    result = subprocess.run(
                        ["git", "lfs", "pull"],
                        cwd=model_info["path"],
                        capture_output=True,
                        text=True,
                        timeout=600  # 10 minutes timeout for large downloads
                    )
                    
                    if result.returncode == 0:
                        download_results[model_id] = "✅ Downloaded successfully"
                    else:
                        download_results[model_id] = f"⚠️  Warning: {result.stderr.strip()}"
                        
                except subprocess.TimeoutExpired:
                    download_results[model_id] = "⏱️  Timeout - large model download in progress"
                except Exception as e:
                    download_results[model_id] = f"❌ Error: {str(e)}"
            else:
                download_results[model_id] = "❌ Model directory not found"
        
        return download_results
    
    def create_model_interface(self, model_id, task, *args):
        """Create a unified interface to interact with any model."""
        if model_id not in self.models:
            return f"❌ Model '{model_id}' not found"
        
        model_info = self.models[model_id]
        
        if not model_info["script"] or not model_info["script"].exists():
            return f"⚠️  Model '{model_info['name']}' has no interactive script"
        
        try:
            # Build command based on task
            cmd = [sys.executable, str(model_info["script"])]
            
            if task == "info":
                cmd.append("--info")
            elif task == "capabilities":
                cmd.append("--capabilities")
            elif task == "generate" and args:
                if model_id == "replit-code":
                    cmd.extend(["--generate", args[0]])
                elif model_id == "qwen-math":
                    cmd.extend(["--solve", args[0]])
                elif model_id == "deepseek-r1":
                    cmd.extend(["--reason", args[0]])
            else:
                return f"⚠️  Task '{task}' not supported for model '{model_info['name']}'"
            
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=60)
            
            if result.returncode == 0:
                return result.stdout
            else:
                return f"❌ Error: {result.stderr}"
                
        except Exception as e:
            return f"❌ Error executing model interface: {str(e)}"
    
    def generate_integration_report(self):
        """Generate a comprehensive integration report."""
        report = []
        report.append("# DevUtility AI Model Integration Report")
        report.append("=" * 50)
        report.append("")
        
        # Model status
        model_status = self.check_all_models()
        report.append("## Model Status Summary")
        report.append("")
        
        for model_id, status in model_status.items():
            status_icon = "✅" if status["status"] in ["Ready", "Ready (Basic)"] else "❌"
            report.append(f"{status_icon} **{status['name']}** ({status['alias']})")
            report.append(f"   Role: {status['role']}")
            report.append(f"   Type: {status['type']}")
            report.append(f"   Status: {status['status']}")
            report.append(f"   Capabilities: {', '.join(status['capabilities'])}")
            if status.get("message"):
                report.append(f"   Details: {status['message']}")
            report.append("")
        
        # Ensemble info
        ensemble = self.get_model_ensemble_info()
        report.append("## AI Ensemble Capabilities")
        report.append("")
        for capability in ensemble["capabilities"]:
            report.append(f"- {capability}")
        report.append("")
        
        # Model types distribution
        report.append("## Model Type Distribution")
        report.append("")
        for model_type, count in ensemble["model_types"].items():
            report.append(f"- {model_type}: {count} model(s)")
        report.append("")
        
        return "\n".join(report)

def main():
    """Main CLI interface for multi-model AI management."""
    import argparse
    
    parser = argparse.ArgumentParser(description="Multi-Model AI Integration Manager")
    parser.add_argument("--status", action="store_true", help="Check status of all models")
    parser.add_argument("--activate", action="store_true", help="Activate all models")
    parser.add_argument("--info", action="store_true", help="Show ensemble information")
    parser.add_argument("--download", action="store_true", help="Download full model files")
    parser.add_argument("--report", action="store_true", help="Generate integration report")
    parser.add_argument("--model", type=str, help="Interact with specific model")
    parser.add_argument("--task", type=str, help="Task to perform with model")
    parser.add_argument("--input", type=str, help="Input for model task")
    
    args = parser.parse_args()
    
    manager = MultiModelManager()
    
    if args.status:
        results = manager.check_all_models()
        print("🤖 AI Model Status Check:")
        print("=" * 40)
        for model_id, status in results.items():
            status_icon = "✅" if status["status"] in ["Ready", "Ready (Basic)"] else "❌"
            print(f"{status_icon} {status['name']}")
            print(f"   Alias: {status['alias']}")
            print(f"   Role: {status['role']}")
            print(f"   Type: {status['type']}")
            print(f"   Status: {status['status']}")
            print(f"   Capabilities: {', '.join(status['capabilities'])}")
            if status.get("message"):
                print(f"   Details: {status['message']}")
            print()
    
    elif args.activate:
        results = manager.activate_all_models()
        print("🚀 Activating AI Models:")
        print("=" * 40)
        for model_id, result in results.items():
            status_icon = "✅" if result["status"] in ["Activated", "Basic Ready"] else "❌"
            print(f"{status_icon} {result['name']}: {result['status']}")
            if result.get("message"):
                print(f"   {result['message']}")
            print()
    
    elif args.info:
        info = manager.get_model_ensemble_info()
        print("🤖 AI Model Ensemble Information:")
        print("=" * 40)
        print(f"Total Models: {info['total_models']}")
        print("\nModel Types:")
        for model_type, count in info['model_types'].items():
            print(f"  {model_type}: {count}")
        print("\nCapabilities:")
        for capability in info['capabilities']:
            print(f"  {capability}")
        print("\nModels:")
        for model in info['models']:
            print(f"  - {model['name']} ({model['id']})")
            print(f"    Alias: {model['alias']}")
            print(f"    Role: {model['role']}")
            print(f"    Type: {model['type']}")
            print(f"    Capabilities: {', '.join(model['capabilities'])}")
            print(f"    Status: {model['status']}")
    
    elif args.download:
        print("🔄 Downloading full model files...")
        results = manager.download_all_models()
        print("\nDownload Results:")
        for model_id, result in results.items():
            print(f"  {manager.models[model_id]['name']}: {result}")
    
    elif args.report:
        report = manager.generate_integration_report()
        print(report)
    
    elif args.model and args.task:
        input_text = args.input or ""
        result = manager.create_model_interface(args.model, args.task, input_text)
        print(result)
    
    else:
        parser.print_help()
        print("\nAvailable models:")
        for model_id, model_info in manager.models.items():
            print(f"  {model_id}: {model_info['name']}")

if __name__ == "__main__":
    main()