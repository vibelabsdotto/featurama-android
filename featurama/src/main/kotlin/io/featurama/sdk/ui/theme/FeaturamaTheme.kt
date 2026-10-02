package io.featurama.sdk.ui.theme

import androidx.compose.ui.graphics.Color

data class FeaturamaTheme(
    val background: Color,
    val card: Color,
    val secondary: Color,
    val text: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentLight: Color,
    val accentForeground: Color,
    val border: Color,
    val borderAccent: Color,
    val gray100: Color,
    val error: Color = defaultError,
) {
    companion object {
        internal val defaultError: Color = Color(0xFFE5484D)
    }
}
