package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.module.notelycompose.ui.components.NotelyChip

/**
 * The filter pills (All / Starred / Voices / Recent). Previously an M2 ScrollableTabRow whose
 * selected-tab indicator was a bare 2dp border box and whose labels had no fontSize/fontWeight at
 * all — replaced with pill chips matching the reference design (selected = solid dark fill,
 * unselected = white with a hairline border).
 */
@Composable
fun FilterSelection(
    titles: List<String>,
    icons: List<ImageVector>,
    tabSelected: String,
    onTabSelected: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        itemsIndexed(titles) { index, title ->
            NotelyChip(
                text = title,
                icon = icons.getOrNull(index),
                selected = title == tabSelected,
                onClick = {
                    onTabSelected(title)
                    focusManager.clearFocus()
                }
            )
        }
    }
}
