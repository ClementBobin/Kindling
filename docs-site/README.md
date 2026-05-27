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
- `docs-site/public/content/utils.json` and `docs-site/public/content/compose.json` are maintained manually.

