package com.tubeboost.ai.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tubeboost.ai.model.*
import com.tubeboost.ai.network.RetrofitClient
import com.tubeboost.ai.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * MainViewModel - Handles all SEO content generation logic
 * Follows MVVM pattern with LiveData for UI state management
 */
class MainViewModel : ViewModel() {

    private val TAG = "MainViewModel"
    private val gson = Gson()

    // ─── LiveData ─────────────────────────────────────────────────────────────

    private val _generationState = MutableLiveData<GenerationState>(GenerationState.Idle)
    val generationState: LiveData<GenerationState> = _generationState

    private val _bonusTagsState = MutableLiveData<GenerationState>(GenerationState.Idle)
    val bonusTagsState: LiveData<GenerationState> = _bonusTagsState

    // Track last used topic for bonus generation
    private var lastTopic: String = ""

    // ─── Main Generation ──────────────────────────────────────────────────────

    /**
     * Main entry point - validates input and triggers content generation
     */
    fun generateContent(context: Context, topic: String) {
        val trimmedTopic = topic.trim()

        // Input validation
        if (trimmedTopic.isEmpty()) {
            _generationState.value = GenerationState.Error("Please enter a video topic")
            return
        }

        if (trimmedTopic.length < 3) {
            _generationState.value = GenerationState.Error("Topic is too short. Please be more descriptive.")
            return
        }

        if (trimmedTopic.length > 200) {
            _generationState.value = GenerationState.Error("Topic is too long. Please keep it under 200 characters.")
            return
        }

        // Check internet connectivity
        if (!NetworkUtils.isInternetAvailable(context)) {
            _generationState.value = GenerationState.Error(
                "No internet connection. Please check your network and try again."
            )
            return
        }

        lastTopic = trimmedTopic
        performGeneration(trimmedTopic, includeDescription = true)
    }

