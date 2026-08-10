package dev.kindling.thod.format.network

/**
 * Categories for HTTP status codes based on their numeric range.
 */
enum class HttpCategory(val title: String) {
    INFORMATIONAL("Informational"),
    SUCCESS("Success"),
    REDIRECTION("Redirection"),
    CLIENT_ERROR("Client Error"),
    SERVER_ERROR("Server Error"),
    UNKNOWN("Unknown");

    companion object {
        fun fromCode(code: Int): HttpCategory = when (code) {
            in 100..199 -> INFORMATIONAL
            in 200..299 -> SUCCESS
            in 300..399 -> REDIRECTION
            in 400..499 -> CLIENT_ERROR
            in 500..599 -> SERVER_ERROR
            else -> UNKNOWN
        }
    }
}

/**
 * Standard HTTP status codes, reason phrases, and helper functions.
 */
enum class HttpStatus(
    val code: Int,
    val reasonPhrase: String,
    val category: HttpCategory = HttpCategory.fromCode(code)
) {
    // 1xx Informational
    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    PROCESSING(102, "Processing"),
    EARLY_HINTS(103, "Early Hints"),

    // 2xx Success
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"),
    MULTI_STATUS(207, "Multi-Status"),
    ALREADY_REPORTED(208, "Already Reported"),
    IM_USED(226, "IM Used"),

    // 3xx Redirection
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    // 4xx Client Error
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    CONTENT_TOO_LARGE(413, "Content Too Large"),
    URI_TOO_LONG(414, "URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    IM_A_TEAPOT(418, "I'm a Teapot"),
    MISDIRECTED_REQUEST(421, "Misdirected Request"),
    UNPROCESSABLE_CONTENT(422, "Unprocessable Content"),
    LOCKED(423, "Locked"),
    FAILED_DEPENDENCY(424, "Failed Dependency"),
    TOO_EARLY(425, "Too Early"),
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

    // 5xx Server Error
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    LOOP_DETECTED(508, "Loop Detected"),
    NOT_EXTENDED(510, "Not Extended"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

    val isSuccess: Boolean get() = category == HttpCategory.SUCCESS
    val isRedirect: Boolean get() = category == HttpCategory.REDIRECTION
    val isClientError: Boolean get() = category == HttpCategory.CLIENT_ERROR
    val isServerError: Boolean get() = category == HttpCategory.SERVER_ERROR
    val isError: Boolean get() = isClientError || isServerError

    /** Example: `200.toHttpStatusString()` or `HttpStatus.OK.formatted` → `"200 OK"` */
    val formatted: String get() = "$code $reasonPhrase"

    companion object {
        private val codeMap: Map<Int, HttpStatus> = entries.associateBy { it.code }

        /** Returns the [HttpStatus] enum matching [code], or `null` if unmapped. */
        fun fromCode(code: Int): HttpStatus? = codeMap[code]
    }
}

// ─── Int Extension Helpers ───────────────────────────────────────────────────

/** Returns the matching [HttpStatus] enum entry, or null if unknown. */
fun Int.toHttpStatusOrNull(): HttpStatus? = HttpStatus.fromCode(this)

/** Returns the standard reason phrase or "Unknown" for non-standard codes. */
fun Int.toHttpReason(): String = HttpStatus.fromCode(this)?.reasonPhrase ?: "Unknown"

/** Returns formatted code and reason string (e.g., "404 Not Found"). */
fun Int.toHttpStatus(): String = HttpStatus.fromCode(this)?.formatted ?: "$this Unknown"

/** Returns the category title string. */
fun Int.httpCategory(): String = HttpCategory.fromCode(this).title

fun Int.isHttpSuccess(): Boolean = this in 200..299
fun Int.isHttpRedirect(): Boolean = this in 300..399
fun Int.isHttpClientError(): Boolean = this in 400..499
fun Int.isHttpServerError(): Boolean = this in 500..599
fun Int.isHttpError(): Boolean = isHttpClientError() || isHttpServerError()