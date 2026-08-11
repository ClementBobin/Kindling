package dev.kindling.android.helper.natif

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri

// ─────────────────────────────────────────────
//  SmsConfig
// ─────────────────────────────────────────────

/**
 * Décrit un SMS à envoyer.
 *
 * Presets :
 * - [SmsConfig.simple] → SMS texte standard
 *
 * Personnalisé :
 * ```kotlin
 * val sms = SmsConfig(
 *     to      = "+33612345678",
 *     body    = "Votre code OTP : 123456",
 *     split   = true
 * )
 * smsHelper.send(sms)
 * ```
 */
data class SmsConfig(
    val to: String,
    val body: String,
    val split: Boolean = true    // découpe automatiquement si > 160 chars
) {
    companion object {
        fun simple(to: String, body: String) = SmsConfig(to = to, body = body)
        fun otp(to: String, code: String)    = SmsConfig(to = to, body = "Votre code : $code")
    }
}

// ─────────────────────────────────────────────
//  SmsHelper
// ─────────────────────────────────────────────

/**
 * Helper SMS centralisé.
 *
 * Deux modes :
 * - **Envoi direct** ([send]) — nécessite `SEND_SMS`, envoie sans UI.
 * - **Composeur SMS** ([openComposer]) — ouvre l'app SMS, aucune permission.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { SmsHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // Ouvrir le composeur (pas de permission)
 * smsHelper.openComposer(SmsConfig.simple("+33612345678", "Bonjour !"))
 *
 * // Envoi direct (SEND_SMS requis)
 * smsHelper.send(SmsConfig.otp("+33612345678", "849201"))
 * ```
 */
class SmsHelper(context: Context) {

    internal val appContext = context.applicationContext

    // ── Send direct ───────────────────────────────────────────────────────────

    /**
     * Envoie le SMS décrit par [config] directement, sans UI.
     * Requiert `SEND_SMS`.
     * @throws IllegalStateException si le [SmsManager] système est indisponible.
     */
    @RequiresPermission(Manifest.permission.SEND_SMS)
    fun send(config: SmsConfig) {
        val manager: SmsManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            appContext.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        checkNotNull(manager) { "SmsManager unavailable on this device" }

        if (config.split) {
            val parts = manager.divideMessage(config.body)
            if (parts.size == 1) {
                manager.sendTextMessage(config.to, null, config.body, null, null)
            } else {
                manager.sendMultipartTextMessage(config.to, null, parts, null, null)
            }
        } else {
            manager.sendTextMessage(config.to, null, config.body, null, null)
        }
    }

    // ── Composer ─────────────────────────────────────────────────────────────

    /**
     * Ouvre l'app SMS par défaut avec le numéro et le corps pré-remplis.
     * Aucune permission requise. Si aucune app SMS n'est disponible,
     * attrape [ActivityNotFoundException] et journalise l'erreur sans planter.
     */
    fun openComposer(config: SmsConfig) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "smsto:${config.to}".toUri()
            putExtra("sms_body", config.body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            appContext.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Log.w("SmsHelper", "No SMS app available to handle ACTION_SENDTO")
        }
    }

    /** Ouvre le composeur avec un numéro uniquement. */
    fun openComposer(phoneNumber: String) =
        openComposer(SmsConfig.simple(phoneNumber, ""))
}