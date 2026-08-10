package dev.kindling.android.http

import okhttp3.CertificatePinner

/**
 * Utility helper to build an OkHttp [CertificatePinner] config for Kindling HTTP clients.
 */
object KCertificatePinning {
    fun create(hostname: String, vararg pins: String): CertificatePinner {
        val builder = CertificatePinner.Builder()
        pins.forEach { pin ->
            builder.add(hostname, pin)
        }
        return builder.build()
    }
}