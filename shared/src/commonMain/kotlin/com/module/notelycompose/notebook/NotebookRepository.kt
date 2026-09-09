package com.module.notelycompose.notebook

import com.module.notelycompose.core.DateTimeUtil
import com.module.notelycompose.database.NoteDatabase
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** A user-created category. [noteCount] is computed by the query, not stored. */
data class Notebook(
    val id: Long,
    val name: String,
    val noteCount: Long
)

/**
 * Notebooks and the note -> notebook assignments. Kept as a plain class over SQLDelight (like
 * [com.module.notelycompose.ai.AiContentDataSource]) rather than a data-source interface plus use
 * cases per operation: the note -> notebook link is stored in its own table, so none of the note
 * models, mappers or use cases need to know about it — see `sqldelight/database/notebook.sq`.
 *
 * Reads are exposed as Flows rather than one-shot snapshots because the note list and the note
 * detail screen get separate ViewModel instances (Compose scopes them per nav entry): assigning a
 * notebook on the detail screen has to reach the list screen, and SQLDelight's query listeners do
 * that for free as long as both go through the one shared NoteDatabase.
 */
class NotebookRepository(private val database: NoteDatabase) {

    private val queries = database.notebookQueries

    fun notebooks(): Flow<List<Notebook>> =
        queries.getAllNotebooks()
            .asFlow()
            .mapToList()
            .map { rows ->
                rows.map { Notebook(id = it.id, name = it.name, noteCount = it.note_count) }
            }

    /** noteId -> notebookId for every assigned note, for filtering the list in one pass. */
    fun assignments(): Flow<Map<Long, Long>> =
        queries.getAllAssignments()
            .asFlow()
            .mapToList()
            .map { rows -> rows.associate { it.note_id to it.notebook_id } }

    fun create(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        queries.insertNotebook(
            name = trimmed,
            created_at = DateTimeUtil.toEpochMilli(DateTimeUtil.now())
        )
    }

    fun rename(id: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        queries.renameNotebook(name = trimmed, id = id)
    }

    /** Deleting a notebook keeps its notes; they just become uncategorised. */
    fun delete(id: Long) {
        queries.clearNotebookAssignments(id)
        queries.deleteNotebook(id)
    }

    fun assign(noteId: Long, notebookId: Long?) {
        if (notebookId == null) {
            queries.removeNoteFromNotebook(noteId)
        } else {
            queries.setNoteNotebook(note_id = noteId, notebook_id = notebookId)
        }
    }

    fun removeNote(noteId: Long) = queries.removeNoteFromNotebook(noteId)

    /**
     * Seeds a starter set the first time the app runs with notebooks enabled, so the feature
     * doesn't open on an empty screen with no hint of what it is for.
     */
    fun seedDefaultsIfEmpty(names: List<String>) {
        if (queries.countNotebooks().executeAsOne() > 0L) return
        names.forEach { create(it) }
    }
}
