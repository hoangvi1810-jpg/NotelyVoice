package com.module.notelycompose.attachment

import audio.utils.deleteFile
import com.module.notelycompose.core.DateTimeUtil
import com.module.notelycompose.database.NoteDatabase
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Attachment(
    val id: Long,
    val noteId: Long,
    val path: String,
    val kind: AttachmentKind,
    val displayName: String
)

/**
 * SQLDelight-backed store for note attachments — see `sqldelight/database/attachment.sq`. Kept as
 * a plain class rather than a data-source interface plus use cases, matching AiContentDataSource
 * and NotebookRepository: this is a self-contained table with one backing store.
 */
class AttachmentRepository(private val database: NoteDatabase) {

    private val queries = database.attachmentQueries

    fun forNote(noteId: Long): Flow<List<Attachment>> =
        queries.getAttachmentsForNote(noteId)
            .asFlow()
            .mapToList()
            .map { rows ->
                rows.map {
                    Attachment(
                        id = it.id,
                        noteId = it.note_id,
                        path = it.path,
                        kind = AttachmentKind.fromId(it.kind),
                        displayName = it.display_name
                    )
                }
            }

    fun add(noteId: Long, file: PickedFile) {
        queries.insertAttachment(
            note_id = noteId,
            path = file.path,
            kind = file.kind.name,
            display_name = file.displayName,
            created_at = DateTimeUtil.toEpochMilli(DateTimeUtil.now())
        )
    }

    /** Deletes the DB row and the file on disk. */
    fun remove(attachmentId: Long) {
        val row = queries.getAttachmentById(attachmentId).executeAsOneOrNull() ?: return
        deleteFile(row.path)
        queries.deleteAttachment(attachmentId)
    }

    /** Called when a note is deleted — removes every attachment file and row for it. */
    fun removeAllForNote(noteId: Long) {
        queries.getAttachmentPathsForNote(noteId).executeAsList().forEach { deleteFile(it) }
        queries.deleteAttachmentsForNote(noteId)
    }
}
