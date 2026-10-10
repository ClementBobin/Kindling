---
title: Contributing to the docs
description: How the documentation is generated, edited by hand, or drafted with AI.
section: Project
order: 1
---

This site is the `:showcase` Gradle module (Compose Multiplatform for Web). Its content comes from `docs-gen/`.

## 1. Generated from KDoc (automatic)

`docs-gen/scripts/docgen.mjs` reads the Kotlin sources of `:core`, `:utils`, `:compose` and `:android` and writes `GeneratedDocs.kt` into the showcase module. Write good KDoc and the page writes itself:

- the first paragraph becomes the page description
- a `### Example usage:` heading followed by a fenced `kotlin` block becomes the usage example
- `@param` tags fill the props table; defaults and types come from the signature
- companion-object presets and enum entries are listed automatically

```bash
node docs-gen/scripts/docgen.mjs          # regenerate
node docs-gen/scripts/docgen.mjs --check  # CI: fail if the generated file is stale
```

## 2. Written by hand

Add prose to a generated page with `docs-gen/extra/<module>/<page>.md`; it is injected after the page intro and never overwritten. Pages that are not API reference (like this one) are Markdown files in `docs-gen/pages/`. Supported extras: `{{demo:<id>}}` for a live preview and `> ` blockquotes for callouts.

## 3. Drafted with AI

Run the **Docs** workflow manually with *ai_drafts* enabled. It finds thin pages, asks Claude to draft an overview and usage section from the source only, and opens a pull request with files in `docs-gen/extra/`. Nothing is published until a human merges it. Needs the `ANTHROPIC_API_KEY` repository secret.

## Live previews

Write a demo composable and register it in `showcase/src/wasmJsMain/kotlin/dev/kindling/showcase/Registry.kt` using the page slug as id, for example `demo("badge", "Badge") { BadgeDemo() }`. The generator then adds a live preview to that page.

## Run locally

```bash
./gradlew :showcase:wasmJsBrowserDevelopmentRun
```
