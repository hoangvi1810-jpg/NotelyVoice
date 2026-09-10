package com.module.notelycompose.notebook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import com.module.notelycompose.notes.ui.theme.LocalCustomColors

/**
 * Rich-text editor used for notebook (typed) notes only -- NOT reused by voice notes, which keep
 * the existing BasicTextField-based `NoteEditor` in NoteDetailScreen.kt untouched. Persists via
 * [onHtmlChange] (debounced by the caller) and mirrors plain text out via [onPlainTextChange] so
 * search/AI/export keep reading notesEntity.content unchanged.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookRichEditor(
    modifier: Modifier = Modifier,
    initialHtml: String?,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    onHtmlChange: (String) -> Unit,
    onPlainTextChange: (String) -> Unit
) {
    val colors = LocalCustomColors.current
    val state = rememberRichTextState()

    // Guards against a real bug this hit on first reopen of an existing note: rememberRichTextState
    // always starts empty, so the state.annotatedString effect below fires once immediately with
    // nothing in it -- before initialHtml has arrived from the (async) DB load. Without this guard
    // that transient empty state got persisted and silently wiped the note's saved content ~500ms
    // later. `initialHtml != null` means the parent has given a definitive answer (loaded content,
    // or "" confirming there truly is none); `state.annotatedString.text.isNotEmpty()` covers a
    // brand-new note, where there is nothing to load and the user's own typing must flow through
    // immediately. Once true this only ever goes to true and stays there.
    var readyToPersist by remember { mutableStateOf(false) }

    LaunchedEffect(initialHtml) {
        if (!initialHtml.isNullOrEmpty()) {
            state.setHtml(initialHtml)
        }
        if (initialHtml != null) readyToPersist = true
    }

    LaunchedEffect(state.annotatedString) {
        if (state.annotatedString.text.isNotEmpty()) readyToPersist = true
        if (readyToPersist) {
            onHtmlChange(state.toHtml())
            onPlainTextChange(state.annotatedString.text)
        }
    }

    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (isFocused) {
            RichEditorToolbar(state = state)
        }
        RichTextEditor(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    isFocused = it.isFocused
                    onFocusChange(it.isFocused)
                },
            textStyle = TextStyle(
                color = colors.onSurface,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            colors = RichTextEditorDefaults.richTextEditorColors(
                containerColor = colors.bodyBackgroundColor,
                textColor = colors.onSurface,
                cursorColor = colors.accent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun RichEditorToolbar(state: RichTextState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        ToolbarToggle(
            icon = Icons.Default.FormatBold,
            isActive = state.currentSpanStyle.fontWeight == FontWeight.Bold,
            onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
        )
        ToolbarToggle(
            icon = Icons.Default.FormatItalic,
            isActive = state.currentSpanStyle.fontStyle == FontStyle.Italic,
            onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
        )
        ToolbarToggle(
            icon = Icons.Default.FormatUnderlined,
            isActive = state.currentSpanStyle.textDecoration == TextDecoration.Underline,
            onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
        )
        ToolbarToggle(
            icon = Icons.Default.Title,
            isActive = state.currentSpanStyle.fontSize == 22.sp,
            onClick = { state.toggleSpanStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)) }
        )
        ToolbarToggle(
            icon = Icons.Default.FormatListBulleted,
            isActive = false,
            onClick = { state.toggleUnorderedList() }
        )
    }
}

@Composable
private fun ToolbarToggle(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) colors.accentSoft else Color.Transparent)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.accent)
    }
}