    /**
     * Generate bonus tags after user watches rewarded ad
     */
    fun generateBonusTags(context: Context) {
        if (lastTopic.isEmpty()) {
            _bonusTagsState.value = GenerationState.Error("No topic found. Please generate content first.")
            return
        }

        if (!NetworkUtils.isInternetAvailable(context)) {
            _bonusTagsState.value = GenerationState.Error("No internet connection.")
            return
        }

        viewModelScope.launch {
            _bonusTagsState.value = GenerationState.Loading

            try {
                val request = createChatRequest(lastTopic, includeDescription = false)
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.generateSeoContent(request)
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    val result = parseApiResponse(body, lastTopic)
                    if (result != null) {
                        _bonusTagsState.value = GenerationState.Success(result)
                    } else {
                        _bonusTagsState.value = GenerationState.Error(
                            "Unable to generate bonus tags. Please try again."
                        )
                    }
                } else {
                    _bonusTagsState.value = GenerationState.Error(
                        "Server error: ${response.code()}. Please try again."
                    )
                }
            } catch (e: Exception) {
                _bonusTagsState.value = GenerationState.Error(
                    "Unable to generate bonus tags. Please try again."
                )
            }
        }
    }

    /**
     * Performs the actual API call
     */
    private fun performGeneration(topic: String, includeDescription: Boolean) {
        viewModelScope.launch {
            _generationState.value = GenerationState.Loading

            try {
                val request = createChatRequest(topic, includeDescription)

                Log.d(TAG, "Calling API for topic: $topic")

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.generateSeoContent(request)
                }

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.error != null) {
                        _generationState.value = GenerationState.Error(
                            "API Error: ${body.error.message ?: "Unknown error"}"
                        )
                        return@launch
                    }

                    val result = parseApiResponse(body, topic)
                    if (result != null && result.isValid()) {
                        Log.d(TAG, "Generation successful: ${result.tags.size} tags, ${result.titles.size} titles")
                        _generationState.value = GenerationState.Success(result)
                    } else {
                        _generationState.value = GenerationState.Error(
                            "Unable to generate results. Please try again."
                        )
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid API key. Please check your configuration."
                        403 -> "Access denied. Please check your API permissions."
                        429 -> "Too many requests. Please wait a moment and try again."
                        500, 502, 503 -> "Server is temporarily unavailable. Please try again later."
                        else -> "Unable to generate results. Please try again."
                    }
                    _generationState.value = GenerationState.Error(errorMessage)
                }

            } catch (e: UnknownHostException) {
                Log.e(TAG, "DNS resolution failed: ${e.message}")
                _generationState.value = GenerationState.Error(
                    "Cannot reach server. Please check your internet connection."
                )
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Request timed out: ${e.message}")
                _generationState.value = GenerationState.Error(
                    "Request timed out. Please try again."
                )
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                _generationState.value = GenerationState.Error(
                    "Network error. Please check your connection and try again."
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error: ${e.message}", e)
                _generationState.value = GenerationState.Error(
                    "Unable to generate results. Please try again."
                )
            }
        }
    }

    /**
     * Parses the AI API response and extracts SEO content
     * Handles various JSON formats the AI might return
     */
    private fun parseApiResponse(response: ChatCompletionResponse?, topic: String): SeoResult? {
        if (response == null) return null

        val content = response.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.trim() ?: return null

        Log.d(TAG, "Raw AI response content: ${content.take(200)}...")

        return try {
            // Clean potential markdown code blocks
            val cleanJson = cleanJsonString(content)
            val aiResponse = gson.fromJson(cleanJson, AiContentResponse::class.java)

            SeoResult(
                topic = topic,
                tags = aiResponse.tags?.filter { it.isNotBlank() } ?: emptyList(),
                hashtags = normalizeHashtags(aiResponse.hashtags),
                titles = aiResponse.titles?.filter { it.isNotBlank() } ?: emptyList(),
                description = aiResponse.description?.trim() ?: ""
            )
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON parse error, trying manual extraction: ${e.message}")
            // Fallback: try to extract arrays manually
            extractContentManually(content, topic)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected parse error: ${e.message}")
            null
        }
    }

    /**
     * Cleans JSON string from markdown code fences the AI might add
     */
    private fun cleanJsonString(raw: String): String {
        var cleaned = raw.trim()

        // Remove markdown code blocks
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json").trim()
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```").trim()
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```").trim()
        }

        return cleaned
    }

    /**
     * Normalizes hashtags to ensure they all have # prefix
     */
    private fun normalizeHashtags(hashtags: List<String>?): List<String> {
        return hashtags
            ?.filter { it.isNotBlank() }
            ?.map { tag ->
                val trimmed = tag.trim()
                if (trimmed.startsWith("#")) trimmed else "#$trimmed"
            } ?: emptyList()
    }

    /**
     * Last-resort manual extraction when JSON parsing fails
     */
    private fun extractContentManually(content: String, topic: String): SeoResult? {
        return try {
            // Try to find JSON object boundaries
            val start = content.indexOf('{')
            val end = content.lastIndexOf('}')

            if (start >= 0 && end > start) {
                val jsonSubstring = content.substring(start, end + 1)
                val aiResponse = gson.fromJson(jsonSubstring, AiContentResponse::class.java)
                SeoResult(
                    topic = topic,
                    tags = aiResponse.tags?.filter { it.isNotBlank() } ?: emptyList(),
                    hashtags = normalizeHashtags(aiResponse.hashtags),
                    titles = aiResponse.titles?.filter { it.isNotBlank() } ?: emptyList(),
                    description = aiResponse.description?.trim() ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Manual extraction also failed: ${e.message}")
            null
        }
    }

    /**
     * Reset state back to idle
     */
    fun resetState() {
        _generationState.value = GenerationState.Idle
    }

    fun resetBonusState() {
        _bonusTagsState.value = GenerationState.Idle
    }
}
