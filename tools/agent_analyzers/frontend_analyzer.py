#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Frontend Issue Analyzer for Phi2 Agent
Analyzes GitHub issues to extract frontend requirements
"""

import argparse
import json
import os
import sys
import re
import requests
from typing import Dict, List, Optional

class FrontendAnalyzer:
    def __init__(self):
        self.frontend_keywords = {
            "ui_components": ["button", "form", "input", "modal", "dialog", "menu", "navbar", "sidebar"],
            "technologies": ["html", "css", "javascript", "js", "react", "vue", "angular", "vanilla"],
            "features": ["responsive", "mobile", "desktop", "animation", "transition", "interactive"],
            "storage": ["localstorage", "local storage", "sessionstorage", "indexeddb", "persistence"],
            "frameworks": ["bootstrap", "tailwind", "material", "bulma", "foundation"],
            "compatibility": ["android", "ios", "mobile", "chrome", "firefox", "safari", "edge"]
        }
        
        self.complexity_indicators = {
            "simple": ["static", "basic", "simple", "minimal", "plain"],
            "moderate": ["interactive", "dynamic", "responsive", "forms", "validation"],
            "complex": ["spa", "single page", "framework", "bundling", "build process", "testing"]
        }

    def extract_issue_number(self, issue_input: str) -> Optional[int]:
        """Extract issue number from various input formats"""
        if issue_input.isdigit():
            return int(issue_input)
        
        # Try to extract from PR body or other text
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
            47: {
                "number": 47,
                "title": "To-Do List Application with Local Storage",
                "body": """## Overview
Create a simple to-do list application that persists tasks using local storage on the client side. The app should allow users to add, edit, delete, and mark tasks as completed, and all changes should be reflected in local storage for persistence across sessions.

## Requirements
- Users can add new tasks
- Users can edit existing tasks  
- Users can delete tasks
- Users can mark tasks as completed/incomplete
- Tasks are saved in local storage and restored on page reload

## Technical Specifications
- Frontend only (no backend required)
- Use vanilla JavaScript, HTML, and CSS (no frameworks)
- Code should be compatible with Android 10 browser (ES6 minimum, avoid newer APIs)
- UI should be responsive and simple

