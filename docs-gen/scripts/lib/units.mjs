// Discovers documentation "units" (one page each) from the Kotlin sources of every public module.

import fs from 'node:fs';
import path from 'node:path';
import { scan } from './kotlin.mjs';

/** Docs section -> Gradle module + how its sources are grouped into pages. */
export const SECTIONS = {
  components: { module: 'core', pkg: 'core', label: 'Components', blurb: 'Compose components and design tokens from `:core`.' },
  utils: { module: 'utils', pkg: 'utils', label: 'Utils', blurb: 'Coroutine utilities and formatting helpers from `:utils`.' },
  compose: { module: 'compose', pkg: 'compose', label: 'Compose', blurb: 'Typed navigation and the `KViewModel` base from `:compose`.' },
  android: { module: 'android', pkg: 'android', label: 'Android', blurb: 'Native device helpers, HTTP client and storage from `:android`.' },
};

/** Directories (relative to the package root) whose files each become their own page. */
const PER_FILE_DIRS = new Set(['natif']);

const SKIP_DIRS = /^(build|bin|node_modules|\.git|generated|\.gradle|test|[a-zA-Z]*Test)$/;

function walk(dir, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    if (e.isDirectory()) {
      if (!SKIP_DIRS.test(e.name)) walk(path.join(dir, e.name), out);
    } else if (e.name.endsWith('.kt') && !/Test\.kt$/.test(e.name)) out.push(path.join(dir, e.name));
  }
  return out;
}

export function humanize(key) {
  return key
    .replace(/[-_]/g, ' ')
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1 $2')
    .split(' ')
    .filter(Boolean)
    .map((w) => (w === w.toUpperCase() ? w : w[0].toUpperCase() + w.slice(1)))
    .join(' ');
}

export const slugify = (key) => humanize(key).toLowerCase().replace(/\s+/g, '-');

const stripK = (name) => name.replace(/^K(?=[A-Z])/, '');
const stripSuffix = (name) => name.replace(/Helper$/, '');

/** Decide which page a source file belongs to. Returns { key, kind } or null to skip. */
function unitFor(section, rel) {
  const parts = rel.split('/');
  const file = parts[parts.length - 1].replace(/\.kt$/, '');
  const dirs = parts.slice(0, -1);

  if (section === 'components') {
    if (dirs[0] === 'components' && dirs[1] === 'ui') {
      if (dirs.length >= 3) return { key: dirs[2], kind: 'ui' };
      return { key: stripK(file), kind: 'ui' };
    }
    if (dirs[0] === 'components' && dirs[1]) return { key: dirs[1], kind: 'api' };
    if (dirs[0] === 'theme') return { key: 'theme', kind: 'api' };
    if (dirs[0] === 'utils') return { key: 'core-utils', kind: 'api' };
    return { key: 'core-' + file, kind: 'api' };
  }

  // utils/compose/android: strip a leading "method" folder (utils/method/format/text -> format-text)
  const d = dirs[0] === 'method' ? dirs.slice(1) : dirs;
  if (d.length === 0) return { key: stripSuffix(stripK(file)), kind: 'api' };
  if (PER_FILE_DIRS.has(d[0])) return { key: stripSuffix(file), kind: 'api' };
  return { key: d.join('-'), kind: 'api' };
}

function declId(d) {
  return `${d.keyword.split(' ')[0]}:${d.receiver}.${d.name}/${d.params.length}`;
}

function isHidden(d) {
  if (d.name.startsWith('_')) return true;
  if (d.annotations.some((a) => /^@Preview\b/.test(a))) return true;
  if (d.modifiers.includes('actual')) return false; // resolved against expect during dedupe
  return false;
}

export function discoverUnits(repoRoot, sectionFilter) {
  const units = new Map();

  for (const [section, cfg] of Object.entries(SECTIONS)) {
    if (sectionFilter && sectionFilter !== section) continue;
    const srcDir = path.join(repoRoot, cfg.module, 'src');
    const marker = `/dev/kindling/${cfg.pkg}/`;

    for (const file of walk(srcDir).sort()) {
      const norm = file.split(path.sep).join('/');
      const at = norm.indexOf(marker);
      if (at === -1) continue;
      const rel = norm.slice(at + marker.length);
      const u = unitFor(section, rel);
      if (!u) continue;

      const decls = scan(fs.readFileSync(file, 'utf8')).filter((d) => !isHidden(d));
      const repoRel = path.relative(repoRoot, file).split(path.sep).join('/');
      for (const d of decls) d.file = repoRel;

      const id = `${section}/${slugify(u.key)}`;
      if (!units.has(id)) {
        units.set(id, {
          id,
          section,
          key: u.key,
          slug: slugify(u.key),
          title: humanize(u.key),
          kind: u.kind,
          files: new Set(),
          decls: [],
        });
      }
      const unit = units.get(id);
      unit.files.add(repoRel);
      unit.decls.push(...decls);
    }
  }

  const result = [];
  for (const unit of units.values()) {
    unit.decls = dedupe(unit.decls);
    if (unit.decls.length === 0) continue; // everything was internal
    finalize(unit);
    result.push(unit);
  }
  return result.sort((a, b) => a.section.localeCompare(b.section) || a.title.localeCompare(b.title));
}

/** expect/actual and per-platform duplicates collapse to one entry, preferring the documented one. */
function dedupe(decls) {
  const map = new Map();
  for (const d of decls) {
    const id = declId(d);
    const prev = map.get(id);
    if (!prev) map.set(id, d);
    else if ((!prev.doc && d.doc) || (prev.modifiers.includes('actual') && !d.modifiers.includes('actual'))) map.set(id, d);
  }
  return [...map.values()];
}

function finalize(unit) {
  const isType = (d) => ['class', 'interface', 'object', 'fun interface', 'typealias'].includes(d.keyword);
  unit.composables = unit.decls.filter((d) => d.keyword === 'fun' && d.composable);
  unit.types = unit.decls.filter(isType);
  unit.others = unit.decls.filter((d) => !unit.composables.includes(d) && !unit.types.includes(d));

  const want = unit.title.replace(/\s+/g, '').toLowerCase();
  // Exact match (KBadge / Badge) first, then a name that starts with the page name (CameraHelper for "Camera").
  const byName = (list) =>
    list.find((d) => d.name.toLowerCase() === `k${want}` || d.name.toLowerCase() === want) ??
    list.find((d) => d.name.toLowerCase().replace(/^k(?=[a-z])/, '').startsWith(want));
  unit.primary =
    byName(unit.composables) ??
    unit.composables.find((d) => d.doc?.summary) ??
    byName(unit.types) ??
    unit.types.find((d) => d.doc?.summary) ??
    unit.decls.find((d) => d.doc?.summary) ??
    null;

  // Primary first, then source order.
  const order = (list) => (unit.primary && list.includes(unit.primary) ? [unit.primary, ...list.filter((d) => d !== unit.primary)] : list);
  unit.composables = order(unit.composables);
  unit.types = order(unit.types);

  unit.examples = [];
  for (const d of unit.decls) for (const ex of d.doc?.examples ?? []) unit.examples.push({ ...ex, from: d.name });

  const flat = [];
  for (const d of unit.decls) {
    flat.push(d);
    for (const m of d.members ?? []) flat.push(m);
  }
  unit.undocumented = unit.decls.filter((d) => !d.doc?.summary).map((d) => d.name);
  unit.documentedRatio = unit.decls.length ? 1 - unit.undocumented.length / unit.decls.length : 1;
}
