package io.featurama.sdk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.featurama.sdk.Featurama
import io.featurama.sdk.FeaturamaClient
import io.featurama.sdk.exception.FeaturamaException
import io.featurama.sdk.model.FeatureRequest
import io.featurama.sdk.model.FeatureRequestList
import io.featurama.sdk.model.ProjectConfig
import io.featurama.sdk.ui.components.*
import io.featurama.sdk.ui.strings.FeaturamaStrings
import io.featurama.sdk.ui.theme.FeaturamaTheme
import io.featurama.sdk.ui.theme.ThemeFactory
import io.featurama.sdk.ui.utils.DeviceInfoProvider
import io.featurama.sdk.ui.utils.VoterIdProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

/**
 * Pre-built feature board, including submission, author editing and comments.
 * Initialize [Featurama] before displaying this screen. A configured user identifier
 * takes precedence over the persistent, app-local anonymous identifier.
 */
@Composable
fun FeaturamaScreen(
    accentColor: Color = Color(0xFF007AFF),
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onClose: (() -> Unit)? = null,
    strings: FeaturamaStrings = FeaturamaStrings(),
) {
    val context = LocalContext.current
    val theme = remember(accentColor, isDarkTheme) { ThemeFactory.create(accentColor, isDarkTheme) }
    val client = Featurama.getClient()
    val anonymousIdentifier = remember(context.applicationContext) { VoterIdProvider.getOrCreate(context) }
    val userIdentifier = client.getUserIdentifier()?.takeIf { it.isNotBlank() } ?: anonymousIdentifier
    // Capturing the client keeps in-flight work tied to this project, even if the
    // singleton is reinitialized. A new client/user discards private UI state.
    key(client, userIdentifier) {
        FeaturamaContent(client, userIdentifier, theme, strings, onClose)
    }
}

