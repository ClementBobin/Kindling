package dev.kindling.sample.previews

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.*

// ── Spinner ───────────────────────────────────────────────────────────────────

@Preview(name = "Spinner — sizes", showBackground = true)
@Composable
private fun SpinnerSizes() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        KSpinner(size = KSpinnerSize.Sm)
        KSpinner(size = KSpinnerSize.Default)
        KSpinner(size = KSpinnerSize.Lg)
        KSpinner(size = KSpinnerSize.Xl)
    }
    KSpinner(size = KSpinnerSize.Default, label = "Loading…")
}

// ── Input ─────────────────────────────────────────────────────────────────────

@Preview(name = "Input — states", showBackground = true)
@Composable
private fun InputStates() = PreviewSurface {
    KInput(value = "",          onValueChange = {}, placeholder = "Placeholder")
    KInput(value = "Some text", onValueChange = {})
    KInput(value = "Error",     onValueChange = {}, isError = true)
    KInput(value = "",          onValueChange = {}, enabled = false, placeholder = "Disabled")
    KInput(value = "",          onValueChange = {}, isPassword = true, placeholder = "Password")
}

@Preview(name = "FormField", showBackground = true)
@Composable
private fun FormFieldPreview() = PreviewSurface {
    KFormField(
        label         = "Email",
        value         = "bad-email",
        onValueChange = {},
        placeholder   = "m@example.com",
        helperText    = "We'll never share your email.",
        isError       = true,
        errorMessage  = "Please enter a valid email address."
    )
    KFormField(
        label         = "Username",
        value         = "kindling_user",
        onValueChange = {},
        helperText    = "Must be unique."
    )
}

// ── Stepper ───────────────────────────────────────────────────────────────────

private val sampleSteps = listOf(
    KStep("Account", "Create your account"),
    KStep("Profile", "Set up your profile"),
    KStep("Review",  "Review your details"),
    KStep("Done",    "All set!")
)

@Preview(name = "Stepper — horizontal step 1", showBackground = true)
@Composable
private fun StepperHorizontalStep1() = PreviewSurface {
    KStepper(steps = sampleSteps, currentStep = 0)
}

@Preview(name = "Stepper — horizontal step 3", showBackground = true)
@Composable
private fun StepperHorizontalStep3() = PreviewSurface {
    KStepper(steps = sampleSteps, currentStep = 2)
}

@Preview(name = "Stepper — vertical", showBackground = true)
@Composable
private fun StepperVertical() = PreviewSurface {
    KStepper(steps = sampleSteps, currentStep = 1, orientation = KStepperOrientation.Vertical)
}

// ── Skeleton ──────────────────────────────────────────────────────────────────

@Preview(name = "Skeleton", showBackground = true)
@Composable
private fun SkeletonPreview() = PreviewSurface {
    repeat(3) { KSkeletonListItem() }
    KSkeletonCard()
}

// ── Label ─────────────────────────────────────────────────────────────────────

@Preview(name = "Label", showBackground = true)
@Composable
private fun LabelPreview() = PreviewSurface {
    KLabel("Enabled label")
    KLabel("Disabled label", disabled = true)
}