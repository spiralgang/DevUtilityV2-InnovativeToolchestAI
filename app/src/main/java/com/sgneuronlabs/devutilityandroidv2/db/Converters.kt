package com.sgneuronlabs.devutilityandroidv2.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return value.split(",").map { it.trim() }
    }

    @TypeConverter
    fun fromListString(list: List<String>): String {
        return list.joinToString(",")
    }
}