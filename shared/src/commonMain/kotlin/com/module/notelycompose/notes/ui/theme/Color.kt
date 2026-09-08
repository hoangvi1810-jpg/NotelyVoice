package com.module.notelycompose.notes.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// All values below are re-pointed at Palette.kt (the single source of truth) so this file and
// the M3 ColorScheme in ColorScheme.kt cannot drift apart. Legacy field names are kept verbatim
// for the ~40 existing call sites; new semantic roles are appended at the bottom of each block.

val DarkCustomColors = CustomColors(
    sortAscendingIconColor = Palette.DarkAccent,
    backgroundViewColor = Palette.DarkSurface,
    dateContentColorViewColor = Palette.DarkOnSurface,
    dateContentIconColor = Palette.DarkOnSurfaceVariant,
    bottomBarBackgroundColor = Palette.DarkSurface, // was Color.White — inverted bug in dark mode
    bottomBarIconColor = Palette.DarkAccent,
    noteListBackgroundColor = Palette.DarkSunken, // was a light beige — inverted bug in dark mode
    bodyBackgroundColor = Palette.DarkBackground,
    onBodyColor = Palette.DarkOnSurface,
    bodyContentColor = Palette.DarkOnSurface,
    contentTopColor = Palette.DarkOnSurface,
    floatActionButtonBorderColor = Palette.DarkOutline,
    floatActionButtonIconColor = Palette.DarkOnAccent,
    searchOutlinedTextFieldColor = Palette.DarkOutline,
    topButtonIconColor = Palette.DarkOnSurface,
    noteTextColor = Palette.DarkOnSurface, // was Color.Black — inverted bug in dark mode
    noteIconColor = Palette.DarkOnSurface,
    iOSBackButtonColor = Color(0xFF3074F6),
    transparentColor = Color.Transparent,
    bottomFormattingContainerColor = Palette.DarkSunken,
    bottomFormattingContentColor = Palette.DarkOnSurface,
    activeThumbTrackColor = Palette.DarkAccent,
    playerBoxBackgroundColor = Palette.DarkSunken, // was a light-gray bar — inverted bug in dark mode
    starredColor = Palette.Star,
    settingsIconColor = Palette.DarkOnSurfaceVariant,
    settingCancelBackgroundColor = Palette.DarkSunken,
    settingCancelTextColor = Palette.DarkOnSurface,
    settingLanguageBackgroundColor = Palette.DarkSunken,
    languageSearchBorderColor = Palette.DarkOutline,
    languageSearchCancelButtonColor = Palette.DarkOutline,
    languageSearchCancelIconTintColor = Palette.DarkOnSurface,
    languageListHeaderColor = Palette.DarkOnSurfaceVariant,
    languageListTextColor = Palette.DarkOnSurface,
    languageListBackgroundColor = Palette.DarkSurface,
    languageListDividerColor = Palette.DarkOutline,
    languageSearchUnfocusedColor = Palette.DarkOnSurface,
    shareDialogBackgroundColor = Palette.DarkSurface,
    shareDialogButtonColor = Palette.DarkOnSurface,
    statusBarBackgroundColor = Palette.DarkBackground, // was cream in both themes
    settingsBodyTextColor = Palette.DarkOnSurfaceVariant,
    settingsBodyBorderColor = Palette.DarkOutline,
    selectAllCheckboxColor = Palette.DarkAccent,
    selectAllCancelColor = Palette.DarkOnSurfaceVariant,
    modelSelectionDescColor = Palette.DarkOnSurfaceVariant,
    modelSelectionBgColor = Palette.DarkBackground,

    surface = Palette.DarkSurface,
    surfaceElevated = Palette.DarkSurface,
    surfaceSunken = Palette.DarkSunken,
    onSurface = Palette.DarkOnSurface,
    onSurfaceVariant = Palette.DarkOnSurfaceVariant,
    outline = Palette.DarkOutline,
    accent = Palette.DarkAccent,
    onAccent = Palette.DarkOnAccent,
    accentSoft = Palette.DarkAccentSoft,
    chipSelectedBg = Palette.DarkAccent,
    chipSelectedFg = Palette.DarkOnAccent,
    chipUnselectedBg = Palette.DarkChipUnselectedBg,
    chipUnselectedFg = Palette.DarkChipUnselectedFg,
    heroGradientStart = Palette.HeroStart,
    heroGradientEnd = Palette.HeroEnd,
    danger = Palette.DarkDanger,
    onDanger = Palette.White,
    dangerSoft = Palette.DarkDangerSoft,
    success = Palette.Success,
    shadowTint = Palette.DarkShadowTint
)

