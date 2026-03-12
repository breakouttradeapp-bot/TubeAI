package com.tubeboost.ai.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Cerebras/OpenAI compatible API response
 */
data class ChatCompletionResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("object") val objectType: String? = null,
    @SerializedName("created") val created: Long? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("choices") val choices: List<Choice>? = null,
    @SerializedName("usage") val usage: UsageInfo? = null,
    @SerializedName("error") val error: ApiError? = null
)

data class Choice(
    @SerializedName("index") val index: Int? = null,
    @SerializedName("message") val message: ResponseMessage? = null,
    @SerializedName("finish_reason") val finishReason: String? = null
)

data class ResponseMessage(
    @SerializedName("role") val role: String? = null,
    @SerializedName("content") val content: String? = null
)

data class UsageInfo(
    @SerializedName("prompt_tokens") val promptTokens: Int? = null,
    @SerializedName("completion_tokens") val completionTokens: Int? = null,
    @SerializedName("total_tokens") val totalTokens: Int? = null
)

data class ApiError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("code") val code: String? = null
)

/**
 * Parsed SEO content result (used internally and passed between activities)
 */
@Parcelize
data class SeoResult(
    val topic: String = "",
    val tags: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val titles: List<String> = emptyList(),
    val description: String = ""
) : Parcelable {

    fun isValid(): Boolean {
        return tags.isNotEmpty() || hashtags.isNotEmpty() || titles.isNotEmpty()
    }

    fun getTagsFormatted(): String {
        return tags.joinToString(", ")
    }

    fun getHashtagsFormatted(): String {
        return hashtags.joinToString(" ")
    }

    fun getTitlesFormatted(): String {
        return titles.mapIndexed { index, title ->
            "${index + 1}. $title"
        }.joinToString("\n")
    }

    fun getFullShareText(): String {
        val sb = StringBuilder()
        sb.appendLine("🎬 Video SEO Results for: \"$topic\"")
        sb.appendLine()

        if (titles.isNotEmpty()) {
            sb.appendLine("🔥 VIRAL TITLES:")
            titles.forEachIndexed { index, title ->
                sb.appendLine("${index + 1}. $title")
            }
            sb.appendLine()
        }

        if (tags.isNotEmpty()) {
            sb.appendLine("🏷️ SEO TAGS:")
            sb.appendLine(getTagsFormatted())
            sb.appendLine()
        }

        if (hashtags.isNotEmpty()) {
            sb.appendLine("# HASHTAGS:")
            sb.appendLine(getHashtagsFormatted())
            sb.appendLine()
        }

        if (description.isNotEmpty()) {
            sb.appendLine("📝 DESCRIPTION:")
            sb.appendLine(description)
            sb.appendLine()
        }

        sb.appendLine("Generated with TubeBoost AI - Video Creator Tools")
        return sb.toString()
    }
}

/**
 * Inner JSON model that the AI returns inside the content field
 */
data class AiContentResponse(
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("hashtags") val hashtags: List<String>? = null,
    @SerializedName("titles") val titles: List<String>? = null,
    @SerializedName("description") val description: String? = null
)

/**
 * UI State sealed class for the generation flow
 */
sealed class GenerationState {
    object Idle : GenerationState()
    object Loading : GenerationState()
    data class Success(val result: SeoResult) : GenerationState()
    data class Error(val message: String) : GenerationState()
}
