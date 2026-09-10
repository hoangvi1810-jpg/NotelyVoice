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
import androidx.compose.material.icons.filled.ContentPaste
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
import androidx.compose.runtime.key
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
import androidx.compose.ui.platform.LocalClipboardManager
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
 *
 * [onImagePasteClick] backs a manual "Dán" button (see [PasteButton] below): the rich-editor
 * library this wraps (richeditor-compose, still pre-1.0) doesn't surface Android/iOS's native
 * long-press select/copy/paste gesture for text, and clipboard-image auto-paste-on-focus alone
 * missed the common case of the field already being focused when a new image gets copied -- a
 * button the user can always tap is the reliable fallback for both.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookRichEditor(
    modifier: Modifier = Modifier,
    initialHtml: String?,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    onHtmlChange: (String) -> Unit,
    onPlainTextChange: (String) -> Unit,
    onImagePasteClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    // `RichTextState.setHtml()` only reliably updates what's actually drawn when it runs during
    // this editor's first composition (the initialHtml path below) -- calling it later, e.g. from
    // the "Dán" button after the field is already mounted, updates state.annotatedString/toHtml()
    // (confirmed via logging) but the visible BasicTextField never redraws. Rather than fight that,
    // a text paste goes through the exact same reliable path a fresh note-open uses: bump
    // `remountKey` to fully discard and recreate the state with the new content as its initial
    // value. `pendingHtml` tracks what that recreation should hydrate from -- it starts at
    // [initialHtml] and only a text-paste ever reassigns it.
    var remountKey by remember { mutableStateOf(0) }
    var pendingHtml by remember { mutableStateOf(initialHtml) }
    var lastEditorPlainText by remember { mutableStateOf("") }
    // A paste triggers ensureNoteSaved() in the caller (a brand-new note has no id yet), which
    // changes currentNoteId, which makes NoteDetailScreen re-run richContentViewModel.load(id) for
    // this now-real id -- but the paste's own HTML save is debounced (~500ms) and hasn't reached
    // the DB yet, so that reload reads back nothing and this effect would stomp the just-pasted
    // pendingHtml with null moments after setting it. Once a paste has happened locally, ignore any
    // further initialHtml updates from the parent for the rest of this screen's lifetime -- this
    // editor is now the source of truth for its own content, same as a normal BasicTextField.
    var hasLocalEdit by remember { mutableStateOf(false) }

    LaunchedEffect(initialHtml) {
        if (!hasLocalEdit) pendingHtml = initialHtml
    }

    key(remountKey) {
        EditorContent(
            modifier = modifier,
            initialHtml = pendingHtml,
            focusRequester = focusRequester,
            onFocusChange = onFocusChange,
            onHtmlChange = onHtmlChange,
            onPlainTextChange = {
                lastEditorPlainText = it
                onPlainTextChange(it)
            },
            onPasteClick = {
                focusRequester.requestFocus()
                val text = clipboardManager.getText()?.text
                if (!text.isNullOrEmpty()) {
                    hasLocalEdit = true
                    val combined = if (lastEditorPlainText.isEmpty()) {
                        text
                    } else {
                        "$lastEditorPlainText\n$text"
                    }
                    val html = "<p>${escapeHtml(combined)}</p>"
                    pendingHtml = html
                    onHtmlChange(html)
                    onPlainTextChange(combined)
                    remountKey++
                }
                onImagePasteClick()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorContent(
    modifier: Modifier,
    initialHtml: String?,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    onHtmlChange: (String) -> Unit,
    onPlainTextChange: (String) -> Unit,
    onPasteClick: () -> Unit
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
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = if (isFocused) 0.dp else 8.dp)
        ) {
            PasteButton(onClick = onPasteClick)
        }
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

private fun escapeHtml(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\n", "<br>")

@Composable
private fun PasteButton(onClick: () -> Unit) {
    val colors = LocalCustomColors.current
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.accentSoft)
    ) {
        Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Dán", tint = colors.accent)
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
