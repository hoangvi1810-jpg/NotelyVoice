package com.module.notelycompose.notes.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.vectors.IcDetailAlignCenter
import com.module.notelycompose.resources.vectors.IcDetailAlignLeft
import com.module.notelycompose.resources.vectors.IcDetailAlignRight
import com.module.notelycompose.resources.vectors.IcDetailBold
import com.module.notelycompose.resources.vectors.IcDetailItalic
import com.module.notelycompose.resources.vectors.IcDetailUnderline
import com.module.notelycompose.resources.vectors.Images
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.editing_bold
import com.module.notelycompose.resources.editing_italic
import com.module.notelycompose.resources.editing_underline
import com.module.notelycompose.resources.editing_align_left
import com.module.notelycompose.resources.editing_align_center
import com.module.notelycompose.resources.editing_align_right
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditingToolbar(
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onSetAlignment: (alignment: TextAlign) -> Unit
) {
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 8.dp)
            // Was raw Color.Blue background with Color.LightGray icons — an unthemed leftover
            // placeholder, not an intentional design choice.
            .background(
                color = colors.surfaceSunken,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onToggleBold() }) {
            Icon(
                imageVector = Images.Icons.IcDetailBold,
                contentDescription = stringResource(Res.string.editing_bold),
                tint = colors.onSurfaceVariant
            )
        }
        IconButton(onClick = { onToggleItalic() }) {
            Icon(
                imageVector = Images.Icons.IcDetailItalic,
                contentDescription = stringResource(Res.string.editing_italic),
                tint = colors.onSurfaceVariant
            )
        }
        IconButton(onClick = { onToggleUnderline() }) {
            Icon(
                imageVector = Images.Icons.IcDetailUnderline,
                contentDescription = stringResource(Res.string.editing_underline),
                tint = colors.onSurfaceVariant
            )
        }
        IconButton(onClick = { onSetAlignment(TextAlign.Left) }) {
            Icon(
                imageVector = Images.Icons.IcDetailAlignLeft,
                contentDescription = stringResource(Res.string.editing_align_left),
                tint = colors.onSurfaceVariant
            )
        }
        IconButton(onClick = { onSetAlignment(TextAlign.Center) }) {
            Icon(
                imageVector = Images.Icons.IcDetailAlignCenter,
                contentDescription = stringResource(Res.string.editing_align_center),
                tint = colors.onSurfaceVariant
            )
        }
        IconButton(onClick = { onSetAlignment(TextAlign.Right) }) {
            Icon(
                imageVector = Images.Icons.IcDetailAlignRight,
                contentDescription = stringResource(Res.string.editing_align_right),
                tint = colors.onSurfaceVariant
            )
        }
    }
}
