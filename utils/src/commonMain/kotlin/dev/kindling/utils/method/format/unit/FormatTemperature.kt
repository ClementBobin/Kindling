package dev.kindling.utils.method.format.unit

/** Converts °C to °F. Example: `100.0.celsiusToFahrenheit()` → `212.0` */
fun Double.celsiusToFahrenheit(): Double = this * 9.0 / 5.0 + 32.0
/** Converts °F to °C. */
fun Double.fahrenheitToCelsius(): Double = (this - 32.0) * 5.0 / 9.0
/** Converts °C to Kelvin. */
fun Double.celsiusToKelvin(): Double = this + 273.15
/** Converts Kelvin to °C. */
fun Double.kelvinToCelsius(): Double = this - 273.15
/** Converts °F to Kelvin. */
fun Double.fahrenheitToKelvin(): Double = (this - 32.0) * 5.0 / 9.0 + 273.15
/** Converts Kelvin to °F. */
fun Double.kelvinToFahrenheit(): Double = (this - 273.15) * 9.0 / 5.0 + 32.0