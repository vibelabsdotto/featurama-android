package io.featurama.sdk.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import java.util.UUID

@Composable
internal fun RequestList(
    theme: FeaturamaTheme,
    strings: FeaturamaStrings,
    data: FeatureRequestList?,
    isLoading: Boolean,
    error: String?,
    votingIds: Set<UUID>,
    userIdentifier: String,
    onToggleVote: (FeatureRequest) -> Unit,
    onRefresh: () -> Unit,
    onOpen: (FeatureRequest) -> Unit,
    onEdit: (FeatureRequest) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        item(key = "controls") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    colors = ButtonDefaults.textButtonColors(contentColor = theme.accent),
                ) {
                    Text(strings.refresh)
                }
            }
        }
        // Failures during refresh or voting must remain visible with existing data.
        error?.let { message ->
            item(key = "error") {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Text(message, fontSize = 14.sp, color = theme.error)
                    TextButton(
                        onClick = onRefresh,
                        enabled = !isLoading,
                        colors = ButtonDefaults.textButtonColors(contentColor = theme.accent),
                    ) { Text(strings.retry) }
                }
            }
        }
        if (data?.items?.isEmpty() == true && !isLoading) {
            item(key = "empty") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(strings.empty, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                        color = theme.textSecondary, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(strings.emptyHint, fontSize = 14.sp,
                        color = theme.textSecondary, textAlign = TextAlign.Center)
                }
            }
        }
        items(data?.items.orEmpty(), key = { it.id.toString() }) { request ->
            RequestCard(
                theme = theme,
                strings = strings,
                request = request,
                isVoting = request.id in votingIds,
                onToggleVote = { onToggleVote(request) },
                onOpen = { onOpen(request) },
                onEdit = if (request.submitterIdentifier == userIdentifier && request.id !in votingIds) ({ onEdit(request) }) else null,
            )
        }
        if (isLoading) {
            item(key = "loading") {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = theme.accent)
                }
            }
        } else if (data?.hasNextPage == true) {
            item(key = "more") {
                TextButton(
                    onClick = onLoadMore,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = theme.accent),
                ) { Text(strings.loadMore) }
            }
        }
    }
}
