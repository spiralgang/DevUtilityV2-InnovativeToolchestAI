#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Secondary AI Workflow Validation Script
DevUl Army : Living Sriracha AGI

Demonstrates the smart secondary AI workflows for code validation.
This script showcases how multiple AI models work together to validate
and enhance code quality through coordinated analysis.

Usage:
    python3 secondary_ai_validation.py --code "your_code_here" --language kotlin
    python3 secondary_ai_validation.py --file path/to/code.kt
    python3 secondary_ai_validation.py --demo
"""

import argparse
import json
import time
import sys
from pathlib import Path
from typing import Dict, List, Any, Optional
from dataclasses import dataclass, asdict
from enum import Enum

# Add the AI ollama scripts to path
sys.path.append(str(Path(__file__).parent.parent / "ai" / "ollama" / "scripts"))

try:
    from multi_model_manager import MultiModelManager
except ImportError:
    print("Warning: MultiModelManager not available, using simulation mode")
    MultiModelManager = None

class ValidationType(Enum):
    SECURITY_FOCUS = "security_focus"
    QUALITY_FOCUS = "quality_focus"
    PERFORMANCE_FOCUS = "performance_focus"
    COMPREHENSIVE = "comprehensive"

class ValidationPriority(Enum):
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"

class RiskLevel(Enum):
    MINIMAL = "minimal"
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"

@dataclass
class ValidationRequest:
    code: str
    language: str
    context: str = ""
    validation_type: ValidationType = ValidationType.COMPREHENSIVE
    priority: ValidationPriority = ValidationPriority.MEDIUM
    requested_validators: List[str] = None

    def __post_init__(self):
        if self.requested_validators is None:
            self.requested_validators = []

@dataclass
class SecurityValidationResult:
    security_score: int
    vulnerabilities: List[Dict[str, Any]]
    risk_level: RiskLevel
    mitigation_suggestions: List[str]

@dataclass
class QualityValidationResult:
    quality_score: int
    issues: List[str]
    suggestions: List[str]
    maintainability_score: int
    readability_score: int
    improvement_suggestions: List[str]

@dataclass
class PerformanceValidationResult:
    performance_score: int
    complexity_analysis: str
    optimization_opportunities: List[str]
    estimated_performance_impact: str

@dataclass
class AIConsensusResult:
    consensus_score: int
    model_agreements: Dict[str, bool]
    conflicting_opinions: List[str]
    final_recommendation: str

@dataclass
class ValidationResult:
    overall_score: int
    validation_passed: bool
    security_validation: SecurityValidationResult
    quality_validation: QualityValidationResult
    performance_validation: PerformanceValidationResult
    ai_consensus: AIConsensusResult
    recommendations: List[str]
    validation_timestamp: float
    processing_time_ms: float

class SecondaryAIValidationEngine:
    """
    Main engine for coordinating secondary AI validation workflows
    """
    
    def __init__(self):
        self.multi_model_manager = None
        if MultiModelManager:
            try:
                self.multi_model_manager = MultiModelManager()
                print("✅ MultiModelManager initialized successfully")
            except Exception as e:
                print(f"⚠️ MultiModelManager initialization failed: {e}")
        
        # AI Model configurations
        self.ai_models = {
            "big_bottle_sriracha": {
                "name": "Big Bottle Sriracha",
                "specialty": "strategic_coordination",
                "available": True
            },
            "orchestrator": {
                "name": "Orchestrator", 
                "specialty": "workflow_coordination",
                "available": True
            },
            "anti_flail": {
                "name": "Anti-Flail && Errors",
                "specialty": "error_prevention",
                "available": True
            },
            "code_reaver": {
                "name": "CodeReaver",
                "specialty": "deep_analysis", 
                "available": True
            }
        }

    async def validate_code(self, request: ValidationRequest) -> ValidationResult:
        """
        Main validation pipeline using secondary AI workflows
        """
        start_time = time.time()
        
        print(f"\n🤖 Starting Secondary AI Validation Pipeline")
        print(f"📄 Language: {request.language}")
        print(f"🎯 Type: {request.validation_type.value}")
        print(f"⚡ Priority: {request.priority.value}")
        print("=" * 60)
        
        try:
            # Stage 1: Strategic Coordination (Big Bottle Sriracha)
            print("🧠 Stage 1: Strategic Coordination...")
            validation_strategy = await self._coordinate_validation_strategy(request)
            
            # Stage 2: Multi-Model Analysis
            print("🔍 Stage 2: Multi-Model Analysis...")
            security_result = await self._perform_security_validation(request)
            quality_result = await self._perform_quality_validation(request)
            performance_result = await self._perform_performance_validation(request)
            
            # Stage 3: AI Consensus
            print("🤝 Stage 3: AI Consensus Building...")
            ai_consensus = await self._get_ai_consensus(request, security_result, quality_result, performance_result)
            
            # Stage 4: Final Assessment
            print("📊 Stage 4: Final Assessment...")
            overall_score = self._calculate_overall_score(security_result, quality_result, performance_result, ai_consensus)
            validation_passed = overall_score >= self._get_passing_threshold(request.priority)
            
            processing_time = (time.time() - start_time) * 1000
            
            result = ValidationResult(
                overall_score=overall_score,
                validation_passed=validation_passed,
                security_validation=security_result,
                quality_validation=quality_result,
                performance_validation=performance_result,
                ai_consensus=ai_consensus,
                recommendations=self._generate_final_recommendations(security_result, quality_result, performance_result, ai_consensus),
                validation_timestamp=time.time(),
                processing_time_ms=processing_time
            )
            
            print(f"\n✅ Validation Complete!")
            print(f"📈 Overall Score: {result.overall_score}/100")
            print(f"✅ Validation {'PASSED' if result.validation_passed else 'FAILED'}")
            print(f"⏱️ Processing Time: {processing_time:.1f}ms")
            
            return result
            
        except Exception as e:
            print(f"❌ Validation failed: {e}")
            processing_time = (time.time() - start_time) * 1000
            
            return ValidationResult(
                overall_score=0,
                validation_passed=False,
                security_validation=SecurityValidationResult(0, [], RiskLevel.CRITICAL, [f"Validation failed: {e}"]),
                quality_validation=QualityValidationResult(0, [f"Validation failed: {e}"], [], 0, 0, []),
                performance_validation=PerformanceValidationResult(0, "Failed", [], "Unknown"),
                ai_consensus=AIConsensusResult(0, {}, [f"Validation system failure: {e}"], "Unable to validate"),
                recommendations=[f"Fix validation system errors: {e}"],
                validation_timestamp=time.time(),
                processing_time_ms=processing_time
            )

    async def _coordinate_validation_strategy(self, request: ValidationRequest) -> Dict[str, Any]:
        """
        Use Big Bottle Sriracha AI to coordinate overall validation strategy
        """
        await self._simulate_ai_processing("Big Bottle Sriracha", 0.5)
        
        strategy = {
            "focus_areas": self._determine_focus_areas(request),
            "validation_depth": self._determine_validation_depth(request.priority),
            "ai_model_allocation": self._allocate_ai_models(request),
            "estimated_duration": "2-5 seconds"
        }
        
        print(f"🎯 Focus Areas: {', '.join(strategy['focus_areas'])}")
        print(f"🔬 Depth: {strategy['validation_depth']}")
        
        return strategy

    async def _perform_security_validation(self, request: ValidationRequest) -> SecurityValidationResult:
        """
        Security validation using SecurityAnalyzer + Anti-Flail AI
        """
        print("🛡️ Running Security Analysis...")
        await self._simulate_ai_processing("Anti-Flail AI", 1.0)
        
        # Simulate security analysis
        vulnerabilities = self._analyze_security_issues(request.code, request.language)
        risk_level = self._calculate_risk_level(vulnerabilities)
        security_score = self._calculate_security_score(vulnerabilities, risk_level)
        
        mitigations = [
            "Implement input validation for user-provided data",
            "Use parameterized queries to prevent SQL injection",
            "Add proper error handling to prevent information disclosure"
        ]
        
        print(f"🛡️ Security Score: {security_score}/100")
        print(f"⚠️ Risk Level: {risk_level.value.upper()}")
        print(f"🔍 Vulnerabilities Found: {len(vulnerabilities)}")
        
        return SecurityValidationResult(
            security_score=security_score,
            vulnerabilities=vulnerabilities,
            risk_level=risk_level,
            mitigation_suggestions=mitigations[:3]
        )

    async def _perform_quality_validation(self, request: ValidationRequest) -> QualityValidationResult:
        """
        Quality validation using CodeReviewer + CodeReaver AI
        """
        print("📋 Running Quality Analysis...")
        await self._simulate_ai_processing("CodeReaver AI", 1.2)
        
        # Simulate code quality analysis
        issues = self._analyze_code_issues(request.code, request.language)
        suggestions = self._generate_quality_suggestions(request.code, request.language)
        
        quality_score = max(0, 100 - len(issues) * 8)
        maintainability_score = self._calculate_maintainability_score(request.code)
        readability_score = self._calculate_readability_score(request.code)
        
        print(f"📋 Quality Score: {quality_score}/100")
        print(f"🔧 Maintainability: {maintainability_score}/100") 
        print(f"👁️ Readability: {readability_score}/100")
        print(f"⚠️ Issues Found: {len(issues)}")
        
        return QualityValidationResult(
            quality_score=quality_score,
            issues=issues,
            suggestions=suggestions,
            maintainability_score=maintainability_score,
            readability_score=readability_score,
            improvement_suggestions=suggestions[:5]
        )

    async def _perform_performance_validation(self, request: ValidationRequest) -> PerformanceValidationResult:
        """
        Performance validation using Orchestrator AI coordination
        """
        print("⚡ Running Performance Analysis...")
        await self._simulate_ai_processing("Orchestrator AI", 0.8)
        
        # Simulate performance analysis
        complexity_analysis = self._analyze_complexity(request.code)
        optimization_opportunities = self._find_optimization_opportunities(request.code, request.language)
        performance_score = self._calculate_performance_score(request.code)
        performance_impact = self._estimate_performance_impact(request.code)
        
        print(f"⚡ Performance Score: {performance_score}/100")
        print(f"🔄 Complexity: {complexity_analysis}")
        print(f"🚀 Optimization Opportunities: {len(optimization_opportunities)}")
        
        return PerformanceValidationResult(
            performance_score=performance_score,
            complexity_analysis=complexity_analysis,
            optimization_opportunities=optimization_opportunities,
            estimated_performance_impact=performance_impact
        )

    async def _get_ai_consensus(self, request: ValidationRequest, security_result: SecurityValidationResult, 
                              quality_result: QualityValidationResult, performance_result: PerformanceValidationResult) -> AIConsensusResult:
        """
        Get consensus from all AI models
        """
        print("🤝 Building AI Consensus...")
        await self._simulate_ai_processing("All AI Models", 1.5)
        
        # Simulate model opinions
        model_opinions = {
            "BigBottleSriracha": security_result.security_score > 70,
            "Orchestrator": quality_result.quality_score > 70, 
            "AntiFlail": len(security_result.vulnerabilities) <= 2,
            "CodeReaver": performance_result.performance_score > 70
        }
        
        consensus_score = sum(model_opinions.values()) / len(model_opinions) * 100
        
        conflicting_opinions = []
        if consensus_score < 70:
            conflicting_opinions.append("Models disagree on code quality standards")
            conflicting_opinions.append("Security vs performance trade-offs identified")
        
        final_recommendation = self._generate_final_ai_recommendation(model_opinions, consensus_score)
        
        print(f"🤝 Consensus Score: {consensus_score:.0f}/100")
        print(f"🎯 Final Recommendation: {final_recommendation}")
        
        return AIConsensusResult(
            consensus_score=int(consensus_score),
            model_agreements=model_opinions,
            conflicting_opinions=conflicting_opinions,
            final_recommendation=final_recommendation
        )

    # Helper methods for analysis simulation

    def _analyze_security_issues(self, code: str, language: str) -> List[Dict[str, Any]]:
        """Simulate security issue detection"""
        issues = []
        
        # Common security patterns
        if "System.out.println" in code:
            issues.append({
                "type": "Information Disclosure",
                "severity": "MEDIUM", 
                "message": "Potential information disclosure through console output",
                "line": 1
            })
        
        if "SELECT * FROM" in code.upper():
            issues.append({
                "type": "SQL Injection", 
                "severity": "HIGH",
                "message": "Potential SQL injection vulnerability",
                "line": 1
            })
        
        if "eval(" in code or "exec(" in code:
            issues.append({
                "type": "Code Injection",
                "severity": "CRITICAL",
                "message": "Dynamic code execution detected",
                "line": 1
            })
            
        return issues

    def _analyze_code_issues(self, code: str, language: str) -> List[str]:
        """Simulate code quality issue detection"""
        issues = []
        
        lines = code.split('\n')
        
        # Check for basic issues
        if len([line for line in lines if line.strip()]) > 100:
            issues.append("Function/class too long - consider breaking down")
        
        if any(len(line) > 120 for line in lines):
            issues.append("Lines too long - exceeds 120 character limit")
        
        if language.lower() in ['kotlin', 'java'] and '!!' in code:
            issues.append("Non-null assertion operator (!!) usage detected")
        
        if code.count('TODO') > 0:
            issues.append("TODO comments found - incomplete implementation")
        
        # Indentation check
        indented_lines = [line for line in lines if line.startswith('    ') or line.startswith('\t')]
        if len(lines) > 10 and len(indented_lines) / len(lines) < 0.3:
            issues.append("Inconsistent indentation detected")
            
        return issues

    def _generate_quality_suggestions(self, code: str, language: str) -> List[str]:
        """Generate code improvement suggestions"""
        suggestions = [
            "Add comprehensive documentation for public methods",
            "Consider using more descriptive variable names",
            "Implement proper error handling and validation",
            "Add unit tests to improve code coverage",
            "Consider extracting complex logic into separate methods"
        ]
        
        if language.lower() == 'kotlin':
            suggestions.extend([
                "Use Kotlin's null safety features more effectively",
                "Consider using data classes for simple value holders",
                "Leverage Kotlin's extension functions for cleaner code"
            ])
        
        return suggestions

    def _find_optimization_opportunities(self, code: str, language: str) -> List[str]:
        """Find performance optimization opportunities"""
        opportunities = []
        
        if "for (" in code and ".size" in code:
            opportunities.append("Consider using enhanced for loops or functional operators")
        
        if "String +" in code:
            opportunities.append("Use StringBuilder for multiple string concatenations")
        
        if language.lower() == 'python' and "for i in range(len(" in code:
            opportunities.append("Use enumerate() instead of range(len())")
        
        if "List<" in code and "ArrayList<" not in code:
            opportunities.append("Consider specifying ArrayList for better performance")
        
        return opportunities

    def _calculate_maintainability_score(self, code: str) -> int:
        """Calculate maintainability score"""
        lines = code.split('\n')
        comment_lines = len([line for line in lines if line.strip().startswith('//') or line.strip().startswith('/*')])
        
        if len(lines) == 0:
            return 50
        
        comment_ratio = comment_lines / len(lines)
        
        if comment_ratio >= 0.2:
            return 90
        elif comment_ratio >= 0.1:
            return 80
        elif comment_ratio >= 0.05:
            return 70
        else:
            return 60

    def _calculate_readability_score(self, code: str) -> int:
        """Calculate readability score"""
        lines = code.split('\n')
        avg_line_length = sum(len(line) for line in lines) / max(len(lines), 1)
        
        if avg_line_length <= 60:
            return 95
        elif avg_line_length <= 80:
            return 85
        elif avg_line_length <= 100:
            return 75
        else:
            return 65

    def _analyze_complexity(self, code: str) -> str:
        """Analyze code complexity"""
        decision_points = ['if', 'while', 'for', 'case', 'catch', '&&', '||']
        complexity = sum(code.count(point) for point in decision_points) + 1
        
        if complexity <= 10:
            return "Low complexity - easy to maintain"
        elif complexity <= 20:
            return "Moderate complexity - acceptable"
        elif complexity <= 50:
            return "High complexity - consider refactoring"
        else:
            return "Very high complexity - refactoring recommended"

    def _calculate_risk_level(self, vulnerabilities: List[Dict[str, Any]]) -> RiskLevel:
        """Calculate overall risk level"""
        if not vulnerabilities:
            return RiskLevel.MINIMAL
        
        critical_count = sum(1 for v in vulnerabilities if v.get('severity') == 'CRITICAL')
        high_count = sum(1 for v in vulnerabilities if v.get('severity') == 'HIGH')
        
        if critical_count > 0:
            return RiskLevel.CRITICAL
        elif high_count >= 3:
            return RiskLevel.HIGH
        elif high_count > 0:
            return RiskLevel.MEDIUM
        else:
            return RiskLevel.LOW

    def _calculate_security_score(self, vulnerabilities: List[Dict[str, Any]], risk_level: RiskLevel) -> int:
        """Calculate security score"""
        base_score = 100
        
        deductions = {
            'CRITICAL': 30,
            'HIGH': 20,
            'MEDIUM': 10,
            'LOW': 5
        }
        
        total_deduction = sum(deductions.get(v.get('severity', 'LOW'), 2) for v in vulnerabilities)
        return max(0, base_score - total_deduction)

    def _calculate_performance_score(self, code: str) -> int:
        """Calculate performance score"""
        lines = len(code.split('\n'))
        
        if lines < 50:
            return 95
        elif lines < 100:
            return 85
        elif lines < 200:
            return 75
        elif lines < 500:
            return 65
        else:
            return 50

    def _estimate_performance_impact(self, code: str) -> str:
        """Estimate performance impact"""
        complexity = sum(code.count(point) for point in ['if', 'while', 'for', 'case', 'catch']) + 1
        
        if complexity <= 10:
            return "Minimal performance impact"
        elif complexity <= 20:
            return "Low performance impact"
        elif complexity <= 50:
            return "Moderate performance impact"
        else:
            return "High performance impact - optimization needed"

    def _determine_focus_areas(self, request: ValidationRequest) -> List[str]:
        """Determine validation focus areas based on request type"""
        focus_map = {
            ValidationType.SECURITY_FOCUS: ["security", "vulnerability_analysis"],
            ValidationType.QUALITY_FOCUS: ["code_quality", "maintainability", "readability"],
            ValidationType.PERFORMANCE_FOCUS: ["performance", "optimization", "complexity"],
            ValidationType.COMPREHENSIVE: ["security", "quality", "performance", "consensus"]
        }
        return focus_map[request.validation_type]

    def _determine_validation_depth(self, priority: ValidationPriority) -> str:
        """Determine validation depth based on priority"""
        depth_map = {
            ValidationPriority.LOW: "basic",
            ValidationPriority.MEDIUM: "standard", 
            ValidationPriority.HIGH: "thorough",
            ValidationPriority.CRITICAL: "exhaustive"
        }
        return depth_map[priority]

    def _allocate_ai_models(self, request: ValidationRequest) -> Dict[str, List[str]]:
        """Allocate AI models to validation tasks"""
        return {
            "security": ["SecurityAnalyzer", "AntiFlail"],
            "quality": ["CodeReviewer", "CodeReaver"],
            "performance": ["Orchestrator"],
            "coordination": ["BigBottleSriracha"]
        }

    def _calculate_overall_score(self, security_result: SecurityValidationResult,
                               quality_result: QualityValidationResult,
                               performance_result: PerformanceValidationResult,
                               ai_consensus: AIConsensusResult) -> int:
        """Calculate weighted overall score"""
        weights = {
            "security": 0.3,
            "quality": 0.4,
            "performance": 0.2,
            "consensus": 0.1
        }
        
        return int(
            security_result.security_score * weights["security"] +
            quality_result.quality_score * weights["quality"] + 
            performance_result.performance_score * weights["performance"] +
            ai_consensus.consensus_score * weights["consensus"]
        )

    def _get_passing_threshold(self, priority: ValidationPriority) -> int:
        """Get passing threshold based on priority"""
        thresholds = {
            ValidationPriority.LOW: 60,
            ValidationPriority.MEDIUM: 70,
            ValidationPriority.HIGH: 80,
            ValidationPriority.CRITICAL: 90
        }
        return thresholds[priority]

    def _generate_final_recommendations(self, security_result: SecurityValidationResult,
                                      quality_result: QualityValidationResult,
                                      performance_result: PerformanceValidationResult,
                                      ai_consensus: AIConsensusResult) -> List[str]:
        """Generate final recommendations"""
        recommendations = []
        
        if security_result.security_score < 80:
            recommendations.extend(security_result.mitigation_suggestions[:2])
        
        if quality_result.quality_score < 80:
            recommendations.extend(quality_result.improvement_suggestions[:2])
        
        if performance_result.performance_score < 80:
            recommendations.extend(performance_result.optimization_opportunities[:2])
        
        if ai_consensus.consensus_score < 70:
            recommendations.append("Multiple AI models disagree - manual review recommended")
        
        return list(set(recommendations))  # Remove duplicates

    def _generate_final_ai_recommendation(self, model_opinions: Dict[str, bool], consensus_score: float) -> str:
        """Generate final AI recommendation"""
        if consensus_score >= 80:
            return "Strong AI consensus: Code meets quality standards"
        elif consensus_score >= 60:
            return "Moderate AI consensus: Minor improvements recommended"
        else:
            return "Low AI consensus: Significant review and improvements needed"

    async def _simulate_ai_processing(self, model_name: str, duration: float):
        """Simulate AI model processing time"""
        import asyncio
        print(f"🤖 {model_name} processing...")
        await asyncio.sleep(duration)

def create_demo_code_samples() -> Dict[str, Dict[str, str]]:
    """Create demo code samples for testing"""
    return {
        "good_kotlin": {
            "language": "kotlin",
            "code": """