## Acceptance Criteria
- All functionality must work without requiring any network connection
- All task changes persist after reload (verify via local storage)
- Clean, commented code and minimal UI""",
                "labels": [{"name": "frontend"}, {"name": "javascript"}, {"name": "enhancement"}]
            }
        }
        
        return mock_issues.get(issue_number, {
            "number": issue_number,
            "title": "Unknown Issue",
            "body": "Issue data not available",
            "labels": []
        })

    def analyze_content(self, content: str) -> Dict:
        """Analyze text content for frontend requirements"""
        content_lower = content.lower()
        
        # Extract technology requirements
        technologies = []
        for tech in self.frontend_keywords["technologies"]:
            if tech in content_lower:
                technologies.append(tech)
        
        # Extract UI components needed
        ui_components = []
        for component in self.frontend_keywords["ui_components"]:
            if component in content_lower:
                ui_components.append(component)
        
        # Extract features
        features = []
        for feature in self.frontend_keywords["features"]:
            if feature in content_lower:
                features.append(feature)
        
        # Extract storage requirements
        storage_needs = []
        for storage in self.frontend_keywords["storage"]:
            if storage in content_lower:
                storage_needs.append(storage)
        
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
            "technologies": technologies,
            "ui_components": ui_components,
            "features": features,
            "storage_needs": storage_needs,
            "complexity": complexity,
            "requirements": requirements[:10]  # Limit to 10 most relevant
        }

    def analyze_issue(self, issue_number: int) -> Dict:
        """Main analysis function"""
        # Fetch issue data
        issue_data = self.fetch_issue_data(issue_number)
        if not issue_data:
            return {"error": f"Could not fetch issue #{issue_number}"}
        
        # Combine title and body for analysis
        title = issue_data.get("title", "")
        body = issue_data.get("body", "")
        labels = [label.get("name", "") for label in issue_data.get("labels", [])]
        
        content = f"{title}\n{body}\n{' '.join(labels)}"
        
        # Analyze content
        analysis = self.analyze_content(content)
        
        # Build comprehensive analysis result
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
            "architecture": "vanilla",
            "file_structure": ["index.html", "styles.css", "script.js"],
            "key_features": [],
            "browser_compatibility": "ES6+ (Android 10 compatible)",
            "testing_strategy": "Manual testing in browser"
        }
        
        # Adjust based on storage needs
        if "localstorage" in analysis["storage_needs"] or "local storage" in analysis["storage_needs"]:
            recommendations["key_features"].append("localStorage integration")
            recommendations["file_structure"].append("storage.js")
        
        # Adjust based on complexity
        if analysis["complexity"] == "complex":
            recommendations["file_structure"].extend(["utils.js", "config.js"])
            recommendations["testing_strategy"] = "Unit tests + manual testing"
        
        # Add responsive design if mobile mentioned
        if "mobile" in analysis["features"] or "responsive" in analysis["features"]:
            recommendations["key_features"].append("responsive design")
            recommendations["css_framework"] = "custom responsive CSS"
        
        return recommendations

    def assess_agent_suitability(self, analysis: Dict) -> Dict:
        """Assess how suitable this issue is for the Frontend Agent"""
        score = 0
        reasons = []
        
        # Technology match
        frontend_techs = ["html", "css", "javascript", "js", "vanilla"]
        for tech in analysis["technologies"]:
            if tech in frontend_techs:
                score += 2
                reasons.append(f"Uses {tech}")
        
        # UI components
        if analysis["ui_components"]:
            score += 3
            reasons.append(f"Requires UI components: {', '.join(analysis['ui_components'])}")
        
        # Storage needs (frontend specialization)
        if analysis["storage_needs"]:
            score += 2
            reasons.append("Requires client-side storage")
        
        # Complexity appropriateness
        if analysis["complexity"] in ["simple", "moderate"]:
            score += 1
            reasons.append(f"Appropriate complexity level: {analysis['complexity']}")
        
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
        base_hours = 2
        
        # Adjust for complexity
        complexity_multipliers = {
            "simple": 1.0,
            "moderate": 1.5,
            "complex": 2.5
        }
        multiplier = complexity_multipliers.get(analysis["complexity"], 1.0)
        
        # Adjust for features
        feature_hours = len(analysis["ui_components"]) * 0.5
        storage_hours = 1 if analysis["storage_needs"] else 0
        
        total_hours = (base_hours + feature_hours + storage_hours) * multiplier
        
        return {
            "estimated_hours": round(total_hours, 1),
            "complexity_factor": multiplier,
            "breakdown": {
                "base_implementation": base_hours,
                "ui_components": feature_hours,
                "storage_integration": storage_hours,
                "complexity_adjustment": f"{multiplier}x"
            }
        }

def main():
    parser = argparse.ArgumentParser(description="Frontend Issue Analyzer")
    parser.add_argument("--issue", required=True, help="Issue number or content")
    parser.add_argument("--output", required=True, help="Output JSON file")
    
    args = parser.parse_args()
    
    analyzer = FrontendAnalyzer()
    
    # Extract issue number
    issue_number = analyzer.extract_issue_number(args.issue)
    if not issue_number:
        print(f"Error: Could not extract issue number from '{args.issue}'", file=sys.stderr)
        sys.exit(1)
    
    # Analyze issue
    print(f"Analyzing issue #{issue_number} for frontend requirements...")
    result = analyzer.analyze_issue(issue_number)
    
    if "error" in result:
        print(f"Error: {result['error']}", file=sys.stderr)
        sys.exit(1)
    
    # Save results
    with open(args.output, "w") as f:
        json.dump(result, f, indent=2)
    
    print(f"Analysis complete. Results saved to {args.output}")
    print(f"Agent suitability: {result['agent_suitability']['level']} ({result['agent_suitability']['confidence']}% confidence)")
    print(f"Estimated effort: {result['estimated_effort']['estimated_hours']} hours")

if __name__ == "__main__":
    main()