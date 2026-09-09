package com.module.notelycompose.attachment

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.darwin.NSObject

/**
 * Mirrors the picker approach already proven in `core:audio` on this project's Kotlin/Native
 * toolchain: PHPicker for photo-library items (as IOSVideoPickerLauncher does) and
 * UIDocumentPickerViewController for files (as IOSAudioPickerLauncher does). Picked items are
 * copied into the app's Documents directory, so the note keeps working if the original is removed.
 *
 * NOTE: like every iOS file in this project, this could not be compile-verified here (no
 * Mac/Xcode) — the CI build is the first real check.
 */
@OptIn(ExperimentalForeignApi::class)
actual class AttachmentPicker {

    private var callback: ((PickedFile?) -> Unit)? = null
    private var pendingKind: AttachmentKind = AttachmentKind.IMAGE

    private val photoDelegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
        override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
            picker.dismissViewControllerAnimated(true, null)

            val result = didFinishPicking.filterIsInstance<PHPickerResult>().firstOrNull()
            if (result == null) {
                finish(null)
                return
            }

            val typeIdentifier = when (pendingKind) {
                AttachmentKind.VIDEO -> UTTypeMovie.identifier
                else -> UTTypeImage.identifier
            }
            val provider = result.itemProvider
            if (!provider.hasItemConformingToTypeIdentifier(typeIdentifier)) {
                finish(null)
                return
            }

            provider.loadFileRepresentationForTypeIdentifier(typeIdentifier) { url, _ ->
                finish(url?.let { copyIntoAppStorage(it, pendingKind) })
            }
        }
    }

    private val documentDelegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(
            controller: UIDocumentPickerViewController,
            didPickDocumentsAtURLs: List<*>
        ) {
            controller.dismissViewControllerAnimated(true, null)
            val url = didPickDocumentsAtURLs.filterIsInstance<NSURL>().firstOrNull()
            // Document picker URLs are security-scoped: reading one without this call fails on a
            // real device (works by accident in some simulator runs), per the identical pattern
            // already proven in IOSAudioPickerLauncher.
            if (url != null && url.startAccessingSecurityScopedResource()) {
                try {
                    finish(copyIntoAppStorage(url, AttachmentKind.PDF))
                } finally {
                    url.stopAccessingSecurityScopedResource()
                }
            } else {
                finish(null)
            }
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            controller.dismissViewControllerAnimated(true, null)
            finish(null)
        }
    }

    actual fun pickImage(onPicked: (PickedFile?) -> Unit) =
        presentPhotoPicker(AttachmentKind.IMAGE, PHPickerFilter.imagesFilter(), onPicked)

    actual fun pickVideo(onPicked: (PickedFile?) -> Unit) =
        presentPhotoPicker(AttachmentKind.VIDEO, PHPickerFilter.videosFilter(), onPicked)

    actual fun pickDocument(onPicked: (PickedFile?) -> Unit) {
        callback = onPicked
        pendingKind = AttachmentKind.PDF

        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypePDF))
        picker.delegate = documentDelegate
        present(picker)
    }

    private fun presentPhotoPicker(
        kind: AttachmentKind,
        filter: PHPickerFilter,
        onPicked: (PickedFile?) -> Unit
    ) {
        callback = onPicked
        pendingKind = kind

        val config = PHPickerConfiguration(photoLibrary = PHPhotoLibrary.sharedPhotoLibrary())
        config.selectionLimit = 1
        config.filter = filter

        val picker = PHPickerViewController(configuration = config)
        picker.delegate = photoDelegate
        present(picker)
    }

    private fun present(controller: platform.UIKit.UIViewController) {
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            viewControllerToPresent = controller,
            animated = true,
            completion = null
        )
    }

    private fun finish(file: PickedFile?) {
        val pending = callback
        callback = null
        pending?.invoke(file)
    }

    private fun copyIntoAppStorage(source: NSURL, kind: AttachmentKind): PickedFile? {
        val fileManager = NSFileManager.defaultManager
        val documentsDir = fileManager
            .URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
            .firstOrNull() as? NSURL ?: return null

        val displayName = source.lastPathComponent ?: "attachment"
        val stamp = NSDate().timeIntervalSince1970.toLong()
        val target = documentsDir.URLByAppendingPathComponent("attachment_${stamp}_$displayName")
            ?: return null

        target.path?.let { if (fileManager.fileExistsAtPath(it)) fileManager.removeItemAtURL(target, null) }

        val copied = fileManager.copyItemAtURL(source, target, null)
        val path = target.path
        return if (copied && path != null) {
            PickedFile(path = path, displayName = displayName, kind = kind)
        } else {
            null
        }
    }
}
