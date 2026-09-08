package com.module.notelycompose.notes.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.launch

/**
 * The app-wide "water drop" press feedback: content sinks slightly on a soft spring, plus a
 * translucent violet bloom that spreads from the touch point and fades — provided once via
 * `LocalIndication` in MyApplicationTheme, this reaches the ~44 plain `.clickable`/
 * `.combinedClickable` call sites in the app for free (see Interactions.kt for the spring specs
 * shared with `Modifier.pressScale`, which covers the Material components — Card, Button,
 * IconButton, FAB... — that pass their own `ripple()` into `clickable` and never read
 * LocalIndication in the first place).
 *
 * Deliberately scale-only (no content-alpha dimming here, unlike `pressScale`): this indication
 * sits on every plain clickable in the app, including recycled list rows, so it stays on the fast
 * path (no offscreen alpha layer) — the alpha treatment lives on `pressScale` for the ~64 explicit
 * button/icon-button/FAB sites instead, where it's a much smaller surface.
 *
 * Implemented as an `object` (not a class) so equals/hashCode are trivially stable, which
 * IndicationNodeFactory relies on to avoid recreating nodes unnecessarily.
 */
object WaterDropIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        WaterDropIndicationNode(interactionSource)

    // Indication declares these as abstract (not merely inherited from Any), so an explicit
    // override is required even for a singleton object.
    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = -1
}

private class WaterDropIndicationNode(
    private val interactionSource: InteractionSource
) : Modifier.Node(), DrawModifierNode {

    private val scale = Animatable(1f)
    private val bloomAlpha = Animatable(0f)
    private var bloomCenter: Offset = Offset.Zero

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        bloomCenter = interaction.pressPosition
                        launch { scale.animateTo(PressDepth.Standard.scale, IndicationPressDownSpec) }
                        bloomAlpha.snapTo(0f)
                        launch { bloomAlpha.animateTo(0.10f, tween(160)) }
                    }
                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        launch { scale.animateTo(1f, IndicationReleaseSpec) }
                        launch { bloomAlpha.animateTo(0f, tween(320, easing = FastOutSlowInEasing)) }
                    }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val s = scale.value
        scale(s, s, pivot = center) {
            this@draw.drawContent()
        }
        if (bloomAlpha.value > 0f) {
            drawCircle(
                color = Palette.Violet600.copy(alpha = bloomAlpha.value),
                radius = size.maxDimension * 0.6f,
                center = bloomCenter
            )
        }
    }
}
