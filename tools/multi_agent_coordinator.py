#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Multi-Agent Issue Coordinator
Automatically assigns AI agents to GitHub issues and creates pull requests
"""

import argparse
import json
import os
import sys
import time
import logging
import requests
from pathlib import Path
from typing import Dict, List, Optional, Tuple

# Setup logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class AgentCoordinator:
    def __init__(self, github_token: str, repo: str):
        self.github_token = github_token
        self.repo = repo
        self.base_url = f"https://api.github.com/repos/{repo}"
        self.headers = {
            "Authorization": f"token {github_token}",
            "Accept": "application/vnd.github.v3+json"
        }
        
        # Load agent configurations
        self.load_agent_configs()
        
        # Specialized agent assignments based on issue patterns
        self.agent_specializations = {
            "frontend": {
                "keywords": ["html", "css", "javascript", "frontend", "ui", "interface", "todo", "local storage"],
                "agent": "phi2",
                "description": "Frontend Specialist Agent - Handles UI, web development, and client-side features"
            },
            "infrastructure": {
                "keywords": ["workflow", "ci/cd", "repository", "structure", "setup", "maintenance", "upgrade"],
                "agent": "deepseek", 
                "description": "Infrastructure Agent - Manages repository structure, workflows, and system setup"
            },
            "security": {
                "keywords": ["security", "audit", "safeguard", "protection", "standards", "legacy"],
                "agent": "mixtral",
                "description": "Security & Standards Agent - Ensures code quality, security, and compliance"
            },
            "integration": {
                "keywords": ["agent", "ai", "integration", "coordination", "orchestration", "multi-model"],
                "agent": "qwen",
                "description": "Integration Specialist Agent - Handles AI agent coordination and system integration"
            }
        }

    def load_agent_configs(self):
        """Load agent configurations from repository"""
        try:
            with open("configs/agent_registry.json", "r") as f:
                self.agent_registry = json.load(f)
            logger.info("Loaded agent registry configuration")
        except FileNotFoundError:
            logger.warning("Agent registry not found, using defaults")
            self.agent_registry = {"agents": {}, "settings": {"default_agent": "deepseek"}}

    def analyze_issue(self, issue_data: Dict) -> Tuple[str, str]:
        """Analyze issue content and route through Mixtral coordinator as requested"""
        title = issue_data.get("title", "").lower()
        body = issue_data.get("body", "").lower()
        labels = [label["name"].lower() for label in issue_data.get("labels", [])]
        
        content = f"{title} {body} {' '.join(labels)}"
        
        # Score each specialization for Mixtral to coordinate
        scores = {}
        for spec_name, spec_config in self.agent_specializations.items():
            score = 0
            for keyword in spec_config["keywords"]:
                score += content.count(keyword.lower())
            scores[spec_name] = score
        
        # Get highest scoring specialization 
        best_spec = max(scores.items(), key=lambda x: x[1])
        
        # All coordination now goes through Mixtral on Replit as requested
        coordinator_agent = "mixtral"
        
        if best_spec[1] > 0:
            spec_config = self.agent_specializations[best_spec[0]]
            description = f"Mixtral Coordinator - Will delegate to {spec_config['agent']} for {spec_config['description']}"
            return coordinator_agent, description
        else:
            # Default coordination through Mixtral
            description = "Mixtral Coordinator - General purpose task coordination through Replit AI"
            return coordinator_agent, description

    def get_issue(self, issue_number: int) -> Optional[Dict]:
        """Fetch issue data from GitHub API"""
        try:
            response = requests.get(f"{self.base_url}/issues/{issue_number}", headers=self.headers)
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            logger.error(f"Failed to fetch issue #{issue_number}: {e}")
            return None

    def create_agent_branch(self, issue_number: int, agent_name: str) -> str:
        """Create or use the single AI coordination branch"""
        branch_name = "ai-coordination"  # Single branch for all AI work as requested
        
        try:
            # Check if ai-coordination branch exists
            try:
                branch_response = requests.get(f"{self.base_url}/git/refs/heads/{branch_name}", headers=self.headers)
                if branch_response.status_code == 200:
                    logger.info(f"Using existing {branch_name} branch")
                    return branch_name
            except requests.RequestException:
                pass
            
            # Get main branch SHA
            main_response = requests.get(f"{self.base_url}/git/refs/heads/main", headers=self.headers)
            main_response.raise_for_status()
            main_sha = main_response.json()["object"]["sha"]
            
            # Create new branch only if it doesn't exist
            branch_data = {
                "ref": f"refs/heads/{branch_name}",
                "sha": main_sha
            }
            
            branch_response = requests.post(f"{self.base_url}/git/refs", json=branch_data, headers=self.headers)
            if branch_response.status_code == 201:
                logger.info(f"Created branch: {branch_name}")
                return branch_name
            elif branch_response.status_code == 422:
                # Branch already exists
                logger.info(f"Branch already exists: {branch_name}")
                return branch_name
            else:
                branch_response.raise_for_status()
                
        except requests.RequestException as e:
            logger.error(f"Failed to create branch for issue #{issue_number}: {e}")
            return None

    def create_agent_pr(self, issue_number: int, agent_name: str, branch_name: str, issue_data: Dict) -> Optional[str]:
        """Create a pull request for Mixtral coordination on the ai-coordination branch"""
        
        pr_title = f"🧠 Mixtral AI Coordination: Issue #{issue_number} - {issue_data['title']}"
        
        pr_body = f"""# 🧠 Mixtral AI Coordination through Replit

