#!/usr/bin/env node
// AI-assisted documentation drafts.
//
//   ANTHROPIC_API_KEY=... node docs-gen/scripts/ai-draft.mjs                 draft every page flagged "thin" (max --limit, default 5)
//   node docs-gen/scripts/ai-draft.mjs --only=components/dialog              draft one page
//   node docs-gen/scripts/ai-draft.mjs --dry-run                             list candidates, call nothing
//
// Output goes to extra/<section>/<slug>.md (never to the generated pages), with a review banner.
// The CI job opens a pull request so a human reviews every AI-written line before it is published.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const websiteDir = path.resolve(here, '..'); // docs-gen/
const repoRoot = path.resolve(websiteDir, '..');
const args = process.argv.slice(2);
const opt = (n, d) => args.find((a) => a.startsWith(`--${n}=`))?.split('=')[1] ?? d;
const dry = args.includes('--dry-run');
const only = opt('only');
const limit = Number(opt('limit', 5));
const model = process.env.DOCS_AI_MODEL ?? 'claude-sonnet-5-5';

const reportFile = path.join(websiteDir, '.docgen', 'report.json');
if (!fs.existsSync(reportFile)) {
  console.error('Run `node docs-gen/scripts/docgen.mjs` first (it writes .docgen/report.json).');
  process.exit(1);
}
const { units } = JSON.parse(fs.readFileSync(reportFile, 'utf8'));
let todo = only ? units.filter((u) => u.id === only) : units.filter((u) => u.thin);
todo = todo.slice(0, limit);
console.log(`${todo.length} page(s) to draft:`, todo.map((u) => u.id).join(', ') || '(none)');
if (dry || !todo.length) process.exit(0);
if (!process.env.ANTHROPIC_API_KEY) {
  console.error('ANTHROPIC_API_KEY is not set.');
  process.exit(1);
}

const SYSTEM = `You write documentation for Kindling, a Jetpack Compose / Compose Multiplatform component library inspired by shadcn/ui.
Write plain Markdown that will be injected into an existing API reference page (signatures and parameter tables are already generated, so do NOT repeat them).
Output ONLY Markdown with these sections, in order:
## Overview   (2-4 sentences: what it is, when to use it)
## Usage      (one or two realistic Kotlin code blocks that compile against the provided source; only use APIs visible in the source)
## Notes      (optional bullets: accessibility, theming, gotchas that are evident from the source)
Rules: never invent parameters, components or behaviour that is not in the source. Use fenced \`\`\`kotlin blocks. No frontmatter, no H1, no HTML, no JSX. Only headings, paragraphs, bullet lists, blockquotes and fenced code.`;

for (const u of todo) {
  const sources = u.sources
    .map((f) => `// ${f}\n${fs.readFileSync(path.join(repoRoot, f), 'utf8').slice(0, 14000)}`)
    .join('\n\n');
  const res = await fetch('https://api.anthropic.com/v1/messages', {
    method: 'POST',
    headers: {
      'content-type': 'application/json',
      'x-api-key': process.env.ANTHROPIC_API_KEY,
      'anthropic-version': '2023-06-01',
    },
    body: JSON.stringify({
      model,
      max_tokens: 2000,
      system: SYSTEM,
      messages: [{ role: 'user', content: `Page: ${u.title} (module :${u.file.split('/')[2]})\n\nSource files:\n\n${sources}` }],
    }),
  });
  if (!res.ok) {
    console.error(`  ${u.id}: API error ${res.status} ${await res.text()}`);
    process.exitCode = 1;
    continue;
  }
  const data = await res.json();
  const text = data.content?.filter((c) => c.type === 'text').map((c) => c.text).join('\n').trim();
  if (!text) {
    console.error(`  ${u.id}: empty response`);
    continue;
  }
  const out = path.join(websiteDir, 'extra', u.id + '.md');
  fs.mkdirSync(path.dirname(out), { recursive: true });
  const body = text.replace(/^```(?:mdx|markdown)?\n/, '').replace(/\n```$/, '');
  fs.writeFileSync(out, `<!-- AI-drafted by docs-gen/scripts/ai-draft.mjs (${model}). Review before merging; edit freely, it will not be overwritten. -->\n\n${body}\n`);
  console.log(`  drafted extra/${u.id}.md`);
}
