package io.featurama.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import io.featurama.sdk.ui.icons.SendIcon
import io.featurama.sdk.ui.strings.FeaturamaStrings
import io.featurama.sdk.ui.theme.FeaturamaTheme
import kotlinx.coroutines.launch

@Composable
internal fun CreateRequestForm(
    theme: FeaturamaTheme,
    strings: FeaturamaStrings,
    onSubmit: suspend (String, String) -> Unit,
    onCancel: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .background(theme.card, RoundedCornerShape(12.dp))
            .border(1.dp, theme.borderAccent, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        // Title input
        BasicTextField(
            value = title,
            onValueChange = { title = it },
            textStyle = TextStyle(fontSize = 16.sp, color = theme.text),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.secondary, RoundedCornerShape(8.dp))
                .padding(14.dp),
            decorationBox = { inner ->
                Box {
                    if (title.isEmpty()) {
                        Text(strings.titlePlaceholder, color = theme.textSecondary, fontSize = 16.sp)
                    }
                    inner()
                }
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description input
        BasicTextField(
            value = description,
            onValueChange = { description = it },
            textStyle = TextStyle(fontSize = 16.sp, color = theme.text),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .background(theme.secondary, RoundedCornerShape(8.dp))
                .padding(14.dp),
            decorationBox = { inner ->
                Box {
                    if (description.isEmpty()) {
                        Text(strings.descriptionPlaceholder, color = theme.textSecondary, fontSize = 16.sp)
                    }
                    inner()
                }
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            // Cancel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(theme.secondary, RoundedCornerShape(8.dp))
                    .clickable { onCancel() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(strings.cancel, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = theme.text)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Submit
            val canSubmit = title.trim().isNotEmpty() && !isSubmitting
            Box(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (canSubmit) 1f else 0.5f)
                    .background(theme.accent, RoundedCornerShape(8.dp))
                    .clickable(enabled = canSubmit) {
                        scope.launch {
                            isSubmitting = true
                            try {
                                onSubmit(title.trim(), description.trim())
                                title = ""
                                description = ""
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SendIcon(size = 16.dp, color = theme.accentForeground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.submit, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.accentForeground)
                }
            }
        }
    }
}
