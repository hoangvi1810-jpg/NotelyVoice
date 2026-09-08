package com.module.notelycompose.notes.ui.theme

import androidx.compose.ui.graphics.Brush

/**
 * The app's deep-violet "hero" gradient — used for accent surfaces that should stand out from the
 * flat body background, chiefly the recording button (see the plan's P2 RecordingScreen rework).
 * Not meant for large body/background areas, where the flat [Palette.Violet050] canvas reads
 * better and stays legible across both themes.
 */
val HeroGradientBrush = Brush.linearGradient(
    colors = listOf(Palette.HeroStart, Palette.HeroEnd)
)

fun heroGradient(): Brush = HeroGradientBrush
