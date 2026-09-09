package com.module.notelycompose.notes.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.be_vietnam_pro_bold
import com.module.notelycompose.resources.be_vietnam_pro_medium
import com.module.notelycompose.resources.be_vietnam_pro_regular
import com.module.notelycompose.resources.be_vietnam_pro_semibold
import org.jetbrains.compose.resources.Font

/**
 * The app's typeface. Be Vietnam Pro covers the full set of Vietnamese diacritics, which Poppins
 * (used previously) renders with fallback glyphs — the source of the broken-looking headings.
 * All four weights ship, so the type scale gets true Medium and SemiBold rather than the
 * nearest-weight approximation.
 */
@Composable
fun BeVietnamProFontFamily() = FontFamily(
    Font(Res.font.be_vietnam_pro_regular, weight = FontWeight.Normal),
    Font(Res.font.be_vietnam_pro_medium, weight = FontWeight.Medium),
    Font(Res.font.be_vietnam_pro_semibold, weight = FontWeight.SemiBold),
    Font(Res.font.be_vietnam_pro_bold, weight = FontWeight.Bold)
)
