package com.module.notelycompose.notes.presentation.detail

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.utils.deleteFile
import com.module.notelycompose.notes.domain.DeleteNoteById
import com.module.notelycompose.notes.domain.GetLastNote
import com.module.notelycompose.notes.domain.GetNoteById
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.UpdateNoteUseCase
import com.module.notelycompose.notes.domain.model.NoteDomainModel
import com.module.notelycompose.notes.presentation.detail.model.EditorPresentationState
import com.module.notelycompose.notes.presentation.detail.model.RecordingPathPresentationModel
import com.module.notelycompose.notes.presentation.detail.model.TextPresentationFormat
import com.module.notelycompose.notes.presentation.helpers.TextEditorHelper
import com.module.notelycompose.notes.presentation.helpers.formattedDate
import com.module.notelycompose.notes.presentation.mapper.EditorPresentationToUiStateMapper
import com.module.notelycompose.notes.presentation.mapper.TextAlignPresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextFormatPresentationMapper
import com.module.notelycompose.notes.ui.detail.EditorUiState
import com.module.notelycompose.onboarding.data.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val ID_NOT_SET = 0L

class TextEditorViewModel(
    private val getNoteByIdUseCase: GetNoteById,
    private val insertNoteUseCase: InsertNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteById,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val getLastNoteUseCase: GetLastNote,
    private val editorPresentationToUiStateMapper: EditorPresentationToUiStateMapper,
    private val textFormatPresentationMapper: TextFormatPresentationMapper,
    private val textAlignPresentationMapper: TextAlignPresentationMapper,
    private val textEditorHelper: TextEditorHelper,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _editorPresentationState = MutableStateFlow(EditorPresentationState())
    val editorPresentationState: StateFlow<EditorPresentationState> = _editorPresentationState
    private var _currentNoteId = MutableStateFlow<Long?>(ID_NOT_SET)

    internal val currentNoteId: StateFlow<Long?> = _currentNoteId.asStateFlow()
    private val _noteIdTrigger = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            _noteIdTrigger
                .filterNotNull()
                .take(1)
                .collect { id ->
                    val note = getNoteByIdUseCase.execute(id)
                    note?.let { retrievedNote ->
                        processNote(retrievedNote)
                        _currentNoteId.value = id
                    }
                }
        }
    }

    private fun processNote(retrievedNote: NoteDomainModel) {
        viewModelScope.launch {
            loadNote(
                title = retrievedNote.title,
                content = retrievedNote.content,
                formats = retrievedNote.formatting.map {
                    textFormatPresentationMapper.mapToPresentationModel(it)
                },
                textAlign = textAlignPresentationMapper.mapToComposeTextAlign(
                    retrievedNote.textAlign
                ),
                recordingPath = retrievedNote.recordingPath,
                starred = retrievedNote.starred,
                createdAt = getFormattedDate(retrievedNote.createdAt),
                bodyTextSize = preferencesRepository.getBodyTextSize().first()
            )
        }
    }

    fun onGetNoteById(id: String) {
        _noteIdTrigger.value = id.toLong()
    }

    private fun getLastNote() = getLastNoteUseCase.execute()

    fun onUpdateContent(newContent: TextFieldValue) {
        updateContent(newContent)
        // The title is owned solely by the title field (and the AI auto-title). It used to mirror
        // content.text on every keystroke, which meant typing a body instantly wiped a title the
        // user had just typed.
        createOrUpdateEvent(
            title = _editorPresentationState.value.title,
            content = newContent.text,
            starred = _editorPresentationState.value.starred,
            formatting = _editorPresentationState.value.formats,
            textAlign = _editorPresentationState.value.textAlign,
            recordingPath = _editorPresentationState.value.recording.recordingPath,
        )
    }

    fun onUpdateRecordingPath(recordingPath: String) {
        _editorPresentationState.update {
            it.copy(
                recording = recordingPath(recordingPath)
            )
        }
        onUpdateContent(newContent = _editorPresentationState.value.content)
    }

    fun onDeleteRecord() {
        deleteFile(_editorPresentationState.value.recording.recordingPath)
        _editorPresentationState.update {
            it.copy(
                recording = recordingPath(/*reset record path */"")
            )
        }
        onUpdateContent(newContent = _editorPresentationState.value.content)
    }

    private fun recordingPath(recordingPath: String) = RecordingPathPresentationModel(
        recordingPath = recordingPath,
        isRecordingExist = recordingPath.isNotEmpty()
    )

    private fun loadNote(
        title: String,
        content: String,
        formats: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String,
        starred: Boolean,
        createdAt: String,
        bodyTextSize: Float
    ) {
        _editorPresentationState.update {
            it.copy(
                content = TextFieldValue(content),
                title = title,
                formats = formats,
                textAlign = textAlign,
                recording = recordingPath(recordingPath),
                starred = starred,
                createdAt = createdAt,
                bodyTextSize = bodyTextSize
            )
        }
    }

    fun onGetUiState(presentationState: EditorPresentationState): EditorUiState {
        return editorPresentationToUiStateMapper.mapToUiState(presentationState)
    }

    /**
     * Sets the note's title. Used by the title field at the top of the note detail screen and by
     * the AI auto-title feature (see NoteAiViewModel.generatedTitle). Goes through
     * [createOrUpdateEvent] rather than [updateNote] so that titling a brand-new note — one that
     * has no row yet because nothing has been typed into the body — creates it instead of being
     * silently dropped.
     */
    fun updateTitle(title: String) {
        val state = _editorPresentationState.value
        _editorPresentationState.update { it.copy(title = title) }
        createOrUpdateEvent(
            title = title,
            content = state.content.text,
            starred = state.starred,
            formatting = state.formats,
            textAlign = state.textAlign,
            recordingPath = state.recording.recordingPath
        )
    }

    private fun insertNote(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String
    ) {
        viewModelScope.launch { insertNoteNow(title, content, starred, formatting, textAlign, recordingPath) }
    }

    private suspend fun insertNoteNow(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String
    ): Long? {
        val id = insertNoteUseCase.execute(
            title = title,
            content = content,
            starred = starred,
            formatting = formatting.map { textFormatPresentationMapper.mapToDomainModel(it) },
            textAlign = textAlignPresentationMapper.mapToDomainModel(textAlign),
            recordingPath = recordingPath
        )
        _currentNoteId.value = id
        return id
    }

    /**
     * Attaching a file (or anything else keyed on a real note id) needs a saved row to exist —
     * a brand-new note stays at id 0 until the first keystroke inserts it. Tapping "Đính kèm"
     * before typing anything used to silently no-op: [AttachmentViewModel.pick] bails on a null
     * noteId with no error, so the picker never opened and nothing told the user why. Called
     * before opening the attachment menu so an empty note gets its row created on demand.
     */
    suspend fun ensureNoteSaved(): Long {
        val existing = _currentNoteId.value
        if (existing != null && existing != ID_NOT_SET) return existing
        val state = _editorPresentationState.value
        return insertNoteNow(
            title = state.title,
            content = state.content.text,
            starred = state.starred,
            formatting = state.formats,
            textAlign = state.textAlign,
            recordingPath = state.recording.recordingPath
        ) ?: ID_NOT_SET
    }

    private fun updateNote(
        noteId: Long,
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String
    ) {
        viewModelScope.launch {
            updateNoteUseCase.execute(
                id = noteId,
                title = title,
                content = content,
                starred = starred,
                formatting = formatting.map { textFormatPresentationMapper.mapToDomainModel(it) },
                textAlign = textAlignPresentationMapper.mapToDomainModel(textAlign),
                recordingPath = recordingPath
            )
        }
    }

    fun onDeleteNote() {
        _currentNoteId.value?.let { noteId ->
            val path = _editorPresentationState.value.recording.recordingPath
            deleteFile(filePath = path)
            deleteNote(id = noteId)
        }
    }

    private fun deleteNote(id: Long) {
        viewModelScope.launch {
            deleteNoteUseCase.execute(id)
        }
    }

    fun onToggleStar() {
        val starred = _editorPresentationState.value.starred
        _editorPresentationState.update {
            it.copy(
                starred = !starred
            )
        }
        onUpdateContent(newContent = _editorPresentationState.value.content)
    }

    private fun getFormattedDate(
        createdAt: LocalDateTime = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
    ): String {
        return createdAt.formattedDate()
    }

    private fun createOrUpdateEvent(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String
    ) {
        val currentNoteId = _currentNoteId.value
        when {
            currentNoteId != null && currentNoteId != ID_NOT_SET -> {
                updateNote(
                    noteId = currentNoteId,
                    title = title,
                    content = content,
                    starred = starred,
                    formatting = formatting,
                    textAlign = textAlign,
                    recordingPath = recordingPath
                )
            }

            else -> {
                insertNote(
                    title = title,
                    content = content,
                    starred = starred,
                    formatting = formatting,
                    textAlign = textAlign,
                    recordingPath = recordingPath
                )
            }
        }
    }

    private fun updateContent(newContent: TextFieldValue) {
        viewModelScope.launch(Dispatchers.Default) {
            textEditorHelper.updateContent(
                newContent = newContent,
                currentState = _editorPresentationState.value,
                getFormattedDate = { getFormattedDate() },
                updateState = { newState ->
                    _editorPresentationState.update { newState }
                },
                bodyTextSize = preferencesRepository.getBodyTextSize().first()
            )
        }
    }

    fun onToggleBold() {
        normaliseSelection()
        textEditorHelper.toggleFormat(
            currentState = _editorPresentationState.value,
            transform = { it.copy(isBold = !it.isBold) },
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
        refreshSelection()
    }

    fun onToggleItalic() {
        normaliseSelection()
        textEditorHelper.toggleFormat(
            currentState = _editorPresentationState.value,
            transform = { it.copy(isItalic = !it.isItalic) },
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
        refreshSelection()
    }

    fun setTextSize(size: Float) {
        normaliseSelection()
        textEditorHelper.toggleFormat(
            currentState = _editorPresentationState.value,
            transform = { it.copy(textSize = size) },
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
        refreshSelection()
    }

    fun onToggleUnderline() {
        normaliseSelection()
        textEditorHelper.toggleFormat(
            currentState = _editorPresentationState.value,
            transform = { it.copy(isUnderline = !it.isUnderline) },
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
        refreshSelection()
    }

    private fun refreshSelection() {
        textEditorHelper.refreshSelection(
            currentState = _editorPresentationState.value,
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
    }

    private fun normaliseSelection() {
        textEditorHelper.normaliseSelection(
            currentState = _editorPresentationState.value,
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
    }

    fun onSetAlignment(alignment: TextAlign) {
        _editorPresentationState.update { it.copy(textAlign = alignment) }
        val content = _editorPresentationState.value.content
        val formats = _editorPresentationState.value.formats
        val textAlign = _editorPresentationState.value.textAlign
        val starred = _editorPresentationState.value.starred
        val recordingPath = _editorPresentationState.value.recording.recordingPath
        if (content.text.isNotEmpty()) {
            createOrUpdateEvent(
                title = content.text,
                content = content.text,
                starred = starred,
                formatting = formats,
                textAlign = textAlign,
                recordingPath = recordingPath
            )
        }
    }

    fun onToggleBulletList() {
        textEditorHelper.toggleBulletList(
            currentState = _editorPresentationState.value,
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
    }
}
