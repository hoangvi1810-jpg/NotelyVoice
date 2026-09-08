package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.note_item_starred
import com.module.notelycompose.resources.note_item_voice
import com.module.notelycompose.resources.note_item_note
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteType(
    isStarred: Boolean,
    isVoice: Boolean
) {
    when {
        isStarred && isVoice -> {
            Row {
                NoteTypeCard(stringResource(Res.string.note_item_starred))
                Spacer(modifier = Modifier.width(8.dp))
                NoteTypeCard(stringResource(Res.string.note_item_voice))
            }
        }
        isStarred -> {
            NoteTypeCard(stringResource(Res.string.note_item_starred))
        }
        isVoice -> {
            NoteTypeCard(stringResource(Res.string.note_item_voice))
        }
        else -> {
            NoteTypeCard(stringResource(Res.string.note_item_note))
        }
    }
}

@Composable
private fun NoteTypeCard(text: String) {
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(CircleShape)
            .background(colors.accentSoft)
    ) {
        Text(
            text = text,
            color = colors.accent,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
