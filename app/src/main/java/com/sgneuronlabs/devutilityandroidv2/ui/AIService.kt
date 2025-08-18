package com.sgneuronlabs.devutilityandroidv2.ui

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class OpenAIRequest(
    @Json(name = "prompt") val prompt: String,
    @Json(name = "max_tokens") val maxTokens: Int = 50,
    @Json(name = "model") val model: String = "text-davinci-003"
)

data class OpenAIResponse(
    @Json(name = "choices") val choices: List<Choice>,
    @Json(name = "error") val error: Error?
) {
    data class Choice(
        @Json(name = "text") val text: String
    )
    data class Error(
        @Json(name = "message") val message: String
    )
}

interface OpenAIService {
    @POST("v1/completions")
    suspend fun generateCodeSuggestion(
        @Header("Authorization") apiKey: String,
        @Body request: OpenAIRequest
    ): OpenAIResponse
}

class AIService(private val userApiKey: String) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            )
        )
        .build()

    private val apiService = retrofit.create(OpenAIService::class.java)

    suspend fun generateCodeSuggestion(input: String, language: String = "kotlin"): String {
        return withContext(Dispatchers.IO) {
            val request = OpenAIRequest(
                prompt = "Generate a $language code snippet for: $input"
            )
            val response = apiService.generateCodeSuggestion("Bearer $userApiKey", request)
            response.error?.let { throw Exception(it.message) }
            response.choices.firstOrNull()?.text?.trim() ?: "No suggestion available"
        }
    }
}