package com.module.notelycompose.notes.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.ai.AiRepository
import com.module.notelycompose.ai.AiResult
import com.module.notelycompose.ai.AiTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteAiUiState(
    val noteId: Long = 0L,
    val template: AiTemplate = AiTemplate.CLASSIC,
    val aiNote: String = "",
    val highlights: String = "",
    val summary: String = "",
    val hasGenerated: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Set once, right after the first successful generation for this note (see `regenerate`).
    // The screen observes this and writes it into the note's title via TextEditorViewModel —
    // consumed once, then cleared, so it never re-fires on recomposition or template switches.
    val generatedTitle: String? = null,
    val generatedTags: List<String> = emptyList()
)

/**
 * Bridges [AiRepository] to the note detail screen's AI Note/Highlights/Summary tabs. Kept as a
 * separate ViewModel from [TextEditorViewModel] since it has its own async load/generate
 * lifecycle (network calls, loading/error states) that shouldn't complicate the text editor's
 * synchronous state machine.
 */
class NoteAiViewModel(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteAiUiState())
    val uiState: StateFlow<NoteAiUiState> = _uiState

    private var loadedNoteId: Long = -1L

    /** Loads cached AI content for [noteId], if any. Safe to call on every recomposition. */
    fun loadIfNeeded(noteId: Long) {
        if (noteId <= 0L || noteId == loadedNoteId) return
        loadedNoteId = noteId
        viewModelScope.launch {
            val defaultTemplate = aiRepository.getLastUsedTemplate()
            val cached = aiRepository.getCached(noteId)
            _uiState.value = if (cached != null) {
                NoteAiUiState(
                    noteId = noteId,
                    template = cached.template,
                    aiNote = cached.aiNote,
                    highlights = cached.highlights,
                    summary = cached.summary,
                    hasGenerated = cached.aiNote.isNotBlank() ||
                        cached.highlights.isNotBlank() ||
                        cached.summary.isNotBlank()
                )
            } else {
                NoteAiUiState(noteId = noteId, template = defaultTemplate)
            }
        }
    }

    /** Switching templates regenerates immediately — the whole point of picking one. */
    fun selectTemplate(template: AiTemplate, transcript: String) {
        _uiState.update { it.copy(template = template) }
        regenerate(transcript)
    }

    fun regenerate(transcript: String) {
        val noteId = _uiState.value.noteId
        if (noteId <= 0L) {
            _uiState.update {
                it.copy(errorMessage = "Lưu ghi âm trước khi tạo AI Note.")
            }
            return
        }
        val template = _uiState.value.template
        val isFirstGeneration = !_uiState.value.hasGenerated
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = aiRepository.regenerate(noteId, transcript, template)) {
                is AiResult.Success -> {
                    val cached = aiRepository.getCached(noteId)
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            hasGenerated = true,
                            aiNote = cached?.aiNote ?: current.aiNote,
                            highlights = cached?.highlights ?: current.highlights,
                            summary = cached?.summary ?: current.summary
                        )
                    }
                    // Auto-title+tag once per note, the first time it has any AI content —
                    // not on every regenerate/template switch after that.
                    if (isFirstGeneration) {
                        val titleAndTags = aiRepository.generateTitleAndTags(transcript)
                        if (titleAndTags != null) {
                            val (title, tags) = titleAndTags
                            aiRepository.saveTags(noteId, tags)
                            _uiState.update { it.copy(generatedTitle = title, generatedTags = tags) }
                        }
                    }
                }

                is AiResult.Failure -> {
                    // Whatever succeeded before the failing call(s) is still cached and already
                    // reflected in uiState from a previous load — only surface the error banner.
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    /** Call once the screen has applied [NoteAiUiState.generatedTitle] to the note. */
    fun consumeGeneratedTitle() {
        _uiState.update { it.copy(generatedTitle = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
