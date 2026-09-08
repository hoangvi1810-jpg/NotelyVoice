package com.module.notelycompose.notes.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.ai.AiRepository
import com.module.notelycompose.ai.DEFAULT_OPENROUTER_MODEL
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Lets the user paste their own OpenRouter API key and pick a model — powers the AI Note /
 * Highlights / Summary tabs and auto title+tags on the note detail screen. The key is written
 * straight to [AiRepository] (Android Keystore / iOS Keychain-backed), never through DataStore,
 * and is never read back and re-displayed once saved — only whether one is configured.
 */
@Composable
fun AiSettingsSection(
    aiRepository: AiRepository = koinInject()
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var hasKey by remember { mutableStateOf<Boolean?>(null) } // null while the initial check loads
    var apiKeyInput by remember { mutableStateOf("") }
    var model by remember { mutableStateOf(DEFAULT_OPENROUTER_MODEL) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        hasKey = !aiRepository.getApiKey().isNullOrBlank()
        model = aiRepository.getModel()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "AI (OpenRouter)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = LocalCustomColors.current.bodyContentColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Dùng cho AI Note, Highlights, Summary và tự đặt tên. Lấy API key miễn phí " +
                "tại openrouter.ai/keys.",
            fontSize = 12.sp,
            color = LocalCustomColors.current.settingsBodyTextColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when (hasKey) {
            true -> Text(
                text = "✓ Đã lưu API key",
                fontSize = 13.sp,
                color = LocalCustomColors.current.success,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            false -> Text(
                text = "Chưa cấu hình API key — AI Note/Highlights/Summary sẽ không hoạt động.",
                fontSize = 13.sp,
                color = LocalCustomColors.current.settingsBodyTextColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            null -> Unit
        }

        OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            placeholder = {
                Text(if (hasKey == true) "Nhập key mới để thay thế" else "sk-or-v1-...")
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            // Compose Multiplatform's long-press paste menu is unreliable on iOS (works fine on
            // Android) -- a key this long is painful to type by hand, so read the clipboard
            // directly instead of relying on the system context menu.
            trailingIcon = {
                Text(
                    text = "Dán",
                    color = LocalCustomColors.current.accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable {
                            clipboardManager.getText()?.text?.let { apiKeyInput = it.trim() }
                        }
                )
            }
        )

        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            label = { Text("Model (OpenRouter)") },
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (apiKeyInput.isNotBlank()) {
                            aiRepository.setApiKey(apiKeyInput.trim())
                            apiKeyInput = ""
                            hasKey = true
                        }
                        aiRepository.setModel(model.trim().ifBlank { DEFAULT_OPENROUTER_MODEL })
                        statusMessage = "Đã lưu"
                    }
                }
            ) {
                Text("Lưu")
            }

            if (hasKey == true) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            aiRepository.clearApiKey()
                            hasKey = false
                            statusMessage = "Đã xoá API key"
                        }
                    }
                ) {
                    Text("Xoá key")
                }
            }
        }

        statusMessage?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = LocalCustomColors.current.settingsBodyTextColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
