package com.spiralgang.srirachaarmy.devutility.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for code snippet operations
 * Part of DevUtility V2.5 embedded database solution with Room
 */
@Dao
interface CodeSnippetDao {
    
    @Query("SELECT * FROM code_snippets ORDER BY updatedAt DESC")
    fun getAllSnippets(): Flow<List<CodeSnippet>>
    
    @Query("SELECT * FROM code_snippets WHERE language = :language ORDER BY updatedAt DESC")
    fun getSnippetsByLanguage(language: String): Flow<List<CodeSnippet>>
    
    @Query("SELECT * FROM code_snippets WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteSnippets(): Flow<List<CodeSnippet>>
    
    @Query("SELECT * FROM code_snippets WHERE title LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchSnippets(query: String): Flow<List<CodeSnippet>>
    
    @Query("SELECT * FROM code_snippets WHERE id = :id")
    suspend fun getSnippetById(id: Int): CodeSnippet?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: CodeSnippet): Long
    
    @Update
    suspend fun updateSnippet(snippet: CodeSnippet)
    
    @Delete
    suspend fun deleteSnippet(snippet: CodeSnippet)
    
    @Query("DELETE FROM code_snippets WHERE id = :id")
    suspend fun deleteSnippetById(id: Int)
    
    @Query("SELECT * FROM code_snippets WHERE cloudSynced = 0")
    suspend fun getUnsyncedSnippets(): List<CodeSnippet>
    
    @Query("UPDATE code_snippets SET cloudSynced = 1, lastSyncedAt = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: Int, timestamp: Long)
    
    @Query("SELECT DISTINCT language FROM code_snippets ORDER BY language")
    fun getAvailableLanguages(): Flow<List<String>>
    
    @Query("SELECT DISTINCT category FROM code_snippets ORDER BY category")
    fun getAvailableCategories(): Flow<List<String>>
}