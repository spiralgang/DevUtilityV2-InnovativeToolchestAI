#!/usr/bin/env python3
"""
Anti-Flailing Decision Framework Integration with Agentic-Matrix
Host-side Python guard implementation
"""

import json
import time
import logging
import hashlib
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Optional, Tuple, Union
from dataclasses import dataclass, asdict
from enum import Enum

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class FlailingRiskLevel(Enum):
    """Risk levels for flailing behavior detection"""
    LOW = "low"
    MODERATE = "moderate" 
    HIGH = "high"
    CRITICAL = "critical"

class ExpansionDecision(Enum):
    """Possible decisions for capability expansion requests"""
    NO_EXPANSION_NEEDED = "no_expansion_needed"
    TARGETED_ENHANCEMENT = "targeted_enhancement"
    CONTROLLED_ADDITION = "controlled_addition"
    STRATEGIC_PIVOT = "strategic_pivot"
    EMERGENCY_EXPANSION = "emergency_expansion"
    REJECTED_FLAILING = "rejected_flailing"

@dataclass
class KnowledgeGapAnalysis:
    """Analysis of knowledge gaps before expansion consideration"""
    gap_id: str
    current_capabilities: List[str]
    required_task: str
    gap_severity: float  # 0.0 - 1.0
    existing_alternatives: List[str]
    enhancement_potential: float  # 0.0 - 1.0
    justification_required: bool
    timestamp: str

@dataclass
class CapabilityExpansionRequest:
    """Request for new capability addition"""
    request_id: str
    proposed_capability: str
    justification: str
    current_system_state: Dict
    integration_plan: Optional[str]
    risk_assessment: Optional[str]
    alternatives_explored: List[str]
    timestamp: str

@dataclass
class ExpansionEvaluation:
    """Evaluation result for capability expansion"""
    evaluation_id: str
    request_id: str
    decision: ExpansionDecision
    confidence: float  # 0.0 - 1.0
    reasoning: str
    required_actions: List[str]
    monitoring_requirements: List[str]
    flailing_risk: FlailingRiskLevel
    timestamp: str

