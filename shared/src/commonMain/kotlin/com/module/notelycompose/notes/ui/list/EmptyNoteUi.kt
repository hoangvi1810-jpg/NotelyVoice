package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.ic_empty_notes
import org.jetbrains.compose.resources.painterResource
import com.module.notelycompose.resources.empty_list_title
import com.module.notelycompose.resources.empty_list_description
import com.module.notelycompose.resources.empty_list_description_tablet
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmptyNoteUi(
    isTablet: Boolean
) {
    val colors = LocalCustomColors.current
    val emptyNoteDescStr = if(isTablet) {
        stringResource(Res.string.empty_list_description_tablet)
    } else {
        stringResource(Res.string.empty_list_description)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .offset(y = (-40).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_empty_notes),
            contentDescription = "No Notes",
            modifier = Modifier.size(140.dp),
            tint = colors.accentSoft
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.empty_list_title),
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = emptyNoteDescStr,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
