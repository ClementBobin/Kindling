#!/usr/bin/env node
// KDoc -> MDX documentation generator for the Kindling docs site.
//
//   node scripts/docgen.mjs            regenerate pages under content/docs/<section>/
//   node scripts/docgen.mjs --check    fail (exit 1) if generated pages are out of date; writes nothing
//   node scripts/docgen.mjs --only=utils --verbose
//
// Pages carry `generated: true` in their frontmatter and are overwritten on every run.
// To hand-edit a page, set `generated: false` (or remove the key): the generator then leaves it alone.
// Prose written by hand or by the AI draft job (scripts/ai-draft.mjs) goes in docs-extra/<section>/<slug>.mdx
// and is injected into the generated page, so regeneration never loses it.

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { discoverUnits, SECTIONS } from './lib/units.mjs';
import { renderIndex, renderUnit } from './lib/render.mjs';

const here = path.dirname(fileURLToPath(import.meta.url));
const websiteDir = path.resolve(here, '..');
const repoRoot = path.resolve(websiteDir, '..');
const contentDir = path.join(websiteDir, 'content', 'docs');

const args = process.argv.slice(2);
const flag = (n) => args.includes(`--${n}`);
const opt = (n) => args.find((a) => a.startsWith(`--${n}=`))?.split('=')[1];
const check = flag('check');
const verbose = flag('verbose');
const only = opt('only');

/** Parse the `generated:` flag from a page's frontmatter. */
function readFlag(file) {
  const text = fs.readFileSync(file, 'utf8');
  const m = text.match(/^---\n([\s\S]*?)\n---/);
  if (!m) return { generated: false, ai: false };
  return {
    generated: /^generated:\s*true\s*$/m.test(m[1]),
    ai: /^ai:\s*true\s*$/m.test(m[1]),
  };
}

function showcaseIds() {
  const ids = new Set(['gallery']);
  const dir = path.join(repoRoot, 'showcase');
  const walk = (d) => {
    if (!fs.existsSync(d)) return;
    for (const e of fs.readdirSync(d, { withFileTypes: true })) {
      const p = path.join(d, e.name);
      if (e.isDirectory()) {
        if (!/^(build|node_modules)$/.test(e.name)) walk(p);
      } else if (e.name.endsWith('.kt')) {
        for (const m of fs.readFileSync(p, 'utf8').matchAll(/\bdemo\(\s*"([a-z0-9-]+)"/g)) ids.add(m[1]);
      }
    }
  };
  walk(path.join(dir, 'src'));
  return ids;
}

function readVersion() {
  const fromEnv = process.env.NEXT_PUBLIC_LIBRARY_VERSION ?? process.env.RELEASE_VERSION;
  if (fromEnv) return fromEnv.replace(/^v/, '');
  const readme = fs.readFileSync(path.join(repoRoot, 'README.md'), 'utf8');
  return readme.match(/kindling:core:([0-9][\w.\-]*)/)?.[1] ?? '0.3.0';
}

const extraDir = path.join(websiteDir, 'docs-extra');
function extra(section, slug) {
  const f = path.join(extraDir, section, `${slug}.mdx`);
  return fs.existsSync(f) ? fs.readFileSync(f, 'utf8') : '';
}

const ctx = {
  extra,
  repo: process.env.GITHUB_REPOSITORY ?? 'ClementBobin/Kindling',
  branch: process.env.DOCS_BRANCH ?? 'main',
  version: readVersion(),
  showcaseIds: showcaseIds(),
};

const units = discoverUnits(repoRoot, only);
const stats = { written: 0, unchanged: 0, manual: 0, stale: 0, drift: [] };
const produced = new Set();
const report = [];

function emit(file, content) {
  produced.add(file);
  const rel = path.relative(websiteDir, file);
  if (fs.existsSync(file)) {
    const flags = readFlag(file);
    if (!flags.generated) {
      stats.manual++;
      if (verbose) console.log(`  keep   ${rel} (${flags.ai ? 'AI draft' : 'manual'})`);
      return;
    }
    if (fs.readFileSync(file, 'utf8') === content) {
      stats.unchanged++;
      return;
    }
  }
  stats.drift.push(rel);
  if (check) return;
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, content);
  stats.written++;
  if (verbose) console.log(`  write  ${rel}`);
}

for (const section of Object.keys(SECTIONS)) {
  if (only && only !== section) continue;
  const sectionUnits = units.filter((u) => u.section === section);
  if (!sectionUnits.length) continue;
  const dir = path.join(contentDir, section);

  for (const u of sectionUnits) {
    emit(path.join(dir, `${u.slug}.mdx`), renderUnit(u, ctx));
    report.push({
      id: u.id,
      title: u.title,
      kind: u.kind,
      file: `content/docs/${section}/${u.slug}.mdx`,
      sources: [...u.files],
      declarations: u.decls.length,
      undocumented: u.undocumented,
      examples: u.examples.length,
      hasShowcase: u.kind === 'ui' && ctx.showcaseIds.has(u.slug),
      hasExtra: Boolean(extra(section, u.slug)),
      documentedRatio: Number(u.documentedRatio.toFixed(2)),
      // Candidates for the AI draft job: UI pages with no usage example, or poorly documented pages.
      thin: !extra(section, u.slug) && ((u.kind === 'ui' && u.examples.length === 0) || u.documentedRatio < 0.6),
    });
  }

  emit(path.join(dir, 'index.mdx'), renderIndex(section, sectionUnits, ctx));

  const meta = path.join(dir, 'meta.json');
  if (!fs.existsSync(meta) && !check) {
    fs.writeFileSync(meta, `${JSON.stringify({ title: SECTIONS[section].label, pages: ['index', '...'] }, null, 2)}\n`);
  }
}

// Remove generated pages whose source no longer exists.
if (!only) {
  for (const section of Object.keys(SECTIONS)) {
    const dir = path.join(contentDir, section);
    if (!fs.existsSync(dir)) continue;
    for (const f of fs.readdirSync(dir)) {
      if (!f.endsWith('.mdx')) continue;
      const file = path.join(dir, f);
      if (produced.has(file)) continue;
      if (readFlag(file).generated) {
        stats.stale++;
        stats.drift.push(`${path.relative(websiteDir, file)} (stale)`);
        if (!check) fs.rmSync(file);
      }
    }
  }
}

if (!check) {
  const reportDir = path.join(websiteDir, '.docgen');
  fs.mkdirSync(reportDir, { recursive: true });
  fs.writeFileSync(path.join(reportDir, 'report.json'), `${JSON.stringify({ version: ctx.version, units: report }, null, 2)}\n`);
}

const decls = report.reduce((n, u) => n + u.declarations, 0);
const undoc = report.reduce((n, u) => n + u.undocumented.length, 0);
const thin = report.filter((u) => u.thin).length;
console.log(
  `docgen: ${report.length} pages from ${decls} declarations ` +
    `(${decls ? Math.round(((decls - undoc) / decls) * 100) : 100}% documented, ${thin} flagged for AI drafts) — ` +
    `${check ? 'would change' : 'written'} ${check ? stats.drift.length : stats.written}, ` +
    `unchanged ${stats.unchanged}, kept manual ${stats.manual}, stale ${stats.stale}`,
);

if (check && stats.drift.length) {
  console.error('\nGenerated docs are out of date. Run `npm run docs:generate` in website/ and commit the result:');
  for (const f of stats.drift.slice(0, 25)) console.error(`  - ${f}`);
  process.exit(1);
}
