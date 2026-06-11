package dev.kindling.android.natif

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// ─────────────────────────────────────────────
//  PermissionStatus
// ─────────────────────────────────────────────

/**
 * Résultat d'une vérification ou d'une demande de permission.
 *
 * - [PermissionStatus.Granted]          → accordée
 * - [PermissionStatus.Denied]           → refusée (peut re-demander)
 * - [PermissionStatus.PermanentlyDenied] → refusée définitivement (ouvrir les réglages)
 */
sealed class PermissionStatus {
    data object Granted           : PermissionStatus()
    data object Denied            : PermissionStatus()
    data object PermanentlyDenied : PermissionStatus()
}

// ─────────────────────────────────────────────
//  PermissionRequest
// ─────────────────────────────────────────────

/**
 * Décrit une ou plusieurs permissions à demander.
 *
 * Presets courants :
 * - [PermissionRequest.Camera]
 * - [PermissionRequest.Microphone]
 * - [PermissionRequest.Notifications]  (API 33+)
 * - [PermissionRequest.Vibrate]
 * - [PermissionRequest.Internet]
 * - [PermissionRequest.Bluetooth]      (API 31+)
 *
 * Personnalisé :
 * ```kotlin
 * val request = PermissionRequest(android.Manifest.permission.READ_CONTACTS)
 * permissionHelper.check(request)
 * ```
 */
data class PermissionRequest(val permissions: List<String>) {
    constructor(vararg permissions: String) : this(permissions.toList())

    companion object {
        val Camera        = PermissionRequest(android.Manifest.permission.CAMERA)
        val Microphone    = PermissionRequest(android.Manifest.permission.RECORD_AUDIO)
        val Vibrate       = PermissionRequest(android.Manifest.permission.VIBRATE)
        val Internet      = PermissionRequest(android.Manifest.permission.INTERNET)

        val Notifications = PermissionRequest(
            if (android.os.Build.VERSION.SDK_INT >= 33)
                android.Manifest.permission.POST_NOTIFICATIONS
            else
                android.Manifest.permission.INTERNET // normal perm, always granted — placeholder
        )

        val Bluetooth = PermissionRequest(
            *if (android.os.Build.VERSION.SDK_INT >= 31) arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) else arrayOf(
                android.Manifest.permission.BLUETOOTH
            )
        )
    }
}

// ─────────────────────────────────────────────
//  PermissionHelper
// ─────────────────────────────────────────────

/**
 * Helper de permissions centralisé.
 *
 * Deux modes d'utilisation :
 *
 * **1. Vérification synchrone** (sans Activity) :
 * ```kotlin
 * val status = permissionHelper.check(PermissionRequest.Camera)
 * ```
 *
 * **2. Demande runtime** (nécessite une [FragmentActivity]) :
 * ```kotlin
 * // Dans onCreate ou onStart :
 * val launcher = permissionHelper.registerLauncher(activity) { results ->
 *     results.forEach { (permission, status) -> /* … */ }
 * }
 *
 * // Déclenchement :
 * permissionHelper.request(PermissionRequest.Camera, launcher)
 * ```
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { PermissionHelper(androidContext()) }
 * ```
 */
class PermissionHelper(context: Context) {

    internal val appContext = context.applicationContext

    // ── Synchronous check ─────────────────────────────────────────────────────

    /**
     * Vérifie le statut d'une [PermissionRequest] sans déclencher de dialogue.
     * Pour une request multi-permissions, retourne [PermissionStatus.Granted]
     * seulement si toutes les permissions sont accordées.
     */
    fun check(request: PermissionRequest): PermissionStatus {
        val allGranted = request.permissions.all {
            ContextCompat.checkSelfPermission(appContext, it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        return if (allGranted) PermissionStatus.Granted else PermissionStatus.Denied
    }

    /** `true` si toutes les permissions du [request] sont accordées. */
    fun isGranted(request: PermissionRequest): Boolean =
        check(request) == PermissionStatus.Granted

    // ── Runtime request ───────────────────────────────────────────────────────

    /**
     * Enregistre un launcher de permission à partir d'une [FragmentActivity].
     * À appeler dans `onCreate` ou `onStart`, avant que l'Activity soit started.
     *
     * @param activity L'Activity hôte.
     * @param onResult Callback avec un `Map<String, PermissionStatus>` pour chaque permission.
     */
    fun registerLauncher(
        activity: FragmentActivity,
        onResult: (Map<String, PermissionStatus>) -> Unit
    ): ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            val mapped = results.mapValues { (permission, granted) ->
                when {
                    granted -> PermissionStatus.Granted
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            activity.shouldShowRequestPermissionRationale(permission) ->
                        PermissionStatus.Denied
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.M ->
                        // API 21-22 : permissions accordées à l'install, ce cas ne devrait pas arriver
                        PermissionStatus.Denied
                    else -> PermissionStatus.PermanentlyDenied
                }
            }
            onResult(mapped)
        }

    /**
     * Déclenche la demande pour le [request] via le [launcher] fourni.
     * Les permissions déjà accordées sont filtrées automatiquement.
     */
    fun request(
        request: PermissionRequest,
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        val pending = request.permissions.filter {
            ContextCompat.checkSelfPermission(appContext, it) !=
                    PackageManager.PERMISSION_GRANTED
        }
        if (pending.isNotEmpty()) launcher.launch(pending.toTypedArray())
    }

    // ── Settings redirect ─────────────────────────────────────────────────────

    /**
     * Ouvre les réglages de l'app — utile après un [PermissionStatus.PermanentlyDenied].
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}