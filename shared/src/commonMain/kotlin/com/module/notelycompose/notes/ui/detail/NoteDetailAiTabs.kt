package com.module.notelycompose.notes.ui.detail

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.module.notelycompose.ai.AiTemplate
import com.module.notelycompose.notes.presentation.detail.NoteAiUiState
import com.module.notelycompose.notes.ui.theme.AppRadii
import com.module.notelycompose.notes.ui.theme.Elevation
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.ui.theme.softShadow

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

/**
 * The segmented AI/Highlights/Summary/Transcript control. Previously the selected tab was just a
 * flat color swap with no elevation, which read as flat; now a white pill slides under the
 * selected label with a soft shadow, closer to the reference design's tab treatment.
 */
@Composable
fun NoteDetailTabRow(
    selectedTab: NoteDetailTab,
    onTabSelected: (NoteDetailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCustomColors.current
    val tabs = NoteDetailTab.entries
    val selectedIndex = tabs.indexOf(selectedTab)

    // NOTE: BoxWithConstraints is built on SubcomposeLayout, which throws at runtime if asked
    // for intrinsic measurements (Modifier.height(IntrinsicSize.Min) et al) -- crashed on-device
    // when first tried here. Use a fixed height on both the pill and the row instead.
    val tabRowHeight = 44.dp
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surfaceSunken)
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / tabs.size
        val pillOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = tween(220),
            label = "tabPillOffset"
        )

        Box(
            modifier = Modifier
                .offset(x = pillOffset)
                .width(tabWidth)
                .height(tabRowHeight)
                .softShadow(Elevation.card, cornerRadius = 18.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(tabRowHeight),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEach { tab ->
                val selected = tab == selectedTab
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) colors.onSurface else colors.onSurfaceVariant,
                        maxLines = 1
                    )
                }
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
    val colors = LocalCustomColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${template.displayName()} Template",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.clickable(onClick = onOpenTemplatePicker)
        )
        IconButton(onClick = onRegenerate, modifier = Modifier.height(28.dp)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(16.dp),
                    strokeWidth = 2.dp,
                    color = colors.accent
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Tạo lại",
                    tint = colors.accent
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
    val colors = LocalCustomColors.current
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.dangerSoft)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.danger,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismissError) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = colors.danger)
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
                        CircularProgressIndicator(color = colors.accent)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Đang tạo nội dung...",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
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
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onGenerate,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.onAccent
                            )
                        ) {
                            Text(text = "Tạo với AI")
                        }
                    }
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    if (tab == NoteDetailTab.HIGHLIGHTS) {
                        text.lines().filter { it.isNotBlank() }.forEach { line ->
                            Row(modifier = Modifier.padding(vertical = 6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp, end = 10.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colors.accent)
                                )
                                Text(
                                    text = renderSimpleMarkdownBold(
                                        line.removePrefix("-").removePrefix("•").trim()
                                    ),
                                    color = colors.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        Text(
                            text = text,
                            color = colors.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
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
    val colors = LocalCustomColors.current
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
                    text = "Note Template",
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
    val colors = LocalCustomColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppRadii.shapeMd)
            .background(
                if (selected) colors.accentSoft
                else colors.surfaceSunken
            )
            .then(
                if (selected) {
                    Modifier.border(1.5.dp, colors.accent, AppRadii.shapeMd)
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
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = template.description(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )
        }
        if (selected) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = colors.accent)
        }
    }
}