## Issue Coordination
- **Issue:** #{issue_number} - {issue_data['title']}
- **Coordinator:** Mixtral (Replit AI)
- **Branch:** {branch_name} (Single branch for all AI work as requested)
- **Coordination Strategy:** All AI agents work through Mixtral central coordinator

## Original Issue Description
{issue_data.get('body', 'No description provided')}

## Mixtral Coordination Plan
This PR coordinates AI work through Mixtral on Replit as requested:

1. **Issue Analysis:** Mixtral analyzes requirements and task complexity
2. **Agent Delegation:** Coordinates with specialized agents (Phi2, DeepSeek, Qwen) as needed
3. **Work Segmentation:** Splits tasks into manageable segments for collaborative work
4. **Quality Control:** Ensures all work meets repository standards
5. **Integration:** Coordinates multiple AI agents working together on complex issues

## Single Branch Enforcement
- ✅ **Single Branch:** All AI work consolidated on `{branch_name}` branch
- ✅ **No Agent Branches:** Eliminates multiple agent-specific branches as requested  
- ✅ **Coordinated Workflow:** All AI agents coordinate through Mixtral central system
- ✅ **Manual + Auto:** Supports both automatic assignment and manual frontend interface

## Mixtral Integration
- **Replit AI:** Central coordination through Mixtral on Replit platform
- **Multi-Agent:** Can coordinate multiple AI agents for complex tasks
- **Task Segmentation:** Intelligent work distribution based on agent specializations
- **Quality Assurance:** Comprehensive review and validation of all AI work

## Review Requirements
- [ ] Mixtral coordination functioning correctly
- [ ] Single branch workflow enforced
- [ ] Manual AI interface accessible from frontend
- [ ] Auto-fix functionality preserved
- [ ] All AI work properly integrated and tested

---
*This PR was automatically created by the Mixtral AI Coordination System*
*Eliminates multi-branch complexity and centralizes AI work as requested*

