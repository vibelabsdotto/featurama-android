package io.featurama.sdk.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                .size(40.dp)
                .then(if (onClose != null) Modifier.clickable { onClose() } else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (onClose != null) {
                CloseIcon(size = 24.dp, color = theme.text)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = strings.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.text,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable { onAdd() },
            contentAlignment = Alignment.Center,
        ) {
            PlusIcon(size = 24.dp, color = theme.accent)
        }
    }
}
