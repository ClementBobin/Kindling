#!/usr/bin/env node
// KDoc -> Kotlin docs generator for the Kindling docs site (the :showcase Compose Wasm app).
//
//   node docs-gen/scripts/docgen.mjs            regenerate showcase/.../docs/generated/*.kt
//   node docs-gen/scripts/docgen.mjs --check    exit 1 if generated files are out of date (writes nothing)
//   node docs-gen/scripts/docgen.mjs --verbose
//
// Inputs:  Kotlin sources of :core :utils :compose :android (KDoc), docs-gen/pages/*.md (hand-written pages),
//          docs-gen/extra/<section>/<slug>.md (hand/AI prose injected into a generated page).
// Output:  one Kotlin file per page + a registry, in the showcase module. Directory is fully owned by this script.

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { discoverUnits, SECTIONS } from './lib/units.mjs';
import { indexPage, unitPage } from './lib/blocks.mjs';
import { mdToBlocks, parseFrontmatter } from './lib/md.mjs';

const here = path.dirname(fileURLToPath(import.meta.url));
const genDir = path.resolve(here, '..');
const repoRoot = path.resolve(genDir, '..');
const outDir = path.join(repoRoot, 'showcase/src/wasmJsMain/kotlin/dev/kindling/showcase/docs/generated');
const args = process.argv.slice(2);
const check = args.includes('--check');
const verbose = args.includes('--verbose');

function readVersion() {
  const env = process.env.RELEASE_VERSION ?? process.env.LIBRARY_VERSION;
  if (env) return env.replace(/^v/, '');
  const readme = fs.readFileSync(path.join(repoRoot, 'README.md'), 'utf8');
  return readme.match(/kindling:core:([0-9][\w.\-]*)/)?.[1] ?? '0.3.0';
}

