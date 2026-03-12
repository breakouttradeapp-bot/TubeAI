package com.tubeboost.ai.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for Cerebras AI API (OpenAI-compatible format)
 */
data class ChatRequest(
    @SerializedName("model") val model: String = "llama-3.3-70b",
    @SerializedName("stream") val stream: Boolean = false,
    @SerializedName("messages") val messages: List<ChatMessage>,
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("top_p") val topP: Double = 1.0,
    @SerializedName("max_tokens") val maxTokens: Int = 2048
)

data class ChatMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

/**
 * Builds a well-structured prompt for SEO content generation
 */
fun buildSeoPrompt(topic: String, includeDescription: Boolean = true): String {
    return if (includeDescription) {
        """You are an expert video SEO specialist and content strategist. Generate comprehensive SEO content for a video about: "$topic"

Return ONLY a valid JSON object with exactly these keys:
{
  "tags": [array of 30 highly searched SEO tags/keywords, each 1-4 words, no # symbol],
  "hashtags": [array of 15 trending hashtags WITH # symbol, mix of broad and niche],
  "titles": [array of 5 viral, click-worthy video titles that maximize CTR],
  "description": "A compelling 150-200 word video description optimized for search, include a call-to-action"
}

Requirements:
- Tags: Mix high-volume keywords with long-tail keywords relevant to the topic
- Hashtags: Include trending and niche hashtags for maximum reach  
- Titles: Use power words, numbers, questions, or emotional triggers
- Description: Natural language, keyword-rich, engaging opening line
- Return ONLY the JSON object, no markdown, no explanation"""
    } else {
        """You are an expert video SEO specialist. Generate SEO tags and hashtags for a video about: "$topic"

Return ONLY a valid JSON object:
{
  "tags": [array of 30 additional SEO tags different from previously generated ones],
  "hashtags": [array of 15 additional trending hashtags WITH # symbol],
  "titles": [array of 5 more viral video title variations],
  "description": ""
}

Return ONLY the JSON object, no markdown, no explanation"""
    }
}

/**
 * Helper to create the full API request
 */
fun createChatRequest(topic: String, includeDescription: Boolean = true): ChatRequest {
    return ChatRequest(
        messages = listOf(
            ChatMessage(
                role = "user",
                content = buildSeoPrompt(topic, includeDescription)
            )
        )
    )
}
