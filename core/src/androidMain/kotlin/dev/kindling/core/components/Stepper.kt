package dev.kindling.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  Stepper state  (useStepper hook)
// ─────────────────────────────────────────────

/**
 * Observable state for a [Stepper].
 *
 * Access via [useStepper] inside any composable that descends from [Stepper].
 * Mirrors the `useStepper` hook from `stepper.tsx`.
 */
@Stable
class StepperState internal constructor(
    initialValue: String,
    val steps: List<String>,
    val onValueChange: (String) -> Unit,
    val onValidate: (suspend (value: String, direction: KNavigationDirection) -> Boolean)?
) {
    var value by mutableStateOf(initialValue)
        internal set

    val currentIndex: Int get() = steps.indexOf(value)

    val canGoNext: Boolean get() = currentIndex < steps.size - 1
    val canGoPrev: Boolean get() = currentIndex > 0

    fun dataState(stepValue: String): KStepState {
        val idx     = steps.indexOf(stepValue)
        val current = currentIndex
        return when {
            idx < current  -> KStepState.Completed
            idx == current -> KStepState.Active
            else           -> KStepState.Inactive
        }
    }

    internal suspend fun navigateTo(
        target: String,
        direction: KNavigationDirection
    ): Boolean {
        if (onValidate != null) {
            val ok = onValidate.invoke(target, direction)
            if (!ok) return false
        }
        value = target
        onValueChange(target)
        return true
    }
}

/**
 * Creates and remembers a [StepperState].
 *
 * ```kotlin
 * val stepper = rememberStepperState(
 *     steps        = listOf("account", "billing", "review"),
 *     defaultValue = "account"
 * )
 * Stepper(state = stepper) { … }
 * ```
 */
@Composable
fun rememberStepperState(
    steps: List<String>,
    defaultValue: String = steps.firstOrNull() ?: "",
    value: String? = null,
    onValueChange: (String) -> Unit = {},
    onValidate: (suspend (value: String, direction: KNavigationDirection) -> Boolean)? = null
): StepperState {
    val state = remember(steps) {
        StepperState(value ?: defaultValue, steps, onValueChange, onValidate)
    }
    LaunchedEffect(value) { if (value != null) state.value = value }
    return state
}

// Internal composition local so child slots can access state
private val LocalStepperState = compositionLocalOf<StepperState?> { null }

/**
 * Reads the nearest [StepperState] — mirrors `useStepper` from `stepper.tsx`.
 *
 * ```kotlin
 * val stepper = useStepper()
 * Text("Step ${stepper.currentIndex + 1} of ${stepper.steps.size}")
 * ```
 */
@Composable
fun useStepper(): StepperState =
    LocalStepperState.current
        ?: error("`useStepper()` must be called inside a `Stepper` composable")

// ─────────────────────────────────────────────
//  Stepper  (root)
// ─────────────────────────────────────────────

/**
 * Shadcn/ui-style Stepper root — mirrors `Stepper` from `stepper.tsx`.
 *
 * Provides [StepperState] to all child slots via [useStepper].
 * Supports horizontal and vertical orientations, RTL via [LocalLayoutDirection].
 *
 * ```kotlin
 * val state = rememberStepperState(steps = listOf("step-1", "step-2", "step-3"))
 *
 * Stepper(state = state) {
 *     StepperList {
 *         listOf("Account", "Billing", "Review").forEachIndexed { i, label ->
 *             StepperItem(value = "step-${i+1}") {
 *                 StepperTrigger {
 *                     StepperIndicator()
 *                     Column {
 *                         StepperTitle { Text(label) }
 *                         StepperDescription { Text("Details") }
 *                     }
 *                 }
 *                 StepperSeparator()
 *             }
 *         }
 *     }
 *     StepperContent(value = "step-1") { Text("Step 1 content") }
 *     StepperContent(value = "step-2") { Text("Step 2 content") }
 *     StepperContent(value = "step-3") { Text("Step 3 content") }
 *     Row {
 *         StepperPrev { KButton("Back",  onClick = it) }
 *         StepperNext { KButton("Next",  onClick = it) }
 *     }
 * }
 * ```
 */
