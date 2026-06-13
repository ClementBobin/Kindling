package dev.kindling.android.natif

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build

// ─────────────────────────────────────────────
//  ClipboardContent
// ─────────────────────────────────────────────

/**
 * Décrit le contenu à copier dans le presse-papiers.
 *
 * Presets :
 * - [ClipboardContent.PlainText] → texte brut avec label générique
 *
 * Personnalisé :
 * ```kotlin
 * val content = ClipboardContent(label = "Code promo", text = "KINDLING20")
 * clipboardHelper.copy(content)
 * ```
 */
data class ClipboardContent(
    val label: String,
    val text: String
) {
    companion object {
        fun plainText(text: String) = ClipboardContent(label = "text", text = text)
        fun url(url: String)        = ClipboardContent(label = "url",  text = url)
        fun email(email: String)    = ClipboardContent(label = "email", text = email)
    }
}

// ─────────────────────────────────────────────
//  ClipboardHelper
// ─────────────────────────────────────────────

/**
 * Helper presse-papiers centralisé.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { ClipboardHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * clipboardHelper.copy(ClipboardContent.plainText("hello"))
 * clipboardHelper.copy("https://kindling.dev")  // label générique "text"
 * val text = clipboardHelper.paste()   // null si vide ou non-texte
 * clipboardHelper.clear()              // API 28+ seulement, no-op sinon
 * ```
 */
class ClipboardHelper(context: Context) {

    internal val clipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: throw IllegalStateException("ClipboardManager not available")

    // ── Public API ────────────────────────────────────────────────────────────

    /** Copie [content] dans le presse-papiers. */
    fun copy(content: ClipboardContent) {
        val clip = ClipData.newPlainText(content.label, content.text)
        clipboard.setPrimaryClip(clip)
    }

    fun copy(text: String) = copy(ClipboardContent.plainText(text))

    /**
     * Lit le texte courant du presse-papiers.
     * Retourne `null` si vide, non-texte, ou accès refusé (API 29+ background).
     */
    fun paste(): String? = try {
        clipboard.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.text
            ?.toString()
    } catch (_: SecurityException) {
        null
    }

    /** Vide le presse-papiers (API 28+, no-op sur les versions antérieures). */
    fun clear() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboard.clearPrimaryClip()
        }
    }

    /** `true` si le presse-papiers contient du texte accessible. */
    fun hasText(): Boolean =
        clipboard.hasPrimaryClip() &&
                clipboard.primaryClipDescription
                    ?.hasMimeType(android.content.ClipDescription.MIMETYPE_TEXT_PLAIN) == true
}