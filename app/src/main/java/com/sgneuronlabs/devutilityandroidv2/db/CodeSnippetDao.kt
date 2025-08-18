package com.sgneuronlabs.devutilityandroidv2.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeSnippetDao {
    @Query("SELECT * FROM code_snippets ORDER BY createdAt DESC")
    fun getAllSnippets(): Flow<List<CodeSnippet>>

    @Query("SELECT * FROM code_snippets WHERE language = :language")
    fun getSnippetsByLanguage(language: String): Flow<List<CodeSnippet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: CodeSnippet): Long

    @Update
    suspend fun updateSnippet(snippet: CodeSnippet)

    @Delete
    suspend fun deleteSnippet(snippet: CodeSnippet)

    @Query("DELETE FROM code_snippets WHERE id = :snippetId")
    suspend fun deleteSnippetById(snippetId: Long)
}