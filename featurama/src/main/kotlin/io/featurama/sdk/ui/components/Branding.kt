package io.featurama.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.featurama.sdk.ui.icons.FeaturamaLogoIcon
import io.featurama.sdk.ui.theme.FeaturamaTheme

@Composable
internal fun Branding(modifier: Modifier = Modifier, theme: FeaturamaTheme) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(theme.gray100)
            .clickable { uriHandler.openUri("https://featurama.app") }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeaturamaLogoIcon(size = 16.dp, color = theme.accent)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Powered by Featurama",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = theme.textSecondary,
        )
    }
}
