package dev.kindling.showcase.docs.playground

// Plain browser clipboard access. The textarea fallback covers non-secure contexts (e.g. http://<LAN-ip>:8080).
@JsFun(
    "(text) => { if (navigator.clipboard && window.isSecureContext) { navigator.clipboard.writeText(text); } " +
        "else { const t = document.createElement('textarea'); t.value = text; document.body.appendChild(t); " +
        "t.select(); document.execCommand('copy'); t.remove(); } }",
)
private external fun jsCopy(text: String)

internal fun copyToClipboard(text: String) = jsCopy(text)
