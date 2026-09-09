package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.detail.DeleteConfirmationDialog
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.ui.theme.PressDepth
import com.module.notelycompose.notes.ui.theme.pressScale
import com.module.notelycompose.resources.vectors.IcArrowUpRight
import com.module.notelycompose.resources.vectors.Images
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.note_item_delete
import com.module.notelycompose.resources.note_item_edit
import com.module.notelycompose.resources.words
import com.module.notelycompose.ui.components.NotelyCard
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val ZERO_WORDS = 0

@Composable
fun NoteItem(
    note: NoteUiModel,
    onNoteClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    isChecked: Boolean,
    onCheckedChange: (Long, Boolean) -> Unit,
    isSelectAllAction: Boolean,
    onNoteLongPress: () -> Unit
) {
    val colors = LocalCustomColors.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = { onDeleteClick(note.id) }
    )

    Row {

        if(isSelectAllAction) {
            Column {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { onCheckedChange(note.id, it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.selectAllCheckboxColor
                    )
                )
            }
        }

        Column {

            NotelyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                cornerRadius = 22.dp,
                onClick = { onNoteClick(note.id) },
                onLongClick = { onNoteLongPress() },
                pressDepth = PressDepth.Subtle
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note.createdAt,
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val deleteInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .pressScale(deleteInteraction, PressDepth.Deep)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = deleteInteraction,
                                    indication = null
                                ) { showDeleteDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                tint = colors.onSurfaceVariant,
                                contentDescription = stringResource(Res.string.note_item_delete),
                                modifier = Modifier.size(20.dp).alpha(0.6f)
                            )
                        }
                    }
                    Text(
                        text = note.title,
                        color = colors.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (note.content.isNotBlank()) {
                        Text(
                            text = note.content,
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FlowRow(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            NoteType(
                                isStarred = note.isStarred,
                                isVoice = note.isVoice
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (note.words > ZERO_WORDS) {
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .clip(CircleShape)
                                        .background(colors.accentSoft)
                                ) {
                                    Text(
                                        text = pluralStringResource(Res.plurals.words, note.words, note.words),
                                        color = colors.accent,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        val editInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .size(36.dp)
                                .pressScale(editInteraction, PressDepth.Deep)
                                .clip(CircleShape)
                                .background(colors.accent)
                                .clickable(
                                    interactionSource = editInteraction,
                                    indication = null
                                ) { onNoteClick(note.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Images.Icons.IcArrowUpRight,
                                tint = colors.onAccent,
                                contentDescription = stringResource(Res.string.note_item_edit),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // end
        }
    }


}
