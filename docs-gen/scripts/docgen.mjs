#!/usr/bin/env node
// KDoc -> Kotlin docs generator for the Kindling docs site (the :showcase Compose Wasm app).
//
//   node docs-gen/scripts/docgen.mjs            regenerate showcase/.../docs/generated/*.kt
//   node docs-gen/scripts/docgen.mjs --check    exit 1 if generated files are out of date (writes nothing)
//   node docs-gen/scripts/docgen.mjs --verbose
//   node docs-gen/scripts/docgen.mjs --no-history     skip per-version pages (fast local loop; also DOCS_HISTORY=0)
//   node docs-gen/scripts/docgen.mjs --versions=15    how many releases get their own page snapshots (default 15)
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
import { checkoutTag, label, listTags } from './lib/versions.mjs';

const here = path.dirname(fileURLToPath(import.meta.url));
const genDir = path.resolve(here, '..');
const repoRoot = path.resolve(genDir, '..');
const outDir = path.join(repoRoot, 'showcase/src/wasmJsMain/kotlin/dev/kindling/showcase/docs/generated');
const args = process.argv.slice(2);
const check = args.includes('--check');
const verbose = args.includes('--verbose');
const history = !args.includes('--no-history') && process.env.DOCS_HISTORY !== '0';
const emitN = Number(args.find((a) => a.startsWith('--versions='))?.split('=')[1] ?? process.env.DOCS_MAX_VERSIONS ?? 15);
const tags = history ? listTags(repoRoot) : [];

function readVersion() {
  const env = process.env.RELEASE_VERSION ?? process.env.LIBRARY_VERSION;
  if (env) return env.replace(/^v/, '');
  if (tags.length || !history) {
    const t = listTags(repoRoot)[0];
    if (t) return label(t);
  }
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

// Versions to snapshot, newest first. HEAD stands for the newest release (or RELEASE_VERSION).
const versions = [{ label: ctx.version, root: repoRoot }];
if (history) {
  const cache = path.join(genDir, '.docgen', 'versions');
  for (const t of tags) {
    if (label(t) === ctx.version) continue;
    const root = checkoutTag(repoRoot, t, cache);
    if (root) versions.push({ label: label(t), root, tag: t });
  }
}

const units = discoverUnits(repoRoot);
const firstSeen = new Map(); // unit id -> oldest release containing it
const snaps = new Map(); //      unit id -> [{ version, blocks (no line numbers), hash }] newest first
versions.forEach((v, i) => {
  const list = i === 0 ? units : discoverUnits(v.root);
  for (const u of list) {
    firstSeen.set(u.id, v.label);
    if (i >= emitN) continue;
    const page = unitPage(u, { ...ctx, lines: false });
    const entry = { version: v.label, blocks: page.blocks, meta: page, hash: JSON.stringify(page.blocks) };
    if (!snaps.has(u.id)) snaps.set(u.id, []);
    snaps.get(u.id).push(entry);
  }
});

/** Consecutive releases with identical content collapse into one group. */
function groupsFor(id) {
  const out = [];
  for (const e of snaps.get(id) ?? []) {
    const last = out[out.length - 1];
    if (last && last.hash === e.hash) last.versions.push(e.version);
    else out.push({ hash: e.hash, versions: [e.version], blocks: e.blocks });
  }
  return out;
}

for (const section of Object.keys(SECTIONS)) {
  const list = units.filter((u) => u.section === section);
  if (!list.length) continue;
  pages.push(indexPage(section, list));
  for (const u of list) {
    const page = unitPage(u, { ...ctx, lines: true });
    const groups = groupsFor(u.id);
    if (groups.length) {
      page.since = firstSeen.get(u.id);
      page.available = (snaps.get(u.id) ?? []).map((e) => e.version);
      page.headVersions = groups[0].versions;
      page.history = groups.slice(1).map((g) => ({ versions: g.versions, blocks: g.blocks }));
    }
    pages.push(page);
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
      platforms: u.platforms,
      since: page.since ?? null,
      thin: !hasExtra && ((u.kind === 'ui' && u.examples.length === 0) || u.documentedRatio < 0.6),
    });
  }
}

// Pages that existed in an older release but are gone from HEAD stay reachable (marked as removed in the UI).
const headIds = new Set(units.map((u) => u.id));
for (const [id, list] of snaps) {
  if (headIds.has(id)) continue;
  const groups = groupsFor(id);
  const m = list[0].meta;
  pages.push({
    ...m,
    blocks: groups[0].blocks,
    since: firstSeen.get(id),
    available: list.map((e) => e.version),
    headVersions: groups[0].versions,
    history: groups.slice(1).map((g) => ({ versions: g.versions, blocks: g.blocks })),
  });
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
    case 'declgroup': return `DeclGroup(${k(b.title)}, listOf(\n${b.decls.map((d) => `            ${block(d)},`).join('\n')}\n        ))`;
    case 'decl': {
      const list = (a) => `listOf(${a.map(k).join(', ')})`;
      const impls = b.impls.map((i) => `Impl(${k(i.set)}, ${k(i.kind)}, ${list(i.covers)}, ${k(i.url)})`).join(', ');
      return `Decl(${k(b.name)}, ${list(b.platforms)}, ${b.common}, ${b.universal}, listOf(${impls}), listOf(\n${b.children.map((c) => `                ${block(c)},`).join('\n')}\n            ))`;
    }
    default: throw new Error(`unknown block ${b.t}`);
  }
}

const fnName = (p) => `page_${p.replace(/[^A-Za-z0-9]/g, '_')}`;
const HEADER = '// AUTO-GENERATED by docs-gen/scripts/docgen.mjs. Do not edit; edit KDoc, docs-gen/pages or docs-gen/extra instead.\npackage dev.kindling.showcase.docs.generated\n\nimport dev.kindling.showcase.docs.*\n\n';

const files = new Map();
const blockList = (blocks, indent) => `listOf(\n${blocks.map((b) => `${indent}${block(b)},`).join('\n')}\n${indent.slice(4)})`;
const strList = (a) => `listOf(${a.map(k).join(', ')})`;
for (const p of pages) {
  const fn = fnName(p.path);
  const versioned = p.available?.length
    ? `    since = ${k(p.since)},\n    available = ${strList(p.available)},\n    headVersions = ${strList(p.headVersions)},\n    history = listOf(\n${p.history.map((g, i) => `        VersionGroup(${strList(g.versions)}) { ${fn}_h${i + 1}() },`).join('\n')}\n    ),\n`
    : '';
  const extra = (p.history ?? []).map((g, i) => `\ninternal fun ${fn}_h${i + 1}(): List<Block> = ${blockList(g.blocks, '        ')}\n`).join('');
  files.set(
    `${fn}.kt`,
    `${HEADER}internal fun ${fn}(): DocPage = DocPage(\n    path = ${k(p.path)},\n    section = ${k(p.section)},\n    title = ${k(p.title)},\n    description = ${k(p.description)},\n    order = ${p.order},\n    blocks = ${blockList(p.blocks, '        ')},\n${versioned})\n${extra}`,
  );
}
files.set(
  'GeneratedDocs.kt',
  `${HEADER}/** Version the docs were generated for (shown in the top bar). */\ninternal const val DOCS_VERSION = ${k(ctx.version)}\n\n/** Git ref the latest version's source links point to; older versions link to their tag. */\ninternal const val DOCS_BRANCH = ${k(ctx.branch)}\n\ninternal fun generatedPages(): List<DocPage> = listOf(\n${pages.map((p) => `    ${fnName(p.path)}(),`).join('\n')}\n)\n`,
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
