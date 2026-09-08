package com.module.notelycompose.notes.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Named elevation steps, replacing the ad-hoc 0/2/4dp scattered across screens. */
object Elevation {
    val none = 0.dp
    val card = 2.dp
    val raised = 6.dp
    val overlay = 12.dp
}

/**
 * A wide, low-opacity, color-tinted shadow — the reference design's "expensive" quality comes
 * from this rather than Material's default hard, neutral-gray shadow. Implemented as a stack of
 * translucent rounded rects rather than [androidx.compose.ui.draw.shadow], so it renders
 * identically on Android and iOS/Skia with no platform-specific behavior to verify (there is no
 * Mac available to verify iOS this round — see the plan's iOS risk section).
 *
 * Place BEFORE `.clip()`/`.background()` in the modifier chain so the overflow isn't clipped:
 * `Modifier.softShadow(Elevation.card, AppRadii.lg).clip(AppRadii.shapeLg).background(colors.surface)`
 */
fun Modifier.softShadow(
    elevation: Dp,
    cornerRadius: Dp = AppRadii.lg,
    tint: Color = Color.Unspecified
): Modifier = this.drawBehind {
    if (elevation <= 0.dp) return@drawBehind
    val shadowColor = if (tint.isUnspecified) Palette.ShadowTint else tint
    val elevationPx = elevation.toPx()
    val radiusPx = cornerRadius.toPx()
    val layerCount = 4
    for (i in layerCount downTo 1) {
        val t = i / layerCount.toFloat()
        val spread = elevationPx * t * 0.7f
        val dy = elevationPx * t * 0.8f
        val alpha = (0.035f * (layerCount - i + 1)).coerceIn(0f, 1f)
        drawRoundRect(
            color = shadowColor.copy(alpha = alpha),
            topLeft = Offset(-spread * 0.5f, dy - spread * 0.25f),
            size = Size(size.width + spread, size.height + spread),
            cornerRadius = CornerRadius(radiusPx + spread * 0.3f, radiusPx + spread * 0.3f)
        )
    }
}
