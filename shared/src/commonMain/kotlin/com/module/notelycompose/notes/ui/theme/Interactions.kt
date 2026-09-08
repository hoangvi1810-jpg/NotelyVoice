package com.module.notelycompose.notes.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * How far a target should sink on press. One ratio for every target size looks wrong — the same
 * scale is invisible on a 24dp icon and looks like the screen collapsing on a full-width card.
 */
enum class PressDepth(val scale: Float) {
    Subtle(0.980f),   // note cards, full-width rows, settings rows
    Standard(0.955f), // buttons, chips, FAB, segmented tabs
    Deep(0.910f)      // small icon buttons (<=32dp icons)
}

// Two specs, not one symmetric spring: press-down is fast with no overshoot (feels immediate),
// release is slower with a single gentle overshoot (this single soft settle is the "water drop"
// feel the release-only bounce is what reads as fluid rather than either bouncy or dead/rubbery).
private val PressDownSpec = spring<Float>(dampingRatio = 0.75f, stiffness = 900f)
private val ReleaseSpec = spring<Float>(dampingRatio = 0.62f, stiffness = 380f)

private const val PressedAlpha = 0.92f

/**
 * Soft press feedback for the ~64 IconButton/Button/FloatingActionButton call sites that do not
 * pick up [WaterDropIndication] automatically (Material components pass their own `ripple()`
 * into `clickable` instead of reading [androidx.compose.foundation.LocalIndication]). Uses
 * [androidx.compose.ui.graphics.graphicsLayer] rather than [androidx.compose.ui.draw.scale] so the
 * animation is draw-only and never triggers a measure/layout pass on every frame.
 *
 * Usage: `Button(interactionSource = src, modifier = Modifier.pressScale(src), ...)`
 */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    depth: PressDepth = PressDepth.Standard
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) depth.scale else 1f,
        animationSpec = if (pressed) PressDownSpec else ReleaseSpec,
        label = "pressScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) PressedAlpha else 1f,
        animationSpec = if (pressed) PressDownSpec else ReleaseSpec,
        label = "pressAlpha"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

/** Exposed for [WaterDropIndication], which animates on the same curve as [pressScale]. */
internal val IndicationPressDownSpec get() = PressDownSpec
internal val IndicationReleaseSpec get() = ReleaseSpec
internal const val IndicationPressedAlpha = PressedAlpha
