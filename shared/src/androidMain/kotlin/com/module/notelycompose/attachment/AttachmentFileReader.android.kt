package com.module.notelycompose.attachment

import java.io.File

actual fun readAttachmentBytes(path: String): ByteArray? = try {
    File(path).takeIf { it.exists() }?.readBytes()
} catch (e: Exception) {
    null
}
