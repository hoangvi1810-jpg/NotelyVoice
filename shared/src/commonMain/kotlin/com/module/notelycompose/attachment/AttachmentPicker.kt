package com.module.notelycompose.attachment

enum class AttachmentKind {
    IMAGE, VIDEO, PDF;

    companion object {
        fun fromId(id: String): AttachmentKind =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) } ?: PDF
    }
}

/** A file already copied into app storage and ready to be recorded in the database. */
data class PickedFile(
    val path: String,
    val displayName: String,
    val kind: AttachmentKind
)

/**
 * Picks a file and copies it into app storage, returning null if the user cancelled or the copy
 * failed. Deliberately separate from `core:audio`'s FileManager: that interface funnels everything
 * through a WAV conversion and its Android implementation asks for audio/video media permissions,
 * neither of which applies here. Both pickers used below are permissionless on modern OS versions
 * (Android's photo picker / Storage Access Framework, iOS's PHPicker / UIDocumentPicker).
 */
expect class AttachmentPicker {
    fun pickImage(onPicked: (PickedFile?) -> Unit)
    fun pickVideo(onPicked: (PickedFile?) -> Unit)
    fun pickDocument(onPicked: (PickedFile?) -> Unit)
}
