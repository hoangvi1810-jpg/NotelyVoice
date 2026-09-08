package com.module.notelycompose.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.AppRadii
import com.module.notelycompose.notes.ui.theme.Elevation
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.notes.ui.theme.PressDepth
import com.module.notelycompose.notes.ui.theme.pressScale
import com.module.notelycompose.notes.ui.theme.softShadow

/**
 * A pill-shaped filter chip: selected = solid near-black fill (matches the reference design),
 * unselected = white with a hairline border. Replaces the ScrollableTabRow-based FilterSelection,
 * which had no visible focus/selection styling of its own beyond a thin border box.
 */
@Composable
fun NotelyChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val colors = LocalCustomColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val bg by animateColorAsState(
        targetValue = if (selected) colors.chipSelectedBg else colors.chipUnselectedBg,
        animationSpec = tween(220),
        label = "chipBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) colors.chipSelectedFg else colors.chipUnselectedFg,
        animationSpec = tween(220),
        label = "chipFg"
    )

    Row(
        modifier = modifier
            .pressScale(interactionSource, PressDepth.Standard)
            .let { if (selected) it.softShadow(Elevation.card, cornerRadius = 20.dp) else it }
            .clip(AppRadii.pill)
            .background(bg)
            .let { if (!selected) it.border(1.dp, colors.outline, AppRadii.pill) else it }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
