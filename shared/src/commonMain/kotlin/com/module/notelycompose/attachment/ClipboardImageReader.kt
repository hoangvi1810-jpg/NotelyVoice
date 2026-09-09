package com.module.notelycompose.attachment

/**
 * Reads an image straight off the system clipboard and copies it into app storage, mirroring
 * [AttachmentPicker]'s copy-into-storage step -- this is what backs the "paste image" action next
 * to the attachment button, for pasting a screenshot/photo the way Word does instead of going
 * through the file-picker menu. Returns null if the clipboard holds no image right now.
 */
expect class ClipboardImageReader {
    fun read(): PickedFile?
}
