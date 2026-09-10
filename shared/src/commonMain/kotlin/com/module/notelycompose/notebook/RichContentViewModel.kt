package com.module.notelycompose.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Scoped to the note detail screen, same lifetime as AttachmentViewModel there. Backs
 * NotebookRichEditor's HTML persistence -- separate from TextEditorViewModel/EditorPresentationState
 * on purpose, since that view model's update path is shared with voice notes and must not change.
 *
 * HTML edits are debounced (like other autosave paths in this project) so a fast typist doesn't
 * turn every keystroke into a DB write.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class RichContentViewModel(private val repository: RichContentRepository) : ViewModel() {

    private var noteId: Long? = null

    private val _html = MutableStateFlow<String?>(null)
    val html: StateFlow<String?> = _html.asStateFlow()

    private val pendingSaves = MutableSharedFlow<String>(extraBufferCapacity = 1)

    init {
        pendingSaves
            .debounce(500)
            .onEach { html -> persist(html) }
            .launchIn(viewModelScope)
    }

    /** Loads the saved HTML (if any) for this note. Call once when the note id becomes known. */
    fun load(id: Long) {
        noteId = id
        viewModelScope.launch {
            _html.value = withContext(Dispatchers.Default) { repository.get(id) }
        }
    }

    /** Call on every editor change; persistence itself is debounced. */
    fun onHtmlChanged(html: String) {
        pendingSaves.tryEmit(html)
    }

    private fun persist(html: String) {
        val id = noteId ?: return
        viewModelScope.launch {
            withContext(Dispatchers.Default) { repository.save(id, html) }
        }
    }
}
