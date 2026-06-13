package dev.kindling.android.natif

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// ─────────────────────────────────────────────
//  ShareContent
// ─────────────────────────────────────────────

/**
 * Décrit le contenu à partager via le système Android.
 *
 * Presets :
 * - [ShareContent.text]  → texte brut
 * - [ShareContent.url]   → lien web
 * - [ShareContent.file]  → fichier unique via FileProvider
 *
 * Personnalisé :
 * ```kotlin
 * val content = ShareContent(
 *     mimeType  = "image/png",
 *     subject   = "Ma capture",
 *     text      = "Voici ma capture d'écran",
 *     fileUri   = uri
 * )
 * shareHelper.share(content)
 * ```
 */
data class ShareContent(
    val mimeType: String      = "text/plain",
    val subject: String       = "",
    val text: String          = "",
    val fileUri: Uri?         = null,
    val chooserTitle: String  = "Partager via"
) {
    companion object {
        fun text(text: String, subject: String = "", chooserTitle: String = "Partager via") =
            ShareContent(mimeType = "text/plain", subject = subject, text = text, chooserTitle = chooserTitle)

        fun url(url: String, subject: String = "") =
            ShareContent(mimeType = "text/plain", subject = subject, text = url)

        fun file(
            uri: Uri,
            mimeType: String,
            subject: String = "",
            text: String = ""
        ) = ShareContent(mimeType = mimeType, subject = subject, text = text, fileUri = uri)
    }
}

// ─────────────────────────────────────────────
//  ShareHelper
// ─────────────────────────────────────────────

/**
 * Helper de partage système centralisé.
 *
 * Aucune permission requise pour le partage de texte/URL.
 * Pour les fichiers, déclarer un FileProvider dans le manifest de l'app.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { ShareHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * shareHelper.share(ShareContent.text("Découvrez Kindling !"))
 * shareHelper.share(ShareContent.url("https://github.com/ClementBobin/Kindling"))
 *
 * // Partager un fichier
 * val uri = shareHelper.getFileUri(file, "com.example.app.fileprovider")
 * shareHelper.share(ShareContent.file(uri, "image/png"))
 *
 * // Ouvrir directement dans une app spécifique
 * shareHelper.shareToApp(ShareContent.text("hello"), "com.whatsapp")
 * ```
 */
class ShareHelper(context: Context) {

    internal val appContext = context.applicationContext

    // ── Share ─────────────────────────────────────────────────────────────────

    /** Ouvre le sélecteur de partage système. */
    fun share(content: ShareContent) {
        appContext.startActivity(
            Intent.createChooser(content.toIntent(), content.chooserTitle)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    /**
     * Partage directement vers une app spécifique (par package).
     * Tente de lancer l'intent explicitement ; si l'app n'est pas installée ou
     * n'est pas visible (Android 11+), attrape [ActivityNotFoundException] et
     * ouvre le sélecteur standard à la place.
     */
    fun shareToApp(content: ShareContent, targetPackage: String) {
        val intent = content.toIntent().apply {
            setPackage(targetPackage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            appContext.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            share(content)
        }
    }

    // ── File URI helper ───────────────────────────────────────────────────────

    /**
     * Convertit un [File] en [Uri] partageable via FileProvider.
     * [authority] doit correspondre à l'authority déclarée dans le manifest.
     */
    fun getFileUri(file: File, authority: String): Uri =
        FileProvider.getUriForFile(appContext, authority, file)

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun ShareContent.toIntent(): Intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        if (subject.isNotBlank()) putExtra(Intent.EXTRA_SUBJECT, subject)
        if (text.isNotBlank())    putExtra(Intent.EXTRA_TEXT, text)
        fileUri?.let {
            putExtra(Intent.EXTRA_STREAM, it)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}