#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Integration Test for Secondary AI Validation System
Tests the smart secondary AI workflows with real code examples.
"""

import asyncio
import sys
from pathlib import Path

# Add the AI script path (relative to repository root)
ai_path = Path.cwd() / "ai"
if ai_path.exists():
    sys.path.append(str(ai_path))
else:
    print(f"❌ AI directory not found at {ai_path}")
    sys.exit(1)
try:
    from secondary_ai_validation import SecondaryAIValidationEngine, ValidationRequest, ValidationType, ValidationPriority
except ImportError as e:
    print(f"❌ Failed to import secondary AI validation: {e}")
    sys.exit(1)

async def test_kotlin_code_validation():
    """Test Kotlin code validation"""
    print("🧪 Testing Kotlin Code Validation")
    print("=" * 50)
    
    engine = SecondaryAIValidationEngine()
    
    # Test good Kotlin code
    good_code = """
package com.example

/**
 * A well-structured user validation class
 */
class UserValidator {
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
    
    fun validateEmail(email: String?): Boolean {
        return !email.isNullOrBlank() && emailPattern.matches(email)
    }
    
    fun validatePassword(password: String?): Boolean {
        return !password.isNullOrBlank() && password.length >= 8
    }
}
"""
    
    request = ValidationRequest(
        code=good_code,
        language="kotlin",
        validation_type=ValidationType.COMPREHENSIVE,
        priority=ValidationPriority.HIGH
    )
    
    result = await engine.validate_code(request)
    
    print(f"✅ Good Kotlin Code Results:")
    print(f"   Overall Score: {result.overall_score}/100")
    print(f"   Validation: {'PASSED' if result.validation_passed else 'FAILED'}")
    print(f"   Security Score: {result.security_validation.security_score}/100")
    print(f"   Quality Score: {result.quality_validation.quality_score}/100")
    print(f"   Processing Time: {result.processing_time_ms:.1f}ms")
    
    assert result.validation_passed, "Good Kotlin code should pass validation"
    assert result.overall_score > 80, f"Good code should score >80, got {result.overall_score}"
    
    print("✅ Good Kotlin code test passed!\n")

async def test_problematic_code_validation():
    """Test problematic code validation"""
    print("🧪 Testing Problematic Code Validation")
    print("=" * 50)
    
    engine = SecondaryAIValidationEngine()
    
    # Test problematic code with security issues
    problematic_code = """
public class BadExample {
    public void processUserInput(String userInput) {
        // SQL injection vulnerability
        String query = "SELECT * FROM users WHERE name = '" + userInput + "'";
        
        // Command injection
        Runtime.getRuntime().exec("rm -rf " + userInput);
        
        // Information disclosure
        System.out.println("Processing: " + userInput);
        
        // Hardcoded secret
        String apiKey = "sk-1234567890abcdef";
    }
}
"""
    
    request = ValidationRequest(
        code=problematic_code,
        language="java",
        validation_type=ValidationType.SECURITY_FOCUS,
        priority=ValidationPriority.CRITICAL
    )
    
    result = await engine.validate_code(request)
    
    print(f"⚠️ Problematic Code Results:")
    print(f"   Overall Score: {result.overall_score}/100")
    print(f"   Validation: {'PASSED' if result.validation_passed else 'FAILED'}")
    print(f"   Security Score: {result.security_validation.security_score}/100")
    print(f"   Risk Level: {result.security_validation.risk_level.value.upper()}")
    print(f"   Vulnerabilities: {len(result.security_validation.vulnerabilities)}")
    print(f"   Processing Time: {result.processing_time_ms:.1f}ms")
    
    if result.recommendations:
        print(f"   Recommendations:")
        for i, rec in enumerate(result.recommendations[:3], 1):
            print(f"     {i}. {rec}")
    
    # For critical priority with security issues, should fail
    assert not result.validation_passed, "Problematic code should fail validation at critical priority"
    assert result.security_validation.security_score < 70, f"Problematic code should have low security score, got {result.security_validation.security_score}"
    assert len(result.security_validation.vulnerabilities) > 0, "Should detect vulnerabilities"
    
    print("✅ Problematic code test passed!\n")

async def test_performance_focused_validation():
    """Test performance-focused validation"""
    print("🧪 Testing Performance-Focused Validation")
    print("=" * 50)
    
    engine = SecondaryAIValidationEngine()
    
    # Test code with performance issues
    performance_code = """
def inefficient_function(data_list):
    result = ""
    for i in range(len(data_list)):
        result = result + str(data_list[i])  # String concatenation in loop
    
    # Nested loops
    for i in range(len(data_list)):
        for j in range(len(data_list)):
            for k in range(len(data_list)):
                print(data_list[i] + data_list[j] + data_list[k])
    
    return result
"""
    
    request = ValidationRequest(
        code=performance_code,
        language="python",
        validation_type=ValidationType.PERFORMANCE_FOCUS,
        priority=ValidationPriority.MEDIUM
    )
    
    result = await engine.validate_code(request)
    
    print(f"⚡ Performance-Focused Results:")
    print(f"   Overall Score: {result.overall_score}/100")
    print(f"   Performance Score: {result.performance_validation.performance_score}/100")
    print(f"   Complexity: {result.performance_validation.complexity_analysis}")
    print(f"   Optimization Opportunities: {len(result.performance_validation.optimization_opportunities)}")
    print(f"   Processing Time: {result.processing_time_ms:.1f}ms")
    
    if result.performance_validation.optimization_opportunities:
        print(f"   Optimizations:")
        for i, opt in enumerate(result.performance_validation.optimization_opportunities[:3], 1):
            print(f"     {i}. {opt}")
    
    # Should detect performance issues
    assert len(result.performance_validation.optimization_opportunities) > 0, "Should detect optimization opportunities"
    
    print("✅ Performance-focused test passed!\n")

async def run_comprehensive_tests():
    """Run all integration tests"""
    print("🚀 Secondary AI Validation Integration Tests")
    print("=" * 60)
    
    try:
        await test_kotlin_code_validation()
        await test_problematic_code_validation()
        await test_performance_focused_validation()
        
        print("🎉 All integration tests passed!")
        print("✅ Secondary AI Validation System is working correctly!")
        
    except Exception as e:
        print(f"❌ Integration test failed: {e}")
        raise e

if __name__ == "__main__":
    asyncio.run(run_comprehensive_tests())