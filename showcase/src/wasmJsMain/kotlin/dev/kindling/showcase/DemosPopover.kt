package dev.kindling.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import dev.kindling.core.components.ui.combobox.Combobox
import dev.kindling.core.components.ui.combobox.ComboboxCollection
import dev.kindling.core.components.ui.combobox.ComboboxContent
import dev.kindling.core.components.ui.combobox.ComboboxEmpty
import dev.kindling.core.components.ui.combobox.ComboboxList
import dev.kindling.core.components.ui.combobox.ComboboxTrigger
import dev.kindling.core.components.ui.combobox.KComboboxItem
import dev.kindling.core.components.ui.combobox.rememberComboboxState
import dev.kindling.core.components.ui.inputGroup.InputGroup
import dev.kindling.core.components.ui.inputGroup.InputGroupAddon
import dev.kindling.core.components.ui.inputGroup.InputGroupInput
import dev.kindling.core.components.ui.inputGroup.InputGroupText
import dev.kindling.core.components.ui.inputGroup.KInputGroupAlign
import dev.kindling.core.components.ui.inputOTP.InputOTP
import dev.kindling.core.components.ui.inputOTP.InputOTPGroup
import dev.kindling.core.components.ui.inputOTP.InputOTPSeparator
import dev.kindling.core.components.ui.inputOTP.InputOTPSlot
import dev.kindling.core.components.ui.inputOTP.rememberInputOTPState
import dev.kindling.core.components.ui.maskInput.KMaskInput
import dev.kindling.core.components.ui.maskInput.KMaskPattern
import dev.kindling.core.components.ui.maskInput.getUnmaskedValue
import dev.kindling.core.components.ui.picker.KRoulettePicker
import dev.kindling.core.components.ui.popover.KPopover
import dev.kindling.core.components.ui.popover.KPopoverAlign
import dev.kindling.core.components.ui.popover.KPopoverDescription
import dev.kindling.core.components.ui.popover.KPopoverHeader
import dev.kindling.core.components.ui.popover.KPopoverSide
import dev.kindling.core.components.ui.popover.KPopoverTitle

/** slug: popover */
@Composable
fun PopoverDemo() {
    var open by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().heightIn(min = 170.dp)) {
        KPopover(
            open = open,
            onDismiss = { open = false },
            side = KPopoverSide.Bottom,
            align = KPopoverAlign.Start,
            trigger = {
                KButton(
                    text = if (open) "Close popover" else "Open popover",
                    onClick = { open = !open },
                    variant = KButtonVariant.Outline
                )
            }
        ) {
            KPopoverHeader {
                KPopoverTitle("Dimensions")
                KPopoverDescription("Set the dimensions for the layer.")
            }
            Box(Modifier.width(220.dp))
        }
    }
}

