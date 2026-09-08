package com.module.notelycompose.ai

import kotlinx.serialization.Serializable

/**
 * Note templates — each maps to a different system prompt in [AiPrompts], controlling how the
 * raw transcript is rewritten for the "AI Note" tab.
 */
enum class AiTemplate(val id: String) {
    CLASSIC("classic"),
    BRAINSTORM("brainstorm"),
    MEETING("meeting"),
    LECTURE("lecture"),
    JOURNALING("journaling");

    companion object {
        fun fromId(id: String): AiTemplate = entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}

/** The four generated views shown as tabs on the note detail screen. */
enum class AiOutputType {
    AI_NOTE,
    HIGHLIGHTS,
    SUMMARY
}

/**
 * A stable OpenRouter alias that always points at Google's newest Flash model. Note the leading
 * `~` — it's part of the real model id on OpenRouter's live API (confirmed against
 * GET /api/v1/models; the un-prefixed "google/gemini-flash-latest" 400s as an unknown model).
 */
const val DEFAULT_OPENROUTER_MODEL = "~google/gemini-flash-latest"
const val OPENROUTER_CHAT_COMPLETIONS_URL = "https://openrouter.ai/api/v1/chat/completions"

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.4
)

@Serializable
data class ChatCompletionResponse(
    val choices: List<ChatChoice> = emptyList(),
    val error: ChatError? = null
)

@Serializable
data class ChatChoice(
    val message: ChatMessage
)

@Serializable
data class ChatError(
    val message: String = "Unknown error",
    val code: String? = null
)

/** Result of a single AI generation call. */
sealed class AiResult {
    data class Success(val text: String) : AiResult()
    data class Failure(val reason: AiFailureReason, val message: String) : AiResult()
}

enum class AiFailureReason {
    NO_API_KEY,
    NETWORK,
    AUTH,
    RATE_LIMIT_OR_QUOTA,
    EMPTY_TRANSCRIPT,
    UNKNOWN
}

/** Cached AI-generated content for one note, keyed by note id. */
data class NoteAiContent(
    val noteId: Long,
    val template: AiTemplate,
    val aiNote: String,
    val highlights: String,
    val summary: String,
    val tags: List<String>,
    val generatedAt: Long
) {
    companion object {
        fun empty(noteId: Long) = NoteAiContent(
            noteId = noteId,
            template = AiTemplate.CLASSIC,
            aiNote = "",
            highlights = "",
            summary = "",
            tags = emptyList(),
            generatedAt = 0L
        )
    }
}
