package com.module.notelycompose.attachment

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.module.notelycompose.core.DateTimeUtil
import java.io.File
import java.io.FileOutputStream

/**
 * Holds the launchers registered by MainActivity, following the same pattern as
 * FileSaverLauncherHolder — an ActivityResultLauncher can only be registered from the Activity,
 * but the picker itself is reached from Compose through Koin.
 */
class AttachmentLauncherHolder {
    var mediaPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    var documentPickerLauncher: ActivityResultLauncher<Array<String>>? = null
    var onPicked: ((Uri?) -> Unit)? = null
}

actual class AttachmentPicker(
    private val context: Context,
    private val launcherHolder: AttachmentLauncherHolder
) {

    actual fun pickImage(onPicked: (PickedFile?) -> Unit) =
        launchVisualMedia(ActivityResultContracts.PickVisualMedia.ImageOnly, onPicked)

    actual fun pickVideo(onPicked: (PickedFile?) -> Unit) =
        launchVisualMedia(ActivityResultContracts.PickVisualMedia.VideoOnly, onPicked)

    private fun launchVisualMedia(
        type: ActivityResultContracts.PickVisualMedia.VisualMediaType,
        onPicked: (PickedFile?) -> Unit
    ) {
        val launcher = launcherHolder.mediaPickerLauncher ?: return onPicked(null)
        launcherHolder.onPicked = { uri -> onPicked(uri?.let { copyIntoAppStorage(it) }) }
        launcher.launch(PickVisualMediaRequest(type))
    }

    actual fun pickDocument(onPicked: (PickedFile?) -> Unit) {
        val launcher = launcherHolder.documentPickerLauncher ?: return onPicked(null)
        launcherHolder.onPicked = { uri -> onPicked(uri?.let { copyIntoAppStorage(it) }) }
        launcher.launch(arrayOf("application/pdf"))
    }

    private fun copyIntoAppStorage(uri: Uri): PickedFile? {
        val displayName = queryDisplayName(uri) ?: "attachment"
        val mimeType = context.contentResolver.getType(uri).orEmpty()
        val kind = when {
            mimeType.startsWith("image/") -> AttachmentKind.IMAGE
            mimeType.startsWith("video/") -> AttachmentKind.VIDEO
            else -> AttachmentKind.PDF
        }
        val target = File(
            attachmentsDir(),
            "attachment_${DateTimeUtil.toEpochMilli(DateTimeUtil.now())}_$displayName"
        )
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(target).use { output -> input.copyTo(output) }
            } ?: return null
            PickedFile(path = target.absolutePath, displayName = displayName, kind = kind)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun attachmentsDir(): File =
        File(context.filesDir, "attachments").apply { if (!exists()) mkdirs() }

    private fun queryDisplayName(uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) return cursor.getString(index)
        }
        return null
    }
}
