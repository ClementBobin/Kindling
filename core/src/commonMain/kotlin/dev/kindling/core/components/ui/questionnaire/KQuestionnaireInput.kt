package dev.kindling.core.components.ui.questionnaire

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.kindling.core.components.ui.layout.kindlingFieldBorder

@Composable
fun KQuestionnaireInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Type your answer here...",
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    val shape = MaterialTheme.shapes.medium
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .kindlingFieldBorder(
                interactionSource = interactionSource,
                shape = shape,
                enabled = enabled,
                disabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        // Border is drawn by kindlingFieldBorder so its width follows KindlingShapes.borderWidth.
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent
        )
    )
}
