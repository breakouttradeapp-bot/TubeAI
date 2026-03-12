package com.tubeboost.ai.network

import com.tubeboost.ai.model.ChatCompletionResponse
import com.tubeboost.ai.model.ChatRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit API service interface for Cerebras AI
 * Using OpenAI-compatible chat completions endpoint
 */
interface ApiService {

    @POST("v1/chat/completions")
    suspend fun generateSeoContent(
        @Body request: ChatRequest
    ): Response<ChatCompletionResponse>
}
