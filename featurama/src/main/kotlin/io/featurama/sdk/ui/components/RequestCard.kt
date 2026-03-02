package io.featurama.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import io.featurama.sdk.model.FeatureRequest
import io.featurama.sdk.model.FeatureRequestStatus
import io.featurama.sdk.ui.icons.ChevronUpIcon
import io.featurama.sdk.ui.strings.FeaturamaStrings
import io.featurama.sdk.ui.theme.FeaturamaTheme

@Composable
internal fun RequestCard(
    theme: FeaturamaTheme,
    strings: FeaturamaStrings,
    request: FeatureRequest,
    isVoting: Boolean,
    onToggleVote: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(theme.card, RoundedCornerShape(12.dp))
            .border(1.dp, theme.border, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Vote button
        Column(
            modifier = Modifier
                .background(theme.accentLight, RoundedCornerShape(8.dp))
                .clickable(enabled = !isVoting) { onToggleVote() }
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .widthIn(min = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChevronUpIcon(size = 20.dp, color = theme.accent)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${request.voteCount}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.accent,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = request.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = theme.text,
            )
            if (request.status == FeatureRequestStatus.ROADMAP) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.badgePlanned,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.accent,
                    modifier = Modifier
                        .background(theme.accentLight, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            if (!request.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = request.description!!,
                    fontSize = 13.sp,
                    color = theme.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}
