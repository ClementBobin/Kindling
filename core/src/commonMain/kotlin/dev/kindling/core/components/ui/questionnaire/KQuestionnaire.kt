package dev.kindling.core.components.ui.questionnaire

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A multi-step interactive questionnaire component in Jetpack Compose, supporting
 * single/multi-choice selection, text inputs, error validation, progress tracking, and navigation actions.
 */
@Composable
fun KQuestionnaire(
    modifier: Modifier = Modifier,
    progressText: String = "",
    title: String,
    description: String? = null,
    errorText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
    onPrevious: (() -> Unit)? = null,
    onSkip: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
    isLastStep: Boolean = false,
    canProceed: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress Header
            if (progressText.isNotBlank()) {
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Title & Description
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Main Question Content (Choices / Inputs)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )

            // Error message display
            if (errorText != null) {
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Actions Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onPrevious != null) {
                    OutlinedButton(
                        onClick = onPrevious,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("Previous")
                    }
                }

                if (onSkip != null) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("Skip")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isLastStep && onSubmit != null) {
                    Button(
                        onClick = onSubmit,
                        enabled = canProceed,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("Submit")
                    }
                } else if (onNext != null) {
                    Button(
                        onClick = onNext,
                        enabled = canProceed,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    }
}