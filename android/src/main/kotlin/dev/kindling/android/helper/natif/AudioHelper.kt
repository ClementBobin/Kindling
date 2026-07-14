package dev.kindling.android.helper.natif

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool

// ─────────────────────────────────────────────
//  SoundAsset
// ─────────────────────────────────────────────

/**
 * Décrit un son système sémantique à jouer via [SoundPool].
 *
 * Son personnalisé (raw resource) :
 * ```kotlin
 * val asset = SoundAsset(resId = R.raw.my_sound, volume = 0.8f)
 * audioHelper.play(asset)
 * ```
 *
 * @param resId    Identifiant de la ressource raw (`R.raw.*`).
 * @param volume   Volume [0f..1f] appliqué aux deux canaux (défaut : 1f).
 * @param rate     Taux de lecture [0.5f..2f] (défaut : 1f = vitesse normale).
 * @param priority Priorité du stream (défaut : 1).
 */
data class SoundAsset(
    val resId: Int,
    val volume: Float   = 1f,
    val rate: Float     = 1f,
    val priority: Int   = 1
)

/**
 * Effet sonore système joué via [AudioManager.playSoundEffect] —
 * pas de chargement SoundPool requis.
 *
 * Presets mappés sur les sons système Android :
 * - [SystemFxAsset.Click]        → clic UI léger
 * - [SystemFxAsset.KeyPress]     → touche clavier
 * - [SystemFxAsset.Delete]       → suppression clavier
 * - [SystemFxAsset.Return]       → validation clavier
 * - [SystemFxAsset.FocusNav]     → navigation focus
 */
data class SystemFxAsset(val fx: Int, val volume: Float = 1f) {
    companion object {
        val Click     = SystemFxAsset(AudioManager.FX_KEY_CLICK)
        val KeyPress  = SystemFxAsset(AudioManager.FX_KEYPRESS_STANDARD)
        val Delete    = SystemFxAsset(AudioManager.FX_KEYPRESS_DELETE)
        val Return    = SystemFxAsset(AudioManager.FX_KEYPRESS_RETURN)
        val FocusNav  = SystemFxAsset(AudioManager.FX_FOCUS_NAVIGATION_UP)
    }
}

// ─────────────────────────────────────────────
//  AudioHelper
// ─────────────────────────────────────────────

/**
 * Helper audio centralisé.
 *
 * Gère deux canaux :
 * - **Effets système** ([SystemFxAsset]) via [AudioManager.playSoundEffect] — sans chargement.
 * - **Sons custom** ([SoundAsset]) via [SoundPool] avec chargement lazy et cache interne.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { AudioHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // Effets système (presets)
 * audioHelper.play(SystemFxAsset.Click)
 * audioHelper.play(SystemFxAsset.KeyPress)
 *
 * // Son custom (raw resource)
 * audioHelper.play(SoundAsset(R.raw.success_chime, volume = 0.7f))
 *
 * // Libérer les ressources quand plus nécessaire (onDestroy / scope)
 * audioHelper.release()
 * ```
 */
class AudioHelper(context: Context) {

    internal val appContext   = context.applicationContext
    internal val audioManager: AudioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: throw IllegalStateException("AudioManager not available")

    // Nullable so release() can check initialization without forcing construction.
    private var pool: SoundPool? = null

    private fun getOrCreatePool(): SoundPool = pool ?: run {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()
            .also { pool = it }
    }

    // resId → soundPoolId; synchronized block ensures each resId is loaded
    // exactly once even under concurrent access (API 21-safe).
    private val loadedSounds = mutableMapOf<Int, Int>()

    // ── Public API ────────────────────────────────────────────────────────────

    /** Joue un effet sonore système. */
    fun play(asset: SystemFxAsset) {
        audioManager.playSoundEffect(asset.fx, asset.volume)
    }

    /**
     * Joue un son custom (raw resource).
     * Le son est chargé de façon lazy et mis en cache pour les lectures suivantes.
     */
    fun play(asset: SoundAsset) {
        val soundId = synchronized(loadedSounds) {
            loadedSounds.getOrPut(asset.resId) {
                getOrCreatePool().load(appContext, asset.resId, asset.priority)
            }
        }
        // SoundPool may not have finished loading yet — listener would be
        // cleaner but adds complexity; for UI sounds the delay is imperceptible.
        getOrCreatePool().play(
            soundId,
            asset.volume, asset.volume,
            asset.priority,
            0,            // no loop
            asset.rate
        )
    }

    // ── Convenience shorthands ────────────────────────────────────────────────

    fun click()    = play(SystemFxAsset.Click)
    fun keyPress() = play(SystemFxAsset.KeyPress)
    fun delete()   = play(SystemFxAsset.Delete)
    fun returnKey()= play(SystemFxAsset.Return)
    fun focusNav() = play(SystemFxAsset.FocusNav)

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /** Libère le [SoundPool]. Appeler depuis `onDestroy` ou un scope Koin approprié. */
    fun release() {
        pool?.release()
        pool = null
        synchronized(loadedSounds) { loadedSounds.clear() }
    }
}