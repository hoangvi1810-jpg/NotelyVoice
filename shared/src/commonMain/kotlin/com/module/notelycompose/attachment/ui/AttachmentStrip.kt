package com.module.notelycompose.attachment.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.module.notelycompose.attachment.Attachment
import com.module.notelycompose.attachment.AttachmentKind
import com.module.notelycompose.attachment.readAttachmentBytes
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Pasted images shown large and stacked vertically under a note's text -- not a picker/add-button
 * row (removed: images now arrive purely via clipboard auto-paste-on-focus, see
 * AttachmentViewModel.autoPasteFromClipboardIfNew, so there is nothing left to "add" here). Tap
 * opens the image; long-press reveals a remove control.
 */
@Composable
fun AttachmentStrip(
    attachments: List<Attachment>,
    onRemove: (Long) -> Unit,
    onOpen: (Attachment) -> Unit
) {
    val images = attachments.filter { it.kind == AttachmentKind.IMAGE }
    if (images.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        images.forEach { attachment ->
            PastedImage(
                attachment = attachment,
                onOpen = { onOpen(attachment) },
                onRemove = { onRemove(attachment.id) }
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PastedImage(
    attachment: Attachment,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    val colors = LocalCustomColors.current
    var confirmingRemove by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val thumbnail: ImageBitmap? = remember(attachment.path) {
        try {
            readAttachmentBytes(attachment.path)?.decodeToImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    if (thumbnail == null) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceSunken)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { if (confirmingRemove) confirmingRemove = false else onOpen() },
                onLongClick = { confirmingRemove = true }
            )
    ) {
        Image(
            bitmap = thumbnail,
            contentDescription = attachment.displayName,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )

        if (confirmingRemove) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.danger.copy(alpha = 0.85f))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xoá ảnh",
                    tint = colors.onDanger
                )
            }
        }
    }
}
