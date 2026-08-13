package dev.kindling.core.components.ui.toast

import kotlin.random.Random

data class KToastData(
    val id: Long = Random.nextLong(),
    val message: String,
    val description: String? = null,
    val type: KToastType = KToastType.Default,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val durationMs: Long = 4_000L
)