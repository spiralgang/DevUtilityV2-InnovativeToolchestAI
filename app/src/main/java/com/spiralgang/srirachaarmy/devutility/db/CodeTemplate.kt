package com.spiralgang.srirachaarmy.devutility.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Code template entity for pre-built code templates
 * Part of DevUtility V2.5 template system
 */
@Entity(tableName = "code_templates")
data class CodeTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val code: String,
    val language: String,
    val description: String? = null,
    val category: String = "general",
    val tags: String? = null,
    val isBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val usageCount: Int = 0,
    val rating: Float = 0.0f,
    val authorId: String? = null,
    val templateVersion: String = "1.0",
    val dependencies: String? = null, // JSON string of dependencies
    val placeholders: String? = null // JSON string of placeholder definitions
)