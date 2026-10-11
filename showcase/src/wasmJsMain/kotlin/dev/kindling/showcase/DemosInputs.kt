package dev.kindling.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.ui.button.KButton
import dev.kindling.core.components.ui.button.KButtonVariant
import dev.kindling.core.components.ui.inputGroup.InputGroup
import dev.kindling.core.components.ui.inputGroup.InputGroupAddon
import dev.kindling.core.components.ui.inputGroup.InputGroupButton
import dev.kindling.core.components.ui.inputGroup.InputGroupInput
import dev.kindling.core.components.ui.inputGroup.KInputGroupAlign
import dev.kindling.core.components.ui.inputOTP.InputOTP
import dev.kindling.core.components.ui.inputOTP.InputOTPGroup
import dev.kindling.core.components.ui.inputOTP.InputOTPSeparator
import dev.kindling.core.components.ui.inputOTP.InputOTPSlot
import dev.kindling.core.components.ui.inputOTP.rememberInputOTPState
import dev.kindling.core.components.ui.maskInput.KMaskInput
import dev.kindling.core.components.ui.maskInput.KMaskPattern
import dev.kindling.core.components.ui.maskInput.getUnmaskedValue
import dev.kindling.core.components.ui.questionnaire.KQuestionnaire
import dev.kindling.core.components.ui.questionnaire.KQuestionnaireChoice
import dev.kindling.core.components.ui.questionnaire.KQuestionnaireChoiceType
import dev.kindling.core.components.ui.questionnaire.KQuestionnaireInput
import dev.kindling.core.components.ui.stepper.KStepper
import dev.kindling.core.components.ui.stepper.KStepperContent
import dev.kindling.core.components.ui.stepper.KStepperIndicator
import dev.kindling.core.components.ui.stepper.KStepperItem
import dev.kindling.core.components.ui.stepper.KStepperList
import dev.kindling.core.components.ui.stepper.KStepperNext
import dev.kindling.core.components.ui.stepper.KStepperPrev
import dev.kindling.core.components.ui.stepper.KStepperSeparator
import dev.kindling.core.components.ui.stepper.KStepperTrigger
import dev.kindling.core.components.ui.stepper.rememberKStepperState

@Composable
fun InputGroupDemo() {
    var query by remember { mutableStateOf("") }
    var searched by remember { mutableStateOf("") }
    Column(modifier = Modifier.width(360.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InputGroup {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InputGroupAddon(align = KInputGroupAlign.InlineStart) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
                Box(modifier = Modifier.weight(1f)) {
                    InputGroupInput(value = query, onValueChange = { query = it }, placeholder = "Search...")
                }
                InputGroupAddon(align = KInputGroupAlign.InlineEnd) {
                    InputGroupButton(
                        onClick = { searched = query },
                        variant = KButtonVariant.Secondary
                    ) { Text("Search") }
                }
            }
        }
        if (searched.isNotEmpty()) {
            Text(
                "Searched for: $searched",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InputOTPDemo() {
    var code by remember { mutableStateOf("") }
    val state = rememberInputOTPState(value = code, length = 6, onValueChange = { code = it })
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        InputOTP(state = state) {
            InputOTPGroup {
                InputOTPSlot(state = state, index = 0, isFirst = true)
                InputOTPSlot(state = state, index = 1)
                InputOTPSlot(state = state, index = 2, isLast = true)
            }
            InputOTPSeparator()
            InputOTPGroup {
                InputOTPSlot(state = state, index = 3, isFirst = true)
                InputOTPSlot(state = state, index = 4)
                InputOTPSlot(state = state, index = 5, isLast = true)
            }
        }
        Text(
            if (code.length == 6) "Code entered: $code" else "Enter your 6-digit code",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MaskInputDemo() {
    var phone by remember { mutableStateOf("") }
    var card by remember { mutableStateOf("") }
    Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KMaskInput(
            value = phone,
            onValueChange = { phone = it },
            mask = KMaskPattern.Phone,
            modifier = Modifier.fillMaxWidth()
        )
        KMaskInput(
            value = card,
            onValueChange = { card = it },
            mask = KMaskPattern.CreditCard,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Raw phone digits: ${getUnmaskedValue(phone)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StepperDemo() {
    val steps = listOf("step-1", "step-2", "step-3")
    val state = rememberKStepperState(steps = steps)
    Column(modifier = Modifier.width(360.dp)) {
        KStepper(state = state) {
            KStepperList {
                steps.forEach { step ->
                    KStepperItem(value = step) {
                        KStepperTrigger { KStepperIndicator() }
                        KStepperSeparator()
                    }
                }
            }
            KStepperContent(value = "step-1") { Text("Step 1: create your account.") }
            KStepperContent(value = "step-2") { Text("Step 2: add billing details.") }
            KStepperContent(value = "step-3") { Text("Step 3: review and confirm.") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KStepperPrev { onClick ->
                    KButton(
                        text = "Back",
                        onClick = onClick,
                        variant = KButtonVariant.Outline,
                        enabled = state.canGoPrev
                    )
                }
                KStepperNext { onClick ->
                    KButton(text = "Next", onClick = onClick, enabled = state.canGoNext)
                }
            }
        }
    }
}

@Composable
fun QuestionnaireDemo() {
    var step by remember { mutableStateOf(0) }
    var role by remember { mutableStateOf<String?>(null) }
    var goal by remember { mutableStateOf("") }
    var done by remember { mutableStateOf(false) }
    val roles = listOf("Designer", "Developer", "Other")

    Column(modifier = Modifier.width(380.dp)) {
        if (done) {
            Text("Thanks! Role: ${role ?: "-"}, goal: $goal", style = MaterialTheme.typography.bodyMedium)
        } else if (step == 0) {
            KQuestionnaire(
                progressText = "Question 1 of 2",
                title = "What is your role?",
                content = {
                    roles.forEach { r ->
                        KQuestionnaireChoice(
                            text = r,
                            selected = role == r,
                            onClick = { role = r },
                            type = KQuestionnaireChoiceType.RADIO
                        )
                    }
                },
                onNext = { step = 1 },
                canProceed = role != null
            )
        } else {
            KQuestionnaire(
                progressText = "Question 2 of 2",
                title = "What do you want to build?",
                content = {
                    KQuestionnaireInput(value = goal, onValueChange = { goal = it })
                },
                onPrevious = { step = 0 },
                onSubmit = { done = true },
                isLastStep = true,
                canProceed = goal.isNotBlank()
            )
        }
    }
}
