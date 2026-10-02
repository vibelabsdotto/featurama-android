package io.featurama.sdk.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.featurama.sdk.ui.theme.FeaturamaTheme

/** Use Material's field semantics and focus handling with the SDK's own palette. */
@Composable
internal fun RequestTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    theme: FeaturamaTheme,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        maxLines = if (singleLine) 1 else 6,
        isError = error != null,
        supportingText = if (error != null) ({ Text(error) }) else null,
        keyboardOptions = keyboardOptions,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = theme.text,
            unfocusedTextColor = theme.text,
            disabledTextColor = theme.textSecondary,
            errorTextColor = theme.text,
            cursorColor = theme.accent,
            focusedBorderColor = theme.accent,
            unfocusedBorderColor = theme.border,
            focusedLabelColor = theme.accent,
            unfocusedLabelColor = theme.textSecondary,
            errorBorderColor = theme.error,
            errorLabelColor = theme.error,
            errorSupportingTextColor = theme.error,
        ),
    )
}
