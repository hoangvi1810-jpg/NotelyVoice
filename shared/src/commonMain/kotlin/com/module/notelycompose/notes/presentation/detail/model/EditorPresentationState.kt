package com.module.notelycompose.notes.presentation.detail.model

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import com.module.notelycompose.notes.extension.TEXT_SIZE_BODY
import com.module.notelycompose.notes.presentation.helpers.formattedDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class EditorPresentationState(
    val content: TextFieldValue = TextFieldValue(""),
    val title: String = "",
    // True once the title has been set independently of the content (manual rename, or the
    // AI auto-title feature) -- while false, title mirrors content.text on every edit, same as
    // the app's original behavior for notes nobody has ever renamed.
    val isCustomTitle: Boolean = false,
    val formats: List<TextPresentationFormat> = emptyList(),
    val textAlign: TextAlign = TextAlign.Left,
    val selectionSize: TextFormatPresentationOption = TextPresentationFormats.NoSelection,
    val recording: RecordingPathPresentationModel = RecordingPathPresentationModel(),
    val starred: Boolean = false,
    val createdAt: String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).formattedDate(),
    val bodyTextSize: Float = TEXT_SIZE_BODY
)
