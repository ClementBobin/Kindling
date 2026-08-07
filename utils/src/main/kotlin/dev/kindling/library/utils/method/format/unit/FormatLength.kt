package dev.kindling.library.utils.method.format.unit

import kotlin.math.round

/** Converts cm to feet-and-inches notation. Example: `178.0.cmToFeetInches()` → `"5 ft 10 in"` */
fun Double.cmToFeetInches(): String {
    val totalInches = this / 2.54
    val feet  = (totalInches / 12).toInt()
    val inches = round(totalInches % 12).toInt()
    return "$feet ft $inches in"
}
/** Converts cm to decimal inches. */
fun Double.cmToInches(): Double = this / 2.54
/** Converts cm to decimal feet. */
fun Double.cmToFeet(): Double = this / 30.48
/** Converts cm to meters. */
fun Double.cmToMeters(): Double = this / 100.0
/** Converts meters to cm. */
fun Double.metersToCm(): Double = this * 100.0
/** Converts meters to feet. */
fun Double.metersToFeet(): Double = this * 3.28084
/** Converts meters to miles. */
fun Double.metersToMiles(): Double = this / 1609.344
/** Converts km to miles. */
fun Double.kmToMiles(): Double = this * 0.621371
/** Converts miles to km. */
fun Double.milesToKm(): Double = this * 1.60934
/** Converts inches to cm. */
fun Double.inchesToCm(): Double = this * 2.54
/** Converts feet to cm. */
fun Double.feetToCm(): Double = this * 30.48
/** Converts feet to meters. */
fun Double.feetToMeters(): Double = this * 0.3048