@Composable
fun Stepper(
    state: StepperState,
    modifier: Modifier = Modifier,
    orientation: KStepperOrientation = KStepperOrientation.Horizontal,
    disabled: Boolean = false,
    nonInteractive: Boolean = false,
    loop: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    CompositionLocalProvider(
        LocalStepperState    provides state,
        LocalStepperDisabled provides disabled,
        LocalStepperNonInteractive provides nonInteractive,
        LocalStepperOrientation    provides orientation
    ) {
        Column(
            modifier            = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content             = content
        )
    }
}

// Additional locals
private val LocalStepperDisabled       = compositionLocalOf { false }
private val LocalStepperNonInteractive = compositionLocalOf { false }
private val LocalStepperOrientation    = compositionLocalOf { KStepperOrientation.Horizontal }

// ─────────────────────────────────────────────
//  StepperList
// ─────────────────────────────────────────────

/**
 * Horizontal or vertical list of [StepperItem]s.
 * Mirrors `StepperList`.
 */
@Composable
fun StepperList(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val orientation = LocalStepperOrientation.current
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        content           = content
    )
}

// ─────────────────────────────────────────────
//  StepperItem
// ─────────────────────────────────────────────

private val LocalStepperItemValue = compositionLocalOf { "" }

/**
 * Container for one step — mirrors `StepperItem`.
 *
 * @param value     Unique step identifier matching one of [StepperState.steps].
 * @param completed Override completion state (auto-derived from [StepperState] by default).
 * @param disabled  Disables interaction for this step only.
 */
@Composable
fun RowScope.StepperItem(
    value: String,
    modifier: Modifier = Modifier,
    completed: Boolean? = null,
    disabled: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val state    = useStepper()
    val dsState  = if (completed == true) KStepState.Completed else state.dataState(value)
    val isLast   = state.steps.lastOrNull() == value

    CompositionLocalProvider(LocalStepperItemValue provides value) {
        Row(
            modifier          = modifier.weight(if (isLast) 0f else 1f, fill = !isLast),
            verticalAlignment = Alignment.CenterVertically,
            content           = content
        )
    }
}

// ─────────────────────────────────────────────
//  StepperTrigger
// ─────────────────────────────────────────────

/**
 * Tappable trigger for a step — mirrors `StepperTrigger`.
 *
 * Navigates to this step on tap (unless [nonInteractive]).
 */
@Composable
fun StepperTrigger(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val state          = useStepper()
    val itemValue      = LocalStepperItemValue.current
    val nonInteractive = LocalStepperNonInteractive.current
    val disabled       = LocalStepperDisabled.current
    val scope          = rememberCoroutineScope()

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .then(
                if (!nonInteractive && !disabled && itemValue.isNotEmpty())
                    Modifier.clickable {
                        scope.launch {
                            val idx     = state.steps.indexOf(itemValue)
                            val current = state.currentIndex
                            val dir     = if (idx > current) KNavigationDirection.Next
                                          else KNavigationDirection.Prev
                            state.navigateTo(itemValue, dir)
                        }
                    }
                else Modifier
            )
            .padding(4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content               = content
    )
}

// ─────────────────────────────────────────────
//  StepperIndicator
// ─────────────────────────────────────────────

/**
 * Numbered / checkmark bubble — mirrors `StepperIndicator`.
 *
 * ```kotlin
 * StepperIndicator()                              // auto: number or check
 * StepperIndicator { dataState -> MyIcon(dataState) }  // custom
 * ```
 */
@Composable
fun StepperIndicator(
    modifier: Modifier = Modifier,
    content: (@Composable (KStepState) -> Unit)? = null
) {
    val state     = useStepper()
    val itemValue = LocalStepperItemValue.current
    val dsState   = state.dataState(itemValue)
    val stepPos   = state.steps.indexOf(itemValue) + 1
    val cs        = MaterialTheme.colorScheme

    val bg by animateColorAsState(
        when (dsState) {
            KStepState.Active, KStepState.Completed -> cs.primary
            KStepState.Error                         -> cs.error
            else                                     -> Color.Transparent
        }, tween(200), label = "stepBg"
    )
    val border by animateColorAsState(
        when (dsState) {
            KStepState.Active, KStepState.Completed -> cs.primary
            KStepState.Error                         -> cs.error
            else                                     -> cs.outline
        }, tween(200), label = "stepBorder"
    )
    val fg = when (dsState) {
        KStepState.Active, KStepState.Completed -> cs.onPrimary
        KStepState.Error                         -> cs.onError
        else                                     -> cs.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bg)
            .border(if (dsState == KStepState.Inactive) 2.dp else 0.dp, border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            content != null -> content(dsState)
            dsState == KStepState.Completed ->
                Icon(Icons.Default.Check, null, tint = fg, modifier = Modifier.size(14.dp))
            else ->
                Text(stepPos.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg)
        }
    }
}

