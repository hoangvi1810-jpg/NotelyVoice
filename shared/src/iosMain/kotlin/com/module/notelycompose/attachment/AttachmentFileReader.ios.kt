package com.module.notelycompose.attachment

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual fun readAttachmentBytes(path: String): ByteArray? {
    val url = NSURL.fileURLWithPath(path)
    val data = NSData.dataWithContentsOfURL(url) ?: return null
    val length = data.length.toInt()
    if (length == 0) return ByteArray(0)
    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), data.bytes, data.length)
    }
    return bytes
}
