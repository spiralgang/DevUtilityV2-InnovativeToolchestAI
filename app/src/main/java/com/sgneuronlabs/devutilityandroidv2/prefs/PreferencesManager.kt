package com.sgneuronlabs.devutilityandroidv2.prefs

class PreferencesManager {
    private val preferences = mutableMapOf<String, String>()

    fun saveString(key: String, value: String) {
        preferences[key] = value
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return preferences[key] ?: defaultValue
    }

    fun saveBoolean(key: String, value: Boolean) {
        preferences[key] = value.toString()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return preferences[key]?.toBoolean() ?: defaultValue
    }

    fun saveFloat(key: String, value: Float) {
        preferences[key] = value.toString()
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return preferences[key]?.toFloat() ?: defaultValue
    }
}