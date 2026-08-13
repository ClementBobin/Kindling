# Module kindling

How to write and contribute documentation for Kindling.

## How docs are generated

The API reference is generated automatically from KDoc comments in Kotlin source files using
[Dokka](https://kotlinlang.org/docs/dokka-introduction.html). You never write docs outside the code.

To generate locally:

```bash
./gradlew dokkaGenerate
# Output: build/docs/html/index.html
```

## Writing KDoc

Every public API should have a KDoc comment. The format Dokka renders:

```kotlin
/**
 * Short one-line summary shown in the index.
 *
 * Longer description that appears on the detail page.
 * Supports **Markdown** formatting.
 *
 * @param visible Controls whether the animation triggers or resets.
 * @param durationMs Animation duration in milliseconds.
 * @param content Target content to reveal.
 *
 * ```kotlin
 * // This code block appears as a usage example in the docs
 * KSlideUp(visible = isReady) {
 *     Text("Hello!")
 * }
 * ```
 */
@Composable
fun KSlideUp(
    visible: Boolean = true,
    durationMs: Int = 600,
    content: @Composable () -> Unit
)
```

## Adding extra documentation pages

Place `.md` files in the `docs/` folder. Each file must start with a `# Module kindling` heading
so Dokka picks it up and embeds it into the generated site.

```
docs/
  getting-started.md     ← Module-level, shown on the root index
  contributing-docs.md   ← This file
```

Do **not** write docs under `# Package dev.kindling.foo` unless you specifically want to annotate
a package index page — the `# Module kindling` heading appends content to the root module page.