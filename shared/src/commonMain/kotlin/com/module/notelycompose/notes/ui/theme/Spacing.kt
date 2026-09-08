package com.module.notelycompose.notes.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Plain Dp constants (not a CompositionLocal — spacing never changes at runtime, so there's
 * nothing to provide or recompose). Screen gutter is 20dp (matches NoteList.kt's existing
 * horizontal padding), card inner padding is 20dp, section gap is 24dp.
 */
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}
