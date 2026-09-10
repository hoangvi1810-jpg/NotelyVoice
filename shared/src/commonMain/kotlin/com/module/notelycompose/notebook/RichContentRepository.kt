package com.module.notelycompose.notebook

import com.module.notelycompose.database.NoteDatabase

/**
 * SQLDelight-backed store for a notebook note's rich (HTML) body -- see
 * `sqldelight/database/richContent.sq`. Kept as a plain class matching AttachmentRepository: a
 * self-contained table with one backing store, no separate use-case layer.
 */
class RichContentRepository(private val database: NoteDatabase) {

    private val queries = database.richContentQueries

    fun get(noteId: Long): String? =
        queries.getRichContent(noteId).executeAsOneOrNull()

    fun save(noteId: Long, html: String) {
        queries.upsertRichContent(note_id = noteId, html = html)
    }

    /** Called when a note is deleted. */
    fun removeForNote(noteId: Long) {
        queries.deleteRichContentForNote(noteId)
    }
}
