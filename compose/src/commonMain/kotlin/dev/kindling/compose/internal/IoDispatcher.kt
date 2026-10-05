package dev.kindling.compose.internal

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Dispatcher for blocking / IO-bound work.
 *
 * `Dispatchers.IO` does not exist in common code (it is JVM/Native only), so each
 * platform provides its own: `Dispatchers.IO` on Android, desktop and iOS, and
 * `Dispatchers.Default` on the web, which has no thread pool to offload to.
 */
internal expect val kIoDispatcher: CoroutineDispatcher
