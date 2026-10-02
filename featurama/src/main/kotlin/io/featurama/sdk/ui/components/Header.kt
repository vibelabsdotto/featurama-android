package io.featurama.sdk.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import io.featurama.sdk.ui.icons.CloseIcon
import io.featurama.sdk.ui.icons.PlusIcon
import io.featurama.sdk.ui.strings.FeaturamaStrings
import io.featurama.sdk.ui.theme.FeaturamaTheme

@Composable
internal fun Header(
    theme: FeaturamaTheme,
    strings: FeaturamaStrings,
    onClose: (() -> Unit)?,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .then(if (onClose != null) Modifier
                    .semantics { contentDescription = strings.close }
                    .clickable(role = Role.Button) { onClose() } else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (onClose != null) {
                CloseIcon(size = 24.dp, color = theme.text)
            }
        }
        Text(
            text = strings.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.text,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = strings.newRequest }
                .clickable(role = Role.Button) { onAdd() },
            contentAlignment = Alignment.Center,
        ) {
            PlusIcon(size = 24.dp, color = theme.accent)
        }
    }
}
