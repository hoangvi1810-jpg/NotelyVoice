package com.module.notelycompose.notes.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.getPlatform
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.cancel
import com.module.notelycompose.resources.copy
import com.module.notelycompose.resources.ic_cancel_all
import com.module.notelycompose.resources.ic_copy
import com.module.notelycompose.resources.top_bar_back
import com.module.notelycompose.resources.top_bar_export_audio_folder
import com.module.notelycompose.resources.top_bar_export_as_markdown
import com.module.notelycompose.resources.top_bar_import_audio
import com.module.notelycompose.resources.top_bar_my_note
import com.module.notelycompose.resources.top_bar_export_as_txt
import com.module.notelycompose.resources.top_bar_export_as_pdf
import com.module.notelycompose.resources.top_bar_import_video
import com.module.notelycompose.resources.vectors.IcChevronLeft
import com.module.notelycompose.resources.vectors.Images
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DetailNoteTopBar(
    title: String = stringResource(Res.string.top_bar_my_note),
    onNavigateBack: () -> Unit,
    onShare: () -> Unit = {},
    onCopy: () -> Unit = {},
    onExportAudio: () -> Unit,
    onImportClick: () -> Unit = {},
    onImportVideoClick: () -> Unit = {},
    onExportTextAsTxt: () -> Unit,
    onExportTextAsPDF: () -> Unit,
    onExportTextAsMarkdown: () -> Unit = {},
    isRecordingExist: Boolean
) {
    var showExistingRecordConfirmDialog by remember { mutableStateOf(false) }
    var showExistingVideoRecordConfirmDialog by remember { mutableStateOf(false) }
    if (getPlatform().isAndroid) {
        DetailAndroidNoteTopBar(
            title = title,
            onNavigateBack = onNavigateBack,
            onShare = onShare,
            onCopy = onCopy,
            onExportAudio = onExportAudio,
            onImportClick = {
                if (!isRecordingExist) {
                    onImportClick()
                } else {
                    showExistingRecordConfirmDialog = true
                }
            },
            onImportVideoClick = {
                if (!isRecordingExist) {
                    onImportVideoClick()
                } else {
                    showExistingVideoRecordConfirmDialog = true
                }
            },
            onExportTextAsTxt = onExportTextAsTxt,
            onExportTextAsPDF = onExportTextAsPDF,
            onExportTextAsMarkdown = onExportTextAsMarkdown
        )
    } else {
        DetailIOSNoteTopBar(
            onNavigateBack = onNavigateBack,
            onShare = onShare,
            onCopy = onCopy,
            onExportAudio = onExportAudio,
            onImportClick = {
                if (!isRecordingExist) {
                    onImportClick()
                } else {
                    showExistingRecordConfirmDialog = true
                }
            },
            onImportVideoClick = {
                if (!isRecordingExist) {
                    onImportVideoClick()
                } else {
                    showExistingVideoRecordConfirmDialog = true
                }
            },
            onExportTextAsTxt = onExportTextAsTxt,
            onExportTextAsPDF = onExportTextAsPDF,
            onExportTextAsMarkdown = onExportTextAsMarkdown
        )
    }

    ReplaceRecordingConfirmationDialog(
        showDialog = showExistingRecordConfirmDialog,
        onDismiss = {
            showExistingRecordConfirmDialog = false
        },
        onConfirm = {
            onImportClick()
        },
        option = RecordingConfirmationUiModel.Import
    )

    ReplaceRecordingConfirmationDialog(
        showDialog = showExistingVideoRecordConfirmDialog,
        onDismiss = {
            showExistingVideoRecordConfirmDialog = false
        },
        onConfirm = {
            onImportVideoClick()
        },
        option = RecordingConfirmationUiModel.Import
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailAndroidNoteTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onExportAudio: () -> Unit,
    onImportClick: () -> Unit,
    onImportVideoClick: () -> Unit,
    onExportTextAsTxt: () -> Unit,
    onExportTextAsPDF: () -> Unit,
    onExportTextAsMarkdown: () -> Unit = {}
) {
    val colors = LocalCustomColors.current
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = colors.onSurface) },
        navigationIcon = {
            IconButton(onClick = { onNavigateBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.top_bar_back),
                    tint = colors.onSurface
                )
            }
        },
        actions = {
            IconButton(onClick = { onCopy() }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_copy),
                    contentDescription = stringResource(Res.string.copy),
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = { onShare() }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share note",
                    tint = colors.onSurfaceVariant
                )
            }
            // Hide dropdown menu
            DetailDropDownMenu(
                onExportAudio = onExportAudio,
                onImportClick = onImportClick,
                onImportVideoClick = onImportVideoClick,
                onExportTextAsTxt = onExportTextAsTxt,
                onExportTextAsPDF = onExportTextAsPDF,
                onExportTextAsMarkdown = onExportTextAsMarkdown
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.bodyBackgroundColor,
            titleContentColor = colors.onSurface,
            navigationIconContentColor = colors.onSurface,
            actionIconContentColor = colors.onSurfaceVariant
        ),
        windowInsets = WindowInsets(0)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailIOSNoteTopBar(
    onNavigateBack: () -> Unit,
    onCopy: () -> Unit,
    onExportAudio: () -> Unit,
    onImportClick: () -> Unit,
    onImportVideoClick: () -> Unit,
    onExportTextAsTxt: () -> Unit,
    onExportTextAsPDF: () -> Unit,
    onExportTextAsMarkdown: () -> Unit = {},
    onShare: () -> Unit
) {
    val colors = LocalCustomColors.current
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onNavigateBack()
                }
            ) {
                Icon(
                    imageVector = Images.Icons.IcChevronLeft,
                    contentDescription = stringResource(Res.string.top_bar_back),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.top_bar_back),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        actions = {
            IconButton(onClick = { onCopy() }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_copy),
                    contentDescription = stringResource(Res.string.copy),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = { onShare() }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share note",
                    modifier = Modifier.size(24.dp)
                )
            }
            DetailDropDownMenu(
                onExportAudio = onExportAudio,
                onImportClick = onImportClick,
                onImportVideoClick = onImportVideoClick,
                onExportTextAsTxt = onExportTextAsTxt,
                onExportTextAsPDF = onExportTextAsPDF,
                onExportTextAsMarkdown = onExportTextAsMarkdown
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.bodyBackgroundColor,
            titleContentColor = colors.iOSBackButtonColor,
            navigationIconContentColor = colors.iOSBackButtonColor,
            actionIconContentColor = colors.iOSBackButtonColor
        ),
        modifier = Modifier.padding(start = 0.dp),
        windowInsets = WindowInsets(0)
    )
}

