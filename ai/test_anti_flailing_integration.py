#!/usr/bin/env python3
"""
Test suite for Anti-Flailing Decision Framework Integration
Validates Python guard, Android stubs, and policy integration
"""

import unittest
import json
import tempfile
import os
from pathlib import Path
import sys

# Add the ai directory to the path to import our modules
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'ai'))

from anti_flailing_guard import (
    AntiFlailingGuard, 
    FlailingRiskLevel, 
    ExpansionDecision,
    KnowledgeGapAnalysis,
    ExpansionEvaluation
)

class TestAntiFlailingGuard(unittest.TestCase):
    """Test cases for AntiFlailingGuard implementation"""
    
    def setUp(self):
        """Setup test environment"""
        self.temp_dir = tempfile.mkdtemp()
        self.config_path = os.path.join(self.temp_dir, "test_config.json")
        
        # Create test configuration
        test_config = {
            "flailing_thresholds": {
                "consecutive_expansions": 2,
                "expansion_frequency_per_hour": 1.0,
                "integration_failure_rate": 0.5,
                "decision_reversal_rate": 0.3
            },
            "mandatory_gates": [
                "knowledge_gap_analysis",
                "alternative_exploration",
                "integration_planning",
                "risk_assessment"
            ],
            "cooling_periods": {
                "after_rejection": 60,
                "after_emergency": 120,
                "after_critical_flailing": 300
            },
            "enhancement_thresholds": {
                "min_enhancement_potential": 0.5,
                "max_gap_severity_for_enhancement": 0.7
            }
        }
        
        with open(self.config_path, 'w') as f:
            json.dump(test_config, f)
        
        self.guard = AntiFlailingGuard(self.config_path)
    
    def tearDown(self):
        """Cleanup test environment"""
        import shutil
        shutil.rmtree(self.temp_dir)
    
    def test_knowledge_gap_analysis_low_severity(self):
        """Test knowledge gap analysis with low severity gap"""
        capabilities = ["text_processing", "pattern_recognition", "data_analysis"]
        required_task = "text pattern analysis"
        context = {"urgency": 0.3, "complexity": 0.4}
        
        analysis = self.guard.analyze_knowledge_gap(capabilities, required_task, context)
        
        self.assertIsInstance(analysis, KnowledgeGapAnalysis)
        self.assertLess(analysis.gap_severity, 0.5)
        self.assertGreater(analysis.enhancement_potential, 0.5)
        self.assertFalse(analysis.justification_required)
        self.assertGreater(len(analysis.existing_alternatives), 0)
    
    def test_knowledge_gap_analysis_high_severity(self):
        """Test knowledge gap analysis with high severity gap"""
        capabilities = ["text_processing"]
        required_task = "advanced image recognition and computer vision analysis"
        context = {"urgency": 0.8, "complexity": 0.9}
        
        analysis = self.guard.analyze_knowledge_gap(capabilities, required_task, context)
        
        self.assertIsInstance(analysis, KnowledgeGapAnalysis)
        self.assertGreater(analysis.gap_severity, 0.7)
        self.assertTrue(analysis.justification_required)
    
    def test_expansion_evaluation_no_expansion_needed(self):
        """Test expansion evaluation that concludes no expansion needed"""
        # First create a knowledge gap analysis
        self.guard.analyze_knowledge_gap(
            ["text_processing", "pattern_recognition"],
            "improved_text_analysis",
            {"urgency": 0.3, "complexity": 0.3}
        )
        
        evaluation = self.guard.evaluate_capability_expansion(
            proposed_capability="improved_text_analysis",
            justification="Want better text processing capabilities",
            current_system={"capabilities": ["text_processing", "pattern_recognition"], "load": 0.4},
            integration_plan="Enhance existing text processing module",
            risk_assessment="Low risk - building on existing capabilities",
            alternatives_explored=["enhance_text_processing", "improve_coordination"]
        )
        
        self.assertIsInstance(evaluation, ExpansionEvaluation)
        self.assertEqual(evaluation.decision, ExpansionDecision.NO_EXPANSION_NEEDED)
        self.assertIn("enhance_existing_capabilities", evaluation.required_actions)
    
    def test_expansion_evaluation_controlled_addition(self):
        """Test expansion evaluation that approves controlled addition"""
        # Create a knowledge gap analysis with significant gap
        self.guard.analyze_knowledge_gap(
            ["text_processing"],
            "image_analysis_system",
            {"urgency": 0.7, "complexity": 0.8}
        )
        
        evaluation = self.guard.evaluate_capability_expansion(
            proposed_capability="image_analysis_system",
            justification="Critical need for image processing capabilities for user assistance",
            current_system={"capabilities": ["text_processing"], "load": 0.3},
            integration_plan="Add isolated image processing module with clear interfaces",
            risk_assessment="Medium risk - new domain but isolated implementation",
            alternatives_explored=["enhance_pattern_recognition", "external_service_integration"]
        )
        
        self.assertIsInstance(evaluation, ExpansionEvaluation)
        self.assertEqual(evaluation.decision, ExpansionDecision.CONTROLLED_ADDITION)
        self.assertIn("finalize_integration_plan", evaluation.required_actions)
        self.assertIn("integration_success_metrics", evaluation.monitoring_requirements)
    
    def test_expansion_evaluation_rejected_flailing(self):
        """Test expansion evaluation that rejects due to flailing risk"""
        # Simulate high flailing risk by making consecutive expansions
        for i in range(3):
            self.guard.analyze_knowledge_gap(
                ["basic_capability"],
                f"expansion_{i}",
                {"urgency": 0.5, "complexity": 0.5}
            )
            
            self.guard.evaluate_capability_expansion(
                proposed_capability=f"expansion_{i}",
                justification="Need this capability",
                current_system={"capabilities": ["basic_capability"], "load": 0.5},
                integration_plan="Some plan",
                risk_assessment="Some assessment",
                alternatives_explored=["some_alternative"]
            )
        
        # This should be rejected due to flailing risk
        evaluation = self.guard.evaluate_capability_expansion(
            proposed_capability="another_expansion",
            justification="Need another capability",
            current_system={"capabilities": ["basic_capability"], "load": 0.5},
            integration_plan="Another plan",
            risk_assessment="Another assessment",
            alternatives_explored=["another_alternative"]
        )
        
        self.assertEqual(evaluation.decision, ExpansionDecision.REJECTED_FLAILING)
        self.assertIn("flailing", evaluation.reasoning.lower())
    
    def test_expansion_evaluation_missing_gates(self):
        """Test expansion evaluation with missing mandatory gates"""
        evaluation = self.guard.evaluate_capability_expansion(
            proposed_capability="new_capability",
            justification="Some justification",
            current_system={"capabilities": ["existing"], "load": 0.5}
            # Missing integration_plan, risk_assessment, alternatives_explored
        )
        
        self.assertEqual(evaluation.decision, ExpansionDecision.REJECTED_FLAILING)
        self.assertIn("failed mandatory gates", evaluation.reasoning)
    
    def test_flailing_risk_assessment(self):
        """Test flailing risk assessment logic"""
        # Initially should be low risk
        risk = self.guard._assess_flailing_risk()
        self.assertEqual(risk, FlailingRiskLevel.LOW)
        
        # Simulate consecutive expansions to increase risk
        self.guard.flailing_indicators["consecutive_expansions"] = 3
        risk = self.guard._assess_flailing_risk()
        self.assertEqual(risk, FlailingRiskLevel.CRITICAL)
    
    def test_gap_severity_calculation(self):
        """Test gap severity calculation logic"""
        # High coverage should result in low gap severity
        capabilities = ["text_processing", "pattern_analysis", "data_handling"]
        task = "text pattern analysis"
        context = {"urgency": 0.5, "complexity": 0.5}
        
        severity = self.guard._calculate_gap_severity(capabilities, task, context)
        self.assertLess(severity, 0.5)
        
        # Low coverage should result in high gap severity
        capabilities = ["unrelated_capability"]
        task = "complex image processing and computer vision"
        
        severity = self.guard._calculate_gap_severity(capabilities, task, context)
        self.assertGreater(severity, 0.5)
    
    def test_alternative_identification(self):
        """Test alternative identification logic"""
        capabilities = ["text_processing", "pattern_recognition"]
        task = "improved text analysis"
        
        alternatives = self.guard._identify_alternatives(capabilities, task)
        
        self.assertGreater(len(alternatives), 0)
        self.assertTrue(any("enhance" in alt for alt in alternatives))
        self.assertTrue(any("coordination" in alt for alt in alternatives))
    
    def test_enhancement_potential_calculation(self):
        """Test enhancement potential calculation"""
        # Many enhancement alternatives should give high potential
        alternatives = ["enhance_text_processing", "enhance_pattern_recognition", 
                       "improve_coordination_between_existing_capabilities"]
        potential = self.guard._calculate_enhancement_potential(
            ["text_processing", "pattern_recognition"], 
            "better_analysis", 
            alternatives
        )
        self.assertGreater(potential, 0.5)
        
        # No alternatives should give zero potential
        potential = self.guard._calculate_enhancement_potential(
            ["text_processing"], 
            "completely_unrelated_task", 
            []
        )
        self.assertEqual(potential, 0.0)


