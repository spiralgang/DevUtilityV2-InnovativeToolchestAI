package com.sgneuronlabs.devutilityandroidv2.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "code_snippets")
data class CodeSnippet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val code: String,
    val language: String,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis()
)