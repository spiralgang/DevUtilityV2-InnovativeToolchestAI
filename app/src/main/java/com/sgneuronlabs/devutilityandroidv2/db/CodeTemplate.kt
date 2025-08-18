package com.sgneuronlabs.devutilityandroidv2.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "code_templates")
data class CodeTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val templateCode: String,
    val language: String,
    val description: String = "",
    val category: String = "general",
    val createdAt: Long = System.currentTimeMillis()
)