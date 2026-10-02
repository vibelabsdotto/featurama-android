package io.featurama.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.featurama.sdk.exception.FeaturamaException
import io.featurama.sdk.model.FeatureRequest
import io.featurama.sdk.ui.icons.SendIcon
import io.featurama.sdk.ui.strings.FeaturamaStrings
import io.featurama.sdk.ui.theme.FeaturamaTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

@Composable
internal fun CreateRequestForm(
    theme: FeaturamaTheme,
    strings: FeaturamaStrings,
    onSubmit: suspend (String, String, String?) -> Unit,
    onCancel: () -> Unit,
    emailCollection: String = "none",
    initialRequest: FeatureRequest? = null,
    onSubmittingChanged: (Boolean) -> Unit = {},
) {
    var title by rememberSaveable(initialRequest?.id) { mutableStateOf(initialRequest?.title.orEmpty()) }
    var description by rememberSaveable(initialRequest?.id) { mutableStateOf(initialRequest?.description.orEmpty()) }
    var email by rememberSaveable(initialRequest?.id) { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val isEditing = initialRequest != null
    val collectEmail = !isEditing && emailCollection != "none"
    val emailIsRequired = collectEmail && emailCollection != "optional"
    val normalizedEmail = email.trim()
    val validEmail = emailPattern.matches(normalizedEmail)
    val titleError = strings.titleTooLong.takeIf { title.trim().length > 200 }
    val descriptionError = strings.descriptionTooLong.takeIf { description.trim().length > 2000 }
    val emailError = strings.invalidEmail.takeIf {
        collectEmail && normalizedEmail.isNotEmpty() && !validEmail
    }
    val canSubmit = title.isNotBlank() && description.isNotBlank() &&
        titleError == null && descriptionError == null && emailError == null &&
        (!emailIsRequired || validEmail) && !isSubmitting

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.card, RoundedCornerShape(12.dp))
            .border(1.dp, theme.borderAccent, RoundedCornerShape(12.dp))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            if (isEditing) strings.editRequest else strings.newRequest,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.text,
        )
        RequestTextField(
            value = title,
            onValueChange = { title = it; submitError = null },
            label = strings.titlePlaceholder,
            theme = theme,
            enabled = !isSubmitting,
            singleLine = true,
            error = titleError,
        )
        RequestTextField(
            value = description,
            onValueChange = { description = it; submitError = null },
            label = strings.descriptionPlaceholder,
            theme = theme,
            enabled = !isSubmitting,
            error = descriptionError,
        )
        if (collectEmail) {
            RequestTextField(
                value = email,
                onValueChange = { email = it; submitError = null },
                label = if (emailIsRequired) strings.emailRequired else strings.emailOptional,
                theme = theme,
                enabled = !isSubmitting,
                singleLine = true,
                error = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Text(strings.emailHint, color = theme.textSecondary, fontSize = 12.sp)
        }
        submitError?.let {
            Text(it, fontSize = 13.sp, color = theme.error, modifier = Modifier.fillMaxWidth())
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(
                onClick = onCancel,
                enabled = !isSubmitting,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColors(contentColor = theme.text),
            ) {
                Text(strings.cancel)
            }
            Button(
                onClick = {
                    // Set the guard before launching so repeated taps cannot submit twice.
                    if (isSubmitting) return@Button
                    isSubmitting = true
                    onSubmittingChanged(true)
                    submitError = null
                    scope.launch {
                        try {
                            onSubmit(
                                title.trim(),
                                description.trim(),
                                normalizedEmail.takeIf { collectEmail && it.isNotEmpty() },
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: FeaturamaException) {
                            // Keep the draft and the server's actionable validation message.
                            submitError = e.message
                        } catch (_: Exception) {
                            submitError = strings.error
                        } finally {
                            isSubmitting = false
                            onSubmittingChanged(false)
                        }
                    }
                },
                enabled = canSubmit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.accent,
                    contentColor = theme.accentForeground,
                    disabledContainerColor = theme.accent.copy(alpha = 0.4f),
                    disabledContentColor = theme.accentForeground,
                ),
            ) {
                if (!isEditing && !isSubmitting) {
                    SendIcon(size = 16.dp, color = theme.accentForeground)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(if (isSubmitting) strings.saving else if (isEditing) strings.save else strings.submit)
            }
        }
    }
}
