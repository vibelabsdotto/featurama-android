package io.featurama.sdk.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.featurama.sdk.model.FeatureRequest
import io.featurama.sdk.model.FeatureRequestList
import io.featurama.sdk.ui.strings.FeaturamaStrings
import io.featurama.sdk.ui.theme.FeaturamaTheme

@Composable
internal fun RequestList(
    theme: FeaturamaTheme,
    strings: FeaturamaStrings,
    data: FeatureRequestList?,
    isLoading: Boolean,
    error: String?,
    votingIds: Set<String>,
    onToggleVote: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    // Loading (initial)
    if (isLoading && data == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = theme.accent)
        }
        return
    }

    // Error (no data)
    if (error != null && data == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = strings.error,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .background(theme.accent, RoundedCornerShape(8.dp))
                        .clickable { onRefresh() }
                        .padding(vertical = 10.dp, horizontal = 24.dp),
                ) {
                    Text(strings.retry, color = theme.accentForeground)
                }
            }
        }
        return
    }

    // Empty
    if (data != null && data.items.isEmpty() && !isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = strings.empty,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.emptyHint,
                    fontSize = 14.sp,
                    color = theme.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    // List
    if (data != null && data.items.isNotEmpty()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(data.items, key = { it.id.toString() }) { request ->
                RequestCard(
                    theme = theme,
                    strings = strings,
                    request = request,
                    isVoting = votingIds.contains(request.id.toString()),
                    onToggleVote = { onToggleVote(request.id.toString()) },
                )
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
