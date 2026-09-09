package com.module.notelycompose.attachment

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

actual class AttachmentOpener(private val context: Context) {
    actual fun open(path: String, kind: AttachmentKind) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(path)
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, kind.mimeType())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
