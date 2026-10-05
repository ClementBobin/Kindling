package dev.kindling.utils.method.format.unit

/** Converts Pascal to bar. */
fun Double.pascalToBar(): Double = this / 100_000.0
/** Converts bar to Pascal. */
fun Double.barToPascal(): Double = this * 100_000.0
/** Converts Pascal to PSI. */
fun Double.pascalToPsi(): Double = this / 6894.757
/** Converts PSI to Pascal. */
fun Double.psiToPascal(): Double = this * 6894.757
/** Converts bar to PSI. */
fun Double.barToPsi(): Double = this * 14.5038
/** Converts PSI to bar. */
fun Double.psiToBar(): Double = this / 14.5038
