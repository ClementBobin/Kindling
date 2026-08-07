package dev.kindling.library.utils.method.format.unit

/** Converts liters to US gallons. */
fun Double.litersToGallons(): Double = this * 0.264172
/** Converts US gallons to liters. */
fun Double.gallonsToLiters(): Double = this * 3.78541
/** Converts liters to milliliters. */
fun Double.litersToMl(): Double = this * 1000.0
/** Converts milliliters to liters. */
fun Double.mlToLiters(): Double = this / 1000.0
/** Converts liters to fluid ounces (US). */
fun Double.litersToFlOz(): Double = this * 33.8140
/** Converts fluid ounces (US) to liters. */
fun Double.flOzToLiters(): Double = this / 33.8140
/** Converts liters to cups (US). */
fun Double.litersToCups(): Double = this * 4.22675
/** Converts cups (US) to liters. */
fun Double.cupsToLiters(): Double = this * 0.236588