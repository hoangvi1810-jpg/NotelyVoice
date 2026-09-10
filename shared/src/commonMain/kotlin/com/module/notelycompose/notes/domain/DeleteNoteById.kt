package com.module.notelycompose.notes.domain

import com.module.notelycompose.ai.AiContentDataSource
import com.module.notelycompose.attachment.AttachmentRepository
import com.module.notelycompose.notebook.NotebookRepository
import com.module.notelycompose.notebook.RichContentRepository

/**
 * The single choke point for deleting a note — both the list screen and the note detail screen go
 * through here — so the rows keyed by note id in the other tables are cleaned up here too. Neither
 * table uses a FOREIGN KEY, because SQLite has `foreign_keys` enforcement off by default in this
 * project's drivers, so nothing cascades on its own: the AI cache row used to be left behind on
 * every delete, and a stale notebook assignment would inflate that notebook's note count.
 */
class DeleteNoteById(
    private val noteDataSource: NoteDataSource,
    private val aiContentDataSource: AiContentDataSource,
    private val notebookRepository: NotebookRepository,
    private val attachmentRepository: AttachmentRepository,
    private val richContentRepository: RichContentRepository
) {
    suspend fun execute(id: Long) {
        aiContentDataSource.deleteByNoteId(id)
        notebookRepository.removeNote(id)
        attachmentRepository.removeAllForNote(id)
        richContentRepository.removeForNote(id)
        return noteDataSource.deleteNoteById(id)
    }
}
