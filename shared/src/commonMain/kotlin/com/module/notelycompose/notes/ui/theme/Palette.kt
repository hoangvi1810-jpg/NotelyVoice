package com.module.notelycompose.notes.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Single source of truth for every raw color literal in the app. Both the legacy [CustomColors]
 * instances (Color.kt) and the Material 3 [androidx.compose.material3.ColorScheme] (ColorScheme.kt)
 * point at these constants, so the two theme systems cannot drift apart the way M2 defaults and
 * the custom beige palette did before this file existed.
 */
object Palette {
    // Light — premium lavender/violet, modeled on the reference AirPods-style e-commerce app.
    val Violet600 = Color(0xFF6C4CF1) // primary accent
    val Violet700 = Color(0xFF5A38DC) // pressed/active accent
    val Violet500 = Color(0xFF8468F5)
    val Violet100 = Color(0xFFE4DCFD) // accent-soft fills (chips, badges)
    val Violet050 = Color(0xFFF3F0FB) // app canvas
    val Sunken = Color(0xFFECE8F8)    // search field / segmented-control track
    val White = Color(0xFFFFFFFF)     // cards
    val Ink900 = Color(0xFF1A1330)    // primary text, selected-chip fill
    val Ink600 = Color(0xFF4A4360)    // unselected chip label
    val Ink400 = Color(0xFF6B6480)    // secondary text
    val Outline = Color(0xFFE3DEF3)   // hairlines, unselected chip border
    val HeroStart = Color(0xFF6D45E8) // recording button gradient
    val HeroEnd = Color(0xFF3B1E93)
    val Danger = Color(0xFFE5484D)
    val DangerSoft = Color(0xFFFDECEC)
    val Success = Color(0xFF1F9D55)
    val Star = Color(0xFFF5A623)
    val ShadowTint = Color(0xFF2A1A6B) // used at 8-12% alpha, never plain black

    // Dark — functional only this round (not beautified), per product decision.
    val DarkBackground = Color(0xFF121016)
    val DarkSurface = Color(0xFF1C1826)
    val DarkSunken = Color(0xFF16131E)
    val DarkOnSurface = Color(0xFFECE9F5)
    val DarkOnSurfaceVariant = Color(0xFFA49FB8)
    val DarkOutline = Color(0xFF2E2840)
    val DarkAccent = Color(0xFFA48BFF)
    val DarkOnAccent = Color(0xFF17102E)
    val DarkAccentSoft = Color(0xFF2A2140)
    val DarkChipUnselectedBg = Color(0xFF241E33)
    val DarkChipUnselectedFg = Color(0xFFC9C3DC)
    val DarkDanger = Color(0xFFFF6B6E)
    val DarkDangerSoft = Color(0xFF3A1F22)
    val DarkShadowTint = Color(0xFF000000) // used at 40% alpha
}
