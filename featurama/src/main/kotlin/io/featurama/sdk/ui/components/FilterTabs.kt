package io.featurama.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import io.featurama.sdk.ui.strings.FeaturamaStrings
import io.featurama.sdk.ui.theme.FeaturamaTheme

internal data class FilterItem(val key: String, val label: String)

@Composable
internal fun FilterTabs(
    theme: FeaturamaTheme,
    strings: FeaturamaStrings,
    activeFilter: String,
    onFilterChanged: (String) -> Unit,
) {
    val filters = listOf(
        FilterItem("new", strings.filterNew),
        FilterItem("in_progress", strings.filterInProgress),
        FilterItem("done", strings.filterDone),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(theme.gray100, RoundedCornerShape(10.dp))
            .padding(4.dp),
    ) {
        filters.forEach { filter ->
            val isActive = filter.key == activeFilter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isActive) Modifier.shadow(2.dp, RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .background(
                        if (isActive) theme.card else theme.gray100,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onFilterChanged(filter.key) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = filter.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) theme.text else theme.textSecondary,
                )
            }
        }
    }
}