// ─────────────────────────────────────────────
//  StepperSeparator
// ─────────────────────────────────────────────

/**
 * Connector line between steps — mirrors `StepperSeparator`.
 * Hidden automatically on the last step.
 */
@Composable
fun RowScope.StepperSeparator(modifier: Modifier = Modifier) {
    val state     = useStepper()
    val itemValue = LocalStepperItemValue.current
    val isLast    = state.steps.lastOrNull() == itemValue
    if (isLast) return

    val dsState = state.dataState(itemValue)
    val color by animateColorAsState(
        if (dsState == KStepState.Completed || dsState == KStepState.Active)
            MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline,
        tween(200), label = "separator"
    )
    Box(
        modifier = modifier
            .weight(1f)
            .height(2.dp)
            .background(color)
    )
}

// ─────────────────────────────────────────────
//  StepperTitle
// ─────────────────────────────────────────────

/** Step title text — mirrors `StepperTitle`. */
@Composable
fun StepperTitle(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state     = useStepper()
    val itemValue = LocalStepperItemValue.current
    val active    = state.dataState(itemValue) == KStepState.Active
    ProvideTextStyle(
        MaterialTheme.typography.labelMedium.copy(
            fontSize   = 13.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (active) MaterialTheme.colorScheme.onBackground
                         else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) { Box(modifier = modifier) { content() } }
}

// ─────────────────────────────────────────────
//  StepperDescription
// ─────────────────────────────────────────────

/** Step description text — mirrors `StepperDescription`. */
@Composable
fun StepperDescription(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ProvideTextStyle(
        MaterialTheme.typography.bodySmall.copy(
            fontSize = 11.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) { Box(modifier = modifier) { content() } }
}

// ─────────────────────────────────────────────
//  StepperContent
// ─────────────────────────────────────────────

/**
 * Panel shown only when [value] is the active step.
 * Pass [forceMount] = true to always render (hidden via alpha).
 * Mirrors `StepperContent`.
 */
@Composable
fun StepperContent(
    value: String,
    modifier: Modifier = Modifier,
    forceMount: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val state = useStepper()
    if (state.value != value && !forceMount) return
    Box(modifier = modifier.fillMaxWidth(), content = content)
}

// ─────────────────────────────────────────────
//  StepperPrev
// ─────────────────────────────────────────────

/**
 * Navigates to the previous step.
 * [content] receives an `onClick` lambda — wire it to your button.
 * Mirrors `StepperPrev`.
 *
 * ```kotlin
 * StepperPrev { onClick -> KButton("Back", onClick = onClick) }
 * ```
 */
@Composable
fun StepperPrev(
    modifier: Modifier = Modifier,
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    val state = useStepper()
    val scope = rememberCoroutineScope()
    content(onClick = {
        if (state.canGoPrev) {
            scope.launch {
                val prev = state.steps[state.currentIndex - 1]
                state.navigateTo(prev, KNavigationDirection.Prev)
            }
        }
    })
}

// ─────────────────────────────────────────────
//  StepperNext
// ─────────────────────────────────────────────

/**
 * Navigates to the next step (runs validation if configured).
 * [content] receives an `onClick` lambda — wire it to your button.
 * Mirrors `StepperNext`.
 *
 * ```kotlin
 * StepperNext { onClick -> KButton("Continue", onClick = onClick) }
 * ```
 */
@Composable
fun StepperNext(
    modifier: Modifier = Modifier,
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    val state = useStepper()
    val scope = rememberCoroutineScope()
    content(onClick = {
        if (state.canGoNext) {
            scope.launch {
                val next = state.steps[state.currentIndex + 1]
                state.navigateTo(next, KNavigationDirection.Next)
            }
        }
    })
}