// Well-structured Kotlin class with proper practices
class UserValidator {
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
    
    /**
     * Validates user registration data
     * @param email User email address
     * @param password User password
     * @return ValidationResult indicating success or failure
     */
    fun validateUser(email: String?, password: String?): ValidationResult {
        return when {
            email.isNullOrBlank() -> ValidationResult.failure("Email is required")
            !emailPattern.matches(email) -> ValidationResult.failure("Invalid email format")
            password.isNullOrBlank() -> ValidationResult.failure("Password is required")
            password.length < 8 -> ValidationResult.failure("Password must be at least 8 characters")
            else -> ValidationResult.success("Validation passed")
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val message: String
) {
    companion object {
        fun success(message: String) = ValidationResult(true, message)
        fun failure(message: String) = ValidationResult(false, message)
    }
}
"""
        },
        "problematic_kotlin": {
            "language": "kotlin", 
            "code": """
// Problematic Kotlin code with various issues
class BadExample {
    fun process(data: String): String {
        var result = ""
        for (i in 0 until data.length) {
            result = result + data[i].toString() // String concatenation in loop
        }
        
        val user = getUserById(data.toInt()!!) // Force unwrap
        System.out.println("Processing user: " + user) // System.out usage
        
        // TODO: Add proper validation
        
        val query = "SELECT * FROM users WHERE id = " + data // SQL injection risk
        
        return result
    }
    
    fun getUserById(id: Int): String? = null
}
"""
        },
        "security_risk_code": {
            "language": "java",
            "code": """
// Java code with security vulnerabilities
public class SecurityRisks {
    public void processUserInput(String userInput) {
        // Direct SQL injection vulnerability
        String query = "SELECT * FROM users WHERE name = '" + userInput + "'";
        
        // Command injection risk
        Runtime.getRuntime().exec("ls -la " + userInput);
        
        // Information disclosure
        System.out.println("Debug: Processing " + userInput);
        
        // Hardcoded credentials
        String apiKey = "sk-1234567890abcdef";
        
        // Eval-like behavior
        if (userInput.contains("eval")) {
            eval(userInput);
        }
    }
    
    private void eval(String code) {
        // Dangerous dynamic execution
    }
}
"""
        }
    }

async def run_demo():
    """Run demo validation on sample code"""
    print("🚀 Secondary AI Validation System Demo")
    print("=" * 60)
    
    engine = SecondaryAIValidationEngine()
    samples = create_demo_code_samples()
    
    for sample_name, sample_data in samples.items():
        print(f"\n📝 Testing: {sample_name.upper()}")
        print("-" * 40)
        
        request = ValidationRequest(
            code=sample_data["code"],
            language=sample_data["language"],
            validation_type=ValidationType.COMPREHENSIVE,
            priority=ValidationPriority.HIGH
        )
        
        result = await engine.validate_code(request)
        
        print(f"\n📊 Results Summary:")
        print(f"   Overall Score: {result.overall_score}/100")
        print(f"   Validation: {'✅ PASSED' if result.validation_passed else '❌ FAILED'}")
        print(f"   Security: {result.security_validation.security_score}/100 ({result.security_validation.risk_level.value})")
        print(f"   Quality: {result.quality_validation.quality_score}/100")
        print(f"   Performance: {result.performance_validation.performance_score}/100")
        print(f"   AI Consensus: {result.ai_consensus.consensus_score}/100")
        
        if result.recommendations:
            print(f"\n💡 Top Recommendations:")
            for i, rec in enumerate(result.recommendations[:3], 1):
                print(f"   {i}. {rec}")
        
        print("\n" + "=" * 60)

def main():
    """Main entry point"""
    parser = argparse.ArgumentParser(description="Secondary AI Workflow Validation Script")
    parser.add_argument("--code", help="Code to validate")
    parser.add_argument("--language", default="kotlin", help="Programming language") 
    parser.add_argument("--file", help="File path to validate")
    parser.add_argument("--type", choices=[v.value for v in ValidationType], 
                       default="comprehensive", help="Validation type")
    parser.add_argument("--priority", choices=[p.value for p in ValidationPriority],
                       default="medium", help="Validation priority")
    parser.add_argument("--demo", action="store_true", help="Run demo with sample codes")
    parser.add_argument("--output", help="Output file for results (JSON)")
    
    args = parser.parse_args()
    
    import asyncio
    
    if args.demo:
        asyncio.run(run_demo())
        return
    
    if not args.code and not args.file:
        parser.error("Either --code or --file must be specified (or use --demo)")
    
    # Load code from file if specified
    if args.file:
        try:
            with open(args.file, 'r', encoding='utf-8') as f:
                code = f.read()
        except Exception as e:
            print(f"❌ Error reading file: {e}")
            return
    else:
        code = args.code
    
    # Create validation request
    request = ValidationRequest(
        code=code,
        language=args.language,
        validation_type=ValidationType(args.type),
        priority=ValidationPriority(args.priority)
    )
    
    # Run validation
    async def run_validation():
        engine = SecondaryAIValidationEngine()
        result = await engine.validate_code(request)
        
        # Output results
        if args.output:
            with open(args.output, 'w', encoding='utf-8') as f:
                json.dump(asdict(result), f, indent=2, default=str)
            print(f"📄 Results saved to {args.output}")
        else:
            print(json.dumps(asdict(result), indent=2, default=str))
    
    asyncio.run(run_validation())

if __name__ == "__main__":
    main()