class AntiFlailingGuard:
    """
    Host-side Python guard implementing Anti-Flailing decision framework
    Integrates with Agentic-Matrix to prevent reactive capability expansion
    """
    
    def __init__(self, config_path: Optional[str] = None):
        self.config_path = config_path or "configs/anti_flailing_config.json"
        self.logs_dir = Path("logs")
        self.logs_dir.mkdir(exist_ok=True)
        
        self.config = self._load_config()
        self.decision_history: List[ExpansionEvaluation] = []
        self.knowledge_gaps: List[KnowledgeGapAnalysis] = []
        
        # Initialize monitoring metrics
        self.flailing_indicators = {
            "consecutive_expansions": 0,
            "expansion_frequency": 0.0,
            "integration_failures": 0,
            "decision_reversals": 0
        }
        
        logger.info("AntiFlailingGuard initialized")
    
    def _load_config(self) -> Dict:
        """Load anti-flailing configuration"""
        try:
            with open(self.config_path, 'r') as f:
                config = json.load(f)
                # Extract anti_flailing_config section if it exists
                if "anti_flailing_config" in config:
                    return config["anti_flailing_config"]
                return config
        except FileNotFoundError:
            logger.warning(f"Config file {self.config_path} not found, using defaults")
            return self._default_config()
    
    def _default_config(self) -> Dict:
        """Default anti-flailing configuration"""
        return {
            "flailing_thresholds": {
                "consecutive_expansions": 3,
                "expansion_frequency_per_hour": 2.0,
                "integration_failure_rate": 0.3,
                "decision_reversal_rate": 0.2
            },
            "mandatory_gates": [
                "knowledge_gap_analysis",
                "alternative_exploration", 
                "integration_planning",
                "risk_assessment"
            ],
            "cooling_periods": {
                "after_rejection": 3600,  # 1 hour
                "after_emergency": 7200,  # 2 hours
                "after_critical_flailing": 86400  # 24 hours
            },
            "enhancement_thresholds": {
                "min_enhancement_potential": 0.6,
                "max_gap_severity_for_enhancement": 0.8
            }
        }
    
    def analyze_knowledge_gap(
        self,
        current_capabilities: List[str],
        required_task: str,
        context_info: Dict
    ) -> KnowledgeGapAnalysis:
        """
        Analyze knowledge gap before considering expansion
        Implements Beschooled BigBottleSriracha principle of systematic knowledge building
        """
        gap_id = self._generate_id("gap")
        
        # Analyze gap severity
        gap_severity = self._calculate_gap_severity(current_capabilities, required_task, context_info)
        
        # Identify existing alternatives
        existing_alternatives = self._identify_alternatives(current_capabilities, required_task)
        
        # Calculate enhancement potential of existing capabilities
        enhancement_potential = self._calculate_enhancement_potential(
            current_capabilities, required_task, existing_alternatives
        )
        
        # Determine if expansion justification is required
        justification_required = (
            gap_severity > self.config["enhancement_thresholds"]["max_gap_severity_for_enhancement"] or
            enhancement_potential < self.config["enhancement_thresholds"]["min_enhancement_potential"]
        )
        
        analysis = KnowledgeGapAnalysis(
            gap_id=gap_id,
            current_capabilities=current_capabilities,
            required_task=required_task,
            gap_severity=gap_severity,
            existing_alternatives=existing_alternatives,
            enhancement_potential=enhancement_potential,
            justification_required=justification_required,
            timestamp=datetime.now().isoformat()
        )
        
        self.knowledge_gaps.append(analysis)
        self._log_knowledge_gap(analysis)
        
        return analysis
    
    def evaluate_capability_expansion(
        self,
        proposed_capability: str,
        justification: str,
        current_system: Dict,
        integration_plan: Optional[str] = None,
        risk_assessment: Optional[str] = None,
        alternatives_explored: Optional[List[str]] = None
    ) -> ExpansionEvaluation:
        """
        Evaluate capability expansion request through anti-flailing framework
        Implements structured decision framework preventing reactive expansion
        """
        request_id = self._generate_id("expansion")
        evaluation_id = self._generate_id("eval")
        
        # Create expansion request
        request = CapabilityExpansionRequest(
            request_id=request_id,
            proposed_capability=proposed_capability,
            justification=justification,
            current_system_state=current_system,
            integration_plan=integration_plan,
            risk_assessment=risk_assessment,
            alternatives_explored=alternatives_explored or [],
            timestamp=datetime.now().isoformat()
        )
        
        # Assess current flailing risk
        flailing_risk = self._assess_flailing_risk()
        
        # Check mandatory gates
        gate_results = self._check_mandatory_gates(request)
        
        # Make expansion decision
        decision, confidence, reasoning = self._make_expansion_decision(
            request, gate_results, flailing_risk
        )
        
        # Determine required actions and monitoring
        required_actions = self._determine_required_actions(decision, request)
        monitoring_requirements = self._determine_monitoring_requirements(decision, request)
        
        evaluation = ExpansionEvaluation(
            evaluation_id=evaluation_id,
            request_id=request_id,
            decision=decision,
            confidence=confidence,
            reasoning=reasoning,
            required_actions=required_actions,
            monitoring_requirements=monitoring_requirements,
            flailing_risk=flailing_risk,
            timestamp=datetime.now().isoformat()
        )
        
        # Update decision history and metrics
        self.decision_history.append(evaluation)
        self._update_flailing_metrics(evaluation)
        
        # Log decision
        self._log_expansion_evaluation(evaluation)
        
        return evaluation
    
    def _calculate_gap_severity(
        self,
        current_capabilities: List[str],
        required_task: str,
        context_info: Dict
    ) -> float:
        """Calculate severity of knowledge gap (0.0 = no gap, 1.0 = critical gap)"""
        # Simple heuristic implementation
        # In production, this would use more sophisticated analysis
        
        capability_coverage = len([cap for cap in current_capabilities 
                                 if any(keyword in required_task.lower() 
                                       for keyword in cap.lower().split())])
        
        total_capabilities = len(current_capabilities)
        if total_capabilities == 0:
            return 1.0
        
        coverage_ratio = capability_coverage / total_capabilities
        gap_severity = 1.0 - coverage_ratio
        
        # Adjust based on context (urgency, complexity, etc.)
        urgency_factor = context_info.get("urgency", 0.5)
        complexity_factor = context_info.get("complexity", 0.5)
        
        adjusted_severity = gap_severity * (1.0 + urgency_factor * 0.3 + complexity_factor * 0.2)
        
        return min(adjusted_severity, 1.0)
    
    def _identify_alternatives(self, current_capabilities: List[str], required_task: str) -> List[str]:
        """Identify existing alternatives that could address the task"""
        alternatives = []
        
        # Look for capabilities that could be enhanced or combined
        for capability in current_capabilities:
            if any(keyword in capability.lower() for keyword in required_task.lower().split()):
                alternatives.append(f"enhance_{capability}")
        
        # Look for combinations of existing capabilities
        if len(current_capabilities) >= 2:
            alternatives.append("combine_existing_capabilities")
        
        # Always include the option to improve coordination
        alternatives.append("improve_coordination_between_existing_capabilities")
        
        return alternatives
    
    def _calculate_enhancement_potential(
        self,
        current_capabilities: List[str],
        required_task: str,
        alternatives: List[str]
    ) -> float:
        """Calculate potential for enhancing existing capabilities (0.0 = no potential, 1.0 = high potential)"""
        if not alternatives:
            return 0.0
        
        # Base potential on number and relevance of alternatives
        base_potential = min(len(alternatives) / 5.0, 1.0)  # Normalize to max 5 alternatives
        
        # Boost potential if there are enhancement alternatives
        enhancement_alternatives = [alt for alt in alternatives if "enhance" in alt.lower()]
        if enhancement_alternatives:
            base_potential *= 1.2
        
        # Boost potential if coordination improvements are possible
        if "coordination" in str(alternatives).lower():
            base_potential *= 1.1
        
        return min(base_potential, 1.0)
    
    def _assess_flailing_risk(self) -> FlailingRiskLevel:
        """Assess current risk of flailing behavior"""
        thresholds = self.config["flailing_thresholds"]
        
        # Check consecutive expansions
        if self.flailing_indicators["consecutive_expansions"] >= thresholds["consecutive_expansions"]:
            return FlailingRiskLevel.CRITICAL
        
        # Check expansion frequency
        if self.flailing_indicators["expansion_frequency"] >= thresholds["expansion_frequency_per_hour"]:
            return FlailingRiskLevel.HIGH
        
        # Check integration failure rate
        if self.flailing_indicators["integration_failures"] >= thresholds["integration_failure_rate"]:
            return FlailingRiskLevel.HIGH
        
        # Check decision reversal rate
        if self.flailing_indicators["decision_reversals"] >= thresholds["decision_reversal_rate"]:
            return FlailingRiskLevel.MODERATE
        
        return FlailingRiskLevel.LOW
    
    def _check_mandatory_gates(self, request: CapabilityExpansionRequest) -> Dict[str, bool]:
        """Check if mandatory gates are satisfied"""
        gates = self.config["mandatory_gates"]
        results = {}
        
        # Knowledge gap analysis gate
        results["knowledge_gap_analysis"] = any(
            gap.required_task == request.proposed_capability 
            for gap in self.knowledge_gaps
        )
        
        # Alternative exploration gate
        results["alternative_exploration"] = len(request.alternatives_explored) > 0
        
        # Integration planning gate
        results["integration_planning"] = request.integration_plan is not None
        
        # Risk assessment gate
        results["risk_assessment"] = request.risk_assessment is not None
        
        return results
    
    def _make_expansion_decision(
        self,
        request: CapabilityExpansionRequest,
        gate_results: Dict[str, bool],
        flailing_risk: FlailingRiskLevel
    ) -> Tuple[ExpansionDecision, float, str]:
        """Make expansion decision based on analysis"""
        
        # Check if we're in a critical flailing state
        if flailing_risk == FlailingRiskLevel.CRITICAL:
            return (
                ExpansionDecision.REJECTED_FLAILING,
                0.9,
                "Request rejected due to critical flailing risk. Cooling period required."
            )
        
        # Check mandatory gates
        failed_gates = [gate for gate, passed in gate_results.items() if not passed]
        if failed_gates:
            return (
                ExpansionDecision.REJECTED_FLAILING,
                0.8,
                f"Request rejected due to failed mandatory gates: {', '.join(failed_gates)}"
            )
        
        # Find related knowledge gap analysis
        related_gaps = [gap for gap in self.knowledge_gaps 
                       if gap.required_task == request.proposed_capability]
        
        if not related_gaps:
            return (
                ExpansionDecision.REJECTED_FLAILING,
                0.7,
                "No knowledge gap analysis found for this capability expansion"
            )
        
        gap = related_gaps[-1]  # Use most recent analysis
        
        # Decision logic based on gap analysis
        if gap.enhancement_potential >= 0.8:
            return (
                ExpansionDecision.NO_EXPANSION_NEEDED,
                0.9,
                "Existing capabilities can be enhanced to meet requirements"
            )
        elif gap.enhancement_potential >= 0.6:
            return (
                ExpansionDecision.TARGETED_ENHANCEMENT,
                0.8,
                "Targeted enhancement of existing capabilities recommended"
            )
        elif gap.gap_severity >= 0.8 and flailing_risk == FlailingRiskLevel.LOW:
            return (
                ExpansionDecision.CONTROLLED_ADDITION,
                0.7,
                "Controlled addition justified due to significant gap with low flailing risk"
            )
        elif gap.gap_severity >= 0.9 and "emergency" in request.justification.lower():
            return (
                ExpansionDecision.EMERGENCY_EXPANSION,
                0.6,
                "Emergency expansion approved due to critical gap"
            )
        else:
            return (
                ExpansionDecision.REJECTED_FLAILING,
                0.8,
                "Expansion rejected: insufficient justification or high flailing risk"
            )
    
    def _determine_required_actions(self, decision: ExpansionDecision, request: CapabilityExpansionRequest) -> List[str]:
        """Determine required actions based on decision"""
        actions = []
        
        if decision == ExpansionDecision.NO_EXPANSION_NEEDED:
            actions.extend([
                "enhance_existing_capabilities",
                "improve_coordination_between_capabilities",
                "optimize_current_solution_approach"
            ])
        
        elif decision == ExpansionDecision.TARGETED_ENHANCEMENT:
            actions.extend([
                "identify_specific_capabilities_to_enhance",
                "develop_enhancement_plan",
                "test_enhanced_capabilities",
                "validate_enhancement_effectiveness"
            ])
        
        elif decision == ExpansionDecision.CONTROLLED_ADDITION:
            actions.extend([
                "finalize_integration_plan",
                "implement_gradual_rollout",
                "monitor_integration_metrics",
                "validate_system_coherence"
            ])
        
        elif decision == ExpansionDecision.EMERGENCY_EXPANSION:
            actions.extend([
                "implement_minimal_viable_capability",
                "establish_monitoring_safeguards",
                "plan_proper_integration_post_emergency",
                "document_emergency_justification"
            ])
        
        elif decision == ExpansionDecision.REJECTED_FLAILING:
            actions.extend([
                "enter_cooling_period",
                "reassess_current_capabilities",
                "explore_alternative_approaches",
                "address_underlying_coordination_issues"
            ])
        
        return actions
    
    def _determine_monitoring_requirements(self, decision: ExpansionDecision, request: CapabilityExpansionRequest) -> List[str]:
        """Determine monitoring requirements based on decision"""
        monitoring = []
        
        if decision in [ExpansionDecision.CONTROLLED_ADDITION, ExpansionDecision.EMERGENCY_EXPANSION]:
            monitoring.extend([
                "integration_success_metrics",
                "system_performance_impact",
                "capability_utilization_tracking",
                "coherence_with_existing_system"
            ])
        
        if decision == ExpansionDecision.TARGETED_ENHANCEMENT:
            monitoring.extend([
                "enhancement_effectiveness_metrics",
                "capability_improvement_tracking",
                "resource_utilization_optimization"
            ])
        
        # Always monitor for flailing indicators
        monitoring.extend([
            "expansion_request_frequency",
            "decision_reversal_tracking",
            "alternative_exploration_quality"
        ])
        
        return monitoring
    
    def _update_flailing_metrics(self, evaluation: ExpansionEvaluation):
        """Update flailing indicators based on evaluation"""
        now = datetime.now()
        
        # Update consecutive expansions
        if evaluation.decision in [ExpansionDecision.CONTROLLED_ADDITION, ExpansionDecision.EMERGENCY_EXPANSION]:
            self.flailing_indicators["consecutive_expansions"] += 1
        else:
            self.flailing_indicators["consecutive_expansions"] = 0
        
        # Update expansion frequency (last hour)
        recent_expansions = [
            eval for eval in self.decision_history[-10:]  # Check last 10 decisions
            if eval.decision in [ExpansionDecision.CONTROLLED_ADDITION, ExpansionDecision.EMERGENCY_EXPANSION]
            and (now - datetime.fromisoformat(eval.timestamp)).total_seconds() < 3600
        ]
        self.flailing_indicators["expansion_frequency"] = len(recent_expansions)
    
    def _generate_id(self, prefix: str) -> str:
        """Generate unique ID for tracking"""
        timestamp = str(int(time.time() * 1000))
        hash_input = f"{prefix}_{timestamp}_{id(self)}"
        return f"{prefix}_{hashlib.md5(hash_input.encode()).hexdigest()[:8]}"
    
    def _log_knowledge_gap(self, analysis: KnowledgeGapAnalysis):
        """Log knowledge gap analysis"""
        log_entry = {
            "type": "knowledge_gap_analysis",
            "timestamp": analysis.timestamp,
            "data": asdict(analysis)
        }
        self._write_log("anti_flailing_decisions.jsonl", log_entry)
    
    def _log_expansion_evaluation(self, evaluation: ExpansionEvaluation):
        """Log expansion evaluation"""
        # Convert dataclass to dict and handle enums
        eval_dict = asdict(evaluation)
        eval_dict["decision"] = evaluation.decision.value
        eval_dict["flailing_risk"] = evaluation.flailing_risk.value
        
        log_entry = {
            "type": "expansion_evaluation", 
            "timestamp": evaluation.timestamp,
            "data": eval_dict
        }
        self._write_log("anti_flailing_decisions.jsonl", log_entry)
    
    def _write_log(self, filename: str, entry: Dict):
        """Write log entry to file"""
        log_file = self.logs_dir / filename
        with open(log_file, 'a', encoding='utf-8') as f:
            f.write(json.dumps(entry, ensure_ascii=False) + '\n')

