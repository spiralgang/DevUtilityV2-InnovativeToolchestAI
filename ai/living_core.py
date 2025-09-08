#!/usr/bin/env python3
"""
# Bridges: core_engine_bridge
Living Core - Assimilated from tools/living-code-coreV2!!.js
Provides the core living code engine for agent coordination
"""

import json
import time
import logging
from pathlib import Path

class LivingCore:
    def __init__(self, config_path="configs/activation_rules.json"):
        self.config_path = config_path
        self.activation_rules = self.load_activation_rules()
        self.logger = logging.getLogger(__name__)
        
    def load_activation_rules(self):
        """Load activation rules from configuration"""
        try:
            with open(self.config_path, 'r') as f:
                return json.load(f)
        except Exception as e:
            self.logger.error(f"Failed to load activation rules: {e}")
            return {}
    
    def should_activate(self, branch, path, agent_type):
        """Determine if agent should activate based on rules"""
        rules = self.activation_rules
        
        # Check branch patterns
        branch_match = any(
            self._pattern_match(branch, pattern)
            for pattern in rules.get("branches", [])
        )
        
        # Check path patterns
        path_match = any(
            path.startswith(pattern.rstrip("*"))
            for pattern in rules.get("paths", [])
        )
        
        # Check agent type
        agent_match = agent_type in rules.get("agents", [])
        
        return branch_match and path_match and agent_match
    
    def _pattern_match(self, text, pattern):
        """Simple pattern matching with * wildcard"""
        if "*" not in pattern:
            return text == pattern
        
        prefix = pattern.split("*")[0]
        return text.startswith(prefix)
    
    def log_activation(self, event_type, details):
        """Log activation event with forensic data"""
        log_entry = {
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
            "event": event_type,
            "details": details
        }
        
        log_path = Path("logs/living_core.jsonl")
        log_path.parent.mkdir(exist_ok=True)
        
        with open(log_path, "a") as f:
            f.write(json.dumps(log_entry) + "\n")

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    core = LivingCore()
    core.logger.info("Living Core initialized")