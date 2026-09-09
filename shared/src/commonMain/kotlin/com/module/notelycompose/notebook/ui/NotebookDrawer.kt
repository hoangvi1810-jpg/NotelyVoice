package com.module.notelycompose.notebook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
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
import com.module.notelycompose.notebook.Notebook
import com.module.notelycompose.notes.ui.theme.LocalCustomColors

/**
 * The app's left-side drawer: notebooks live here now instead of a horizontal chip row on the
 * note list, which was cramped once there were more than two or three of them. Selecting a
 * notebook (or "Tất cả sổ") navigates to [com.module.notelycompose.core.Routes.NotebookNotes] --
 * a screen scoped to typed notes only, kept deliberately separate from the voice/AI note list
 * this drawer opens from.
 */
@Composable
fun NotebookDrawerContent(
    notebooks: List<Notebook>,
    onSelectAll: () -> Unit,
    onSelectNotebook: (Long) -> Unit,
    onCreateNotebook: (String) -> Unit,
    onManage: () -> Unit
) {
    val colors = LocalCustomColors.current
    var newName by remember { mutableStateOf("") }

    ModalDrawerSheet(
        modifier = Modifier.widthIn(max = 320.dp),
        drawerContainerColor = colors.surface
    ) {
        Column(modifier = Modifier.fillMaxHeight().padding(vertical = 20.dp)) {
            Text(
                text = "Sổ tay",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                DrawerRow(
                    icon = Icons.Default.Menu,
                    label = "Tất cả sổ",
                    onClick = onSelectAll
                )
                Spacer(modifier = Modifier.height(4.dp))
                notebooks.forEach { notebook ->
                    DrawerRow(
                        icon = Icons.Default.Book,
                        label = "${notebook.name} (${notebook.noteCount})",
                        onClick = { onSelectNotebook(notebook.id) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
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
                    IconButton(
                        onClick = {
                            if (newName.isNotBlank()) {
                                onCreateNotebook(newName)
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

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onManage() }
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.width(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Quản lý sổ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.width(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface
        )
    }
}
