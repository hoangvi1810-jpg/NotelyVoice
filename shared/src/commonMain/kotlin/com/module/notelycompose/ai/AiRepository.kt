package com.module.notelycompose.ai

import com.module.notelycompose.core.DateTimeUtil
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.platform.SecureKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Orchestrates AI generation for a note: builds the right prompt for the chosen template/output
 * (see [AiPrompts]), calls OpenRouter, and caches the result in SQLDelight so re-opening a note or
 * switching tabs doesn't re-spend API credits. This is the replacement for the old
 * `summary/TFIDFSummarizer.kt`, which has no Vietnamese support.
 */
class AiRepository(
    private val client: OpenRouterClient,
    private val dataSource: AiContentDataSource,
    private val secureKeyStore: SecureKeyStore,
    private val preferencesRepository: PreferencesRepository
) {

    fun getCached(noteId: Long): NoteAiContent? = dataSource.getByNoteId(noteId)

    suspend fun getApiKey(): String? = withContext(Dispatchers.Default) {
        secureKeyStore.getApiKey()
    }

    suspend fun setApiKey(key: String) = withContext(Dispatchers.Default) {
        secureKeyStore.setApiKey(key)
    }

    suspend fun clearApiKey() = withContext(Dispatchers.Default) {
        secureKeyStore.clearApiKey()
    }

    suspend fun getModel(): String = preferencesRepository.getOpenRouterModel().first()

    suspend fun setModel(model: String) = preferencesRepository.setOpenRouterModel(model)

    suspend fun getLastUsedTemplate(): AiTemplate =
        AiTemplate.fromId(preferencesRepository.getNoteTemplate().first())

    /**
     * Regenerates AI Note (for [template]), Highlights and Summary for [noteId] from [transcript],
     * running all three OpenRouter calls concurrently. Whatever succeeds is cached even if
     * something else fails — a flaky network blip on one tab shouldn't discard the other two.
     * Returns the first failure encountered, if any, so the caller can show a retry affordance.
     */
    suspend fun regenerate(
        noteId: Long,
        transcript: String,
        template: AiTemplate
    ): AiResult = withContext(Dispatchers.Default) {
        preferencesRepository.setNoteTemplate(template.id)

        val apiKey = secureKeyStore.getApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext AiResult.Failure(
                AiFailureReason.NO_API_KEY,
                "Chưa cấu hình OpenRouter API key trong Cài đặt."
            )
        }
        if (transcript.isBlank()) {
            return@withContext AiResult.Failure(
                AiFailureReason.EMPTY_TRANSCRIPT,
                "Chưa có nội dung ghi âm để xử lý."
            )
        }
        val model = preferencesRepository.getOpenRouterModel().first()
        val existing = dataSource.getByNoteId(noteId) ?: NoteAiContent.empty(noteId)

        val (noteResult, highlightsResult, summaryResult) = coroutineScope {
            val noteDeferred = async {
                client.complete(apiKey, model, AiPrompts.forNoteTemplate(template), transcript)
            }
            val highlightsDeferred = async {
                client.complete(apiKey, model, AiPrompts.forHighlights, transcript)
            }
            val summaryDeferred = async {
                client.complete(apiKey, model, AiPrompts.forSummary, transcript)
            }
            Triple(noteDeferred.await(), highlightsDeferred.await(), summaryDeferred.await())
        }

        val merged = existing.copy(
            template = template,
            aiNote = (noteResult as? AiResult.Success)?.text ?: existing.aiNote,
            highlights = (highlightsResult as? AiResult.Success)?.text ?: existing.highlights,
            summary = (summaryResult as? AiResult.Success)?.text ?: existing.summary,
            generatedAt = DateTimeUtil.toEpochMilli(DateTimeUtil.now())
        )
        dataSource.upsert(merged)

        listOf(noteResult, highlightsResult, summaryResult)
            .filterIsInstance<AiResult.Failure>()
            .firstOrNull()
            ?: AiResult.Success(merged.aiNote)
    }

    /** Generates a short title + tags once, right after transcription finishes. */
    suspend fun generateTitleAndTags(transcript: String): Pair<String, List<String>>? =
        withContext(Dispatchers.Default) {
            val apiKey = secureKeyStore.getApiKey()
            if (apiKey.isNullOrBlank() || transcript.isBlank()) return@withContext null
            val model = preferencesRepository.getOpenRouterModel().first()

            val result = client.complete(apiKey, model, AiPrompts.forTitleAndTags, transcript)
            val text = (result as? AiResult.Success)?.text ?: return@withContext null

            val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
            val title = lines.getOrNull(0)?.take(80) ?: return@withContext null
            val tags = lines.getOrNull(1)
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()
            title to tags
        }

    /** Persists tags from [generateTitleAndTags] into the cache row for [noteId]. */
    suspend fun saveTags(noteId: Long, tags: List<String>) = withContext(Dispatchers.Default) {
        val existing = dataSource.getByNoteId(noteId) ?: NoteAiContent.empty(noteId)
        dataSource.upsert(existing.copy(tags = tags))
    }

    suspend fun deleteForNote(noteId: Long) = withContext(Dispatchers.Default) {
        dataSource.deleteByNoteId(noteId)
    }
}