class TestIntegrationScenarios(unittest.TestCase):
    """Test integration scenarios between components"""
    
    def setUp(self):
        """Setup integration test environment"""
        self.temp_dir = tempfile.mkdtemp()
        self.config_path = os.path.join(self.temp_dir, "integration_config.json")
        
        # Load the actual config from the repository
        repo_config_path = os.path.join(
            os.path.dirname(__file__), 
            '..', 
            'configs', 
            'anti_flailing_config.json'
        )
        
        if os.path.exists(repo_config_path):
            with open(repo_config_path, 'r') as f:
                config = json.load(f)
                # Extract just the anti_flailing_config section
                anti_flailing_config = config.get("anti_flailing_config", {})
        else:
            # Fallback config
            anti_flailing_config = {
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
                    "after_rejection": 3600,
                    "after_emergency": 7200,
                    "after_critical_flailing": 86400
                },
                "enhancement_thresholds": {
                    "min_enhancement_potential": 0.6,
                    "max_gap_severity_for_enhancement": 0.8
                }
            }
        
        with open(self.config_path, 'w') as f:
            json.dump(anti_flailing_config, f)
        
        self.guard = AntiFlailingGuard(self.config_path)
    
    def tearDown(self):
        """Cleanup integration test environment"""
        import shutil
        shutil.rmtree(self.temp_dir)
    
    def test_sriracha_army_integration_scenario(self):
        """Test integration with Sriracha Army persona concepts"""
        # Simulate Big Bottle requesting capability expansion
        big_bottle_capabilities = [
            "central_coordination",
            "resource_allocation", 
            "strategic_planning"
        ]
        
        # Little Bottle identifies need for new capability
        required_task = "advanced_sentiment_analysis"
        
        # Analyze gap through anti-flailing framework
        gap_analysis = self.guard.analyze_knowledge_gap(
            big_bottle_capabilities,
            required_task,
            {"urgency": 0.6, "complexity": 0.7, "heat_level": "MEDIUM"}
        )
        
        self.assertIsInstance(gap_analysis, KnowledgeGapAnalysis)
        
        # Evaluate expansion request
        evaluation = self.guard.evaluate_capability_expansion(
            proposed_capability=required_task,
            justification="Little Bottles need sentiment analysis for better user interaction",
            current_system={
                "big_bottle_active": "true",
                "little_bottles_count": "3",
                "heat_level": "MEDIUM"
            },
            integration_plan="Add sentiment analysis as new Little Bottle with coordination through Big Bottle",
            risk_assessment="Low risk - isolated Little Bottle with clear coordination protocol",
            alternatives_explored=[
                "enhance_existing_text_processing_little_bottle",
                "improve_coordination_between_guidance_and_webnet_bottles"
            ]
        )
        
        # Should approve controlled addition due to good planning
        self.assertIn(evaluation.decision, [
            ExpansionDecision.CONTROLLED_ADDITION,
            ExpansionDecision.TARGETED_ENHANCEMENT
        ])
    
    def test_beschooled_principles_integration(self):
        """Test integration with Beschooled BigBottleSriracha principles"""
        # Test systematic knowledge building principle
        current_capabilities = ["basic_pattern_recognition"]
        
        # Step 1: Analyze gap for incremental enhancement
        gap_analysis = self.guard.analyze_knowledge_gap(
            current_capabilities,
            "intermediate_pattern_recognition",
            {"learning_progression": "incremental", "educational_rigor": True}
        )
        
        # Should prefer enhancement for incremental development
        self.assertGreater(gap_analysis.enhancement_potential, 0.6)
        
        # Step 2: Evaluate incremental capability development
        evaluation = self.guard.evaluate_capability_expansion(
            proposed_capability="intermediate_pattern_recognition",
            justification="Incremental development following Beschooled principles",
            current_system={
                "learning_progression_tracked": "true",
                "competency_validation": "enabled",
                "evidence_based_decisions": "true"
            },
            integration_plan="Enhance existing pattern recognition with intermediate complexity handling",
            risk_assessment="Very low risk - incremental enhancement of existing capability",
            alternatives_explored=[
                "enhance_basic_pattern_recognition",
                "add_training_data_to_existing_system"
            ]
        )
        
        # Should approve targeted enhancement for educational progression
        self.assertEqual(evaluation.decision, ExpansionDecision.TARGETED_ENHANCEMENT)
        self.assertIn("educational", evaluation.reasoning.lower() or "")
    
    def test_android_bridge_integration_scenario(self):
        """Test scenario that would involve Android bridge integration"""
        # Simulate Android app requesting new capability
        android_capabilities = [
            "ui_coordination",
            "screen_automation", 
            "float_window_management"
        ]
        
        gap_analysis = self.guard.analyze_knowledge_gap(
            android_capabilities,
            "voice_recognition_integration",
            {
                "platform": "android",
                "ui_impact": "high",
                "user_experience_critical": True
            }
        )
        
        evaluation = self.guard.evaluate_capability_expansion(
            proposed_capability="voice_recognition_integration",
            justification="Users request voice commands for accessibility",
            current_system={
                "platform": "android",
                "bridge_status": "active",
                "python_host_available": "true"
            },
            integration_plan="Add voice recognition via Python host with Android bridge communication",
            risk_assessment="Medium risk - new audio processing but isolated through bridge",
            alternatives_explored=[
                "enhance_existing_accessibility_features",
                "integrate_with_system_voice_recognition"
            ]
        )
        
        # Should make appropriate decision based on comprehensive planning
        self.assertIsInstance(evaluation, ExpansionEvaluation)
        self.assertIn("bridge", evaluation.reasoning.lower() or evaluation.required_actions)


