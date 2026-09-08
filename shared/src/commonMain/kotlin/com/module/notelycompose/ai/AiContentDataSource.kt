package com.module.notelycompose.ai

import com.module.notelycompose.database.NoteDatabase

/**
 * Thin SQLDelight-backed cache for AI-generated content, keyed by note id. Kept as a plain class
 * (no domain/data interface split) since this is a self-contained cache with a single backing
 * store — see `sqldelight/database/AiContent.sq` for the schema.
 */
class AiContentDataSource(private val database: NoteDatabase) {

    private val queries = database.aiContentQueries

    fun getByNoteId(noteId: Long): NoteAiContent? {
        return queries.getAiContentByNoteId(noteId).executeAsOneOrNull()?.let { row ->
            NoteAiContent(
                noteId = row.note_id,
                template = AiTemplate.fromId(row.template),
                aiNote = row.ai_note,
                highlights = row.highlights,
                summary = row.summary,
                tags = row.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                generatedAt = row.generated_at
            )
        }
    }

    fun upsert(content: NoteAiContent) {
        queries.upsertAiContent(
            note_id = content.noteId,
            template = content.template.id,
            ai_note = content.aiNote,
            highlights = content.highlights,
            summary = content.summary,
            tags = content.tags.joinToString(","),
            generated_at = content.generatedAt
        )
    }

    fun deleteByNoteId(noteId: Long) {
        queries.deleteAiContentByNoteId(noteId)
    }
}
