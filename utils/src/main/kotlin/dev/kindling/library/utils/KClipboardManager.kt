package dev.kindling.utils

import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Wrapper around Android's [AndroidClipboardManager].
 *
 * Android equivalent of the `useCopyToClipboard` and `useClipboardPaste` React hooks.
 *
 * ```kotlin
 * val clipboard = KClipboardManager(context)
 *
 * // Copy:
 * val success = clipboard.copy("Hello, World!")
 *
 * // Observe the last copied text:
 * clipboard.lastCopied.collect { text -> showCopyConfirmation(text) }
 *
 * // Read the current clipboard content:
 * val current = clipboard.read()
 * ```
 *
 * @param context Any [Context]; the application context is used internally.
 * @param label   Label attached to clipboard items. Default: "Copied text".
 */
class KClipboardManager(
    context: Context,
    private val label: String = "Copied text",
) {
    private val manager = context.applicationContext
        .getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager

    private val _lastCopied = MutableStateFlow<String?>(null)

    /**
     * The most recently copied text via [copy], or `null` if nothing has been
     * copied in this session.
     */
    val lastCopied: StateFlow<String?> = _lastCopied.asStateFlow()

    /**
     * Copies [text] to the system clipboard.
     *
     * @return `true` on success, `false` if an exception was thrown.
     */
    fun copy(text: String): Boolean = runCatching {
        val clip = ClipData.newPlainText(label, text)
        manager.setPrimaryClip(clip)
        _lastCopied.value = text
    }.isSuccess

    /**
     * Reads and returns the current primary clip as a plain string,
     * or `null` when the clipboard is empty or contains non-text data.
     */
    fun read(): String? = runCatching {
        val clip = manager.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        clip.getItemAt(0).coerceToText(null)?.toString()
    }.getOrNull()

    /** Clears the [lastCopied] state (does not affect the system clipboard). */
    fun clearLastCopied() { _lastCopied.value = null }

    /** `true` when the system clipboard currently has a text item. */
    val hasText: Boolean get() = manager.hasPrimaryClip() &&
            manager.primaryClipDescription?.hasMimeType("text/*") == true
}