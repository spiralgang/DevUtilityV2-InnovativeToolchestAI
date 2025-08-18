package com.sgneuronlabs.devutilityandroidv2.db

data class AnalysisRule(
    val id: String,
    val name: String,
    val description: String,
    val ruleType: String,
    val pattern: String,
    val enabled: Boolean = true
)