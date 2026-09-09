package com.module.notelycompose.core

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes {
    @Serializable
    data object Home : Routes

    @Serializable
    data object List : Routes

    @Serializable
    data object DetailsGraph : Routes

    // notebookId is set only when opening/creating a note from a notebook screen, so a
    // brand-new note gets assigned to that notebook once it's actually saved (see
    // NoteDetailScreen's pendingNotebookId handling) -- keeps the audio-note FAB flow (which
    // never sets this) from touching notebooks at all.
    @Serializable
    data class Details(val noteId: String?, val notebookId: Long? = null) : Routes

    @Serializable
    data class Recorder(val noteId: String?) : Routes

    /** A single notebook's typed notes, reached from the hamburger drawer. null = every typed
     *  note regardless of notebook ("Tất cả sổ"), never voice notes -- see NotebookNotesScreen. */
    @Serializable
    data class NotebookNotes(val notebookId: Long?) : Routes

    @Serializable
    data object Web : Routes

    @Serializable
    data object Transcription : Routes

    @Serializable
    data object Share : Routes

    @Serializable
    data object Settings : Routes

    @Serializable
    data object Language : Routes

    @Serializable
    data object Menu : Routes

    @Serializable
    data object Downloader : Routes

    @Serializable
    data object SettingsText : Routes

    @Serializable
    data object NoteSettingsText : Routes

    @Serializable
    data object ExportBatchNotes : Routes

    @Serializable
    data object LanguageModelSelection : Routes

    @Serializable
    data object LanguageModelExplanation : Routes
}