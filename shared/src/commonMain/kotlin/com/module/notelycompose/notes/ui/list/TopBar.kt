package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.note_list_add_note
import com.module.notelycompose.resources.top_bar_notes
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopBar(
    title: String = stringResource(Res.string.top_bar_notes),
    isLeftIconVisible: Boolean = true,
    isRightIconVisible: Boolean = true,
    onCreateNoteClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val colors = LocalCustomColors.current
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(colors.bodyBackgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 16.dp)
                .background(colors.bodyBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (isLeftIconVisible) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.bodyContentColor)
                        .align(Alignment.CenterStart)
                        .clickable { onCreateNoteClicked() },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        tint = colors.bodyBackgroundColor,
                        modifier = Modifier.size(24.dp).align(Alignment.Center),
                        contentDescription = stringResource(Res.string.note_list_add_note)
                    )
                }
            }
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = title,
                color = colors.onSurface,
                style = MaterialTheme.typography.headlineMedium
            )

            if (isRightIconVisible) {
                IconButton(
                    onClick = onSettingsClicked,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        tint = colors.settingsIconColor,
                        modifier = Modifier.size(24.dp),
                        contentDescription = ""
                    )
                }
            }
        }
    }
}
