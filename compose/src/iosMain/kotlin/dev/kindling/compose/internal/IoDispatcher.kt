package dev.kindling.compose.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual val kIoDispatcher: CoroutineDispatcher get() = Dispatchers.IO
