package dev.kindling.thod.format.unit

/** Converts m² to ft². */
fun Double.sqMetersToSqFeet(): Double = this * 10.7639
/** Converts ft² to m². */
fun Double.sqFeetToSqMeters(): Double = this * 0.092903
/** Converts km² to mi². */
fun Double.sqKmToSqMiles(): Double = this * 0.386102
/** Converts mi² to km². */
fun Double.sqMilesToSqKm(): Double = this * 2.58999
/** Converts hectares to acres. */
fun Double.hectaresToAcres(): Double = this * 2.47105
/** Converts acres to hectares. */
fun Double.acresToHectares(): Double = this * 0.404686