package dev.kindling.core.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── Data & Types ────────────────────────────────────────────────────────────

enum class KQuestionnaireChoiceType {
    RADIO, CHECKBOX
}

data class KQuestionnaireChoiceItem(
    val id: String,
    val label: String,
    val description: String? = null,
    val shortcut: String? = null,
    val enabled: Boolean = true
)

// ─── Component Implementation ────────────────────────────────────────────────

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

// ─── Sub-Components ──────────────────────────────────────────────────────────

@Composable
fun KQuestionnaireChoice(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: KQuestionnaireChoiceType = KQuestionnaireChoiceType.RADIO,
    description: String? = null,
    shortcut: String? = null,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    when (type) {
                        KQuestionnaireChoiceType.RADIO -> Modifier.selectable(
                            selected = selected,
                            onClick = onClick,
                            role = Role.RadioButton
                        )
                        KQuestionnaireChoiceType.CHECKBOX -> Modifier.toggleable(
                            value = selected,
                            onValueChange = { onClick() },
                            role = Role.Checkbox
                        )
                    }
                } else Modifier
            ),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Indicator
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (type == KQuestionnaireChoiceType.RADIO) {
                    RadioButton(selected = selected, onClick = null, enabled = enabled)
                } else {
                    Checkbox(checked = selected, onCheckedChange = null, enabled = enabled)
                }
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Shortcut badge
            if (shortcut != null) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = shortcut,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun KQuestionnaireInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Type your answer here...",
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium
    )
}