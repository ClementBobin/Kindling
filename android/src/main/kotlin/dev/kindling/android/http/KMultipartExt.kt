package dev.kindling.android.http

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.util.cio.readChannel
import java.io.File

/**
 * Sets a multipart/form-data body on the request.
 *
 * Usage:
 * ```kotlin
 * client.post("upload") {
 *     setBodyMultipart {
 *         append("title", "My photo")
 *         appendFile("file", File("/path/to/photo.jpg"), "image/jpeg")
 *     }
 * }
 * ```
 */
fun HttpRequestBuilder.setBodyMultipart(block: FormBuilder.() -> Unit) {
    setBody(MultiPartFormDataContent(formData(block)))
}

/**
 * Appends a [File] to the multipart form with the given [partName] and [contentType].
 *
 * @param partName    Form field name.
 * @param file        File to upload.
 * @param contentType MIME type (e.g. `"image/jpeg"`, `"application/pdf"`).
 * @param fileName    File name sent in the Content-Disposition header.
 *                    Defaults to [File.name].
 */
fun FormBuilder.appendFile(
    partName:    String,
    file:        File,
    contentType: String = "application/octet-stream",
    fileName:    String = file.name,
) {
    append(
        key      = partName,
        value    = ChannelProvider(size = file.length()) { file.readChannel() },
        headers  = Headers.build {
            append(HttpHeaders.ContentType,        contentType)
            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
        }
    )
}

/**
 * Appends raw [ByteArray] content to the multipart form.
 *
 * @param partName    Form field name.
 * @param bytes       Raw bytes to upload.
 * @param contentType MIME type.
 * @param fileName    File name sent in the Content-Disposition header.
 */
fun FormBuilder.appendBytes(
    partName:    String,
    bytes:       ByteArray,
    contentType: String,
    fileName:    String,
) {
    append(
        key     = partName,
        value   = bytes,
        headers = Headers.build {
            append(HttpHeaders.ContentType,        contentType)
            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
        }
    )
}