# Test and validation functions
def test_anti_flailing_guard():
    """Test the AntiFlailingGuard implementation"""
    guard = AntiFlailingGuard()
    
    # Test knowledge gap analysis
    gap_analysis = guard.analyze_knowledge_gap(
        current_capabilities=["text_processing", "pattern_recognition"],
        required_task="image_analysis",
        context_info={"urgency": 0.7, "complexity": 0.8}
    )
    
    print(f"Gap Analysis: {gap_analysis.gap_severity:.2f} severity, {gap_analysis.enhancement_potential:.2f} enhancement potential")
    
    # Test expansion evaluation
    evaluation = guard.evaluate_capability_expansion(
        proposed_capability="image_analysis",
        justification="Need to process visual data for user assistance",
        current_system={"capabilities": ["text_processing", "pattern_recognition"], "load": 0.6},
        integration_plan="Add image processing module with existing pattern recognition",
        risk_assessment="Low risk - isolated module with clear interfaces",
        alternatives_explored=["enhance_pattern_recognition", "combine_text_and_pattern_analysis"]
    )
    
    print(f"Expansion Decision: {evaluation.decision.value}")
    print(f"Reasoning: {evaluation.reasoning}")
    print(f"Required Actions: {evaluation.required_actions}")
    
    return guard

if __name__ == "__main__":
    test_anti_flailing_guard()