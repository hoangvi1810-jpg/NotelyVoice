package com.module.notelycompose.notes.ui.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme as MaterialTheme2
import androidx.compose.material.Typography as Typography2
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme as MaterialTheme3
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// M2 primary/secondary, kept alive only for the files not yet migrated to M3 (see the plan's
// "hybrid, not a wholesale migration" decision) — re-pointed at the same Palette.kt as everything
// else so the two theme systems cannot drift the way M2 defaults and the old beige palette did.
// background/surface are set explicitly too: M2's lightColors()/darkColors() default them to
// plain white/near-black, which leaks through any M2 Scaffold/Surface that doesn't paint its own
// background Modifier (found the hard way — NoteDetailScreen.kt's tab content Column showed stock
// white instead of the lavender canvas until this was added).
private val LightColorPaletteM2 = lightColors(
    primary = Palette.Violet600,
    primaryVariant = Palette.Violet700,
    secondary = Palette.Violet500,
    background = Palette.Violet050,
    surface = Palette.White,
    onBackground = Palette.Ink900,
    onSurface = Palette.Ink900
)

private val DarkColorPaletteM2 = darkColors(
    primary = Palette.DarkAccent,
    primaryVariant = Palette.Violet700,
    secondary = Palette.DarkAccent,
    background = Palette.DarkBackground,
    surface = Palette.DarkSurface,
    onBackground = Palette.DarkOnSurface,
    onSurface = Palette.DarkOnSurface
)

/**
 * The app previously had exactly one `MaterialTheme` in the whole codebase — the Material 2 one
 * below — while ~32 files use Material 3 components. Those M3 components had no M3 MaterialTheme
 * ancestor at all, so they silently rendered the stock baseline light scheme (default Google
 * purple), ignoring both the app's palette and dark mode. This is the single biggest cause of the
 * "inconsistent/cheap" look.
 *
 * Fix: add a real M3 MaterialTheme wrapper, nested with the M2 one, both fed from the same
 * [Palette]/[appTypography]. Per the plan this is a deliberate hybrid, not a one-shot migration —
 * M2 files get moved to M3 individually as each is visually reworked in later checkpoints, and
 * `implementation(compose.material)` is only removed from the build once the last file is clean.
 * Nesting order between the two MaterialTheme composables does not matter: M2's `LocalContentColor`
 * (androidx.compose.material) and M3's `LocalContentColor` (androidx.compose.material3) are
 * distinct CompositionLocal keys in different packages, so neither shadows the other.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors
    val m3ColorScheme = if (darkTheme) DarkAppColorScheme else LightAppColorScheme
    val m2Colors = if (darkTheme) DarkColorPaletteM2 else LightColorPaletteM2
    val font = BeVietnamProFontFamily()
    val typography = appTypography()

    // Legacy M2 typography slots, covering the handful of remaining `MaterialTheme.typography.*`
    // (M2) call sites so they read the app typeface + the purple palette too, ahead of their own
    // migration.
    val legacyTypography = Typography2(
        h6 = TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
        body1 = TextStyle(fontFamily = font, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        body2 = TextStyle(fontFamily = font, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        caption = TextStyle(fontFamily = font, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp)
    )

    CompositionLocalProvider(
        LocalCustomColors provides customColors,
        LocalIndication provides WaterDropIndication
    ) {
        // Experimental: material3 1.3.x only. Suppresses M3's built-in ripple app-wide so Material
        // components render no indication of their own — WaterDropIndication (clickable sites) and
        // Modifier.pressScale (Button/IconButton/FAB sites) then supply the whole press feel
        // uniformly, instead of a mix of ripple-here/water-drop-there. If this API is removed in a
        // future material3 bump, it fails at compile time (the safe kind of breakage).
        @OptIn(ExperimentalMaterial3Api::class)
        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            MaterialTheme3(
                colorScheme = m3ColorScheme,
                typography = typography,
                shapes = AppShapes
            ) {
                MaterialTheme2(
                    colors = m2Colors,
                    typography = legacyTypography,
                    shapes = androidx.compose.material.Shapes(
                        small = AppRadii.shapeSm,
                        medium = AppRadii.shapeMd,
                        large = AppRadii.shapeXl
                    ),
                    content = content
                )
            }
        }
    }
}
