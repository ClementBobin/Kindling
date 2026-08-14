package dev.kindling.android.natif

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

// ─────────────────────────────────────────────
//  FileDestination
// ─────────────────────────────────────────────

/**
 * Describes the destination of a file in Android's MediaStore.
 *
 * Use the provided presets:
 * - [FileDestination.Downloads] -> Downloads folder.
 * - [FileDestination.Pictures]  -> Pictures folder.
 * - [FileDestination.Documents] -> Documents folder.
 */
data class FileDestination(
    val collection: Uri,
    val relativePath: String
) {
    companion object {
        val Downloads = FileDestination(
            collection   = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            else MediaStore.Files.getContentUri("external"),
            relativePath = Environment.DIRECTORY_DOWNLOADS
        )
        val Pictures = FileDestination(
            collection   = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            relativePath = Environment.DIRECTORY_PICTURES
        )
        val Documents = FileDestination(
            collection   = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else MediaStore.Files.getContentUri("external"),
            relativePath = Environment.DIRECTORY_DOCUMENTS
        )
    }
}

// ─────────────────────────────────────────────
//  FileHelper
// ─────────────────────────────────────────────

/**
 * Centralized file helper for Android (MediaStore + Storage Access Framework).
 *
 * Provides two main approaches:
 * - **MediaStore** (API 29+): For writing to public folders without explicit storage permissions.
 * - **SAF (Storage Access Framework)**: System file picker for reading/creating files.
 *
 * ### Example usage:
 * ```kotlin
 * val fileHelper = FileHelper(context)
 * 
 * // Save to Downloads using MediaStore
 * fileHelper.saveToMediaStore(
 *     context = context,
 *     fileName = "export.csv",
 *     mimeType = "text/csv",
 *     destination = FileDestination.Downloads
 * ) { outputStream ->
 *     outputStream.write("id,name\n1,Kindling".toByteArray())
 * }
 * 
 * // Open system file picker
 * val launcher = fileHelper.registerPickerLauncher(activity) { uri ->
 *     uri?.let { /* Read file */ }
 * }
 * fileHelper.openPicker(launcher, mimeType = "application/pdf")
 * ```
 */
class FileHelper(context: Context) {

    internal val appContext = context.applicationContext

    // ── MediaStore write ──────────────────────────────────────────────────────

    /**
     * Crée un fichier dans MediaStore et écrit via [block].
     * Retourne l'[Uri] du fichier créé, ou `null` en cas d'erreur.
     *
     * La ligne MediaStore est supprimée automatiquement si le flux ne peut pas
     * être ouvert ou si [block] lève une exception, évitant toute ligne orpheline.
     * IS_PENDING n'est remis à 0 qu'après une écriture réussie.
     */
    fun saveToMediaStore(
        context: Context,
        fileName: String,
        mimeType: String,
        destination: FileDestination,
        block: (OutputStream) -> Unit
    ): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, destination.relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(destination.collection, values) ?: return null

        try {
            val stream = resolver.openOutputStream(uri)
                ?: run {
                    resolver.delete(uri, null, null)
                    return null
                }
            stream.use(block)
        } catch (t: Throwable) {
            // Écriture échouée ou stream fermé anormalement : supprime la ligne orpheline.
            resolver.delete(uri, null, null)
            return null
        }

        // Écriture réussie : rendre le fichier visible.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pending = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            resolver.update(uri, pending, null, null)
        }
        return uri
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /** Lit le contenu d'un [Uri] en bytes. Retourne `null` en cas d'erreur. */
    fun readBytes(context: Context, uri: Uri): ByteArray? = runCatching {
        context.contentResolver.openInputStream(uri)?.use(InputStream::readBytes)
    }.getOrNull()

    /** Lit le contenu d'un [Uri] en texte. Retourne `null` en cas d'erreur. */
    fun readText(context: Context, uri: Uri, charset: Charset = Charsets.UTF_8): String? =
        readBytes(context, uri)?.toString(charset)

    // ── SAF picker ────────────────────────────────────────────────────────────

    /**
     * Enregistre un launcher SAF pour sélectionner un fichier.
     * À appeler depuis `onCreate`.
     */
    fun registerPickerLauncher(
        activity: FragmentActivity,
        onResult: (Uri?) -> Unit
    ): ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.OpenDocument(), onResult
        )

    /** Ouvre le sélecteur de fichiers système. */
    fun openPicker(
        launcher: ActivityResultLauncher<Array<String>>,
        mimeType: String = "*/*"
    ) = launcher.launch(arrayOf(mimeType))

    /**
     * Enregistre un launcher pour créer un fichier (SAF).
     * À appeler depuis `onCreate`.
     */
    fun registerCreateLauncher(
        activity: FragmentActivity,
        onResult: (Uri?) -> Unit
    ): ActivityResultLauncher<String> =
        activity.registerForActivityResult(
            ActivityResultContracts.CreateDocument("*/*"), onResult
        )

    // ── App-private cache ─────────────────────────────────────────────────────

    /** Retourne le répertoire cache interne de l'app (pas de permission requise). */
    fun getCacheDir(): File = appContext.cacheDir

    /** Crée un fichier temporaire dans le cache interne. */
    fun createTempFile(prefix: String, suffix: String = ".tmp"): File =
        File.createTempFile(prefix, suffix, appContext.cacheDir)
}