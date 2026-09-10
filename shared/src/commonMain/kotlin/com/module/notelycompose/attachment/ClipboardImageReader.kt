package com.module.notelycompose.attachment

/**
 * Reads an image straight off the system clipboard and copies it into app storage, mirroring
 * [AttachmentPicker]'s copy-into-storage step. Backs the auto-paste behavior: copy a photo
 * elsewhere, tap into the note's text, it shows up immediately -- the way pasting into Word does,
 * no separate button and no file-picker menu. Returns null if the clipboard holds no image right
 * now.
 */
expect class ClipboardImageReader {
    fun read(): PickedFile?

    /** Cheap marker of "what's on the clipboard right now" -- lets callers dedupe repeated
     * auto-paste checks (e.g. every time the note's text field regains focus) without re-reading
     * and re-copying the same image on every focus event. Null means no image is on the clipboard. */
    fun fingerprint(): String?
}
