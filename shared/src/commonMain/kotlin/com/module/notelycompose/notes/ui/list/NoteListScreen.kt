package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import com.module.notelycompose.ui.components.NotelyFab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.module.notelycompose.export.presentation.ExportSelectionViewModel
import com.module.notelycompose.export.ui.ExportSelectedItemConfirmationDialog
import com.module.notelycompose.export.ui.NoSelectionErrorDialog
import com.module.notelycompose.notebook.NotebookViewModel
import com.module.notelycompose.notebook.ui.NotebookDrawerContent
import com.module.notelycompose.notebook.ui.NotebookManagerSheet
import com.module.notelycompose.notes.presentation.list.NoteListIntent
import com.module.notelycompose.notes.presentation.list.NoteListViewModel
import com.module.notelycompose.notes.ui.share.ShareDialog
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.presentation.PlatformUiState
import com.module.notelycompose.Arguments
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.cancel
import com.module.notelycompose.resources.export
import com.module.notelycompose.resources.ic_cancel_all
import com.module.notelycompose.resources.note_list_add_note
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.module.notelycompose.resources.ic_export_selections
import org.jetbrains.compose.resources.painterResource

@Composable
fun NoteListScreen(
    navigateToSettings: () -> Unit,
    navigateToNoteDetails: (String) -> Unit,
    navigateToExportNotes: () -> Unit,
    navigateToNotebookNotes: (Long?) -> Unit,
    viewModel: NoteListViewModel = koinViewModel(),
    exportViewModel: ExportSelectionViewModel = koinViewModel(),
    notebookViewModel: NotebookViewModel = koinViewModel(),
    platformUiState: PlatformUiState
) {
    val notesListState by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    var isSelectAllAction by remember { mutableStateOf(false) }
    var showExportNotesConfirmDialog by remember { mutableStateOf(false) }
    var showNoSelectionDialog by remember { mutableStateOf(false) }
    var isEmptySelection by remember { mutableStateOf(false) }
    val notebookState by notebookViewModel.state.collectAsState()
    var showNotebookManager by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NotebookDrawerContent(
                notebooks = notebookState.notebooks,
                onSelectAll = { drawerScope.launch { drawerState.close() }; navigateToNotebookNotes(null) },
                onSelectNotebook = { id -> drawerScope.launch { drawerState.close() }; navigateToNotebookNotes(id) },
                onCreateNotebook = notebookViewModel::create,
                onManage = { showNotebookManager = true }
            )
        }
    ) {
    Scaffold(
            topBar = {
                TopBar(
                    onMenuClicked = {
                        drawerScope.launch { drawerState.open() }
                    },
                    onSettingsClicked = {
                      navigateToSettings()
                    }
                )
            },
            isFloatingActionButtonDocked = true,
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                NotelyFab(
                    onClick = {
                        if(isSelectAllAction) {
                            // call function depending what was chosen
                            if(isEmptySelection) {
                                showNoSelectionDialog = true
                            } else {
                                showExportNotesConfirmDialog = true
                            }
                        } else {
                            navigateToNoteDetails(Arguments.DEFAULT_NOTE_ID)
                        }

                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        if(isSelectAllAction) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_export_selections),
                                contentDescription = stringResource(Res.string.export),
                                tint = LocalCustomColors.current.floatActionButtonIconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(Res.string.note_list_add_note),
                                tint = LocalCustomColors.current.floatActionButtonIconColor
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(LocalCustomColors.current.bodyBackgroundColor)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }
            ) {
                SearchBar(
                    onSearchByKeyword = { keyword ->
                        viewModel.onProcessIntent(NoteListIntent.OnSearchNote(keyword))
                    }
                )
                // Home is voice-recording-only: typed notes live in their notebook screen instead
                // (NotebookNotesScreen), never mixed into this list -- the count shown here and
                // the empty-state check both need to agree with that, not with the view model's
                // shared (voice + typed) state, which NotebookNotesScreen also reads from.
                val voiceNotes = viewModel.onGetUiState(notesListState).filter { it.isVoice }
                FilterTabBar(
                    selectedTabIndex = notesListState.selectedTabIndex,
                    onFilterTabItemClicked = { titleIndex ->
                        viewModel.onProcessIntent(NoteListIntent.OnFilterNote(titleIndex))
                    },
                    allSizeStr = if (voiceNotes.isEmpty()) "" else "(${voiceNotes.size})"
                )
                NoteList(
                    noteList = voiceNotes,
                    onNoteClicked = { id ->
                        navigateToNoteDetails("$id")
                    },
                    onNoteDeleteClicked = {
                        viewModel.onProcessIntent(NoteListIntent.OnNoteDeleted(it))
                    },
                    isSelectAllAction = isSelectAllAction,
                    onCancelSelectionAction = {
                        isSelectAllAction = !isSelectAllAction
                    },
                    onUpdateSelection = { selectionIds ->
                        isEmptySelection = selectionIds.isEmpty()
                        exportViewModel.onUpdateNoteIds(selectionIds)
                    }
                )
                if (voiceNotes.isEmpty()) EmptyNoteUi(platformUiState.isTablet)
            }
        }
    }

    if (showNotebookManager) {
        NotebookManagerSheet(
            notebooks = notebookState.notebooks,
            onRename = notebookViewModel::rename,
            onDelete = notebookViewModel::delete,
            onDismiss = { showNotebookManager = false }
        )
    }

    ExportSelectedItemConfirmationDialog(
        showExportNotesConfirmDialog = showExportNotesConfirmDialog,
        onExport = { exportAudio, exportTxt ->

            exportViewModel.onUpdateExportOptions(
                exportAudio,
                exportTxt
            )
            navigateToExportNotes()
        },
        onDismiss = {
            showExportNotesConfirmDialog = false
        }
    )

    NoSelectionErrorDialog(
        showDialog = showNoSelectionDialog,
        onDismiss = { showNoSelectionDialog = false }
    )

}
