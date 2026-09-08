package com.module.notelycompose.notes.ui.extensions

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.SoftwareKeyboardController

fun FocusRequester.showKeyboard(
    imeVisible: Boolean,
    keyboardController: SoftwareKeyboardController?
) {
    if(imeVisible) {
        keyboardController?.hide()
    } else {
        try {
            // Throws IllegalStateException if the note editor's BasicTextField isn't currently
            // part of the composition (e.g. this button was tapped from a tab other than
            // Transcript) — real crash observed on-device, see git history for the log.
            requestFocus()
        } catch (e: IllegalStateException) {
            // Nothing to focus; fall through to just showing the keyboard.
        }
        keyboardController?.show()
    }
}
