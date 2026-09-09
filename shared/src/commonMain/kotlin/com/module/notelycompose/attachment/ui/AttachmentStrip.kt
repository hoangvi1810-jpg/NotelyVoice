package com.module.notelycompose.attachment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.module.notelycompose.attachment.Attachment
import com.module.notelycompose.attachment.AttachmentKind
import com.module.notelycompose.notes.ui.theme.LocalCustomColors

/**
 * The row of attachment cards under a note's body, plus a trailing "+" card. Hidden entirely when
 * there are no attachments, so notes without any look exactly as they did before this feature
 * existed — use [AddAttachmentButton] for the always-visible entry point next to the toolbar.
 *
 * Cards show a kind icon and filename rather than a real thumbnail: this project has no image-
 * loading library yet (Coil, Kamel, ...), and adding one purely for this is more risk than it is
 * worth given the iOS side of this feature cannot be compiled locally — see AttachmentPicker.ios.kt.
 */
@Composable
fun AttachmentStrip(
    attachments: List<Attachment>,
    onAddClick: () -> Unit,
    onRemove: (Long) -> Unit,
    onOpen: (Attachment) -> Unit
) {
    if (attachments.isEmpty()) return
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(attachments, key = { it.id }) { attachment ->
            AttachmentCard(
                attachment = attachment,
                onOpen = { onOpen(attachment) },
                onRemove = { onRemove(attachment.id) }
            )
        }
        item {
            AddAttachmentCard(onClick = onAddClick)
        }
    }
}

/** The "+ Đính kèm" affordance next to the format toolbar, visible even with zero attachments. */
@Composable
fun AddAttachmentButton(onClick: () -> Unit) {
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.accentSoft)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Đính kèm",
            style = MaterialTheme.typography.labelLarge,
            color = colors.accent
        )
    }
}

@Composable
private fun AddAttachmentCard(onClick: () -> Unit) {
    val colors = LocalCustomColors.current
    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.accentSoft)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Thêm đính kèm",
            tint = colors.accent
        )
    }
}

@Composable
private fun AttachmentCard(
    attachment: Attachment,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    val colors = LocalCustomColors.current
    var confirmingRemove by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val icon = when (attachment.kind) {
        AttachmentKind.IMAGE -> Icons.Default.Image
        AttachmentKind.VIDEO -> Icons.Default.PlayArrow
        AttachmentKind.PDF -> Icons.Default.Description
    }

    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceSunken)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { if (confirmingRemove) confirmingRemove = false else onOpen() },
                onLongClick = { confirmingRemove = true }
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = attachment.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

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
                    contentDescription = "Xoá đính kèm",
                    tint = colors.onDanger
                )
            }
        }
    }
}
