#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Infrastructure Issue Analyzer for DeepSeek Agent
Analyzes GitHub issues to extract infrastructure and workflow requirements
"""

import argparse
import json
import os
import sys
import re
import requests
from typing import Dict, List, Optional

class InfrastructureAnalyzer:
    def __init__(self):
        self.infrastructure_keywords = {
            "workflow_tech": ["ci/cd", "github actions", "workflow", "pipeline", "automation", "build", "deploy"],
            "infrastructure": ["docker", "kubernetes", "terraform", "ansible", "infrastructure", "devops"],
            "repository": ["structure", "organization", "setup", "maintenance", "cleanup", "standards"],
            "security": ["security", "audit", "scan", "vulnerability", "compliance", "standards"],
            "monitoring": ["monitoring", "logging", "metrics", "observability", "health", "status"],
            "quality": ["quality", "standards", "lint", "test", "validation", "review"]
        }
        
        self.complexity_indicators = {
            "simple": ["basic", "simple", "minimal", "single"],
            "moderate": ["multiple", "integration", "coordination", "workflow"],
            "complex": ["enterprise", "production", "scalable", "distributed", "orchestration"]
        }

    def extract_issue_number(self, issue_input: str) -> Optional[int]:
        """Extract issue number from various input formats"""
        if issue_input.isdigit():
            return int(issue_input)
        
        match = re.search(r'#(\d+)', issue_input)
        if match:
            return int(match.group(1))
            
        return None

    def fetch_issue_data(self, issue_number: int) -> Optional[Dict]:
        """Fetch issue data from GitHub API"""
        github_token = os.environ.get("GITHUB_TOKEN")
        if not github_token:
            print("Warning: No GITHUB_TOKEN found, using mock data", file=sys.stderr)
            return self.get_mock_issue_data(issue_number)
        
        repo = os.environ.get("GITHUB_REPOSITORY", "spiralgang/DevUl-Army--__--Living-Sriracha-AGI")
        
        headers = {
            "Authorization": f"token {github_token}",
            "Accept": "application/vnd.github.v3+json"
        }
        
        try:
            response = requests.get(f"https://api.github.com/repos/{repo}/issues/{issue_number}", headers=headers)
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            print(f"Error fetching issue #{issue_number}: {e}", file=sys.stderr)
            return self.get_mock_issue_data(issue_number)

    def get_mock_issue_data(self, issue_number: int) -> Dict:
        """Provide mock data for known issues"""
        mock_issues = {
            45: {
                "number": 45,
                "title": "Systematically upgrade all legacy workflows and agent logic to advanced, production-grade standards",
                "body": """The repository currently contains a mix of advanced developmental stages and unstable, legacy or mutated agent workflows (including Copilot and unvetted automation). Upgrade all backend and frontend agent logic, workflows, and integration paths to the highest standards as exemplified by CODE-REAVER, living-code, and Android 10+ compatibility. Ensure only distributed, secure, and locally auditable AI agents are active. Remove or refactor all Copilot-dependent, mutation-prone, or destructive legacy logic. Reference vault: /reference for foundational standards.

