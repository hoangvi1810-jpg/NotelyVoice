package com.module.notelycompose.notes.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/**
 * Material 3 ColorScheme, derived from the same [Palette] as [LightCustomColors]/[DarkCustomColors].
 * Supplying this fixes the app's core theming bug: every M3 component (Card, Button,
 * OutlinedTextField, Slider, Checkbox, AlertDialog...) previously had no M3 MaterialTheme
 * ancestor and silently rendered the stock baseline light scheme (default Google purple),
 * ignoring both the app's palette and dark mode.
 */
val LightAppColorScheme = lightColorScheme(
    primary = Palette.Violet600,
    onPrimary = Palette.White,
    primaryContainer = Palette.Violet100,
    onPrimaryContainer = Palette.Ink900,
    secondary = Palette.Violet500,
    onSecondary = Palette.White,
    background = Palette.Violet050,
    onBackground = Palette.Ink900,
    surface = Palette.White,
    onSurface = Palette.Ink900,
    surfaceVariant = Palette.Sunken,
    onSurfaceVariant = Palette.Ink400,
    outline = Palette.Outline,
    outlineVariant = Palette.Outline,
    error = Palette.Danger,
    onError = Palette.White,
    errorContainer = Palette.DangerSoft,
    onErrorContainer = Palette.Danger
)

val DarkAppColorScheme = darkColorScheme(
    primary = Palette.DarkAccent,
    onPrimary = Palette.DarkOnAccent,
    primaryContainer = Palette.DarkAccentSoft,
    onPrimaryContainer = Palette.DarkOnSurface,
    secondary = Palette.DarkAccent,
    onSecondary = Palette.DarkOnAccent,
    background = Palette.DarkBackground,
    onBackground = Palette.DarkOnSurface,
    surface = Palette.DarkSurface,
    onSurface = Palette.DarkOnSurface,
    surfaceVariant = Palette.DarkSunken,
    onSurfaceVariant = Palette.DarkOnSurfaceVariant,
    outline = Palette.DarkOutline,
    outlineVariant = Palette.DarkOutline,
    error = Palette.DarkDanger,
    onError = Palette.DarkOnAccent,
    errorContainer = Palette.DarkDangerSoft,
    onErrorContainer = Palette.DarkDanger
)
