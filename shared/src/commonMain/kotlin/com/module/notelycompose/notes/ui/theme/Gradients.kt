package com.module.notelycompose.notes.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The app's beige/caramel gradient identity (replacing the old flat purple accents in
 * MyApplicationTheme/CustomColors). Meant for accent surfaces that want to stand out from the
 * flat body background — the tab bar, the "Ask AI" button, and the audio player bar from the
 * planned 4-tab note detail screen (AI Note / Highlights / Summary / Transcript) — rather than
 * for large body/background areas, where a flat color reads better and is cheaper to keep legible
 * across both themes.
 */
val BeigeGradientLight = Brush.verticalGradient(
    colors = listOf(Color(0xFFF7EFE1), Color(0xFFE9D9BE))
)

val BeigeGradientDark = Brush.verticalGradient(
    colors = listOf(Color(0xFF3A2E20), Color(0xFF241C13))
)

/** Warm caramel gradient for filled accent surfaces (buttons), readable in both themes. */
val CaramelGradientLight = Brush.horizontalGradient(
    colors = listOf(Color(0xFFB08D5E), Color(0xFF9C6B41))
)

val CaramelGradientDark = Brush.horizontalGradient(
    colors = listOf(Color(0xFFD9B98A), Color(0xFFB08D5E))
)

fun beigeGradient(darkTheme: Boolean): Brush = if (darkTheme) BeigeGradientDark else BeigeGradientLight

fun caramelGradient(darkTheme: Boolean): Brush = if (darkTheme) CaramelGradientDark else CaramelGradientLight
