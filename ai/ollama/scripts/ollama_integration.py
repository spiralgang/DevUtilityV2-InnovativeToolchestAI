#!/usr/bin/env python3
"""
Ollama AI Core Integration for DevUtilityV2.5—InnovativeToolchestAI

This script provides the bridge between the Ollama AI core and the DevUtility 
agentic standards, enabling enhanced coding assistance and conflict resolution.
"""

import os
import sys
import json
import subprocess
import logging
from pathlib import Path
from typing import Optional, Dict, Any

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger('ollama_integration')

class OllamaAICore:
    """Ollama AI Core integration for DevUtility"""
    
    def __init__(self, model_path: Optional[str] = None):
        self.script_dir = Path(__file__).parent
        self.ai_dir = self.script_dir.parent
        self.repo_root = self.ai_dir.parent.parent
        
        # Default to the cloned model directory
        if model_path is None:
            model_path = self.ai_dir / "llama3.1_ollama_v3"
        
        self.model_path = Path(model_path)
        self.config = self._load_config()
        
    def _load_config(self) -> Dict[str, Any]:
        """Load model configuration"""
        config_file = self.model_path / "config.json"
        if config_file.exists():
            with open(config_file, 'r') as f:
                return json.load(f)
        return {"model_type": "llama"}
    
    def is_model_available(self) -> bool:
        """Check if the AI model is available and ready"""
        if not self.model_path.exists():
            logger.warning(f"Model path does not exist: {self.model_path}")
            return False
            
        # Check for GGUF files
        gguf_files = list(self.model_path.glob("*.gguf"))
        if not gguf_files:
            logger.warning("No GGUF model files found")
            return False
            
        # Check if files are actual models or LFS pointers
        for gguf_file in gguf_files:
            if gguf_file.stat().st_size < 1000:  # LFS pointer files are small
                logger.info(f"Model file {gguf_file.name} is a Git LFS pointer")
                logger.info("Run 'git lfs pull' in the model directory to download actual files")
                return False
                
        return True
    
    def is_ollama_installed(self) -> bool:
        """Check if Ollama is installed"""
        try:
            result = subprocess.run(['ollama', '--version'], 
                                 capture_output=True, text=True)
            return result.returncode == 0
        except FileNotFoundError:
            return False
    
    def install_ollama(self) -> bool:
        """Install Ollama if not present"""
        if self.is_ollama_installed():
            logger.info("Ollama is already installed")
            return True
            
        logger.info("Installing Ollama...")
        try:
            # Use the official install script
            result = subprocess.run([
                'curl', '-fsSL', 'https://ollama.ai/install.sh'
            ], capture_output=True, text=True)
            
            if result.returncode == 0:
                # Execute the install script
                install_result = subprocess.run(['sh'], 
                                              input=result.stdout, 
                                              text=True)
                return install_result.returncode == 0
            return False
        except Exception as e:
            logger.error(f"Failed to install Ollama: {e}")
            return False
    
    def setup_integration(self) -> bool:
        """Set up the complete Ollama integration"""
        logger.info("🚀 Setting up Ollama AI Core integration...")
        
        # Step 1: Check model availability
        if not self.is_model_available():
            logger.warning("⚠️  Model files not fully available (may be LFS pointers)")
            logger.info("💡 For full functionality, download model files with 'git lfs pull'")
        
        # Step 2: Install Ollama if needed
        if not self.install_ollama():
            logger.error("❌ Failed to install Ollama")
            return False
            
        # Step 3: Create integration points with existing DevUtility systems
        self._create_integration_points()
        
        logger.info("✅ Ollama AI Core integration setup complete!")
        return True
    
    def _create_integration_points(self):
        """Create integration points with existing DevUtility systems"""
        
        # Integration with conflict resolution system
        conflict_integration = self.repo_root / "scripts" / "ollama_conflict_assistant.py"
        if not conflict_integration.exists():
            self._create_conflict_assistant(conflict_integration)
        
        # Integration with development tools
        dev_integration = self.ai_dir / "dev_assistant.py"
        if not dev_integration.exists():
            self._create_dev_assistant(dev_integration)
    
    def _create_conflict_assistant(self, target_path: Path):
        """Create conflict resolution assistant using Ollama AI"""
        content = '''#!/usr/bin/env python3
"""
Ollama-powered Conflict Resolution Assistant
Integrates with existing DevUtility conflict resolution system
"""

import sys
from pathlib import Path

# Add the AI core to the path
ai_dir = Path(__file__).parent.parent / "ai" / "ollama"
sys.path.insert(0, str(ai_dir))

def analyze_conflict_with_ai(conflict_data):
    """Analyze conflicts using Ollama AI core"""
    # This would integrate with the Ollama API once models are loaded
    print("🤖 AI-powered conflict analysis (Ollama integration placeholder)")
    return {"analysis": "AI analysis pending full model download"}

if __name__ == "__main__":
    print("Ollama Conflict Assistant - Ready for integration")
'''
        with open(target_path, 'w') as f:
            f.write(content)
        target_path.chmod(0o755)
        logger.info(f"Created conflict assistant: {target_path}")
    
    def _create_dev_assistant(self, target_path: Path):
        """Create development assistant using Ollama AI"""
        content = '''#!/usr/bin/env python3
"""
Ollama Development Assistant
Provides AI-powered coding assistance for DevUtility development
"""

class OllamaDeveloperAssistant:
    def __init__(self):
        self.model_name = "llama3.1_ollama_v3"
        
    def get_code_suggestions(self, code_context):
        """Get AI-powered code suggestions"""
        # Placeholder for Ollama API integration
        return "AI suggestions pending full model setup"
        
    def analyze_code_quality(self, file_path):
        """Analyze code quality using AI"""
        # Placeholder for Ollama API integration
        return "AI code analysis pending full model setup"

if __name__ == "__main__":
    assistant = OllamaDeveloperAssistant()
    print("🤖 Ollama Development Assistant - Ready")
'''
        with open(target_path, 'w') as f:
            f.write(content)
        target_path.chmod(0o755)
        logger.info(f"Created development assistant: {target_path}")
    
    def status(self) -> Dict[str, Any]:
        """Get the current status of the Ollama integration"""
        return {
            "model_path": str(self.model_path),
            "model_exists": self.model_path.exists(),
            "model_available": self.is_model_available(),
            "ollama_installed": self.is_ollama_installed(),
            "config": self.config,
            "integration_ready": (self.model_path.exists() and 
                                self.is_ollama_installed())
        }

def main():
    """Main entry point"""
    import argparse
    
    parser = argparse.ArgumentParser(description="Ollama AI Core Integration")
    parser.add_argument('--setup', action='store_true', 
                       help='Set up the Ollama integration')
    parser.add_argument('--status', action='store_true',
                       help='Show integration status')
    parser.add_argument('--model-path', type=str,
                       help='Path to the Ollama model directory')
    
    args = parser.parse_args()
    
    # Initialize the AI core
    ai_core = OllamaAICore(model_path=args.model_path)
    
    if args.setup:
        success = ai_core.setup_integration()
        sys.exit(0 if success else 1)
    elif args.status:
        status = ai_core.status()
        print(json.dumps(status, indent=2))
    else:
        parser.print_help()

if __name__ == "__main__":
    main()