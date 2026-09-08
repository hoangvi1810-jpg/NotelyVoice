package com.module.notelycompose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.module.notelycompose.notes.ui.theme.AppRadii
import com.module.notelycompose.notes.ui.theme.Elevation
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.ui.theme.PressDepth
import com.module.notelycompose.notes.ui.theme.pressScale
import com.module.notelycompose.notes.ui.theme.softShadow

/**
 * A white surface card floating on the app's lavender canvas: soft tinted shadow instead of
 * Material's default hard gray one, and (when clickable) the "water drop" press feel from the
 * redesign plan baked in by construction, so a new call site can never forget it.
 */
@Composable
fun NotelyCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = AppRadii.lg,
    containerColor: Color = LocalCustomColors.current.surface,
    elevation: Dp = Elevation.card,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    pressDepth: PressDepth = PressDepth.Subtle,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .let { if (onClick != null) it.pressScale(interactionSource, pressDepth) else it }
            .softShadow(elevation, cornerRadius)
            .clip(shape)
            .background(containerColor)
            .let {
                if (onClick != null) {
                    it.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else it
            }
    ) {
        content()
    }
}
