package com.module.notelycompose.notebook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.module.notelycompose.notebook.Notebook
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.ui.components.NotelyChip

private const val ALL_NOTEBOOKS_LABEL = "Tất cả sổ"
private const val MANAGE_LABEL = "Quản lý"

/**
 * Second chip row on the note list: filters by notebook. Hidden entirely when no notebooks exist,
 * so the list screen is unchanged for anyone not using them.
 */
@Composable
fun NotebookFilterBar(
    notebooks: List<Notebook>,
    selectedNotebookId: Long?,
    onSelect: (Long?) -> Unit,
    onManage: () -> Unit
) {
    if (notebooks.isEmpty()) return
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            NotelyChip(
                text = ALL_NOTEBOOKS_LABEL,
                selected = selectedNotebookId == null,
                onClick = { onSelect(null) }
            )
        }
        items(notebooks) { notebook ->
            NotelyChip(
                text = "${notebook.name} (${notebook.noteCount})",
                selected = notebook.id == selectedNotebookId,
                onClick = { onSelect(notebook.id) }
            )
        }
        item {
            NotelyChip(
                text = MANAGE_LABEL,
                selected = false,
                onClick = onManage
            )
        }
    }
}

/** Rename or delete notebooks. Deleting keeps the notes; they just become uncategorised. */
@Composable
fun NotebookManagerSheet(
    notebooks: List<Notebook>,
    onRename: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalCustomColors.current
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surface)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quản lý sổ tay",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = colors.onSurface
                    )
                }
            }
            Text(
                text = "Xoá sổ không xoá note bên trong — chúng chỉ về mục chưa phân loại.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                notebooks.forEach { notebook ->
                    NotebookManagerRow(
                        notebook = notebook,
                        confirmingDelete = pendingDeleteId == notebook.id,
                        onRename = { onRename(notebook.id, it) },
                        onDeleteRequested = { pendingDeleteId = notebook.id },
                        onDeleteConfirmed = {
                            pendingDeleteId = null
                            onDelete(notebook.id)
                        },
                        onDeleteCancelled = { pendingDeleteId = null }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun NotebookManagerRow(
    notebook: Notebook,
    confirmingDelete: Boolean,
    onRename: (String) -> Unit,
    onDeleteRequested: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDeleteCancelled: () -> Unit
) {
    val colors = LocalCustomColors.current
    var name by remember(notebook.id, notebook.name) { mutableStateOf(notebook.name) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceSunken)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (confirmingDelete) {
            Text(
                text = "Xoá \"${notebook.name}\"?",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Huỷ",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant,
                modifier = Modifier
                    .clickable { onDeleteCancelled() }
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Xoá",
                style = MaterialTheme.typography.labelLarge,
                color = colors.danger,
                modifier = Modifier
                    .clickable { onDeleteConfirmed() }
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            )
        } else {
            BasicTextField(
                value = name,
                onValueChange = { name = it.replace("\n", "") },
                modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                singleLine = true,
                textStyle = TextStyle(color = colors.onSurface, fontSize = 15.sp),
                cursorBrush = SolidColor(colors.accent)
            )
            if (name.trim() != notebook.name && name.isNotBlank()) {
                IconButton(onClick = { onRename(name) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Lưu tên",
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            IconButton(onClick = onDeleteRequested) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xoá sổ",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
