package com.module.notelycompose.notes.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.modelDownloader.NO_MODEL_SELECTION
import com.module.notelycompose.modelDownloader.VIETNAMESE_MODEL
import com.module.notelycompose.notes.ui.detail.AndroidNoteTopBar
import com.module.notelycompose.notes.ui.detail.IOSNoteTopBar
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.platform.getPlatform
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.ic_question_mark
import com.module.notelycompose.resources.optimized_model_description
import com.module.notelycompose.resources.optimized_model_setting_desc
import com.module.notelycompose.resources.optimized_model_setting_size
import com.module.notelycompose.resources.optimized_model_title
import com.module.notelycompose.resources.question_mark
import com.module.notelycompose.resources.settings_model_selection
import com.module.notelycompose.resources.settings_model_selection_description
import com.module.notelycompose.resources.settings_model_selection_text
import com.module.notelycompose.resources.standard_model_setting_desc
import com.module.notelycompose.resources.standard_model_setting_size
import com.module.notelycompose.resources.standard_model_title
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private const val HIDE_TIME_ELAPSE = 1500L

data class ModelOption(
    val title: String,
    val description: String,
    val size: String = ""
)

@Composable
fun ModelSelectionScreen(
    navigateBack: () -> Unit,
    navigateToModelExplanation: () -> Unit,
    preferencesRepository: PreferencesRepository = koinInject()
) {
    // Vietnamese routes through a different pair of models in ModelSelection.getSelectedModel()
    // (small / large-v3-turbo instead of base / small — see ModelSelection.kt) since the base
    // model misses tones/diacritics too often to be usable. This screen must show the labels and
    // sizes that match what will actually be downloaded, not the generic multilingual copy.
    val language by preferencesRepository.getDefaultTranscriptionLanguage()
        .collectAsState(languageCodeMap.entries.first().key)
    val isVietnamese = language == VIETNAMESE_MODEL

    val modelOptions = if (isVietnamese) {
        listOf(
            ModelOption(
                title = "Standard model (small, multilingual)",
                description = "Lighter fallback for weaker devices\n" +
                    "Still noticeably better on Vietnamese than the base model",
                size = "465 MB"
            ),
            ModelOption(
                title = "Optimized model (large-v3-turbo)",
                description = "Best accuracy for Vietnamese tones & diacritics\n" +
                    "Larger file size, slower performance",
                size = "547 MB"
            )
        )
    } else {
        listOf(
            ModelOption(
                title = stringResource(Res.string.standard_model_title),
                description = stringResource(Res.string.standard_model_setting_desc),
                size = stringResource(Res.string.standard_model_setting_size)
            ),
            ModelOption(
                title = stringResource(Res.string.optimized_model_title),
                description = stringResource(Res.string.optimized_model_setting_desc),
                size = stringResource(Res.string.optimized_model_setting_size)
            )
        )
    }

    var selectedModel by remember { mutableIntStateOf(0) } // Standard model selected by default
    var modelSavedSelection by remember { mutableStateOf(NO_MODEL_SELECTION) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        modelSavedSelection = preferencesRepository.getModelSelection().first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.bodyBackgroundColor)
    ) {
        if (getPlatform().isAndroid) {
            AndroidNoteTopBar(
                title = "",
                onNavigateBack = navigateBack
            )
        } else {
            IOSNoteTopBar(
                onNavigateBack = navigateBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Title
            Text(
                text = stringResource(Res.string.settings_model_selection),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = LocalCustomColors.current.bodyContentColor,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.settings_model_selection_text),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LocalCustomColors.current.bodyContentColor,

                )
                Icon(
                    painter = painterResource(Res.drawable.ic_question_mark),
                    contentDescription = stringResource(Res.string.question_mark),
                    modifier = Modifier
                        .clickable {
                            navigateToModelExplanation()
                        }
                        .size(32.dp).padding(start = 8.dp),
                    tint = LocalCustomColors.current.bodyContentColor
                )
            }

            // Description
            Text(
                text = stringResource(Res.string.settings_model_selection_description),
                fontSize = 16.sp,
                color = LocalCustomColors.current.bodyContentColor,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Model options
            modelOptions.forEachIndexed { index, model ->
                ModelOptionCard(
                    model = model,
                    isSelected = if(modelSavedSelection != NO_MODEL_SELECTION) {
                        modelSavedSelection == index
                    } else {
                        selectedModel == index
                    },
                    onClick = {
                        selectedModel = index
                        coroutineScope.launch {
                            preferencesRepository.setModelSelection(selectedModel)
                        }
                        navigateBack()
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        // end of content
    }
}
