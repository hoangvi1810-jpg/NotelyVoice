package com.module.notelycompose.attachment

import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

/**
 * Same UIActivityViewController pattern as PlatformTheme.ios.kt's shareRecording — the share sheet
 * offers "Open in Photos/Files/..." for the attachment's file URL, since a bare NSURL.openURL does
 * not work for local file paths. Not compile-verified here (no Mac/Xcode) like every iOS file in
 * this project; the CI build is the first real check.
 */
actual class AttachmentOpener {
    actual fun open(path: String, kind: AttachmentKind) {
        val fileUrl = NSURL.fileURLWithPath(path)
        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            activityViewController,
            animated = true,
            completion = null
        )
    }
}
