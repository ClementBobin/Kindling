# Generating the docs site content

The Kindling docs site (`/docs-site`) renders JSON files generated from Kotlin source and KDoc.

## Generate JSON content

```bash
cd docs-site
npm install
npm run generate
```

This writes:

- `docs-site/public/content/core.json`
- `docs-site/public/content/utils.json`
- `docs-site/public/content/compose.json`

## What gets generated

The generator is `docs-site/scripts/generate-core-docs.mjs`.

For each documented API, it extracts:

- **Props**: parameters + constructor properties.
  - KDoc `@param name ...` and `@property name ...` become the prop description.
- **Examples**: fenced code blocks in KDoc, for example:
  - ` ```kotlin ... ``` ` will appear under the “Examples” section.
- **Enums**:
  - If a prop type is an `enum class`, the docs site displays its possible values automatically.
  - You can also force/show enum values via KDoc with `@enum`:
    - `@enum size Default, Sm, Lg`

## Adding custom docs

Everything shown on the docs site comes from KDoc:

- Add extra prose after the first paragraph to expand the summary.
- Add more fenced code blocks to add more examples.
- Add or refine `@param`, `@property`, and `@enum` entries to improve the generated tables.

