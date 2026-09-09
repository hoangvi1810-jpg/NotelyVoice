package com.module.notelycompose.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class NotebookUiState(
    val notebooks: List<Notebook> = emptyList(),
    /** noteId -> notebookId, for filtering the note list without touching the note queries. */
    val assignments: Map<Long, Long> = emptyMap()
)

/**
 * Shared by the note list (filtering by notebook), the note detail (which notebook a note is in)
 * and the notebook manager. Backed by SQLDelight query Flows, so a change made on one screen shows
 * up on the other even though Compose gives each screen its own ViewModel instance.
 */
class NotebookViewModel(
    private val repository: NotebookRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotebookUiState())
    val state: StateFlow<NotebookUiState> = _state

    init {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                repository.seedDefaultsIfEmpty(DEFAULT_NOTEBOOKS)
            }
        }
        combine(repository.notebooks(), repository.assignments()) { notebooks, assignments ->
            NotebookUiState(notebooks = notebooks, assignments = assignments)
        }.onEach { _state.value = it }.launchIn(viewModelScope)
    }

    fun create(name: String) = write { repository.create(name) }

    fun rename(id: Long, name: String) = write { repository.rename(id, name) }

    fun delete(id: Long) = write { repository.delete(id) }

    fun assign(noteId: Long, notebookId: Long?) = write { repository.assign(noteId, notebookId) }

    private fun write(block: () -> Unit) {
        viewModelScope.launch { withContext(Dispatchers.Default) { block() } }
    }

    companion object {
        val DEFAULT_NOTEBOOKS = listOf("Nhật ký", "Ý tưởng", "Công việc")
    }
}
