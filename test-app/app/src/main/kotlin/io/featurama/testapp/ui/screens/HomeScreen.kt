package io.featurama.testapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.featurama.sdk.ui.FeaturamaScreen

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit
) {
    FeaturamaScreen(
        accentColor = Color(0xFF6366F1),
        onClose = onNavigateToSettings,
    )
}
