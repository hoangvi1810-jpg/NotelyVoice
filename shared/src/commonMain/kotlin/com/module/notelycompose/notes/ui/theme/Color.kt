package com.module.notelycompose.notes.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.compositionLocalOf

val DarkCustomColors = CustomColors(
    sortAscendingIconColor = Color(0xFFD9B98A), // was purple 0xFF8514CB — warm tan, visible on black
    backgroundViewColor = Color.Black, // // Color(0xFF181818),
    dateContentColorViewColor = Color.White,
    dateContentIconColor = Color(0xFFCCCCCC),
    bottomBarBackgroundColor = Color.White,
    bottomBarIconColor = Color(0xFFD9B98A), // was purple 0xFF8514CB
    noteListBackgroundColor = Color(0xFFEDE3D0), // was 0xFFEEEEEE — warm beige instead of neutral gray
    bodyBackgroundColor = Color.Black,  // Color(0xFF181818),
    onBodyColor = Color(0xFFF5F5F5),
    bodyContentColor = Color.White,
    contentTopColor = Color.White,
    floatActionButtonBorderColor = Color.White,
    floatActionButtonIconColor = Color.White,
    searchOutlinedTextFieldColor = Color(0xFFCCCCCC),
    topButtonIconColor = Color.White,
    noteTextColor = Color.Black,
    noteIconColor = Color.Black,
    iOSBackButtonColor = Color(0xFF3074F6),
    transparentColor = Color.Transparent,
    bottomFormattingContainerColor = Color(0xFFF2F2F2),
    bottomFormattingContentColor = Color.Black,
    activeThumbTrackColor = Color(0xFF666666),
    playerBoxBackgroundColor = Color(0xFFF2F2F2),
    starredColor = Color.Blue,
    settingsIconColor = Color(0xFFCCCCCC),
    settingCancelBackgroundColor = Color(0xFF333333),
    settingCancelTextColor = Color.White,
    settingLanguageBackgroundColor = Color(0xFF222222),
    languageSearchBorderColor = Color.Gray,
    languageSearchCancelButtonColor = Color.Gray,
    languageSearchCancelIconTintColor = Color.White,
    languageListHeaderColor = Color.Gray,
    languageListTextColor = Color.White,
    languageListBackgroundColor = Color(0xFF2C2C2E),
    languageListDividerColor = Color.Gray,
    languageSearchUnfocusedColor = Color.White,
    shareDialogBackgroundColor = Color(0xFF333333),
    shareDialogButtonColor = Color.White,
    statusBarBackgroundColor = Color(0xFFFFFAD0),
    settingsBodyTextColor = Color.Gray,
    settingsBodyBorderColor = Color.DarkGray,
    selectAllCheckboxColor = Color(0xFFBD7D4E),
    selectAllCancelColor = Color.Gray,
    modelSelectionDescColor = Color.LightGray,
    modelSelectionBgColor = Color.Black
)

val LightCustomColors = CustomColors(
    sortAscendingIconColor = Color(0xFF9C6B41), // was purple 0xFFA260CC — caramel accent
    backgroundViewColor = Color(0xFFFFFFFF),
    dateContentColorViewColor = Color.Black,
    dateContentIconColor = Color(0xFF1E1E24),
    bottomBarBackgroundColor = Color(0xFFFFFFFF),
    bottomBarIconColor = Color.White,
    noteListBackgroundColor = Color(0xFFF6ECDA), // was lavender 0xFFF4E7F9 — warm beige
    bodyBackgroundColor = Color(0xFFFFFFFF),
    onBodyColor = Color(0xFF212121),
    contentTopColor = Color.Black,
    bodyContentColor = Color.Black,
    floatActionButtonBorderColor = Color.Black,
    floatActionButtonIconColor = Color.Black,
    searchOutlinedTextFieldColor = Color.Black,
    topButtonIconColor = Color.Black,
    noteTextColor = Color.White,
    noteIconColor = Color.White,
    iOSBackButtonColor = Color(0xFF3074F6),
    transparentColor = Color.Transparent,
    bottomFormattingContainerColor = Color(0xFFF2F2F2),
    bottomFormattingContentColor = Color.Black,
    activeThumbTrackColor = Color(0xFF666666),
    playerBoxBackgroundColor = Color(0xFFEEEEEE),
    starredColor = Color.Blue,
    settingsIconColor = Color.Black,
    settingCancelBackgroundColor = Color(0xFFEEEEEE),
    settingCancelTextColor = Color.Black,
    settingLanguageBackgroundColor = Color(0xFFEEEEEE),
    languageSearchBorderColor = Color.Black,
    languageSearchCancelButtonColor = Color(0xFFCCCCCC),
    languageSearchCancelIconTintColor = Color.Black,
    languageListHeaderColor = Color.Black,
    languageListTextColor = Color.Black,
    languageListBackgroundColor = Color(0xFFEEEEEE),
    languageListDividerColor = Color.Black.copy(alpha = 0.3f),
    languageSearchUnfocusedColor = Color.Black,
    shareDialogBackgroundColor = Color.White,
    shareDialogButtonColor = Color(0xFF333333),
    statusBarBackgroundColor = Color(0xFFFFFAD0),
    settingsBodyTextColor = Color.DarkGray,
    settingsBodyBorderColor = Color.LightGray,
    selectAllCheckboxColor = Color(0xFFBD7D4E),
    selectAllCancelColor = Color.White,
    modelSelectionDescColor = Color.DarkGray,
    modelSelectionBgColor = Color.White
)

// Create a CompositionLocal to hold the custom colors
val LocalCustomColors = compositionLocalOf {
    LightCustomColors // Default value for custom colors
}
