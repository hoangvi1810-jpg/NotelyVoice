package com.module.notelycompose.attachment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.module.notelycompose.notes.ui.theme.LocalCustomColors

/**
 * The three-way choice shown when "Đính kèm" is tapped. A Dialog rather than a DropdownMenu: the
 * button that opens this lives inside a private composable nested a few levels down from the
 * showAttachmentMenu state, so there is no single fixed anchor to position a DropdownMenu against.
 */
@Composable
fun AttachmentPickerMenu(
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    onPickVideo: () -> Unit,
    onPickDocument: () -> Unit
) {
    val colors = LocalCustomColors.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surface)
                .padding(20.dp)
        ) {
            Text(
                text = "Đính kèm",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            AttachmentOptionRow(
                icon = Icons.Default.Image,
                label = "Ảnh",
                onClick = { onDismiss(); onPickImage() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AttachmentOptionRow(
                icon = Icons.Default.PlayArrow,
                label = "Video",
                onClick = { onDismiss(); onPickVideo() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AttachmentOptionRow(
                icon = Icons.Default.Description,
                label = "Tệp PDF",
                onClick = { onDismiss(); onPickDocument() }
            )
        }
    }
}

@Composable
private fun AttachmentOptionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceSunken)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.accent)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
    }
}
