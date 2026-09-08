package com.module.notelycompose.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.ui.theme.PressDepth
import com.module.notelycompose.notes.ui.theme.pressScale
import com.module.notelycompose.notes.ui.theme.softShadow

/**
 * The primary FAB, restyled from an near-invisible white-on-white/black circle (the old
 * `backgroundColor = LocalCustomColors.current.backgroundViewColor`) to a solid accent circle with
 * a soft accent-tinted shadow and the same press feel as the rest of the redesign.
 */
@Composable
fun NotelyFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalCustomColors.current.accent,
    contentColor: Color = LocalCustomColors.current.onAccent,
    size: Dp = 60.dp,
    cornerRadius: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape: Shape = RoundedCornerShape(cornerRadius)

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .softShadow(elevation = com.module.notelycompose.notes.ui.theme.Elevation.raised, cornerRadius = cornerRadius, tint = containerColor.copy(alpha = 0.35f))
            .pressScale(interactionSource, PressDepth.Standard),
        interactionSource = interactionSource,
        backgroundColor = containerColor,
        contentColor = contentColor,
        shape = shape,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        content()
    }
}
