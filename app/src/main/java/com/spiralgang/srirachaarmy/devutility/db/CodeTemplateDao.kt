package com.spiralgang.srirachaarmy.devutility.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for code template operations
 * Part of DevUtility V2.5 template management system
 */
@Dao
interface CodeTemplateDao {
    
    @Query("SELECT * FROM code_templates ORDER BY usageCount DESC")
    fun getAllTemplates(): Flow<List<CodeTemplate>>
    
    @Query("SELECT * FROM code_templates WHERE language = :language ORDER BY usageCount DESC")
    fun getTemplatesByLanguage(language: String): Flow<List<CodeTemplate>>
    
    @Query("SELECT * FROM code_templates WHERE category = :category ORDER BY usageCount DESC")
    fun getTemplatesByCategory(category: String): Flow<List<CodeTemplate>>
    
    @Query("SELECT * FROM code_templates WHERE isBuiltIn = 1")
    fun getBuiltInTemplates(): Flow<List<CodeTemplate>>
    
    @Query("SELECT * FROM code_templates WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchTemplates(query: String): Flow<List<CodeTemplate>>
    
    @Query("SELECT * FROM code_templates WHERE id = :id")
    suspend fun getTemplateById(id: Int): CodeTemplate?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: CodeTemplate): Long
    
    @Update
    suspend fun updateTemplate(template: CodeTemplate)
    
    @Delete
    suspend fun deleteTemplate(template: CodeTemplate)
    
    @Query("UPDATE code_templates SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: Int)
    
    @Query("UPDATE code_templates SET rating = :rating WHERE id = :id")
    suspend fun updateRating(id: Int, rating: Float)
    
    @Query("SELECT DISTINCT language FROM code_templates ORDER BY language")
    fun getAvailableLanguages(): Flow<List<String>>
    
    @Query("SELECT DISTINCT category FROM code_templates ORDER BY category")
    fun getAvailableCategories(): Flow<List<String>>
}

/**
 * Room type converters for complex data types
 */
class DatabaseConverters {
    // Add converters for JSON strings, lists, etc. as needed
}