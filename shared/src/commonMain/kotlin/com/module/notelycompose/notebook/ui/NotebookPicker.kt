package com.module.notelycompose.notebook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.module.notelycompose.notebook.Notebook
import com.module.notelycompose.notes.ui.theme.LocalCustomColors

const val UNCATEGORISED_LABEL = "Chưa phân loại"

/**
 * The "Sổ tay: X" row under the note title. Tapping it opens [NotebookPickerSheet].
 */
@Composable
fun NotebookRow(
    notebookName: String?,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
            .clip(CircleShape)
            .background(colors.accentSoft)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = notebookName ?: UNCATEGORISED_LABEL,
            style = MaterialTheme.typography.labelMedium,
            color = colors.accent
        )
    }
}

/**
 * Picks the (single) notebook a note belongs to, and can create one inline so you never have to
 * leave the note to file it somewhere new.
 */
@Composable
fun NotebookPickerSheet(
    notebooks: List<Notebook>,
    selectedNotebookId: Long?,
    onSelect: (Long?) -> Unit,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalCustomColors.current
    var newName by remember { mutableStateOf("") }

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
                    text = "Sổ tay",
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
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                NotebookOptionRow(
                    name = UNCATEGORISED_LABEL,
                    selected = selectedNotebookId == null,
                    onClick = {
                        onSelect(null)
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                notebooks.forEach { notebook ->
                    NotebookOptionRow(
                        name = notebook.name,
                        selected = notebook.id == selectedNotebookId,
                        onClick = {
                            onSelect(notebook.id)
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceSunken)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = newName,
                    onValueChange = { newName = it.replace("\n", "") },
                    modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                    singleLine = true,
                    textStyle = TextStyle(color = colors.onSurface, fontSize = 15.sp),
                    cursorBrush = SolidColor(colors.accent),
                    decorationBox = { inner ->
                        if (newName.isEmpty()) {
                            Text(
                                text = "Tạo sổ mới…",
                                color = colors.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        }
                        inner()
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onCreate(newName)
                            newName = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tạo sổ mới",
                        tint = if (newName.isNotBlank()) colors.accent else colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NotebookOptionRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.accentSoft else colors.surfaceSunken)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) colors.accent else colors.onSurface
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
