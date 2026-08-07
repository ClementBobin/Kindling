package dev.kindling.library.utils.method.format.unit

/** Converts kph to mph. */
fun Double.kphToMph(): Double = this * 0.621371
/** Converts mph to kph. */
fun Double.mphToKph(): Double = this * 1.60934
/** Converts m/s to kph. */
fun Double.mpsToKph(): Double = this * 3.6
/** Converts kph to m/s. */
fun Double.kphToMps(): Double = this / 3.6
/** Converts knots to kph. */
fun Double.knotsToKph(): Double = this * 1.852
/** Converts kph to knots. */
fun Double.kphToKnots(): Double = this / 1.852