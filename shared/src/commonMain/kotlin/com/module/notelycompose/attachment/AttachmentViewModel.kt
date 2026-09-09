package com.module.notelycompose.attachment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Scoped to the note detail screen, same lifetime as TextEditorViewModel/NoteAiViewModel there. */
class AttachmentViewModel(
    private val repository: AttachmentRepository,
    private val picker: AttachmentPicker,
    private val opener: AttachmentOpener
) : ViewModel() {

    private val noteId = MutableStateFlow<Long?>(null)

    val attachments: StateFlow<List<Attachment>> = noteId
        .flatMapLatest { id -> id?.let { repository.forNote(it) } ?: emptyFlow() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setNoteId(id: Long) {
        noteId.value = id
    }

    fun pickImage() = pick(picker::pickImage)
    fun pickVideo() = pick(picker::pickVideo)
    fun pickDocument() = pick(picker::pickDocument)

    private fun pick(launch: ((PickedFile?) -> Unit) -> Unit) {
        val id = noteId.value ?: return
        launch { file ->
            if (file != null) {
                viewModelScope.launch {
                    withContext(Dispatchers.Default) { repository.add(id, file) }
                }
            }
        }
    }

    fun remove(attachmentId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) { repository.remove(attachmentId) }
        }
    }

    fun open(attachment: Attachment) = opener.open(attachment.path, attachment.kind)
}