@Composable
fun DetailDropDownMenu(
    onExportAudio: () -> Unit,
    onImportClick: () -> Unit = {},
    onImportVideoClick: () -> Unit = {},
    onExportTextAsTxt: () -> Unit,
    onExportTextAsPDF: () -> Unit,
    onExportTextAsMarkdown: () -> Unit = {}
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val colors = LocalCustomColors.current
    Box {
        IconButton(onClick = { dropdownExpanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options",
                tint = colors.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            modifier = Modifier.padding(vertical = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.top_bar_import_audio)) },
                onClick = {
                    dropdownExpanded = false
                    onImportClick()
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.top_bar_import_video)) },
                onClick = {
                    dropdownExpanded = false
                    onImportVideoClick()
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.top_bar_export_audio_folder)) },
                onClick = {
                    dropdownExpanded = false
                    onExportAudio()
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.top_bar_export_as_txt)) },
                onClick = {
                    dropdownExpanded = false
                    onExportTextAsTxt()
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.top_bar_export_as_pdf)) },
                onClick = {
                    dropdownExpanded = false
                    onExportTextAsPDF()
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.top_bar_export_as_markdown)) },
                onClick = {
                    dropdownExpanded = false
                    onExportTextAsMarkdown()
                }
            )
        }
    }
}
