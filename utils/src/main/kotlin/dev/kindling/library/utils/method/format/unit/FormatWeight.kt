package dev.kindling.library.utils.method.format.unit

/** Converts kg to lbs. */
fun Double.kgToLbs(): Double = this * 2.20462
/** Converts lbs to kg. */
fun Double.lbsToKg(): Double = this / 2.20462
/** Converts kg to grams. */
fun Double.kgToGrams(): Double = this * 1000.0
/** Converts grams to kg. */
fun Double.gramsToKg(): Double = this / 1000.0
/** Converts grams to ounces. */
fun Double.gramsToOunces(): Double = this * 0.035274
/** Converts ounces to grams. */
fun Double.ouncesToGrams(): Double = this * 28.3495
/** Converts lbs to ounces. */
fun Double.lbsToOunces(): Double = this * 16.0
/** Converts ounces to lbs. */
fun Double.ouncesToLbs(): Double = this / 16.0