function showcaseIds() {
  const ids = new Set(['gallery']);
  const walk = (d) => {
    if (!fs.existsSync(d)) return;
    for (const e of fs.readdirSync(d, { withFileTypes: true })) {
      const p = path.join(d, e.name);
      if (e.isDirectory()) {
        if (!/^(build|generated)$/.test(e.name)) walk(p);
      } else if (e.name.endsWith('.kt')) {
        for (const m of fs.readFileSync(p, 'utf8').matchAll(/\bdemo\(\s*"([a-z0-9-]+)"/g)) ids.add(m[1]);
      }
    }
  };
  walk(path.join(repoRoot, 'showcase/src'));
  return ids;
}

const ctx = {
  repo: process.env.GITHUB_REPOSITORY ?? 'ClementBobin/Kindling',
  branch: process.env.DOCS_BRANCH ?? 'main',
  version: readVersion(),
  showcaseIds: showcaseIds(),
  extra(section, slug) {
    const f = path.join(genDir, 'extra', section, `${slug}.md`);
    return fs.existsSync(f) ? fs.readFileSync(f, 'utf8') : '';
  },
};

// ── collect pages ───────────────────────────────────────────────────────────
const pages = [];
const report = [];

for (const f of fs.readdirSync(path.join(genDir, 'pages')).sort()) {
  if (!f.endsWith('.md')) continue;
  const { meta, body } = parseFrontmatter(fs.readFileSync(path.join(genDir, 'pages', f), 'utf8'));
  pages.push({
    path: f.replace(/\.md$/, ''),
    section: meta.section ?? 'Getting started',
    sectionKey: '',
    title: meta.title,
    description: meta.description ?? '',
    order: Number(meta.order ?? 50),
    blocks: mdToBlocks(body.replaceAll('{{version}}', ctx.version)),
  });
}

const units = discoverUnits(repoRoot);
for (const section of Object.keys(SECTIONS)) {
  const list = units.filter((u) => u.section === section);
  if (!list.length) continue;
  pages.push(indexPage(section, list));
  for (const u of list) {
    pages.push(unitPage(u, ctx));
    const hasExtra = Boolean(ctx.extra(section, u.slug));
    report.push({
      id: u.id,
      title: u.title,
      kind: u.kind,
      sources: [...u.files],
      declarations: u.decls.length,
      undocumented: u.undocumented,
      examples: u.examples.length,
      hasShowcase: u.kind === 'ui' && ctx.showcaseIds.has(u.slug),
      hasExtra,
      documentedRatio: Number(u.documentedRatio.toFixed(2)),
      thin: !hasExtra && ((u.kind === 'ui' && u.examples.length === 0) || u.documentedRatio < 0.6),
    });
  }
}

// ── Kotlin emission ─────────────────────────────────────────────────────────
const k = (s) =>
  `"${String(s ?? '')
    .replace(/\\/g, '\\\\')
    .replace(/"/g, '\\"')
    .replace(/\$/g, '\\$')
    .replace(/\r/g, '')
    .replace(/\n/g, '\\n')
    .replace(/\t/g, '\\t')}"`;

function block(b) {
  switch (b.t) {
    case 'h': return `Heading(${b.l}, ${k(b.s)})`;
    case 'p': return `Para(${k(b.s)})`;
    case 'code': return `CodeBlock(${k(b.lang)}, ${k(b.s)})`;
    case 'list': return `Bullets(${b.ordered}, listOf(${b.items.map(k).join(', ')}))`;
    case 'quote': return `Quote(${k(b.s)})`;
    case 'props': return `Props(listOf(${b.rows.map((r) => `PropRow(${k(r.name)}, ${k(r.type)}, ${k(r.def)}, ${k(r.desc)}, ${r.req})`).join(', ')}))`;
    case 'demo': return `Demo(${k(b.id)})`;
    case 'playground': return `Playground(${k(b.id)})`;
    case 'gallery': return 'Gallery';
    case 'hr': return 'Rule';
    case 'src': return `Source(${k(b.url)})`;
    default: throw new Error(`unknown block ${b.t}`);
  }
}

const fnName = (p) => `page_${p.replace(/[^A-Za-z0-9]/g, '_')}`;
const HEADER = '// AUTO-GENERATED by docs-gen/scripts/docgen.mjs. Do not edit; edit KDoc, docs-gen/pages or docs-gen/extra instead.\npackage dev.kindling.showcase.docs.generated\n\nimport dev.kindling.showcase.docs.*\n\n';

const files = new Map();
for (const p of pages) {
  const body = p.blocks.map((b) => `        ${block(b)},`).join('\n');
  files.set(
    `${fnName(p.path)}.kt`,
    `${HEADER}internal fun ${fnName(p.path)}(): DocPage = DocPage(\n    path = ${k(p.path)},\n    section = ${k(p.section)},\n    title = ${k(p.title)},\n    description = ${k(p.description)},\n    order = ${p.order},\n    blocks = listOf(\n${body}\n    ),\n)\n`,
  );
}
files.set(
  'GeneratedDocs.kt',
  `${HEADER}/** Version the docs were generated for (shown in the top bar). */\ninternal const val DOCS_VERSION = ${k(ctx.version)}\n\ninternal fun generatedPages(): List<DocPage> = listOf(\n${pages.map((p) => `    ${fnName(p.path)}(),`).join('\n')}\n)\n`,
);

// ── write / check ───────────────────────────────────────────────────────────
const drift = [];
for (const [name, content] of files) {
  const f = path.join(outDir, name);
  if (!fs.existsSync(f) || fs.readFileSync(f, 'utf8') !== content) drift.push(name);
}
if (fs.existsSync(outDir)) {
  for (const name of fs.readdirSync(outDir)) if (!files.has(name)) drift.push(`${name} (stale)`);
}

const decls = report.reduce((n, u) => n + u.declarations, 0);
const undoc = report.reduce((n, u) => n + u.undocumented.length, 0);
const thin = report.filter((u) => u.thin).length;
const summary = `docgen: ${pages.length} pages (${report.length} API units, ${decls} declarations, ${decls ? Math.round(((decls - undoc) / decls) * 100) : 100}% documented, ${thin} flagged for AI drafts)`;

if (check) {
  console.log(`${summary} — ${drift.length} file(s) out of date`);
  if (drift.length) {
    console.error('\nGenerated docs are out of date. Run `node docs-gen/scripts/docgen.mjs` and commit the result:');
    for (const f of drift.slice(0, 25)) console.error(`  - ${f}`);
    process.exit(1);
  }
} else {
  fs.rmSync(outDir, { recursive: true, force: true });
  fs.mkdirSync(outDir, { recursive: true });
  for (const [name, content] of files) fs.writeFileSync(path.join(outDir, name), content);
  fs.mkdirSync(path.join(genDir, '.docgen'), { recursive: true });
  fs.writeFileSync(path.join(genDir, '.docgen', 'report.json'), `${JSON.stringify({ version: ctx.version, units: report }, null, 2)}\n`);
  console.log(`${summary} — wrote ${files.size} files (${drift.length} changed)`);
  if (verbose) for (const d of drift) console.log(`  ${d}`);
}
