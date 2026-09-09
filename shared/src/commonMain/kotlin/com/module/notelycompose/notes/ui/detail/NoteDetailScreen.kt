package com.module.notelycompose.notes.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.module.notelycompose.audio.presentation.AudioPlayerViewModel
import com.module.notelycompose.audio.ui.player.PlatformAudioPlayerUi
import com.module.notelycompose.audio.ui.player.model.AudioPlayerUiState
import com.module.notelycompose.modelDownloader.DownloaderDialog
import com.module.notelycompose.modelDownloader.DownloaderEffect
import com.module.notelycompose.modelDownloader.ModelDownloaderViewModel
import com.module.notelycompose.audio.presentation.AudioImportViewModel
import com.module.notelycompose.audio.ui.importing.ImportingAudioStateHost
import com.module.notelycompose.modelDownloader.ModelSelection
import com.module.notelycompose.attachment.AttachmentViewModel
import com.module.notelycompose.attachment.ui.AddAttachmentButton
import com.module.notelycompose.attachment.ui.PasteImageButton
import com.module.notelycompose.attachment.ui.AttachmentPickerMenu
import com.module.notelycompose.attachment.ui.AttachmentStrip
import com.module.notelycompose.notebook.NotebookViewModel
import com.module.notelycompose.notebook.ui.NotebookPickerSheet
import com.module.notelycompose.notebook.ui.NotebookRow
import com.module.notelycompose.notes.presentation.detail.NoteAiViewModel
import com.module.notelycompose.notes.presentation.detail.TextEditorViewModel
import com.module.notelycompose.notes.ui.share.ShareDialog
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.ui.components.NotelyFab
import com.module.notelycompose.platform.presentation.PlatformViewModel
import com.module.notelycompose.Arguments
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.confirmation_cancel
import com.module.notelycompose.resources.download_dialog_error
import com.module.notelycompose.resources.ic_transcription
import com.module.notelycompose.resources.note_detail_recorder
import com.module.notelycompose.resources.note_title_placeholder
import com.module.notelycompose.resources.top_bar_my_note
import com.module.notelycompose.resources.transcription_icon
import com.module.notelycompose.resources.vectors.IcRecorder
import com.module.notelycompose.resources.vectors.Images
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    navigateBack: () -> Unit,
    navigateToRecorder: (noteId: String) -> Unit,
    navigateToTranscription: () -> Unit,
    onNavigateToSettingsText: () -> Unit,
    // Set only when this screen was opened from a notebook's "+" -- see Routes.Details.
    pendingNotebookId: Long? = null,
    audioPlayerViewModel: AudioPlayerViewModel = koinViewModel(),
    downloaderViewModel: ModelDownloaderViewModel = koinViewModel(),
    platformViewModel: PlatformViewModel = koinViewModel(),
    audioImportViewModel: AudioImportViewModel = koinViewModel(),
    editorViewModel: TextEditorViewModel,
    modelSelection: ModelSelection = koinInject(),
    noteAiViewModel: NoteAiViewModel = koinViewModel(),
    notebookViewModel: NotebookViewModel = koinViewModel(),
    attachmentViewModel: AttachmentViewModel = koinViewModel()
) {
    val currentNoteId by editorViewModel.currentNoteId.collectAsStateWithLifecycle()
    val importingState by audioImportViewModel.importingAudioState.collectAsStateWithLifecycle()
    val downloaderUiState by downloaderViewModel.uiState.collectAsStateWithLifecycle()
    val editorState = editorViewModel.editorPresentationState.collectAsStateWithLifecycle().value
        .let { editorViewModel.onGetUiState(it) }

    val audioPlayerUiState = audioPlayerViewModel.uiState.collectAsStateWithLifecycle().value
        .let { audioPlayerViewModel.onGetUiState(it) }
    val platformState by platformViewModel.state.collectAsStateWithLifecycle()

    var showFormatBar by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var isTextFieldFocused by remember { mutableStateOf(false) }
    var showDownloadQuestionDialog by remember { mutableStateOf(false) }
    var showExistingRecordConfirmDialog by remember { mutableStateOf(false) }
    var showCopiedTooltip by remember { mutableStateOf(false) }
    var isFabVisible by remember { mutableStateOf(true) }
    // A brand-new note (id "0") has nothing for the AI tabs to show, so landing on AI_NOTE meant
    // "create note" dropped you on an empty "Tạo với AI" panel instead of somewhere you can type.
    var selectedTab by remember {
        mutableStateOf(
            if (noteId == Arguments.DEFAULT_NOTE_ID) {
                NoteDetailTab.TRANSCRIPT
            } else {
                NoteDetailTab.AI_NOTE
            }
        )
    }
    var showTemplateSheet by remember { mutableStateOf(false) }
    var showNotebookSheet by remember { mutableStateOf(false) }
    val aiUiState by noteAiViewModel.uiState.collectAsStateWithLifecycle()
    val notebookState by notebookViewModel.state.collectAsStateWithLifecycle()
    val currentNotebookId = currentNoteId?.let { notebookState.assignments[it] }
    val attachments by attachmentViewModel.attachments.collectAsStateWithLifecycle()
    var showAttachmentMenu by remember { mutableStateOf(false) }
    val screenScope = rememberCoroutineScope()
    // A brand-new note stays at id 0 until the first keystroke inserts it, so the notebook this
    // screen was opened for can't be assigned until then -- consumed exactly once, the first time
    // currentNoteId turns into a real id, so it never re-fires and never overrides a manual
    // notebook change made afterward.
    var pendingNotebook by remember { mutableStateOf(pendingNotebookId) }

    // A note reached through a notebook (either brand-new, via pendingNotebook before its first
    // save, or already assigned in the DB) is a typed note and must never offer recording -- that
    // is exactly how a "note" ends up with audio attached and bleeds into the voice-note world the
    // notebook screens are supposed to be walled off from.
    val isNotebookNote = pendingNotebook != null || currentNotebookId != null

    LaunchedEffect(currentNoteId) {
        currentNoteId?.takeIf { it != 0L }?.let { id ->
            attachmentViewModel.setNoteId(id)
            pendingNotebook?.let { notebookViewModel.assign(id, it) }
            pendingNotebook = null
        }
    }

    // What the copy/share actions act on: whatever the user is actually looking at. Previously
    // both always used the raw transcript, so the AI tabs could not be copied or shared at all.
    val visibleTabText = when (selectedTab) {
        NoteDetailTab.AI_NOTE -> aiUiState.aiNote
        NoteDetailTab.HIGHLIGHTS -> aiUiState.highlights
        NoteDetailTab.SUMMARY -> aiUiState.summary
        NoteDetailTab.TRANSCRIPT -> editorState.content.text
    }.ifBlank { editorState.content.text }

    val exportTitle = editorState.title.ifBlank {
        editorState.content.text.lineSequence().firstOrNull { it.isNotBlank() }?.take(60).orEmpty()
    }.ifBlank { "Ghi chú" }

    LaunchedEffect(currentNoteId) {
        currentNoteId?.let { noteAiViewModel.loadIfNeeded(it) }
    }

    // Auto title + tags: applied once, right after the note's first AI generation.
    LaunchedEffect(aiUiState.generatedTitle) {
        aiUiState.generatedTitle?.let { title ->
            editorViewModel.updateTitle(title)
            noteAiViewModel.consumeGeneratedTitle()
        }
    }

    LaunchedEffect(Unit) {
        if (noteId.toLong() > 0L) {
            editorViewModel.onGetNoteById(noteId)
        }
        downloaderViewModel.effects.collect {
            when (it) {
                is DownloaderEffect.DownloadEffect -> {
                    showDownloadDialog = true
                    showDownloadQuestionDialog = false
                    showLoadingDialog = false
                }

                is DownloaderEffect.ErrorEffect -> {
                    showDownloadDialog = false
                    showErrorDialog = true
                    showLoadingDialog = false
                }

                is DownloaderEffect.ModelsAreReady -> {
                    showDownloadDialog = false
                    showLoadingDialog = false
                    navigateToTranscription()
                }

                is DownloaderEffect.AskForUserAcceptance -> {
                    showDownloadQuestionDialog = true
                    showLoadingDialog = false
                }

                is DownloaderEffect.CheckingEffect -> {
                    showLoadingDialog = true
                    showDownloadDialog = false
                }
            }
        }
    }
    Scaffold(
        topBar = {
            DetailNoteTopBar(
                title = editorState.title.ifBlank { stringResource(Res.string.top_bar_my_note) },
                onNavigateBack = navigateBack,
                onShare = {
                    showShareDialog = true
                },
                onCopy = {
                    platformViewModel.onCopy(visibleTabText)
                },
                onExportAudio = {
                    platformViewModel.onExportAudio(editorState.recording.recordingPath)
                },
                onImportClick = {
                    audioPlayerViewModel.releasePlayer()
                    audioImportViewModel.importAudio()
                },
                onImportVideoClick = {
                    audioPlayerViewModel.releasePlayer()
                    audioImportViewModel.importVideo()
                },
                isRecordingExist = editorState.recording.isRecordingExist,
                onExportTextAsTxt = {
                    platformViewModel.onExportTextAsTxt(editorState.content.text)
                },
                onExportTextAsPDF = {
                    platformViewModel.onExportTextAsPDF(
                        title = exportTitle,
                        transcript = editorState.content.text,
                        aiNote = aiUiState.aiNote,
                        highlights = aiUiState.highlights,
                        summary = aiUiState.summary
                    )
                },
                onExportTextAsMarkdown = {
                    platformViewModel.onExportTextAsMarkdown(
                        title = exportTitle,
                        transcript = editorState.content.text,
                        aiNote = aiUiState.aiNote,
                        highlights = aiUiState.highlights,
                        summary = aiUiState.summary
                    )
                }
            )
        },
        floatingActionButton = {
            // Typed notes (opened through a notebook) never show the recording FAB at all -- see
            // isNotebookNote above.
            if (!isNotebookNote) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (editorState.recording.isRecordingExist) {
                        AnimatedVisibility(
                            visible = isFabVisible,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                        ) {
                            NotelyFab(
                                onClick = { downloaderViewModel.checkTranscriptionAvailability() },
                                containerColor = LocalCustomColors.current.surface,
                                contentColor = LocalCustomColors.current.onSurfaceVariant,
                                size = 48.dp,
                                cornerRadius = 24.dp
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_transcription),
                                    contentDescription = stringResource(Res.string.transcription_icon),
                                    tint = LocalCustomColors.current.onSurfaceVariant
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = isFabVisible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        NotelyFab(
                            onClick = {
                                if (!editorState.recording.isRecordingExist) {
                                    navigateToRecorder("$currentNoteId")
                                } else {
                                    showExistingRecordConfirmDialog = true
                                }
                            },
                            size = 56.dp,
                            cornerRadius = 28.dp
                        ) {
                            Icon(
                                imageVector = Images.Icons.IcRecorder,
                                contentDescription = stringResource(Res.string.note_detail_recorder),
                                tint = LocalCustomColors.current.onAccent
                            )
                        }
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            BottomNavigationBar(
                isTextFieldFocused = isTextFieldFocused,
                selectionSize = editorState.selectionSize,
                isStarred = editorState.isStarred,
                showFormatBar = showFormatBar,
                textFieldFocusRequester = focusRequester,
                onShowTextFormatBar = { showFormatBar = it },
                editorViewModel = editorViewModel,
                navigateBack = navigateBack,
                onNavigateToSettingsText = onNavigateToSettingsText
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LocalCustomColors.current.bodyBackgroundColor)
        ) {
            NoteTitleField(
                title = editorState.title,
                onTitleChange = editorViewModel::updateTitle
            )

            // Notebook categorisation is a typed-note-only concept -- a voice note (opened from
            // Home's mic FAB) must never show it, same reasoning as hiding the mic FAB above.
            if (isNotebookNote) {
                NotebookRow(
                    notebookName = notebookState.notebooks
                        .firstOrNull { it.id == currentNotebookId }
                        ?.name,
                    onClick = { showNotebookSheet = true }
                )
            }

            // A typed note that has never been recorded and never had AI content generated has
            // nothing for the AI Note/Highlights/Summary tabs to show -- rendering them anyway is
            // exactly the "note lands you in the audio/AI workflow" confusion this was meant to
            // avoid. Recomputed every recomposition (not decided once), so it self-corrects the
            // moment a recording is attached or AI content is generated, no load-order race to
            // get wrong.
            val hasAiContext = editorState.recording.isRecordingExist || aiUiState.hasGenerated

            if (hasAiContext) {
                NoteDetailTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                if (selectedTab != NoteDetailTab.TRANSCRIPT) {
                    NoteDetailTemplateBar(
                        template = aiUiState.template,
                        onOpenTemplatePicker = { showTemplateSheet = true },
                        onRegenerate = { noteAiViewModel.regenerate(editorState.content.text) },
                        isLoading = aiUiState.isLoading
                    )
                }
            }

            when {
                !hasAiContext || selectedTab == NoteDetailTab.TRANSCRIPT -> {
                    NoteContent(
                        modifier = Modifier.weight(1f),
                        paddingValues = PaddingValues(0.dp),
                        newNoteDateString = editorState.createdAt,
                        editorState = editorState,
                        showFormatBar = showFormatBar,
                        focusRequester = focusRequester,
                        audioPlayerUiState = audioPlayerUiState,
                        textEditorViewModel = editorViewModel,
                        audioPlayerViewModel = audioPlayerViewModel,
                        onFocusChange = {
                            isTextFieldFocused = it
                        },
                        onFabVisibility = { isFabVisible = it },
                        // Attachments are a notebook (typed-note) feature only -- a voice note
                        // (Home's mic FAB, isNotebookNote == false) must never offer them, whether
                        // it's brand-new or its Transcript tab is what's currently showing here.
                        showAttachments = isNotebookNote,
                        attachments = attachments,
                        onAddAttachmentClick = {
                            // A brand-new note has no id yet (it's inserted on first keystroke),
                            // so attachmentViewModel's noteId stays null and pick() would
                            // silently no-op -- save the row now so the picker always has
                            // somewhere to attach to, even on an empty note.
                            screenScope.launch {
                                val id = editorViewModel.ensureNoteSaved()
                                attachmentViewModel.setNoteId(id)
                                showAttachmentMenu = true
                            }
                        },
                        onRemoveAttachment = attachmentViewModel::remove,
                        onOpenAttachment = attachmentViewModel::open,
                        onPasteImageClick = {
                            screenScope.launch {
                                val id = editorViewModel.ensureNoteSaved()
                                attachmentViewModel.setNoteId(id)
                                attachmentViewModel.pasteImageFromClipboard()
                            }
                        }
                    )
                }

                else -> {
                    AiGeneratedContentArea(
                        tab = selectedTab,
                        uiState = aiUiState,
                        onGenerate = { noteAiViewModel.regenerate(editorState.content.text) },
                        onDismissError = noteAiViewModel::dismissError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showAttachmentMenu) {
        AttachmentPickerMenu(
            onDismiss = { showAttachmentMenu = false },
            onPickImage = attachmentViewModel::pickImage,
            onPickVideo = attachmentViewModel::pickVideo,
            onPickDocument = attachmentViewModel::pickDocument
        )
    }

    if (showNotebookSheet) {
        NotebookPickerSheet(
            notebooks = notebookState.notebooks,
            selectedNotebookId = currentNotebookId,
            onSelect = { notebookId ->
                currentNoteId?.takeIf { it != 0L }?.let { noteId ->
                    notebookViewModel.assign(noteId, notebookId)
                }
            },
            onCreate = notebookViewModel::create,
            onDismiss = { showNotebookSheet = false }
        )
    }

    if (showTemplateSheet) {
        NoteTemplateBottomSheet(
            selectedTemplate = aiUiState.template,
            onSelectTemplate = { template ->
                noteAiViewModel.selectTemplate(template, editorState.content.text)
            },
            onDismiss = { showTemplateSheet = false }
        )
    }


    if (showDownloadDialog) {
        LocalSoftwareKeyboardController.current?.hide()
        DownloaderDialog(
            transcriptionModel = downloaderUiState.selectedModel,
            downloaderUiState,
            onDismiss = { showDownloadDialog = false }
        )
    }

    if (showErrorDialog) {
        LocalSoftwareKeyboardController.current?.hide()
        AlertDialog(
            modifier = Modifier.height(120.dp),
            title = { Text(stringResource(resource = Res.string.download_dialog_error)) },
            onDismissRequest = { showErrorDialog = false },
            buttons = {
                Button(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp),
                    onClick = {
                        showErrorDialog = false
                    },
                ) { Text(stringResource(resource = Res.string.confirmation_cancel)) }
            }
        )
    }
    if (showDownloadQuestionDialog) {
        LocalSoftwareKeyboardController.current?.hide()
        DownloadModelDialog(
            onDownload = {
                downloaderViewModel.startDownload()
                showDownloadQuestionDialog = false
            },
            onCancel = {
                showDownloadQuestionDialog = false
            },
            transcriptionModel = downloaderUiState.selectedModel
        )
    }


    if (showLoadingDialog) {
        PreparingLoadingDialog()
    }
    ReplaceRecordingConfirmationDialog(
        showDialog = showExistingRecordConfirmDialog,
        onDismiss = {
            showExistingRecordConfirmDialog = false
        },
        onConfirm = {
            navigateToRecorder("$currentNoteId")
        },
        option = RecordingConfirmationUiModel.Record
    )

    if (showShareDialog) {
        ShareDialog(
            onShareAudioRecording = {
                platformViewModel.shareRecording(editorState.recording.recordingPath)
            },
            onShareTexts = {
                platformViewModel.shareText(visibleTabText)
            },
            onDismiss = { showShareDialog = false }
        )
    }

    ImportingAudioStateHost(
        state = importingState,
        onSuccess = editorViewModel::onUpdateRecordingPath,
        onRelease = audioImportViewModel::releaseState
    )

    LaunchedEffect(platformState.copySuccess, showCopiedTooltip) {
        if (platformState.copySuccess == true) {
            showCopiedTooltip = true
        }
    }
    3
    CopiedNotification(
        visible = showCopiedTooltip,
        onDismiss = {
            showCopiedTooltip = false
            platformViewModel.onClearCopyState()
        },
        modifier = Modifier
            .padding(bottom = 52.dp)
    )
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NoteContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    newNoteDateString: String,
    editorState: EditorUiState,
    showFormatBar: Boolean,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    audioPlayerUiState: AudioPlayerUiState,
    textEditorViewModel: TextEditorViewModel,
    audioPlayerViewModel: AudioPlayerViewModel,
    onFabVisibility: (Boolean) -> Unit,
    showAttachments: Boolean,
    attachments: List<com.module.notelycompose.attachment.Attachment>,
    onAddAttachmentClick: () -> Unit,
    onRemoveAttachment: (Long) -> Unit,
    onOpenAttachment: (com.module.notelycompose.attachment.Attachment) -> Unit,
    onPasteImageClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showDeleteRecordingDialog by remember { mutableStateOf(false) }
    val dismissState = rememberDismissState()

    // This container must NOT also be verticalScroll: NoteEditor's BasicTextField already owns
    // a verticalScroll for its (potentially very long) text, and nesting a second verticalScroll
    // around a Modifier.weight(1f) child gives that child unbounded height to measure against,
    // which is undefined/broken sizing. That was the root cause of the cursor disappearing into a
    // blank area when the keyboard opened -- BasicTextField's built-in "scroll to keep the cursor
    // visible" had no sane bounded viewport to scroll within. Keeping this Column fixed-size (only
    // imePadding shrinks it for the keyboard) lets NoteEditor's own scroll be the single, correctly
    // bounded scrollable region.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(LocalCustomColors.current.bodyBackgroundColor)
            .imePadding()
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            DateHeader(newNoteDateString)

            if (editorState.recording.isRecordingExist) {

                if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                    LaunchedEffect(Unit) {
                        showDeleteRecordingDialog = true
                    }
                }

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    background = {
                        // Background that appears when swiping. Was `.width(800.dp)` — a
                        // hardcoded overflow that pushed the delete icon off past the visible
                        // edge. The horizontal padding here must match PlatformAudioPlayerUi's own
                        // outer padding (16dp) — otherwise this background (edge-to-edge) peeks out
                        // past the narrower audio-player card at rest, which is exactly what
                        // happened on-device before this padding was added.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(LocalCustomColors.current.danger),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = LocalCustomColors.current.onDanger,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    },
                    dismissContent = {
                        PlatformAudioPlayerUi(
                            filePath = editorState.recording.recordingPath,
                            uiState = audioPlayerUiState,
                            onLoadAudio = audioPlayerViewModel::onLoadAudio,
                            onClear = audioPlayerViewModel::onClear,
                            onSeekTo = audioPlayerViewModel::onSeekTo,
                            onTogglePlayPause = audioPlayerViewModel::onTogglePlayPause
                        )
                    }
                )
            }

            NoteEditor(
                modifier = Modifier.fillMaxWidth().weight(1f),
                editorState = editorState,
                showFormatBar = showFormatBar,
                focusRequester = focusRequester,
                onFocusChange = onFocusChange,
                textEditorViewModel = textEditorViewModel,
                onFabVisibility = onFabVisibility
            )

            if (showAttachments) {
                Row(
                    modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // "Dán ảnh" reads the clipboard directly -- copy a photo elsewhere, tap this,
                    // it shows up immediately, no file-picker menu. Always offered alongside the
                    // picker-based "Đính kèm", not a replacement for it (PDFs/videos still need
                    // the picker).
                    PasteImageButton(onClick = onPasteImageClick)
                    // AttachmentStrip hides itself entirely when empty (so notes without
                    // attachments render exactly as before this feature existed) -- this is the
                    // one always-visible entry point to add the first one.
                    if (attachments.isEmpty()) {
                        AddAttachmentButton(onClick = onAddAttachmentClick)
                    }
                }
                if (attachments.isNotEmpty()) {
                    AttachmentStrip(
                        attachments = attachments,
                        onAddClick = onAddAttachmentClick,
                        onRemove = onRemoveAttachment,
                        onOpen = onOpenAttachment
                    )
                }
            }
        }
    }
    DeleteRecordingConfirmationDialog(
        showDialog = showDeleteRecordingDialog,
        onDismiss = {
            showDeleteRecordingDialog = false
            coroutineScope.launch {
                dismissState.reset()
            }
        },
        onConfirm = {
            textEditorViewModel.onDeleteRecord()
        }
    )
}


/**
 * The note's title, edited in place at the top of the screen. Writes are debounced so typing a
 * title is one DB write per pause rather than one per character, and [TextEditorViewModel
 * .updateTitle] flags the title as custom so body edits stop mirroring into it.
 */
@Composable
private fun NoteTitleField(
    title: String,
    onTitleChange: (String) -> Unit
) {
    val colors = LocalCustomColors.current
    var input by remember { mutableStateOf(title) }

    // Titles can also arrive from outside this field: the note finishing its DB load, or the AI
    // auto-title after the first generation.
    LaunchedEffect(title) {
        if (title != input) input = title
    }

    LaunchedEffect(input) {
        if (input != title) {
            delay(500)
            onTitleChange(input)
        }
    }

    BasicTextField(
        value = input,
        onValueChange = { input = it.replace("\n", "") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp),
        textStyle = TextStyle(
            color = colors.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        ),
        cursorBrush = SolidColor(colors.accent),
        singleLine = true,
        decorationBox = { innerTextField ->
            if (input.isEmpty()) {
                Text(
                    text = stringResource(Res.string.note_title_placeholder),
                    color = colors.onSurfaceVariant,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            innerTextField()
        }
    )
}

@Composable
private fun DateHeader(dateString: String) {
    Text(
        text = dateString,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        fontSize = 12.sp,
        color = LocalCustomColors.current.onSurfaceVariant
    )
}

@Composable
private fun NoteEditor(
    modifier: Modifier = Modifier,
    editorState: EditorUiState,
    showFormatBar: Boolean,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    textEditorViewModel: TextEditorViewModel,
    onFabVisibility: (Boolean) -> Unit
) {

    val scrollState = rememberScrollState()
    var previousScrollValue by remember { mutableStateOf(0) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collect { currentScroll ->
                val scrollingUp = currentScroll < previousScrollValue
                val result = scrollingUp || currentScroll == 0
                previousScrollValue = currentScroll
                onFabVisibility(result)
            }
    }

    // Compose Multiplatform's iOS text-actions menu (copy/paste) breaks for any BasicTextField
    // that carries a non-null VisualTransformation, even one that changes nothing -- a known gap
    // in the CMP version this project is pinned to (fixed only in 1.9.3+/1.12.0, see
    // https://github.com/JetBrains/compose-multiplatform/issues/4502). A brand-new note has no
    // formats yet, so it was hitting this on every fresh note. Skip the transformation entirely
    // when there is nothing to render, so Paste keeps working until the user actually bolds/
    // italicises/underlines something in this note.
    val transformation = if (editorState.formats.isEmpty()) {
        VisualTransformation.None
    } else {
        VisualTransformation { text ->
            TransformedText(
                buildAnnotatedString {
                    append(text)
                    editorState.formats.forEach { format ->
                        addStyle(
                            SpanStyle(
                                fontWeight = if (format.isBold) FontWeight.Bold else null,
                                fontStyle = if (format.isItalic) FontStyle.Italic else null,
                                textDecoration = if (format.isUnderline)
                                    TextDecoration.Underline else null,
                                fontSize = format.textSize?.sp ?: TextUnit.Unspecified
                            ),
                            format.range.first.coerceIn(0, text.length),
                            format.range.last.coerceIn(0, text.length)
                        )
                    }
                },
                OffsetMapping.Identity
            )
        }
    }

    BasicTextField(
        value = editorState.content,
        onValueChange = textEditorViewModel::onUpdateContent,
        modifier =
            modifier
                .focusRequester(focusRequester)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
                .onFocusChanged {
                    onFocusChange(it.isFocused)
                },
        textStyle = TextStyle(
            color = LocalCustomColors.current.onSurface,
            textAlign = editorState.textAlign,
            fontSize = editorState.bodyTextSize.sp,
            // A fixed lineHeight on a TextStyle whose fontSize is user-adjustable clips/overlaps
            // at large sizes; scale it with fontSize instead. (Previously worked by accident
            // because there was no lineHeight set at all.)
            lineHeight = (editorState.bodyTextSize * 1.5f).sp
        ),
        cursorBrush = SolidColor(LocalCustomColors.current.accent),
        readOnly = showFormatBar,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        ),
        visualTransformation = transformation
    )
}
