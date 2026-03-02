package io.featurama.sdk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.featurama.sdk.Featurama
import io.featurama.sdk.model.FeatureRequest
import io.featurama.sdk.model.FeatureRequestList
import io.featurama.sdk.model.ProjectConfig
import io.featurama.sdk.ui.components.*
import io.featurama.sdk.ui.strings.FeaturamaStrings
import io.featurama.sdk.ui.theme.FeaturamaTheme
import io.featurama.sdk.ui.theme.ThemeFactory
import io.featurama.sdk.ui.utils.VoterIdProvider
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * A pre-built full-screen Composable for displaying and managing feature requests.
 *
 * This uses the [Featurama] singleton internally, so make sure to call [Featurama.init] first.
 *
 * @param accentColor Accent color for the UI theme. Defaults to iOS blue.
 * @param isDarkTheme Whether to use dark theme. Defaults to system setting.
 * @param onClose Called when the user taps the close button. If null, no close button is shown.
 * @param strings Localization strings override.
 */
@Composable
fun FeaturamaScreen(
    accentColor: Color = Color(0xFF007AFF),
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onClose: (() -> Unit)? = null,
    strings: FeaturamaStrings = FeaturamaStrings(),
) {
    val context = LocalContext.current
    val theme = remember(accentColor, isDarkTheme) {
        ThemeFactory.create(accentColor, isDarkTheme)
    }

    val voterId = remember { VoterIdProvider.getOrCreate(context) }
    var activeFilter by remember { mutableStateOf("new") }
    var isAdding by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf<FeatureRequestList?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var votingIds by remember { mutableStateOf(emptySet<String>()) }
    var projectConfig by remember { mutableStateOf<ProjectConfig?>(null) }
    val showBranding = projectConfig?.branding?.showBranding ?: true
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isLoading = true
            error = null
            try {
                data = Featurama.getFeatureRequests(pageSize = 50, filter = activeFilter)
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            projectConfig = Featurama.getProjectConfig()
        } catch (_: Exception) {
            // Config fetch failed — default to showing branding
        }
    }

    LaunchedEffect(activeFilter) {
        loadData()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background)
                .statusBarsPadding(),
        ) {
            Header(
                theme = theme,
                strings = strings,
                onClose = onClose,
                onAdd = { isAdding = true },
            )

            FilterTabs(
                theme = theme,
                strings = strings,
                activeFilter = activeFilter,
                onFilterChanged = { activeFilter = it },
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isAdding) {
                CreateRequestForm(
                    theme = theme,
                    strings = strings,
                    onSubmit = { title, desc ->
                        Featurama.createFeatureRequest(
                            title = title,
                            description = desc,
                            submitterIdentifier = voterId,
                        )
                        isAdding = false
                        loadData()
                    },
                    onCancel = { isAdding = false },
                )
            }

            RequestList(
                theme = theme,
                strings = strings,
                data = data,
                isLoading = isLoading,
                error = error,
                votingIds = votingIds,
                onToggleVote = { requestId ->
                    if (!votingIds.contains(requestId)) {
                        scope.launch {
                            votingIds = votingIds + requestId
                            try {
                                Featurama.toggleVote(UUID.fromString(requestId), voterId)
                                loadData()
                            } finally {
                                votingIds = votingIds - requestId
                            }
                        }
                    }
                },
                onRefresh = { loadData() },
            )
        }

        if (showBranding) {
            Branding(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                theme = theme,
            )
        }
    }
}