@Composable
private fun FeaturamaContent(
    client: FeaturamaClient,
    userIdentifier: String,
    theme: FeaturamaTheme,
    strings: FeaturamaStrings,
    onClose: (() -> Unit)?,
) {
    val context = LocalContext.current
    var activeFilter by rememberSaveable { mutableStateOf("new") }
    var isAdding by remember { mutableStateOf(false) }
    var editingRequest by remember { mutableStateOf<FeatureRequest?>(null) }
    var selectedRequest by remember { mutableStateOf<FeatureRequest?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf<FeatureRequestList?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var votingIds by remember { mutableStateOf(emptySet<UUID>()) }
    var projectConfig by remember { mutableStateOf<ProjectConfig?>(null) }
    var configError by remember { mutableStateOf<String?>(null) }
    var configRevision by remember { mutableIntStateOf(0) }
    var loadJob by remember { mutableStateOf<Job?>(null) }
    var loadGeneration by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    fun loadData(reset: Boolean = true, clear: Boolean = false) {
        if (!reset && (isLoading || data?.hasNextPage != true)) return
        val filter = activeFilter
        val previous = data
        val page = if (reset) 1 else (previous?.page ?: 0) + 1
        loadJob?.cancel()
        loadGeneration += 1
        val generation = loadGeneration
        if (clear) data = null
        isLoading = true
        error = null
        loadJob = scope.launch {
            try {
                val loaded = client.getFeatureRequests(
                    page = page,
                    pageSize = 20,
                    filter = filter,
                    submitterIdentifier = userIdentifier,
                )
                if (generation != loadGeneration || activeFilter != filter) return@launch
                data = if (reset) loaded else loaded.copy(
                    items = (previous?.items.orEmpty() + loaded.items).distinctBy { it.id },
                )
                selectedRequest = selectedRequest?.let { selected ->
                    loaded.items.firstOrNull { it.id == selected.id } ?: selected
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (generation == loadGeneration && activeFilter == filter) {
                    error = if (e is FeaturamaException) e.message else strings.error
                }
            } finally {
                if (generation == loadGeneration) isLoading = false
            }
        }
    }

    fun replaceRequest(updated: FeatureRequest) {
        data = data?.let { list ->
            list.copy(items = list.items.map { if (it.id == updated.id) updated else it })
        }
        if (selectedRequest?.id == updated.id) selectedRequest = updated
    }

    fun editRequest(request: FeatureRequest) {
        if (request.submitterIdentifier != userIdentifier || request.id in votingIds) return
        selectedRequest = null
        editingRequest = request
        isAdding = false
    }

    fun toggleVote(request: FeatureRequest) {
        if (!request.isApproved || request.id in votingIds) return
        votingIds = votingIds + request.id
        error = null
        // A pre-mutation fetch must not overwrite a successful vote with stale state.
        loadJob?.cancel()
        loadGeneration += 1
        isLoading = false
        scope.launch {
            try {
                val updated = if (request.hasVoted) client.removeVote(request.id, userIdentifier)
                    else client.vote(request.id, userIdentifier)
                // Public vote responses intentionally redact submitterIdentifier and
                // can omit hasVoted. Retain ownership and the acknowledged action.
                replaceRequest(updated.copy(
                    submitterIdentifier = request.submitterIdentifier,
                    hasVoted = !request.hasVoted,
                ))
                loadData()
            } catch (e: CancellationException) {
                throw e
            } catch (e: FeaturamaException) {
                error = e.message
            } catch (_: Exception) {
                error = strings.error
            } finally {
                votingIds = votingIds - request.id
            }
        }
    }

    LaunchedEffect(configRevision) {
        configError = null
        try {
            projectConfig = client.getProjectConfig()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Branding remains visible, but submission waits for a known email policy.
            configError = strings.configError
        }
    }
    LaunchedEffect(activeFilter) { loadData(clear = true) }

    Column(
        modifier = Modifier.fillMaxSize().background(theme.background)
            .statusBarsPadding().navigationBarsPadding(),
    ) {
        Header(theme = theme, strings = strings, onClose = onClose, onAdd = {
            editingRequest = null
            isAdding = true
            notice = null
        })
        FilterTabs(theme = theme, strings = strings, activeFilter = activeFilter,
            onFilterChanged = { activeFilter = it })
        notice?.let { message ->
            Text(message, color = theme.accent,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
        }
        RequestList(
            theme = theme,
            strings = strings,
            data = data,
            isLoading = isLoading,
            error = error,
            votingIds = votingIds,
            userIdentifier = userIdentifier,
            onToggleVote = ::toggleVote,
            onRefresh = { loadData() },
            onOpen = { selectedRequest = it },
            onEdit = ::editRequest,
            onLoadMore = { loadData(reset = false) },
            modifier = Modifier.weight(1f),
        )
        if (projectConfig?.branding?.showBranding != false) {
            Branding(modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 12.dp), theme = theme)
        }
    }

    val editing = editingRequest
    if (isAdding || editing != null) {
        fun closeEditor() {
            if (!isSubmitting) {
                isAdding = false
                editingRequest = null
            }
        }
        Dialog(
            onDismissRequest = ::closeEditor,
            properties = DialogProperties(
                dismissOnBackPress = !isSubmitting,
                dismissOnClickOutside = !isSubmitting,
                usePlatformDefaultWidth = false,
            ),
        ) {
            Box(
                modifier = Modifier.imePadding().widthIn(max = 600.dp)
                    .fillMaxWidth(0.94f).fillMaxHeight(0.9f),
                contentAlignment = Alignment.Center,
            ) {
                if (editing != null || projectConfig != null) {
                    key(editing?.id) {
                        CreateRequestForm(
                            theme = theme,
                            strings = strings,
                            emailCollection = projectConfig?.emailCollection?.lowercase(Locale.ROOT) ?: "none",
                            initialRequest = editing,
                            onSubmittingChanged = { isSubmitting = it },
                            onCancel = ::closeEditor,
                            onSubmit = { title, description, email ->
                                if (editing != null) {
                                    val updated = client.updateFeatureRequest(
                                        id = editing.id,
                                        title = title,
                                        description = description,
                                        submitterIdentifier = userIdentifier,
                                    )
                                    replaceRequest(updated.copy(hasVoted = editing.hasVoted))
                                    notice = strings.requestUpdated
                                } else {
                                    val created = client.createFeatureRequest(
                                        title = title,
                                        description = description,
                                        submitterIdentifier = userIdentifier,
                                        email = email,
                                        deviceInfo = DeviceInfoProvider.collect(context),
                                    )
                                    // New requests can rank beyond the first page. Open the
                                    // accepted result so its pending state and edit action are reachable.
                                    selectedRequest = created.copy(hasVoted = true)
                                    notice = strings.requestSubmitted
                                }
                                editingRequest = null
                                isAdding = false
                                if (editing == null && activeFilter != "new") activeFilter = "new"
                                else loadData()
                            },
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(theme.card, RoundedCornerShape(12.dp)).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val message = configError
                        if (message != null) {
                            Text(message, color = theme.error)
                            TextButton(onClick = { configRevision += 1 },
                                colors = ButtonDefaults.textButtonColors(contentColor = theme.accent)) { Text(strings.retry) }
                        } else {
                            CircularProgressIndicator(color = theme.accent)
                            Text(strings.loading, color = theme.textSecondary)
                        }
                        TextButton(onClick = ::closeEditor,
                            colors = ButtonDefaults.textButtonColors(contentColor = theme.text)) { Text(strings.cancel) }
                    }
                }
            }
        }
    }

    selectedRequest?.let { request ->
        key(request.id) {
            RequestDetailDialog(
                client = client,
                userIdentifier = userIdentifier,
                request = request,
                theme = theme,
                strings = strings,
                isVoting = request.id in votingIds,
                requestError = error,
                onToggleVote = { toggleVote(request) },
                onEdit = { editRequest(request) },
                onClose = { selectedRequest = null },
                onCommentCountChanged = { count ->
                    // Read current state instead of overwriting a concurrent request vote.
                    val latest = selectedRequest?.takeIf { it.id == request.id }
                    if (latest != null) replaceRequest(latest.copy(commentCount = count))
                },
            )
        }
    }
}