Closes #{issue_number}
"""

        pr_data = {
            "title": pr_title,
            "body": pr_body,
            "head": branch_name,
            "base": "main",
            "draft": False
        }

        try:
            response = requests.post(f"{self.base_url}/pulls", json=pr_data, headers=self.headers)
            response.raise_for_status()
            pr_data = response.json()
            logger.info(f"Created PR #{pr_data['number']} for agent {agent_name} on issue #{issue_number}")
            return pr_data["html_url"]
        except requests.RequestException as e:
            logger.error(f"Failed to create PR for issue #{issue_number}: {e}")
            return None

    def assign_agent_to_issue(self, issue_number: int, force_reassign: bool = False) -> Dict:
        """Assign an AI agent to work on an issue"""
        result = {
            "issue_number": issue_number,
            "success": False,
            "agent": None,
            "pr_url": None,
            "error": None
        }

        # Get issue data
        issue_data = self.get_issue(issue_number)
        if not issue_data:
            result["error"] = f"Could not fetch issue #{issue_number}"
            return result

        # Check if already assigned (has open PR from ai-coordination branch)
        if not force_reassign:
            try:
                prs_response = requests.get(f"{self.base_url}/pulls", 
                                          params={"state": "open", "head": f"{self.repo.split('/')[0]}:ai-coordination"}, 
                                          headers=self.headers)
                if prs_response.ok:
                    existing_prs = [pr for pr in prs_response.json() 
                                  if f"Issue #{issue_number}" in pr["title"] or f"issue-{issue_number}" in pr["title"].lower()]
                    if existing_prs:
                        result["error"] = f"Issue #{issue_number} already has an active AI coordination PR: {existing_prs[0]['html_url']}"
                        return result
            except Exception as e:
                logger.warning(f"Could not check existing PRs: {e}")

        # Analyze issue to determine best agent
        agent_name, agent_description = self.analyze_issue(issue_data)
        result["agent"] = {"name": agent_name, "description": agent_description}

        # Create branch for agent
        branch_name = self.create_agent_branch(issue_number, agent_name)
        if not branch_name:
            result["error"] = f"Failed to create branch for issue #{issue_number}"
            return result

        # Create PR for agent
        pr_url = self.create_agent_pr(issue_number, agent_name, branch_name, issue_data)
        if not pr_url:
            result["error"] = f"Failed to create PR for issue #{issue_number}"
            return result

        result["success"] = True
        result["pr_url"] = pr_url
        
        # Log the assignment
        self.log_assignment(issue_number, agent_name, agent_description, pr_url)
        
        return result

    def log_assignment(self, issue_number: int, agent_name: str, agent_description: str, pr_url: str):
        """Log agent assignment for forensic tracking"""
        os.makedirs("logs", exist_ok=True)
        
        log_entry = {
            "ts": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
            "event": "agent_assigned",
            "issue_number": issue_number,
            "agent_name": agent_name,
            "agent_description": agent_description,
            "pr_url": pr_url,
            "repository": self.repo
        }
        
        with open("logs/agent_coordination.jsonl", "a") as f:
            f.write(json.dumps(log_entry) + "\n")

def main():
    parser = argparse.ArgumentParser(description="Multi-Agent Issue Coordinator")
    parser.add_argument("--issues", required=True, help="Comma-separated issue numbers")
    parser.add_argument("--force-reassign", default="false", help="Force reassignment")
    parser.add_argument("--repo", required=True, help="GitHub repository (owner/repo)")
    
    args = parser.parse_args()
    
    github_token = os.environ.get("GITHUB_TOKEN")
    if not github_token:
        logger.error("GITHUB_TOKEN environment variable not set")
        sys.exit(1)
    
    coordinator = AgentCoordinator(github_token, args.repo)
    
    # Parse issue numbers
    issue_numbers = [int(n.strip()) for n in args.issues.split(",") if n.strip()]
    force_reassign = args.force_reassign.lower() == "true"
    
    logger.info(f"Coordinating agents for issues: {issue_numbers}")
    logger.info(f"Force reassign: {force_reassign}")
    
    results = []
    for issue_number in issue_numbers:
        logger.info(f"Processing issue #{issue_number}")
        result = coordinator.assign_agent_to_issue(issue_number, force_reassign)
        results.append(result)
        
        if result["success"]:
            logger.info(f"✅ Issue #{issue_number} assigned to agent '{result['agent']['name']}' - PR: {result['pr_url']}")
        else:
            logger.error(f"❌ Failed to assign agent to issue #{issue_number}: {result['error']}")
    
    # Summary
    successful = [r for r in results if r["success"]]
    failed = [r for r in results if not r["success"]]
    
    print(f"\n📊 Coordination Summary:")
    print(f"✅ Successfully assigned: {len(successful)}")
    print(f"❌ Failed assignments: {len(failed)}")
    
    if successful:
        print(f"\n🤖 Agent Assignments:")
        for result in successful:
            print(f"  Issue #{result['issue_number']}: {result['agent']['name']} - {result['pr_url']}")
    
    if failed:
        print(f"\n⚠️ Failed Assignments:")
        for result in failed:
            print(f"  Issue #{result['issue_number']}: {result['error']}")

if __name__ == "__main__":
    main()