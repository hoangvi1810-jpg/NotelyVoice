package com.module.notelycompose.attachment

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIPasteboard

@OptIn(ExperimentalForeignApi::class)
actual class ClipboardImageReader {

    actual fun read(): PickedFile? {
        val image = UIPasteboard.generalPasteboard.image ?: return null
        val data = UIImagePNGRepresentation(image) ?: return null

        val fileManager = NSFileManager.defaultManager
        val documentsDir = fileManager
            .URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
            .firstOrNull() as? NSURL ?: return null

        val stamp = NSDate().timeIntervalSince1970.toLong()
        val displayName = "pasted_$stamp.png"
        val target = documentsDir.URLByAppendingPathComponent("attachment_$displayName") ?: return null

        val written = data.writeToURL(target, true)
        val path = target.path
        return if (written && path != null) {
            PickedFile(path = path, displayName = displayName, kind = AttachmentKind.IMAGE)
        } else {
            null
        }
    }
}
