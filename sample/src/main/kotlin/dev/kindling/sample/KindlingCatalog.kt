package dev.kindling.sample

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.*

@Composable
fun KindlingCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Text("Kindling Catalog", style = MaterialTheme.typography.headlineMedium)

        ButtonSection()
        SpinnerSection()
        InputSection()
        StepperSection()
    }
}

@Composable
private fun ButtonSection() {
    SectionHeader("Button")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Default",     onClick = {})
            KButton(text = "Secondary",  onClick = {}, variant = KButtonVariant.Secondary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Outline",     onClick = {}, variant = KButtonVariant.Outline)
            KButton(text = "Ghost",       onClick = {}, variant = KButtonVariant.Ghost)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Destructive", onClick = {}, variant = KButtonVariant.Destructive)
            KButton(text = "Link",        onClick = {}, variant = KButtonVariant.Link)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Small",    onClick = {}, size = KButtonSize.Sm)
            KButton(text = "Large",    onClick = {}, size = KButtonSize.Lg)
            KButton(text = "Loading",  onClick = {}, isLoading = true)
            KButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}

@Composable
private fun SpinnerSection() {
    SectionHeader("Spinner")
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        KSpinner(size = KSpinnerSize.Sm)
        KSpinner(size = KSpinnerSize.Default)
        KSpinner(size = KSpinnerSize.Lg)
        KSpinner(size = KSpinnerSize.Xl)
    }
    KSpinner(size = KSpinnerSize.Default, label = "Loading…")
}

@Composable
private fun InputSection() {
    SectionHeader("Input")
    var text by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        KInput(value = text, onValueChange = { text = it }, placeholder = "Type something…")
        KFormField(
            label         = "Email",
            value         = email,
            onValueChange = { email = it },
            placeholder   = "m@example.com",
            helperText    = "We'll never share your email.",
            isError       = email.isNotEmpty() && !email.contains("@"),
            errorMessage  = "Please enter a valid email address."
        )
        KInput(value = "", onValueChange = {}, placeholder = "Password", isPassword = true)
    }
}

@Composable
private fun StepperSection() {
    SectionHeader("Stepper")
    var step by remember { mutableStateOf(1) }
    val steps = listOf(
        KStep("Account",  "Create your account"),
        KStep("Profile",  "Set up your profile"),
        KStep("Review",   "Review your details"),
        KStep("Done",     "All set!")
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KStepper(steps = steps, currentStep = step)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Back", onClick = { if (step > 0) step-- },
                variant = KButtonVariant.Outline, size = KButtonSize.Sm, enabled = step > 0)
            KButton(text = "Next", onClick = { if (step < steps.lastIndex) step++ },
                size = KButtonSize.Sm, enabled = step < steps.lastIndex)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    HorizontalDivider()
}