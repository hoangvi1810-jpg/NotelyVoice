package com.module.notelycompose.attachment

fun AttachmentKind.mimeType(): String = when (this) {
    AttachmentKind.IMAGE -> "image/*"
    AttachmentKind.VIDEO -> "video/*"
    AttachmentKind.PDF -> "application/pdf"
}

/**
 * Opens an attachment in whatever the OS considers the right viewer for it. Android hands the file
 * straight to `ACTION_VIEW`; iOS reuses the `UIActivityViewController` pattern already proven in
 * this codebase for [com.module.notelycompose.platform.PlatformUtils.shareRecording] — a bare
 * `openURL` does not work for local file paths the way it does for URL schemes.
 */
expect class AttachmentOpener {
    fun open(path: String, kind: AttachmentKind)
}
