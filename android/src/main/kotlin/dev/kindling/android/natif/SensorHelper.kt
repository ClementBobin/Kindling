package dev.kindling.android.natif

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// ─────────────────────────────────────────────
//  SensorConfig
// ─────────────────────────────────────────────

/**
 * Décrit un capteur et sa fréquence d'échantillonnage.
 *
 * Presets :
 * - [SensorConfig.Accelerometer] → accéléromètre (détection mouvement / secousse)
 * - [SensorConfig.Gyroscope]     → gyroscope (rotation)
 * - [SensorConfig.Magnetometer]  → magnétomètre (boussole)
 * - [SensorConfig.Light]         → capteur de luminosité ambiante
 * - [SensorConfig.Proximity]     → capteur de proximité
 * - [SensorConfig.Pressure]      → baromètre
 * - [SensorConfig.StepCounter]   → compteur de pas (API 19+)
 * - [SensorConfig.Rotation]      → vecteur de rotation
 *
 * Personnalisé :
 * ```kotlin
 * val config = SensorConfig(
 *     sensorType  = Sensor.TYPE_GRAVITY,
 *     samplingUs  = SensorManager.SENSOR_DELAY_NORMAL
 * )
 * ```
 */
data class SensorConfig(
    val sensorType: Int,
    val samplingUs: Int = SensorManager.SENSOR_DELAY_NORMAL
) {
    companion object {
        val Accelerometer = SensorConfig(Sensor.TYPE_ACCELEROMETER)
        val Gyroscope     = SensorConfig(Sensor.TYPE_GYROSCOPE)
        val Magnetometer  = SensorConfig(Sensor.TYPE_MAGNETIC_FIELD)
        val Light         = SensorConfig(Sensor.TYPE_LIGHT,     SensorManager.SENSOR_DELAY_UI)
        val Proximity     = SensorConfig(Sensor.TYPE_PROXIMITY, SensorManager.SENSOR_DELAY_UI)
        val Pressure      = SensorConfig(Sensor.TYPE_PRESSURE,  SensorManager.SENSOR_DELAY_NORMAL)
        val StepCounter   = SensorConfig(Sensor.TYPE_STEP_COUNTER, SensorManager.SENSOR_DELAY_NORMAL)
        val Rotation      = SensorConfig(Sensor.TYPE_ROTATION_VECTOR)

        // Convenience sampling presets
        val Fastest = SensorManager.SENSOR_DELAY_FASTEST
        val Game    = SensorManager.SENSOR_DELAY_GAME
        val UI      = SensorManager.SENSOR_DELAY_UI
        val Normal  = SensorManager.SENSOR_DELAY_NORMAL
    }
}

// ─────────────────────────────────────────────
//  SensorReading
// ─────────────────────────────────────────────

/** Lecture d'un capteur : valeurs brutes + timestamp. */
data class SensorReading(
    val values: FloatArray,
    val accuracy: Int,
    val timestampNs: Long
) {
    val x: Float get() = values.getOrElse(0) { 0f }
    val y: Float get() = values.getOrElse(1) { 0f }
    val z: Float get() = values.getOrElse(2) { 0f }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SensorReading) return false
        return accuracy == other.accuracy
                && timestampNs == other.timestampNs
                && values.contentEquals(other.values)
    }

    override fun hashCode(): Int {
        var result = values.contentHashCode()
        result = 31 * result + accuracy
        result = 31 * result + timestampNs.hashCode()
        return result
    }
}

// ─────────────────────────────────────────────
//  SensorHelper
// ─────────────────────────────────────────────

/**
 * Helper de capteurs centralisé.
 *
 * Aucune permission requise pour les capteurs standard.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { SensorHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // Vérifier la disponibilité
 * val available = sensorHelper.isAvailable(SensorConfig.Accelerometer)
 *
 * // Stream de lectures (dans viewModelScope)
 * sensorHelper.readingFlow(SensorConfig.Accelerometer)
 *     .onEach { reading ->
 *         val magnitude = sqrt(reading.x.pow(2) + reading.y.pow(2) + reading.z.pow(2))
 *     }
 *     .launchIn(viewModelScope)
 *
 * // Lecture unique
 * val light = sensorHelper.readOnce(SensorConfig.Light)
 * ```
 */
class SensorHelper(context: Context) {

    internal val appContext    = context.applicationContext
    internal val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // ── Availability ──────────────────────────────────────────────────────────

    fun isAvailable(config: SensorConfig): Boolean =
        sensorManager.getDefaultSensor(config.sensorType) != null

    fun getSensor(config: SensorConfig): Sensor? =
        sensorManager.getDefaultSensor(config.sensorType)

    fun listSensors(): List<Sensor> =
        sensorManager.getSensorList(Sensor.TYPE_ALL)

    // ── Continuous flow ───────────────────────────────────────────────────────

    /**
     * Flow émettant un [SensorReading] à chaque événement capteur.
     * Se désabonne automatiquement à la fermeture du flow.
     */
    fun readingFlow(config: SensorConfig): Flow<SensorReading> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(config.sensorType)
            ?: run { close(IllegalStateException("Sensor ${config.sensorType} not available")); return@callbackFlow }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(SensorReading(event.values.copyOf(), event.accuracy, event.timestamp))
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, config.samplingUs)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    // ── Single reading ────────────────────────────────────────────────────────

    /**
     * Attend la première lecture du capteur et retourne sa valeur.
     * Annulable via le scope appelant.
     */
    suspend fun readOnce(config: SensorConfig): SensorReading =
        readingFlow(config).first()
}