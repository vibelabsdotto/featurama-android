package io.featurama.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.featurama.sdk.FeaturamaClient
import io.featurama.sdk.exception.FeaturamaException
import io.featurama.sdk.model.Comment
import io.featurama.sdk.model.FeatureRequest
import io.featurama.sdk.ui.icons.ChevronUpIcon
import io.featurama.sdk.ui.strings.FeaturamaStrings
import io.featurama.sdk.ui.theme.FeaturamaTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
internal fun RequestDetailDialog(
    client: FeaturamaClient,
    userIdentifier: String,
    request: FeatureRequest,
    theme: FeaturamaTheme,
    strings: FeaturamaStrings,
    isVoting: Boolean,
    requestError: String?,
    onToggleVote: () -> Unit,
    onEdit: () -> Unit,
    onClose: () -> Unit,
    onCommentCountChanged: (Int) -> Unit,
) {
    var comments by remember { mutableStateOf<List<Comment>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isVotingComment by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var content by rememberSaveable { mutableStateOf("") }
    var authorName by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val isMutating = isSubmitting || isVotingComment || isVoting
    val canInteract = !isMutating && !isLoading
    val isOwnRequest = request.submitterIdentifier == userIdentifier
    val canComment = request.isApproved || isOwnRequest

    suspend fun loadComments() {
        isLoading = true
        error = null
        try {
            val loaded = client.getComments(request.id)
            comments = loaded
            onCommentCountChanged(loaded.size)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FeaturamaException) {
            error = e.message
        } catch (_: Exception) {
            error = strings.error
        } finally {
            isLoading = false
        }
    }

    fun refreshComments() {
        if (isLoading || isSubmitting || isVotingComment || isVoting) return
        isLoading = true
        scope.launch { loadComments() }
    }

    LaunchedEffect(request.id) { loadComments() }

    Dialog(
        onDismissRequest = { if (!isMutating) onClose() },
        properties = DialogProperties(
            dismissOnBackPress = !isMutating,
            dismissOnClickOutside = !isMutating,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .widthIn(max = 600.dp)
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.9f)
                .background(theme.background, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(strings.comments, fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
                    color = theme.text, modifier = Modifier.weight(1f))
                TextButton(
                    onClick = onClose,
                    enabled = !isMutating,
                    colors = ButtonDefaults.textButtonColors(contentColor = theme.accent),
                ) { Text(strings.close) }
            }
            // Keep action failures visible even when the thread is scrolled to its end.
            (error ?: requestError)?.let { message ->
                Text(message, color = theme.error, fontSize = 13.sp)
                if (error != null) {
                    TextButton(
                        onClick = ::refreshComments,
                        enabled = canInteract,
                        colors = ButtonDefaults.textButtonColors(contentColor = theme.accent),
                    ) { Text(strings.retry) }
                }
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                item(key = "request") {
                    RequestCard(
                        theme = theme,
                        strings = strings,
                        request = request,
                        isVoting = isMutating,
                        onToggleVote = onToggleVote,
                        onEdit = if (isOwnRequest && !isMutating) onEdit else null,
                        showFullDescription = true,
                    )
                }
                item(key = "refresh") {
                    TextButton(
                        onClick = ::refreshComments,
                        enabled = canInteract,
                        colors = ButtonDefaults.textButtonColors(contentColor = theme.accent),
                    ) { Text(strings.refresh) }
                }
                if (comments?.isEmpty() == true) {
                    item(key = "empty") {
                        Text(strings.noComments, color = theme.textSecondary,
                            modifier = Modifier.padding(vertical = 16.dp))
                    }
                }
                items(comments.orEmpty(), key = { it.id.toString() }) { comment ->
                    CommentRow(
                        comment = comment,
                        theme = theme,
                        strings = strings,
                        enabled = canInteract,
                        onToggleVote = {
                            if (isSubmitting || isVotingComment || isLoading || isVoting) return@CommentRow
                            isVotingComment = true
                            error = null
                            scope.launch {
                                try {
                                    val updated = client.toggleCommentVote(request.id, comment.id, userIdentifier)
                                    comments = comments?.map { if (it.id == updated.id) updated else it }
                                    // The backend has no comment hasVoted field. Read back counts,
                                    // and do not invent a persisted selected state for this voter.
                                    loadComments()
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: FeaturamaException) {
                                    error = e.message
                                } catch (_: Exception) {
                                    error = strings.error
                                } finally {
                                    isVotingComment = false
                                }
                            }
                        },
                    )
                }
                if (isLoading) {
                    item(key = "loading") {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = theme.accent)
                        }
                    }
                }
                if (canComment) {
                    item(key = "composer") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            RequestTextField(
                                value = authorName,
                                onValueChange = { authorName = it },
                                label = strings.authorNamePlaceholder,
                                theme = theme,
                                enabled = !isMutating,
                                singleLine = true,
                            )
                            RequestTextField(
                                value = content,
                                onValueChange = { content = it },
                                label = strings.commentPlaceholder,
                                theme = theme,
                                enabled = !isMutating,
                            )
                            Button(
                                onClick = {
                                    if (isSubmitting || isVotingComment || isLoading || isVoting || content.isBlank()) return@Button
                                    isSubmitting = true
                                    error = null
                                    scope.launch {
                                        try {
                                            val added = client.addComment(
                                                featureRequestId = request.id,
                                                content = content.trim(),
                                                authorIdentifier = userIdentifier,
                                                authorName = authorName.trim().takeIf { it.isNotEmpty() },
                                            )
                                            comments = (comments.orEmpty() + added).distinctBy { it.id }
                                            onCommentCountChanged(comments.orEmpty().size)
                                            content = ""
                                            // A failed verification read must not invite resending
                                            // the already accepted comment.
                                            loadComments()
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (e: FeaturamaException) {
                                            error = e.message
                                        } catch (_: Exception) {
                                            error = strings.error
                                        } finally {
                                            isSubmitting = false
                                        }
                                    }
                                },
                                enabled = canInteract && content.isNotBlank() && comments != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.accent,
                                    contentColor = theme.accentForeground,
                                ),
                            ) { Text(if (isSubmitting) strings.saving else strings.addComment) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentRow(
    comment: Comment,
    theme: FeaturamaTheme,
    strings: FeaturamaStrings,
    enabled: Boolean,
    onToggleVote: () -> Unit,
) {
    val createdAt = remember(comment.createdAt) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date.from(comment.createdAt))
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            .background(theme.card, RoundedCornerShape(8.dp)).padding(12.dp),
    ) {
        val isDeveloper = comment.authorRole == "developer"
        Text(
            comment.authorName?.takeIf { it.isNotBlank() }
                ?: if (isDeveloper) strings.developerAuthor else strings.anonymousAuthor,
            fontWeight = FontWeight.SemiBold,
            color = theme.text,
        )
        if (isDeveloper && !comment.authorName.isNullOrBlank()) {
            Text(strings.developerAuthor, color = theme.accent, fontSize = 11.sp)
        }
        Text(createdAt, fontSize = 11.sp, color = theme.textSecondary)
        Text(comment.content, color = theme.text, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        TextButton(
            onClick = onToggleVote,
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(contentColor = theme.accent),
        ) {
            ChevronUpIcon(size = 16.dp, color = if (enabled) theme.accent else theme.textSecondary)
            Spacer(modifier = Modifier.width(6.dp))
            Text("${strings.commentVote} (${comment.voteCount})")
        }
    }
}
