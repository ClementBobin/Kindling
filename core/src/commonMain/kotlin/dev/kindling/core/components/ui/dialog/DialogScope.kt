package dev.kindling.core.components.ui.dialog

/** Slot structure passed as receiver to [Dialog] content. */
class DialogScope internal constructor(val onDismiss: () -> Unit)