**Priority:** Maintain all properly good developmental stages; upgrade or quarantine the rest to advanced agentic standards. All upgrades must pass CODE-REAVER audit, preserve Android 10 compatibility, and enforce local autonomy (NO COPILOT).""",
                "labels": [{"name": "enhancement"}, {"name": "Possible security concern"}, {"name": "LINEAR DEVELOPMENT"}]
            },
            42: {
                "number": 42,
                "title": "STRUCTURE REPOSITORY STRUCTURE SETUP && MAINTAN",
                "body": "Setup and maintain repository structure with GitHub-native automation and agent integration.",
                "labels": [{"name": "bug"}, {"name": "documentation"}, {"name": "help wanted"}]
            }
        }
        
        return mock_issues.get(issue_number, {
            "number": issue_number,
            "title": "Unknown Issue",
            "body": "Issue data not available",
            "labels": []
        })

    def analyze_content(self, content: str) -> Dict:
        """Analyze text content for infrastructure requirements"""
        content_lower = content.lower()
        
        # Extract workflow technologies
        workflow_tech = []
        for tech in self.infrastructure_keywords["workflow_tech"]:
            if tech in content_lower:
                workflow_tech.append(tech)
        
        # Extract infrastructure components
        infrastructure = []
        for component in self.infrastructure_keywords["infrastructure"]:
            if component in content_lower:
                infrastructure.append(component)
        
        # Extract repository needs
        repository_needs = []
        for need in self.infrastructure_keywords["repository"]:
            if need in content_lower:
                repository_needs.append(need)
        
        # Extract security requirements
        security_needs = []
        for security in self.infrastructure_keywords["security"]:
            if security in content_lower:
                security_needs.append(security)
        
        # Extract monitoring requirements
        monitoring_needs = []
        for monitoring in self.infrastructure_keywords["monitoring"]:
            if monitoring in content_lower:
                monitoring_needs.append(monitoring)
        
        # Extract quality requirements
        quality_needs = []
        for quality in self.infrastructure_keywords["quality"]:
            if quality in content_lower:
                quality_needs.append(quality)
        
        # Determine complexity
        complexity = "simple"
        for level, indicators in self.complexity_indicators.items():
            for indicator in indicators:
                if indicator in content_lower:
                    if level == "complex":
                        complexity = "complex"
                        break
                    elif level == "moderate" and complexity == "simple":
                        complexity = "moderate"
        
        # Extract specific requirements
        requirements = []
        lines = content.split('\n')
        for line in lines:
            line = line.strip()
            if line.startswith('-') or line.startswith('*') or 'must' in line.lower() or 'should' in line.lower():
                requirements.append(line)
        
        return {
            "workflow_tech": workflow_tech,
            "infrastructure": infrastructure,
            "repository_needs": repository_needs,
            "security_needs": security_needs,
            "monitoring_needs": monitoring_needs,
            "quality_needs": quality_needs,
            "complexity": complexity,
            "requirements": requirements[:10]
        }

    def analyze_issue(self, issue_number: int) -> Dict:
        """Main analysis function"""
        issue_data = self.fetch_issue_data(issue_number)
        if not issue_data:
            return {"error": f"Could not fetch issue #{issue_number}"}
        
        title = issue_data.get("title", "")
        body = issue_data.get("body", "")
        labels = [label.get("name", "") for label in issue_data.get("labels", [])]
        
        content = f"{title}\n{body}\n{' '.join(labels)}"
        
        analysis = self.analyze_content(content)
        
        result = {
            "issue": {
                "number": issue_number,
                "title": title,
                "body": body,
                "labels": labels
            },
            "analysis": analysis,
            "recommendations": self.generate_recommendations(analysis),
            "agent_suitability": self.assess_agent_suitability(analysis),
            "estimated_effort": self.estimate_effort(analysis)
        }
        
        return result

    def generate_recommendations(self, analysis: Dict) -> Dict:
        """Generate implementation recommendations"""
        recommendations = {
            "architecture": "production-grade",
            "workflow_files": [".github/workflows/enhanced/"],
            "key_features": [],
            "security_level": "enterprise",
            "monitoring_strategy": "comprehensive",
            "quality_gates": "mandatory"
        }
        
        # Adjust based on workflow tech needs
        if analysis["workflow_tech"]:
            recommendations["key_features"].append("Enhanced CI/CD pipeline")
            recommendations["workflow_files"].extend(["advanced-ci.yml", "agent-orchestration.yml"])
        
        # Adjust based on security needs
        if analysis["security_needs"]:
            recommendations["key_features"].append("Security scanning integration")
            recommendations["security_tools"] = ["CodeQL", "dependency scanning", "SAST"]
        
        # Adjust based on complexity
        if analysis["complexity"] == "complex":
            recommendations["deployment_strategy"] = "blue-green"
            recommendations["monitoring_level"] = "enterprise"
        
        return recommendations

    def assess_agent_suitability(self, analysis: Dict) -> Dict:
        """Assess how suitable this issue is for the Infrastructure Agent"""
        score = 0
        reasons = []
        
        # Workflow technology match
        if analysis["workflow_tech"]:
            score += 3
            reasons.append(f"Workflow technologies: {', '.join(analysis['workflow_tech'])}")
        
        # Infrastructure components
        if analysis["infrastructure"]:
            score += 2
            reasons.append(f"Infrastructure needs: {', '.join(analysis['infrastructure'])}")
        
        # Repository management
        if analysis["repository_needs"]:
            score += 3
            reasons.append(f"Repository management: {', '.join(analysis['repository_needs'])}")
        
        # Security requirements
        if analysis["security_needs"]:
            score += 2
            reasons.append("Security and compliance requirements")
        
        # Quality requirements
        if analysis["quality_needs"]:
            score += 1
            reasons.append("Quality standards and testing")
        
        suitability = "low"
        if score >= 7:
            suitability = "high"
        elif score >= 4:
            suitability = "medium"
        
        return {
            "score": score,
            "level": suitability,
            "reasons": reasons,
            "confidence": min(100, score * 10)
        }

    def estimate_effort(self, analysis: Dict) -> Dict:
        """Estimate development effort"""
        base_hours = 4  # Infrastructure work typically takes longer
        
        complexity_multipliers = {
            "simple": 1.0,
            "moderate": 1.8,
            "complex": 3.0
        }
        multiplier = complexity_multipliers.get(analysis["complexity"], 1.0)
        
        # Adjust for components
        workflow_hours = len(analysis["workflow_tech"]) * 2
        security_hours = 3 if analysis["security_needs"] else 0
        monitoring_hours = 2 if analysis["monitoring_needs"] else 0
        
        total_hours = (base_hours + workflow_hours + security_hours + monitoring_hours) * multiplier
        
        return {
            "estimated_hours": round(total_hours, 1),
            "complexity_factor": multiplier,
            "breakdown": {
                "base_infrastructure": base_hours,
                "workflow_integration": workflow_hours,
                "security_implementation": security_hours,
                "monitoring_setup": monitoring_hours,
                "complexity_adjustment": f"{multiplier}x"
            }
        }

def main():
    parser = argparse.ArgumentParser(description="Infrastructure Issue Analyzer")
    parser.add_argument("--issue", required=True, help="Issue number or content")
    parser.add_argument("--output", required=True, help="Output JSON file")
    
    args = parser.parse_args()
    
    analyzer = InfrastructureAnalyzer()
    
    issue_number = analyzer.extract_issue_number(args.issue)
    if not issue_number:
        print(f"Error: Could not extract issue number from '{args.issue}'", file=sys.stderr)
        sys.exit(1)
    
    print(f"Analyzing issue #{issue_number} for infrastructure requirements...")
    result = analyzer.analyze_issue(issue_number)
    
    if "error" in result:
        print(f"Error: {result['error']}", file=sys.stderr)
        sys.exit(1)
    
    with open(args.output, "w") as f:
        json.dump(result, f, indent=2)
    
    print(f"Analysis complete. Results saved to {args.output}")
    print(f"Agent suitability: {result['agent_suitability']['level']} ({result['agent_suitability']['confidence']}% confidence)")
    print(f"Estimated effort: {result['estimated_effort']['estimated_hours']} hours")

if __name__ == "__main__":
    main()