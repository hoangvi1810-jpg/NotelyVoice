package com.module.notelycompose.notes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The app's type scale, replacing the ~103 inline `fontSize = X.sp` call sites that previously had
 * no shared system. Built on [PoppingsFontFamily].
 *
 * NOTE: only poppins_regular (400) and poppins_bold (700) ship today — poppins_medium (500) and
 * poppins_semibold (600) were not available to add in this pass (no network access to fetch the
 * font binaries). Compose's weight-matching resolves the missing weights to the nearest available
 * one (500->400, 600->700), so [FontWeight.Medium]/[FontWeight.SemiBold] below render correctly
 * today, just visually closer to Normal/Bold than a true middle weight. Drop poppins_medium.ttf
 * and poppins_semibold.ttf into commonMain/composeResources/font/ and add them to
 * [PoppingsFontFamily] to sharpen this scale later — no call site here needs to change.
 */
@Composable
fun appTypography(): Typography {
    val poppins = PoppingsFontFamily()
    return Typography(
        displaySmall = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
        headlineLarge = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.4).sp),
        headlineMedium = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp),
        headlineSmall = TextStyle(fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
        titleLarge = TextStyle(fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
        titleMedium = TextStyle(fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = TextStyle(fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
        labelLarge = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp),
        labelMedium = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
        labelSmall = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp)
    )
}