class TestPolicyCompliance(unittest.TestCase):
    """Test compliance with policy configurations"""
    
    def setUp(self):
        """Setup policy compliance test environment"""
        self.config_path = os.path.join(
            os.path.dirname(__file__), 
            '..', 
            'configs', 
            'anti_flailing_config.json'
        )
        
        # Create guard with actual repository config if available
        if os.path.exists(self.config_path):
            self.guard = AntiFlailingGuard(self.config_path)
        else:
            self.guard = AntiFlailingGuard()  # Use default config
    
    def test_policy_loading(self):
        """Test that policy configuration loads correctly"""
        self.assertIsNotNone(self.guard.config)
        self.assertIn("flailing_thresholds", self.guard.config)
        self.assertIn("mandatory_gates", self.guard.config)
        self.assertIn("cooling_periods", self.guard.config)
        self.assertIn("enhancement_thresholds", self.guard.config)
    
    def test_mandatory_gates_enforcement(self):
        """Test that mandatory gates are properly enforced"""
        # Test with all gates satisfied
        request_with_gates = {
            "requestId": "test_001",
            "proposedCapability": "test_capability",
            "justification": "test justification",
            "currentSystemState": {},
            "integrationPlan": "test plan",
            "riskAssessment": "test assessment",
            "alternativesExplored": ["alternative1"],
            "timestamp": "2024-01-01"
        }
        
        # Mock a knowledge gap analysis first
        self.guard.knowledge_gaps.append(
            KnowledgeGapAnalysis(
                gap_id="test_gap",
                current_capabilities=["existing"],
                required_task="test_capability",
                gap_severity=0.5,
                existing_alternatives=["alternative1"],
                enhancement_potential=0.6,
                justification_required=False,
                timestamp="2024-01-01"
            )
        )
        
        from anti_flailing_guard import CapabilityExpansionRequest
        request = CapabilityExpansionRequest(**request_with_gates)
        
        gate_results = self.guard._check_mandatory_gates(request)
        
        # All gates should pass
        for gate, result in gate_results.items():
            self.assertTrue(result, f"Gate {gate} should pass")
    
    def test_cooling_period_compliance(self):
        """Test that cooling periods are respected"""
        # This would typically involve time-based testing
        # For now, just verify cooling periods are configured
        cooling_periods = self.guard.config.get("cooling_periods", {})
        
        self.assertIn("after_rejection", cooling_periods)
        self.assertIn("after_emergency", cooling_periods)
        self.assertIn("after_critical_flailing", cooling_periods)
        
        # Verify reasonable values
        self.assertGreater(cooling_periods["after_rejection"], 0)
        self.assertGreater(cooling_periods["after_emergency"], cooling_periods["after_rejection"])
        self.assertGreater(cooling_periods["after_critical_flailing"], cooling_periods["after_emergency"])


if __name__ == "__main__":
    # Create test suite
    suite = unittest.TestSuite()
    
    # Add test cases
    suite.addTest(unittest.makeSuite(TestAntiFlailingGuard))
    suite.addTest(unittest.makeSuite(TestIntegrationScenarios))
    suite.addTest(unittest.makeSuite(TestPolicyCompliance))
    
    # Run tests
    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(suite)
    
    # Exit with appropriate code
    sys.exit(0 if result.wasSuccessful() else 1)