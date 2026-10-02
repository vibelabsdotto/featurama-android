package io.featurama.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onOpen: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    showFullDescription: Boolean = false,
) {
    val voteLabel = when {
        !request.isApproved -> strings.pendingVote
        request.hasVoted -> strings.removeVote
        else -> strings.vote
    }
    val voteColor = if (request.hasVoted) theme.accentForeground else theme.accent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(theme.card, RoundedCornerShape(12.dp))
            .border(1.dp, theme.border, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier
                    .alpha(if (isVoting || !request.isApproved) 0.5f else 1f)
                    .background(if (request.hasVoted) theme.accent else theme.accentLight, RoundedCornerShape(8.dp))
                    .semantics {
                        contentDescription = voteLabel
                        selected = request.hasVoted
                    }
                    .clickable(
                        enabled = !isVoting && request.isApproved,
                        role = Role.Button,
                        onClickLabel = voteLabel,
                        onClick = onToggleVote,
                    )
                    .widthIn(min = 56.dp)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ChevronUpIcon(size = 20.dp, color = voteColor)
                Text("${request.voteCount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = voteColor)
                if (request.hasVoted) {
                    Text(strings.voted, fontSize = 10.sp, color = voteColor)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f).then(
                    if (onOpen != null) Modifier.clickable(role = Role.Button, onClick = onOpen) else Modifier
                ),
            ) {
                Text(request.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.text)
                val badge = when {
                    !request.isApproved -> strings.badgePending
                    request.status == FeatureRequestStatus.ROADMAP -> strings.badgePlanned
                    request.status == FeatureRequestStatus.IN_PROGRESS -> strings.filterInProgress
                    request.status == FeatureRequestStatus.DONE -> strings.filterDone
                    else -> null
                }
                if (badge != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.accent,
                        modifier = Modifier.background(theme.accentLight, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                request.description?.takeIf { it.isNotEmpty() }?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        it,
                        fontSize = 13.sp,
                        color = theme.textSecondary,
                        maxLines = if (showFullDescription) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
        if (!request.isApproved && showFullDescription) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(strings.pendingHint, color = theme.textSecondary, fontSize = 12.sp)
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (onOpen != null) {
                TextButton(onClick = onOpen, colors = ButtonDefaults.textButtonColors(contentColor = theme.accent)) {
                    Text("${strings.comments} (${request.commentCount})")
                }
            } else {
                Text("${strings.comments} (${request.commentCount})", color = theme.textSecondary, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            if (onEdit != null) {
                TextButton(onClick = onEdit, colors = ButtonDefaults.textButtonColors(contentColor = theme.accent)) {
                    Text(strings.edit)
                }
            }
        }
    }
}
