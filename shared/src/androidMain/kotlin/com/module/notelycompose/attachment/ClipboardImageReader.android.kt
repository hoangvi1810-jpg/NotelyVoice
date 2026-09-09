package com.module.notelycompose.attachment

import android.content.ClipboardManager
import android.content.Context
import com.module.notelycompose.core.DateTimeUtil
import java.io.File
import java.io.FileOutputStream

actual class ClipboardImageReader(private val context: Context) {

    actual fun read(): PickedFile? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        val uri = clip.getItemAt(0).uri ?: return null
        val mimeType = context.contentResolver.getType(uri) ?: return null
        if (!mimeType.startsWith("image/")) return null

        val displayName = "pasted_${DateTimeUtil.toEpochMilli(DateTimeUtil.now())}.png"
        val target = File(attachmentsDir(), displayName)
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(target).use { output -> input.copyTo(output) }
            } ?: return null
            PickedFile(path = target.absolutePath, displayName = displayName, kind = AttachmentKind.IMAGE)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun attachmentsDir(): File =
        File(context.filesDir, "attachments").apply { if (!exists()) mkdirs() }
}
