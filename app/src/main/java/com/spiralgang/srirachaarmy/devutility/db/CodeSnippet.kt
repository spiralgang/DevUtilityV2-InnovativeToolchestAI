package com.spiralgang.srirachaarmy.devutility.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Code snippet entity for Room database
 * Part of DevUtility V2.5 embedded database solution
 */
@Entity(tableName = "code_snippets")
data class CodeSnippet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val code: String,
    val language: String,
    val tags: String? = null,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val category: String = "general",
    val isPublic: Boolean = false,
    val authorId: String? = null,
    val cloudSynced: Boolean = false,
    val lastSyncedAt: Long? = null
)