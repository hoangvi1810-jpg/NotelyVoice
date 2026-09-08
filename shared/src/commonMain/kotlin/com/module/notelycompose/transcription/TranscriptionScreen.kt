package com.module.notelycompose.transcription

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.presentation.detail.TextEditorViewModel
import com.module.notelycompose.notes.ui.theme.AppRadii
import com.module.notelycompose.notes.ui.theme.Elevation
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.ui.theme.softShadow
import com.module.notelycompose.platform.HandlePlatformBackNavigation
import com.module.notelycompose.platform.getPlatform
import com.module.notelycompose.resources.vectors.IcChevronLeft
import com.module.notelycompose.resources.vectors.Images
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.top_bar_back
import com.module.notelycompose.resources.transcription_dialog_append
import com.module.notelycompose.resources.transcription_dialog_original
import com.module.notelycompose.resources.transcription_dialog_summarize
import com.module.notelycompose.resources.transcription_dialog_error_got_it
import com.module.notelycompose.resources.transcription_dialog_error_audio_file_title
import com.module.notelycompose.resources.transcription_dialog_error_audio_file_desc
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TranscriptionScreen(
    navigateBack: () -> Unit,
    viewModel: TranscriptionViewModel = koinViewModel(),
    editorViewModel: TextEditorViewModel
) {
    val colors = LocalCustomColors.current
    val scrollState = rememberScrollState()
    val transcriptionUiState by viewModel.uiState.collectAsState()
    val editorState by editorViewModel.editorPresentationState.collectAsState()


    LaunchedEffect(transcriptionUiState.originalText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    DisposableEffect(Unit) {
        viewModel.requestAudioPermission()
        viewModel.initRecognizer()
        viewModel.startRecognizer(editorState.recording.recordingPath)
        onDispose {
            viewModel.stopRecognizer()
            viewModel.finishRecognizer()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bodyBackgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 48.dp)
        ) {
            Box(modifier = Modifier.align(Alignment.Start)
                .padding(start = 4.dp, bottom = 12.dp, top = 4.dp)) {
                BackButton(onNavigateBack = {
                    viewModel.stopRecognizer()
                    viewModel.finishRecognizer()
                    navigateBack()
                    }
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .softShadow(Elevation.card, AppRadii.lg)
                    .clip(RoundedCornerShape(AppRadii.lg))
                    .background(colors.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = if(transcriptionUiState.viewOriginalText) transcriptionUiState.originalText else transcriptionUiState.summarizedText,
                    color = colors.onSurface,
                    style = TextStyle(
                        fontSize = editorState.bodyTextSize.sp,
                        lineHeight = (editorState.bodyTextSize * 1.5f).sp
                    )
                )
            }
            if(transcriptionUiState.progress == 0){
                LinearProgressIndicator(
                    modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                    color = colors.accent,
                    trackColor = colors.surfaceSunken
                )
            } else if(transcriptionUiState.progress in 1..99){
               SmoothLinearProgressBar((transcriptionUiState.progress / 100f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !transcriptionUiState.inTranscription,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if(!transcriptionUiState.inTranscription) {
                            colors.outline
                        } else {
                            colors.outline.copy(alpha = 0.5f)
                        }
                    ),
                    shape = AppRadii.shapeMd,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.onSurface,
                        disabledContentColor = colors.onSurfaceVariant
                    ),
                    content = {
                        Text(
                            stringResource(Res.string.transcription_dialog_append)
                        )
                    },
                    onClick = {
                        val result = if (transcriptionUiState.viewOriginalText) transcriptionUiState.originalText else transcriptionUiState.summarizedText
                        editorViewModel.onUpdateContent(TextFieldValue("${editorState.content.text}\n$result"))
                        navigateBack()
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !transcriptionUiState.inTranscription,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if(!transcriptionUiState.inTranscription) {
                            colors.outline
                        } else {
                            colors.outline.copy(alpha = 0.5f)
                        }
                    ),
                    shape = AppRadii.shapeMd,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.onSurface,
                        disabledContentColor = colors.onSurfaceVariant
                    ),
                    content = {
                        Text(
                            if(transcriptionUiState.viewOriginalText) stringResource(Res.string.transcription_dialog_summarize) else
                                stringResource(Res.string.transcription_dialog_original),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }, onClick = {
                        viewModel.summarize()
                    })
            }

        }
    }

    HandlePlatformBackNavigation(enabled = true) {
        navigateBack()
    }

    if(transcriptionUiState.hasError) {
        AlertDialog(
            onDismissRequest = navigateBack,
            confirmButton = {
                TextButton(onClick = navigateBack) {
                    Text(stringResource(Res.string.transcription_dialog_error_got_it))
                }
            },
            title = { Text(stringResource(Res.string.transcription_dialog_error_audio_file_title)) },
            text = { Text(stringResource(Res.string.transcription_dialog_error_audio_file_desc)) }
        )
    }

}
@Composable
fun BackButton(
    onNavigateBack: () -> Unit
) {
    val colors = LocalCustomColors.current
    if (getPlatform().isAndroid) {
        IconButton(
            onClick = onNavigateBack,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(Res.string.top_bar_back),
                tint = colors.onSurface
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onNavigateBack() }
        ) {
            Icon(
                imageVector = Images.Icons.IcChevronLeft,
                contentDescription = stringResource(Res.string.top_bar_back),
                modifier = Modifier.size(28.dp),
                tint = colors.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.top_bar_back),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface
            )
        }
    }
}


@Composable
fun SmoothLinearProgressBar(progress: Float) {
    val colors = LocalCustomColors.current
    // Animate the progress value for smooth transitions
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500) // Adjust duration as needed
    )

    LinearProgressIndicator(
        // Was passing the raw `progress` param here instead of `animatedProgress` -- the
        // animateFloatAsState above was computed and then silently discarded, causing the visible
        // stutter/jump instead of a smooth fill.
        progress = { animatedProgress },
        modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
        color = colors.accent,
        trackColor = colors.surfaceSunken
    )
}
