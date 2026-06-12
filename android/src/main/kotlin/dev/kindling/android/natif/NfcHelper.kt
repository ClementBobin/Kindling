package dev.kindling.android.natif

import android.app.Activity
import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import androidx.core.net.toUri

// ─────────────────────────────────────────────
//  NfcState
// ─────────────────────────────────────────────

sealed class NfcState {
    data object Enabled     : NfcState()
    data object Disabled    : NfcState()
    data object Unavailable : NfcState()
}

// ─────────────────────────────────────────────
//  NfcReadConfig
// ─────────────────────────────────────────────

/**
 * Décrit la configuration d'une session de lecture NFC.
 *
 * Presets :
 * - [NfcReadConfig.Default] → lecture NDEF standard
 */
data class NfcReadConfig(
    val readerFlags: Int = NfcAdapter.FLAG_READER_NFC_A
            or NfcAdapter.FLAG_READER_NFC_B
            or NfcAdapter.FLAG_READER_NFC_F
            or NfcAdapter.FLAG_READER_NFC_V,
    val presenceCheckDelay: Int = 300
) {
    companion object {
        val Default = NfcReadConfig()
        val NdefOnly = NfcReadConfig(
            readerFlags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B
        )
    }
}

// ─────────────────────────────────────────────
//  NfcTagResult
// ─────────────────────────────────────────────

/** Résultat d'une lecture de tag NFC. */
sealed class NfcTagResult {
    data class NdefText(val text: String)           : NfcTagResult()
    data class NdefUri(val uri: String)             : NfcTagResult()
    data class RawTag(val tag: Tag)                 : NfcTagResult()
    data class Error(val message: String)           : NfcTagResult()
}

// ─────────────────────────────────────────────
//  NfcHelper
// ─────────────────────────────────────────────

/**
 * Helper NFC centralisé.
 *
 * Permission requise : `android.permission.NFC`
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { NfcHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // Vérifier la disponibilité
 * when (nfcHelper.getState()) {
 *     NfcState.Enabled     -> startReading()
 *     NfcState.Disabled    -> promptEnableNfc()
 *     NfcState.Unavailable -> showUnsupported()
 * }
 *
 * // Lire des tags (dans onResume / depuis un scope)
 * nfcHelper.tagFlow(activity, NfcReadConfig.Default)
 *     .onEach { result -> when (result) {
 *         is NfcTagResult.NdefText -> handleText(result.text)
 *         is NfcTagResult.NdefUri  -> handleUri(result.uri)
 *         is NfcTagResult.RawTag   -> handleRaw(result.tag)
 *         is NfcTagResult.Error    -> showError(result.message)
 *     }}
 *     .launchIn(lifecycleScope)
 * ```
 */
class NfcHelper(context: Context) {

    internal val appContext = context.applicationContext
    internal val nfcManager = appContext.getSystemService(Context.NFC_SERVICE) as? NfcManager
    internal val adapter: NfcAdapter? = nfcManager?.defaultAdapter

    // ── State ─────────────────────────────────────────────────────────────────

    fun getState(): NfcState = when {
        adapter == null    -> NfcState.Unavailable
        adapter.isEnabled  -> NfcState.Enabled
        else               -> NfcState.Disabled
    }

    fun isAvailable(): Boolean  = adapter != null
    fun isEnabled(): Boolean    = adapter?.isEnabled == true

    /** Ouvre les réglages NFC système. */
    fun openSettings(context: Context) {
        context.startActivity(
            android.content.Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    // ── Reader mode flow ──────────────────────────────────────────────────────

    /**
     * Flow émettant un [NfcTagResult] à chaque tag détecté.
     * Active le reader mode sur l'[activity] à la souscription,
     * le désactive à la fermeture du flow.
     */
    fun tagFlow(activity: Activity, config: NfcReadConfig = NfcReadConfig.Default): Flow<NfcTagResult> =
        callbackFlow {
            val a = adapter ?: run { close(); return@callbackFlow }

            val callback = NfcAdapter.ReaderCallback { tag ->
                trySend(tag.toResult())
            }

            val extras = Bundle().apply {
                putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, config.presenceCheckDelay)
            }

            a.enableReaderMode(activity, callback, config.readerFlags, extras)

            awaitClose { a.disableReaderMode(activity) }
        }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Écrit un message texte NDEF sur le [tag].
     * Retourne `true` si l'écriture a réussi, `false` si le tag n'est pas NDEF,
     * n'est pas accessible en écriture, ou si une erreur survient.
     */
    fun writeText(tag: Tag, text: String, languageCode: String = "en"): Boolean = runCatching {
        val record  = NdefRecord.createTextRecord(languageCode, text)
        writeNdefMessage(tag, NdefMessage(arrayOf(record)))
    }.getOrDefault(false)

    /**
     * Écrit une URI NDEF sur le [tag].
     * Retourne `true` si l'écriture a réussi, `false` si le tag n'est pas NDEF,
     * n'est pas accessible en écriture, ou si une erreur survient.
     */
    fun writeUri(tag: Tag, uri: String): Boolean = runCatching {
        val record = NdefRecord.createUri(uri.toUri())
        writeNdefMessage(tag, NdefMessage(arrayOf(record)))
    }.getOrDefault(false)

    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * Obtient un handle [Ndef] sur [tag], vérifie qu'il est accessible en
     * écriture, puis écrit [message].
     *
     * Retourne `false` sans exception si :
     * - le tag ne supporte pas NDEF ([Ndef.get] retourne `null`)
     * - le tag est en lecture seule ([Ndef.isWritable] == `false`)
     *
     * Toute autre erreur I/O est laissée remonter pour être capturée par le
     * `runCatching` de l'appelant.
     */
    private fun writeNdefMessage(tag: Tag, message: NdefMessage): Boolean {
        val ndef = Ndef.get(tag) ?: return false
        return ndef.use { n ->
            if (!n.isWritable) return false
            n.connect()
            n.writeNdefMessage(message)
            true
        }
    }

    private fun Tag.toResult(): NfcTagResult = runCatching {
        val ndef = Ndef.get(this) ?: return NfcTagResult.RawTag(this)
        ndef.use { n ->
            n.connect()
            val message = n.ndefMessage ?: return NfcTagResult.RawTag(this)
            val record  = message.records.firstOrNull() ?: return NfcTagResult.RawTag(this)

            when (record.tnf) {
                NdefRecord.TNF_WELL_KNOWN -> {
                    when {
                        record.type.contentEquals(NdefRecord.RTD_TEXT) -> {
                            val payload = record.payload
                            val encoding = if (payload[0].toInt() and 0x80 == 0) "UTF-8" else "UTF-16"
                            val langLen  = payload[0].toInt() and 0x3F
                            NfcTagResult.NdefText(
                                String(payload, 1 + langLen, payload.size - 1 - langLen, charset(encoding))
                            )
                        }
                        record.type.contentEquals(NdefRecord.RTD_URI) -> {
                            NfcTagResult.NdefUri(record.toUri()?.toString() ?: "")
                        }
                        else -> NfcTagResult.RawTag(this)
                    }
                }
                NdefRecord.TNF_ABSOLUTE_URI -> NfcTagResult.NdefUri(record.toUri()?.toString() ?: "")
                else                        -> NfcTagResult.RawTag(this)
            }
        }
    }.getOrElse { NfcTagResult.Error(it.message ?: "Unknown NFC error") }
}