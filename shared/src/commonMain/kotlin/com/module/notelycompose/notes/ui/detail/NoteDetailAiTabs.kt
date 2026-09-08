package com.module.notelycompose.notes.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.module.notelycompose.ai.AiTemplate
import com.module.notelycompose.notes.presentation.detail.NoteAiUiState
import com.module.notelycompose.notes.ui.theme.LocalCustomColors

/** The four tabs shown on the note detail screen, mirroring AI Note / Highlights / Summary /
 * Transcript from the reference app. TRANSCRIPT renders the existing editable [NoteEditor] as-is
 * — it is not AI-generated content, so it's handled separately from the other three. */
enum class NoteDetailTab(val label: String) {
    AI_NOTE("AI Note"),
    HIGHLIGHTS("Highlights"),
    SUMMARY("Summary"),
    TRANSCRIPT("Transcript")
}

fun AiTemplate.displayName(): String = when (this) {
    AiTemplate.CLASSIC -> "Classic"
    AiTemplate.BRAINSTORM -> "Brainstorm"
    AiTemplate.MEETING -> "Meeting"
    AiTemplate.LECTURE -> "Lecture"
    AiTemplate.JOURNALING -> "Journaling"
}

private fun AiTemplate.description(): String = when (this) {
    AiTemplate.CLASSIC -> "Giữ nguyên lời nói, chỉ dọn ngữ pháp và dấu câu"
    AiTemplate.BRAINSTORM -> "Gom ý rời rạc thành từng nhóm chủ đề, tách riêng việc cần làm"
    AiTemplate.MEETING -> "Biên bản họp: điểm chính, quyết định, mục hành động"
    AiTemplate.LECTURE -> "Ghi chú bài giảng: khái niệm chính và chi tiết bổ trợ"
    AiTemplate.JOURNALING -> "Nhật ký có cấu trúc, giữ nguyên cảm xúc và thông điệp"
}

@Composable
fun NoteDetailTabRow(
    selectedTab: NoteDetailTab,
    onTabSelected: (NoteDetailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(LocalCustomColors.current.noteListBackgroundColor)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NoteDetailTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (selected) LocalCustomColors.current.bodyBackgroundColor
                        else Color.Transparent
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = LocalCustomColors.current.bodyContentColor,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun NoteDetailTemplateBar(
    template: AiTemplate,
    onOpenTemplatePicker: () -> Unit,
    onRegenerate: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${template.displayName()} Template",
            fontSize = 12.sp,
            color = LocalCustomColors.current.settingsBodyTextColor,
            modifier = Modifier.clickable(onClick = onOpenTemplatePicker)
        )
        IconButton(onClick = onRegenerate, modifier = Modifier.height(28.dp)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(16.dp),
                    strokeWidth = 2.dp,
                    color = LocalCustomColors.current.selectAllCheckboxColor
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Tạo lại",
                    tint = LocalCustomColors.current.selectAllCheckboxColor
                )
            }
        }
    }
}

@Composable
fun AiGeneratedContentArea(
    tab: NoteDetailTab,
    uiState: NoteAiUiState,
    onGenerate: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val text = when (tab) {
        NoteDetailTab.AI_NOTE -> uiState.aiNote
        NoteDetailTab.HIGHLIGHTS -> uiState.highlights
        NoteDetailTab.SUMMARY -> uiState.summary
        NoteDetailTab.TRANSCRIPT -> ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        uiState.errorMessage?.let { message ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFBEAEA))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = Color(0xFFB3261E),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismissError) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color(0xFFB3261E))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LocalCustomColors.current.selectAllCheckboxColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Đang tạo nội dung...",
                            fontSize = 13.sp,
                            color = LocalCustomColors.current.settingsBodyTextColor
                        )
                    }
                }
            }

            text.isBlank() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Chưa có nội dung.\nNhấn nút bên dưới để AI tạo từ bản ghi.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = LocalCustomColors.current.settingsBodyTextColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onGenerate,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = LocalCustomColors.current.selectAllCheckboxColor
                            )
                        ) {
                            Text(text = "Tạo với AI", color = Color.White)
                        }
                    }
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    if (tab == NoteDetailTab.HIGHLIGHTS) {
                        text.lines().filter { it.isNotBlank() }.forEach { line ->
                            Row(modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = "•  ",
                                    color = LocalCustomColors.current.bodyContentColor,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = renderSimpleMarkdownBold(
                                        line.removePrefix("-").removePrefix("•").trim()
                                    ),
                                    color = LocalCustomColors.current.bodyContentColor,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = text,
                            color = LocalCustomColors.current.bodyContentColor,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/** Turns `**word**` spans into bold text — the only markdown AiPrompts.forHighlights asks for. */
private fun renderSimpleMarkdownBold(text: String) = buildAnnotatedString {
    val boldRegex = Regex("\\*\\*(.+?)\\*\\*")
    var lastIndex = 0
    boldRegex.findAll(text).forEach { match ->
        append(text.substring(lastIndex, match.range.first))
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append(match.groupValues[1])
        pop()
        lastIndex = match.range.last + 1
    }
    append(text.substring(lastIndex))
}

@Composable
fun NoteTemplateBottomSheet(
    selectedTemplate: AiTemplate,
    onSelectTemplate: (AiTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(LocalCustomColors.current.bodyBackgroundColor)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Note Template",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalCustomColors.current.bodyContentColor
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = LocalCustomColors.current.bodyContentColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            AiTemplate.entries.forEach { template ->
                NoteTemplateOptionRow(
                    template = template,
                    selected = template == selectedTemplate,
                    onClick = {
                        onSelectTemplate(template)
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun NoteTemplateOptionRow(
    template: AiTemplate,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = LocalCustomColors.current.selectAllCheckboxColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) accent.copy(alpha = 0.12f)
                else LocalCustomColors.current.noteListBackgroundColor
            )
            .then(
                if (selected) {
                    Modifier.border(2.dp, accent, RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.displayName(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LocalCustomColors.current.bodyContentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = template.description(),
                fontSize = 12.sp,
                color = LocalCustomColors.current.settingsBodyTextColor
            )
        }
        if (selected) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = accent)
        }
    }
}
