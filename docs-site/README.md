# Kindling docs site

Vite + React docs site published to GitHub Pages.

## Local dev

```bash
cd docs-site
npm install
npm run dev
```

## Build

```bash
cd docs-site
npm run build
```

## Content

- `docs-site/public/content/core.json` is generated from `core/src/androidMain/kotlin/dev/kindling/core/components/*.kt` via `npm run generate`.
- `docs-site/public/content/utils.json` is generated from `utils/src/main/kotlin/dev/kindling/library/utils/*.kt` (KDoc-driven) via `npm run generate`.
- `docs-site/public/content/compose.json` is generated from `compose/src/main/kotlin/dev/kindling/compose/*.kt` (KDoc-driven) via `npm run generate`.
