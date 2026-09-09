package com.module.notelycompose.platform.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.platform.Platform
import com.module.notelycompose.platform.PlatformUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class PlatformViewModel (
    private val platformInfo: Platform,
    private val platformUtils: PlatformUtils,
    private val preferencesRepository: PreferencesRepository
) :ViewModel(){
    private val _state = MutableStateFlow(PlatformUiState())
    val state: StateFlow<PlatformUiState> = _state

    init {
        loadAppInfo()
    }

    private fun loadAppInfo() {
        _state.value = _state.value.copy(
            appVersion = platformInfo.appVersion,
            platformName = platformInfo.name,
            isAndroid = platformInfo.isAndroid,
            isTablet = platformInfo.isTablet,
            isLandscape = platformInfo.isLandscape
        )
    }

    fun shareText(text: String) {
         if (text.isNotBlank()) {
             platformUtils.shareText(text)
         }
    }

    fun shareRecording(path: String) {
         if (path.isNotBlank()) {
             if(_state.value.isAndroid) {
                 platformUtils.shareRecording(path)
             } else {
                 onExportAudio(path)
             }
         }
    }

    fun onExportAudio(path: String) {
        if (path.isNotBlank()) {
            val defaultFileName = "recording_${Clock.System.now().toEpochMilliseconds()}.wav"
            _state.value = _state.value.copy(isExporting = true)

            platformUtils.exportRecordingWithFilePicker(
                sourcePath = path,
                fileName = defaultFileName
            ) { success, message ->
                _state.value = _state.value.copy(
                    isExporting = false,
                    exportSuccess = success,
                    exportMessage = message ?: if (success) "Audio exported successfully" else "Failed to export audio"
                )
            }
        }
    }

    fun onExportTextAsTxt(text: String) {
        if (text.isNotBlank()) {
            val defaultFileName = "text_${Clock.System.now().toEpochMilliseconds()}.txt"
            _state.value = _state.value.copy(isExporting = true)

            platformUtils.exportTextWithFilePicker(
                text = text,
                fileName = defaultFileName
            ) { success, message ->
                _state.value = _state.value.copy(
                    isExporting = false,
                    exportSuccess = success,
                    exportMessage = message ?: if (success) "Text exported successfully" else "Failed to export text"
                )
            }
        }
    }

    /**
     * Builds the full note document — AI Note / Highlights / Summary / Transcript sections, each
     * included only if non-blank, so exporting before ever generating AI content just yields the
     * transcript. [markdown] switches between Markdown heading syntax and plain headings, since
     * the PDF generators render plain text and would print the `#` characters literally.
     */
    private fun buildNoteDocument(
        title: String,
        transcript: String,
        aiNote: String,
        highlights: String,
        summary: String,
        markdown: Boolean
    ): String {
        fun heading(text: String) = if (markdown) "## $text" else text.uppercase()
        return buildString {
            appendLine(if (markdown) "# $title" else title)
            if (summary.isNotBlank()) {
                appendLine()
                appendLine(heading("Summary"))
                appendLine(summary)
            }
            if (highlights.isNotBlank()) {
                appendLine()
                appendLine(heading("Highlights"))
                highlights.lineSequence().filter { it.isNotBlank() }.forEach { line ->
                    appendLine("- ${line.removePrefix("-").removePrefix("•").trim()}")
                }
            }
            if (aiNote.isNotBlank()) {
                appendLine()
                appendLine(heading("AI Note"))
                appendLine(aiNote)
            }
            appendLine()
            appendLine(heading("Transcript"))
            appendLine(transcript)
        }
    }

    /**
     * Reuses the same generic text-file-picker plumbing as [onExportTextAsTxt]; Markdown is just
     * plain text with a `.md` extension, no new platform code needed.
     */
    fun onExportTextAsMarkdown(
        title: String,
        transcript: String,
        aiNote: String,
        highlights: String,
        summary: String
    ) {
        val markdown = buildNoteDocument(
            title = title,
            transcript = transcript,
            aiNote = aiNote,
            highlights = highlights,
            summary = summary,
            markdown = true
        )

        val defaultFileName = "note_${Clock.System.now().toEpochMilliseconds()}.md"
        _state.value = _state.value.copy(isExporting = true)

        platformUtils.exportTextWithFilePicker(
            text = markdown,
            fileName = defaultFileName
        ) { success, message ->
            _state.value = _state.value.copy(
                isExporting = false,
                exportSuccess = success,
                exportMessage = message ?: if (success) "Markdown exported successfully" else "Failed to export Markdown"
            )
        }
    }

    /** Same content as the Markdown export — previously this exported the raw transcript only. */
    fun onExportTextAsPDF(
        title: String,
        transcript: String,
        aiNote: String,
        highlights: String,
        summary: String
    ) {
        val text = buildNoteDocument(
            title = title,
            transcript = transcript,
            aiNote = aiNote,
            highlights = highlights,
            summary = summary,
            markdown = false
        )
        viewModelScope.launch {
            if (text.isNotBlank()) {
                val defaultFileName = "pdf_${Clock.System.now().toEpochMilliseconds()}.pdf"
                val textSize = preferencesRepository.getBodyTextSize().first()
                _state.value = _state.value.copy(isExporting = true)

                platformUtils.exportTextAsPDFWithFilePicker(
                    text = text,
                    fileName = defaultFileName,
                    textSize = textSize
                ) { success, message ->
                    _state.value = _state.value.copy(
                        isExporting = false,
                        exportSuccess = success,
                        exportMessage = message ?: if (success) "PDF exported successfully" else "Failed to export PDF"
                    )
                }
            }
        }
    }

    fun onCopy(text: String) {
        if (text.isNotBlank()) {
            onClearCopyState()
            platformUtils.copyTextToClipboard(text) { success, _ ->
                _state.value = _state.value.copy(
                    copySuccess = success
                )
            }
        }
    }

    fun onClearCopyState() {
        _state.value = _state.value.copy(copySuccess = null)
    }

    fun clearExportStatus() {
        _state.value = _state.value.copy(
            exportSuccess = null,
            exportMessage = null
        )
    }
}

data class PlatformUiState(
    val appVersion: String = "",
    val platformName: String = "",
    val isAndroid: Boolean = false,
    val isTablet: Boolean = false,
    val isLandscape: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean? = null,
    val exportMessage: String? = null,
    val copySuccess: Boolean? = null
)
