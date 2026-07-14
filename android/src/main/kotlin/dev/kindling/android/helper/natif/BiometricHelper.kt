package dev.kindling.android.helper.natif

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// ─────────────────────────────────────────────
//  BiometricConfig
// ─────────────────────────────────────────────

/**
 * Décrit la configuration du dialogue biométrique.
 *
 * Presets :
 * - [BiometricConfig.Strong]      → empreinte / face 3D uniquement
 * - [BiometricConfig.StrongOrPin] → biométrie forte OU PIN/schéma/mot de passe
 *
 * Personnalisé :
 * ```kotlin
 * val config = BiometricConfig(
 *     title       = "Connexion sécurisée",
 *     subtitle    = "Confirmez votre identité",
 *     description = "Utilisez votre empreinte pour vous connecter à Cyna.",
 *     negativeButtonText = "Annuler",
 *     allowedAuthenticators = Authenticators.BIOMETRIC_STRONG
 * )
 * ```
 */
data class BiometricConfig(
    val title: String,
    val subtitle: String                = "",
    val description: String             = "",
    val negativeButtonText: String      = "Annuler",
    val allowedAuthenticators: Int      = Authenticators.BIOMETRIC_STRONG
            or Authenticators.DEVICE_CREDENTIAL
) {
    companion object {
        fun strong(title: String, subtitle: String = "", description: String = "") =
            BiometricConfig(
                title                = title,
                subtitle             = subtitle,
                description          = description,
                negativeButtonText   = "Annuler",
                allowedAuthenticators = Authenticators.BIOMETRIC_STRONG
            )

        fun strongOrPin(title: String, subtitle: String = "", description: String = "") =
            BiometricConfig(
                title                = title,
                subtitle             = subtitle,
                description          = description,
                allowedAuthenticators = Authenticators.BIOMETRIC_STRONG
                        or Authenticators.DEVICE_CREDENTIAL
            )
    }
}

// ─────────────────────────────────────────────
//  BiometricResult
// ─────────────────────────────────────────────

/**
 * Résultat d'une authentification biométrique.
 *
 * - [BiometricResult.Success]          → authentification réussie
 * - [BiometricResult.Error]            → erreur système (message fourni)
 * - [BiometricResult.Failed]           → tentative échouée (mauvaise empreinte, etc.)
 * - [BiometricResult.Unavailable]      → biométrie non disponible sur l'appareil
 * - [BiometricResult.NoneEnrolled]     → aucune biométrie enregistrée
 */
sealed class BiometricResult {
    data object Success                         : BiometricResult()
    data class  Error(val message: String)      : BiometricResult()
    data object Failed                          : BiometricResult()
    data object Unavailable                     : BiometricResult()
    data object NoneEnrolled                    : BiometricResult()
}

// ─────────────────────────────────────────────
//  BiometricHelper
// ─────────────────────────────────────────────

/**
 * Helper d'authentification biométrique centralisé.
 *
 * Nécessite `androidx.biometric:biometric` dans les dépendances du module :
 * ```kotlin
 * implementation("androidx.biometric:biometric:1.2.0-alpha05")
 * ```
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { BiometricHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // Vérifier la disponibilité
 * val available = biometricHelper.canAuthenticate(BiometricConfig.strongOrPin("Connexion"))
 *
 * // Lancer l'authentification (depuis un Fragment ou une Activity)
 * biometricHelper.authenticate(
 *     activity = this,
 *     config   = BiometricConfig.strongOrPin(
 *         title    = "Connexion Cyna",
 *         subtitle = "Confirmez votre identité"
 *     )
 * ) { result ->
 *     when (result) {
 *         BiometricResult.Success      -> navigateToHome()
 *         is BiometricResult.Error     -> showError(result.message)
 *         BiometricResult.Failed       -> showRetry()
 *         BiometricResult.NoneEnrolled -> promptEnrollment()
 *         BiometricResult.Unavailable  -> fallbackToPassword()
 *     }
 * }
 * ```
 */
class BiometricHelper(context: Context) {

    internal val appContext      = context.applicationContext
    internal val biometricManager = BiometricManager.from(appContext)

    // ── Availability ──────────────────────────────────────────────────────────

    /**
     * Vérifie si l'authentification est possible avec le [config] fourni.
     * Retourne `true` uniquement si l'appareil supporte et a des biométries enregistrées.
     */
    fun canAuthenticate(config: BiometricConfig): Boolean =
        biometricManager.canAuthenticate(config.allowedAuthenticators) ==
                BiometricManager.BIOMETRIC_SUCCESS

    /**
     * Résolution détaillée de la disponibilité :
     * - `BIOMETRIC_SUCCESS`          → prêt
     * - `BIOMETRIC_ERROR_NONE_ENROLLED` → aucune biométrie enregistrée
     * - `BIOMETRIC_ERROR_NO_HARDWARE` / `BIOMETRIC_ERROR_HW_UNAVAILABLE` → non disponible
     */
    fun availabilityStatus(config: BiometricConfig): Int =
        biometricManager.canAuthenticate(config.allowedAuthenticators)

    // ── Authentication ────────────────────────────────────────────────────────

    /**
     * Lance le dialogue d'authentification biométrique.
     *
     * @param activity L'Activity hôte (doit être en premier plan).
     * @param config   Configuration du dialogue.
     * @param onResult Callback appelé sur le thread principal avec le [BiometricResult].
     */
    fun authenticate(
        activity: FragmentActivity,
        config: BiometricConfig,
        onResult: (BiometricResult) -> Unit
    ) {
        when (val status = biometricManager.canAuthenticate(config.allowedAuthenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> { /* proceed */ }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onResult(BiometricResult.NoneEnrolled)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                onResult(BiometricResult.Unavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED,
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                onResult(BiometricResult.Unavailable)
                return
            }
            else -> {
                onResult(BiometricResult.Error("Unknown biometric status: $status"))
                return
            }
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(BiometricResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(BiometricResult.Error(errString.toString()))
            }

            override fun onAuthenticationFailed() {
                onResult(BiometricResult.Failed)
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(config.title)
            .setSubtitle(config.subtitle)
            .setDescription(config.description)
            .setAllowedAuthenticators(config.allowedAuthenticators)
            .apply {
                // negativeButtonText is incompatible with DEVICE_CREDENTIAL
                val hasDeviceCred = config.allowedAuthenticators and
                        Authenticators.DEVICE_CREDENTIAL != 0
                if (!hasDeviceCred) setNegativeButtonText(config.negativeButtonText)
            }
            .build()

        prompt.authenticate(promptInfo)
    }
}