package com.module.notelycompose.notebook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notebook.NotebookViewModel
import com.module.notelycompose.notes.presentation.list.NoteListIntent
import com.module.notelycompose.notes.presentation.list.NoteListViewModel
import com.module.notelycompose.notes.ui.list.EmptyNoteUi
import com.module.notelycompose.notes.ui.list.NoteList
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.ui.components.NotelyFab
import org.koin.compose.viewmodel.koinViewModel

/**
 * A single notebook's notes -- reached from the hamburger drawer, deliberately its own screen
 * rather than a filter on the main list. Shows typed notes only (never voice notes: `isVoice`
 * comes from `recordingPath` being non-empty, same signal NoteDetailScreen uses to hide the AI
 * tabs), so this stays the "just my typed notes" space the drawer promises regardless of what a
 * note happens to be assigned to. `notebookId == null` means every typed note, any notebook
 * ("Tất cả sổ") -- see Routes.NotebookNotes.
 */
@Composable
fun NotebookNotesScreen(
    notebookId: Long?,
    navigateBack: () -> Unit,
    navigateToNoteDetails: (noteId: String, notebookId: Long?) -> Unit,
    viewModel: NoteListViewModel = koinViewModel(),
    notebookViewModel: NotebookViewModel = koinViewModel()
) {
    val colors = LocalCustomColors.current
    val notesListState by viewModel.state.collectAsState()
    val notebookState by notebookViewModel.state.collectAsState()
    var isSelectAllAction by remember { mutableStateOf(false) }

    val notebookName = notebookId?.let { id -> notebookState.notebooks.firstOrNull { it.id == id }?.name }
    val title = notebookName ?: "Tất cả sổ"

    val notes = viewModel.onGetUiState(notesListState).filter { note ->
        !note.isVoice && (notebookId == null || notebookState.assignments[note.id] == notebookId)
    }

    Scaffold(
        containerColor = colors.bodyBackgroundColor,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bodyBackgroundColor)
                    .padding(top = 16.dp, bottom = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = navigateBack, modifier = Modifier.padding(start = 4.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = colors.onSurface
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            NotelyFab(onClick = { navigateToNoteDetails("0", notebookId) }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Note mới",
                    tint = colors.onAccent
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.bodyBackgroundColor)
        ) {
            NoteList(
                noteList = notes,
                onNoteClicked = { id -> navigateToNoteDetails("$id", null) },
                onNoteDeleteClicked = { viewModel.onProcessIntent(NoteListIntent.OnNoteDeleted(it)) },
                isSelectAllAction = isSelectAllAction,
                onCancelSelectionAction = { isSelectAllAction = !isSelectAllAction },
                onUpdateSelection = {}
            )
            if (notes.isEmpty()) EmptyNoteUi(isTablet = false)
        }
    }
}
