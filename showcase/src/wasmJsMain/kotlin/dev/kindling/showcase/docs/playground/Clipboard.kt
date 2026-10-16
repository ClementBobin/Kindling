package dev.kindling.showcase.docs.playground

// Plain browser clipboard access. The textarea fallback covers non-secure contexts (e.g. http://<LAN-ip>:8080).
@JsFun(
    "(text) => { const prev = document.activeElement; " +
        "const fallback = () => { const t = document.createElement('textarea'); t.value = text; t.style.position = 'fixed'; t.style.opacity = '0'; " +
        "document.body.appendChild(t); t.focus({ preventScroll: true }); t.select(); try { document.execCommand('copy'); } catch (e) {} t.remove(); " +
        "if (prev && prev.focus) prev.focus({ preventScroll: true }); }; " +
        "if (navigator.clipboard && window.isSecureContext) { navigator.clipboard.writeText(text).catch(fallback); } else { fallback(); } }",
)
private external fun jsCopy(text: String)

internal fun copyToClipboard(text: String) = jsCopy(text)

@JsFun(
    "(text) => { const u = URL.createObjectURL(new Blob([text], { type: 'text/plain;charset=utf-8' })); window.open(u, '_blank'); }",
)
private external fun jsOpenText(text: String)

/** Shows [text] as plain text in a new browser tab. */
internal fun openTextTab(text: String) = jsOpenText(text)
