package dev.kindling.android.natif

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri

// ─────────────────────────────────────────────
//  NetworkType
// ─────────────────────────────────────────────

enum class NetworkType {
    Gsm, Cdma, Lte, Nr5G, Unknown
}

// ─────────────────────────────────────────────
//  TelephonyInfo
// ─────────────────────────────────────────────

/**
 * Informations réseau téléphonique courantes.
 *
 * @param simState      État de la SIM.
 * @param networkType   Type de réseau actif.
 * @param operatorName  Nom de l'opérateur (peut être vide en mode avion).
 * @param isRoaming     `true` si en itinérance.
 */
data class TelephonyInfo(
    val simState: SimState,
    val networkType: NetworkType,
    val operatorName: String,
    val isRoaming: Boolean
)

enum class SimState {
    Absent, Ready, PinRequired, PukRequired, NetworkLocked, Unknown
}

// ─────────────────────────────────────────────
//  TelephonyHelper
// ─────────────────────────────────────────────

/**
 * Helper téléphonique centralisé.
 *
 * Permissions requises selon l'usage :
 * - `READ_PHONE_STATE`  → état SIM, type réseau, isRoaming
 * - `CALL_PHONE`        → lancer un appel direct
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { TelephonyHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * val info = telephonyHelper.getInfo()
 * println("Opérateur : ${info.operatorName} — ${info.networkType}")
 *
 * // Ouvrir le composeur (pas de permission)
 * telephonyHelper.openDialer(context, "+33612345678")
 *
 * // Appel direct (CALL_PHONE requis)
 * telephonyHelper.callDirect(context, "+33612345678")
 * ```
 */
class TelephonyHelper(context: Context) {

    internal val appContext       = context.applicationContext
    internal val telephonyManager =
        appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    // ── Info ──────────────────────────────────────────────────────────────────

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getInfo(): TelephonyInfo = TelephonyInfo(
        simState     = telephonyManager.simState.toSimState(),
        networkType  = getNetworkType(),
        operatorName = telephonyManager.networkOperatorName ?: "",
        isRoaming    = telephonyManager.isNetworkRoaming
    )

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getNetworkType(): NetworkType {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            telephonyManager.dataNetworkType
        else
            telephonyManager.networkType

        return when (type) {
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GSM    -> NetworkType.Gsm
            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA   -> NetworkType.Gsm
            TelephonyManager.NETWORK_TYPE_LTE    -> NetworkType.Lte
            TelephonyManager.NETWORK_TYPE_NR     -> NetworkType.Nr5G
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A -> NetworkType.Cdma
            else                                 -> NetworkType.Unknown
        }
    }

    fun getOperatorName(): String = telephonyManager.networkOperatorName ?: ""

    fun isRoaming(): Boolean = telephonyManager.isNetworkRoaming

    // ── Dialer / Call ─────────────────────────────────────────────────────────

    /** Ouvre le composeur pré-rempli — aucune permission requise. */
    fun openDialer(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, "tel:$phoneNumber".toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Lance un appel direct — nécessite `CALL_PHONE`. */
    @RequiresPermission(Manifest.permission.CALL_PHONE)
    fun callDirect(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL, "tel:$phoneNumber".toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun Int.toSimState(): SimState = when (this) {
        TelephonyManager.SIM_STATE_ABSENT         -> SimState.Absent
        TelephonyManager.SIM_STATE_READY          -> SimState.Ready
        TelephonyManager.SIM_STATE_PIN_REQUIRED   -> SimState.PinRequired
        TelephonyManager.SIM_STATE_PUK_REQUIRED   -> SimState.PukRequired
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> SimState.NetworkLocked
        else                                      -> SimState.Unknown
    }
}