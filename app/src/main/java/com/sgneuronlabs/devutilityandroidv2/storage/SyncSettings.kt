package com.sgneuronlabs.devutilityandroidv2.storage

data class SyncSettings(
    val autoSync: Boolean = false,
    val wifiOnly: Boolean = true,
    val maxFileSize: Long = 10 * 1024 * 1024, // 10MB
    val syncFrequency: Long = 3600000 // 1 hour in milliseconds
)