val LightCustomColors = CustomColors(
    sortAscendingIconColor = Palette.Violet600,
    backgroundViewColor = Palette.White,
    dateContentColorViewColor = Palette.Ink900,
    dateContentIconColor = Palette.Ink900,
    bottomBarBackgroundColor = Palette.White,
    bottomBarIconColor = Palette.Violet600, // was Color.White on a white bg — invisible bug
    noteListBackgroundColor = Palette.Sunken,
    bodyBackgroundColor = Palette.Violet050,
    onBodyColor = Palette.Ink900,
    contentTopColor = Palette.Ink900,
    bodyContentColor = Palette.Ink900,
    floatActionButtonBorderColor = Palette.Outline,
    floatActionButtonIconColor = Palette.White,
    searchOutlinedTextFieldColor = Palette.Outline,
    topButtonIconColor = Palette.Ink900,
    noteTextColor = Palette.White,
    noteIconColor = Palette.White,
    iOSBackButtonColor = Color(0xFF3074F6),
    transparentColor = Color.Transparent,
    bottomFormattingContainerColor = Palette.Sunken,
    bottomFormattingContentColor = Palette.Ink900,
    activeThumbTrackColor = Palette.Violet600,
    playerBoxBackgroundColor = Palette.Sunken,
    starredColor = Palette.Star,
    settingsIconColor = Palette.Ink900,
    settingCancelBackgroundColor = Palette.Sunken,
    settingCancelTextColor = Palette.Ink900,
    settingLanguageBackgroundColor = Palette.Sunken,
    languageSearchBorderColor = Palette.Outline,
    languageSearchCancelButtonColor = Palette.Outline,
    languageSearchCancelIconTintColor = Palette.Ink900,
    languageListHeaderColor = Palette.Ink600,
    languageListTextColor = Palette.Ink900,
    languageListBackgroundColor = Palette.Sunken,
    languageListDividerColor = Palette.Outline,
    languageSearchUnfocusedColor = Palette.Ink900,
    shareDialogBackgroundColor = Palette.White,
    shareDialogButtonColor = Palette.Ink900,
    statusBarBackgroundColor = Palette.Violet050, // was a cream that matched nothing
    settingsBodyTextColor = Palette.Ink400,
    settingsBodyBorderColor = Palette.Outline,
    selectAllCheckboxColor = Palette.Violet600,
    selectAllCancelColor = Palette.White,
    modelSelectionDescColor = Palette.Ink400,
    modelSelectionBgColor = Palette.White,

    surface = Palette.White,
    surfaceElevated = Palette.White,
    surfaceSunken = Palette.Sunken,
    onSurface = Palette.Ink900,
    onSurfaceVariant = Palette.Ink400,
    outline = Palette.Outline,
    accent = Palette.Violet600,
    onAccent = Palette.White,
    accentSoft = Palette.Violet100,
    chipSelectedBg = Palette.Ink900,
    chipSelectedFg = Palette.White,
    chipUnselectedBg = Palette.White,
    chipUnselectedFg = Palette.Ink600,
    heroGradientStart = Palette.HeroStart,
    heroGradientEnd = Palette.HeroEnd,
    danger = Palette.Danger,
    onDanger = Palette.White,
    dangerSoft = Palette.DangerSoft,
    success = Palette.Success,
    shadowTint = Palette.ShadowTint
)

// static: colors only change on a theme switch (which recomposes everything anyway), so there's
// no need to pay for per-read recomposition tracking.
val LocalCustomColors = staticCompositionLocalOf {
    LightCustomColors
}
