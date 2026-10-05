package dev.kindling.compose.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val kIoDispatcher: CoroutineDispatcher get() = Dispatchers.Default
