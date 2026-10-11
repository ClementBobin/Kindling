// Documentation unit (see units.mjs) -> page blocks for the Kotlin docs site.
import { kdocToMdx, plain } from './kotlin.mjs';
import { mdToBlocks } from './md.mjs';
import { SECTIONS } from './units.mjs';

const SIG_MODS = new Set(['suspend', 'inline', 'operator', 'infix', 'data', 'enum', 'sealed', 'abstract', 'open', 'value', 'annotation', 'const', 'lateinit']);

function sentenceTrim(s, max = 180) {
  const t = plain(s);
  if (t.length <= max) return t;
  const cut = t.slice(0, max);
  const dot = cut.lastIndexOf('. ');
  return dot > 60 ? cut.slice(0, dot + 1) : `${cut.replace(/\s+\S*$/, '')}…`;
}

const inline = (s) => kdocToMdx(plain(s) ? String(s).replace(/\n/g, ' ') : '');

function paramSig(p) {
  const kw = p.property ? (p.mutable ? 'var ' : 'val ') : '';
  return `${p.vararg ? 'vararg ' : ''}${kw}${p.name}${p.type ? `: ${p.type}` : ''}${p.default !== null ? ` = ${p.default}` : ''}`;
}

export function signature(d) {
  const mods = d.modifiers.filter((m) => SIG_MODS.has(m)).join(' ');
  const prefix = mods ? `${mods} ` : '';
  const ann = d.composable ? '@Composable\n' : '';
  const wrap = (head, tail) => {
    const one = `${head}(${d.params.map(paramSig).join(', ')})${tail}`;
    if (d.params.length <= 1 && one.length <= 90) return one;
    return `${head}(\n${d.params.map((p) => `    ${paramSig(p)},`).join('\n')}\n)${tail}`;
  };
  if (d.keyword === 'fun') {
    const head = `${prefix}fun ${d.generics ? `${d.generics} ` : ''}${d.receiver ? `${d.receiver}.` : ''}${d.name}`;
    return ann + wrap(head, d.returns ? `: ${d.returns}` : '');
  }
  if (d.keyword === 'val' || d.keyword === 'var') {
    return `${prefix}${d.keyword} ${d.receiver ? `${d.receiver}.` : ''}${d.name}${d.type ? `: ${d.type}` : ''}`;
  }
  if (d.keyword === 'typealias') return `typealias ${d.name} = ${d.type}`;
  const head = `${prefix}${d.keyword} ${d.name}${d.generics}`;
  const sup = d.supertypes ? ` : ${d.supertypes}` : '';
  return d.params.length === 0 ? `${head}${sup}` : wrap(head, sup);
}

const oneLine = (d) => signature(d).replace(/^@Composable\n/, '').replace(/\s*\n\s*/g, ' ').replace(/,\s*\)/, ')');

function propsBlock(d) {
  if (!d.params?.length) return null;
  const docParams = d.doc?.params ?? {};
  return {
    t: 'props',
    rows: d.params.map((p) => ({
      name: p.name,
      type: p.type || 'Any',
      def: p.default !== null ? (p.default.length > 90 ? `${p.default.slice(0, 87)}…` : p.default) : '',
      desc: plain(docParams[p.name] ?? p.doc ?? ''),
      req: p.required,
    })),
  };
}

function memberList(label, list) {
  if (!list?.length) return [];
  const items = list.slice(0, 60).map((m) => `\`${oneLine(m)}\`${m.doc?.summary ? ` — ${inline(m.doc.summary)}` : ''}`);
  if (list.length > 60) items.push(`…and ${list.length - 60} more`);
  return [{ t: 'p', s: `**${label}**` }, { t: 'list', ordered: false, items }];
}

function declBlock(d, ctx, isPrimary) {
  const b = [];
  const dep = d.annotations.find((a) => /^@Deprecated\b/.test(a));
  if (dep) b.push({ t: 'quote', s: `**Deprecated.** ${inline(dep.match(/"([^"]*)"/)?.[1] ?? 'This API is deprecated.')}` });
  if (d.annotations.some((a) => /^@(Experimental\w*|ExperimentalKindlingApi|RequiresOptIn)\b/.test(a))) {
    b.push({ t: 'quote', s: '**Experimental.** This API may change without notice.' });
  }
  if (!isPrimary) {
    if (d.doc?.summary) b.push(...mdToBlocks(kdocToMdx(d.doc.summary)));
    if (d.doc?.body) b.push(...mdToBlocks(kdocToMdx(d.doc.body)));
  }
  b.push({ t: 'code', lang: 'kotlin', s: signature(d) });
  const pb = propsBlock(d);
  if (pb) b.push(pb);
  if (d.entries?.length) {
    b.push({ t: 'p', s: '**Values**' });
    b.push({ t: 'list', ordered: false, items: d.entries.map((e) => `\`${e.name}\`${e.doc?.summary ? ` — ${inline(e.doc.summary)}` : ''}`) });
  }
  b.push(...memberList('Presets', d.companion), ...memberList('Members', d.members));
  for (const t of d.doc?.tags ?? []) {
    if (t.tag === 'return' || t.tag === 'returns') b.push({ t: 'p', s: `**Returns** ${inline(t.text)}` });
    else if (t.tag === 'throws' || t.tag === 'exception') {
      const m = t.text.match(/^(\S+)\s*(.*)$/);
      if (m) b.push({ t: 'p', s: `**Throws** \`${m[1]}\`${m[2] ? ` — ${inline(m[2])}` : ''}` });
    } else if (t.tag === 'see') b.push({ t: 'p', s: `**See also** ${inline(t.text)}` });
  }
  const impls = d.impls.map((i) => ({
    set: i.set,
    kind: i.kind,
    covers: i.covers,
    url: `https://github.com/${ctx.repo}/blob/{{ref}}/${i.file}${ctx.lines ? `#L${i.line}` : ''}`,
  }));
  return { t: 'decl', name: d.name, platforms: d.platforms, common: d.common, universal: d.universal, impls, children: b };
}

function packageOf(file) {
  const m = file.match(/\/kotlin\/(dev\/kindling\/.*)\/[^/]+\.kt$/);
  return m ? m[1].replace(/\//g, '.') : '';
}

export function pageDescription(unit) {
  const s = unit.primary?.doc?.summary;
  return s ? sentenceTrim(s) : `${unit.title} API reference.`;
}

export function unitPage(unit, ctx) {
  const cfg = SECTIONS[unit.section];
  const primary = unit.primary;
  const blocks = [];
  if (primary?.doc?.body) blocks.push(...mdToBlocks(kdocToMdx(primary.doc.body)));
  const extra = ctx.extra(unit.section, unit.slug);
  if (extra) blocks.push(...mdToBlocks(extra));

  const hasDemo = unit.kind === 'ui' && ctx.showcaseIds.has(unit.slug);
  const [first, ...rest] = unit.examples;
  if (hasDemo) blocks.push({ t: 'h', l: 2, s: 'Preview' }, { t: 'demo', id: unit.slug });
  if (first) blocks.push({ t: 'h', l: 2, s: 'Usage' }, { t: 'code', lang: first.lang, s: first.code });
  if (rest.length) {
    blocks.push({ t: 'h', l: 2, s: 'More examples' });
    for (const e of rest) {
      if (e.from) blocks.push({ t: 'p', s: `**${e.from}**` });
      blocks.push({ t: 'code', lang: e.lang, s: e.code });
    }
  }
  blocks.push(
    { t: 'h', l: 2, s: 'Installation' },
    { t: 'code', lang: 'kotlin', s: `implementation("io.github.clementbobin.kindling:${cfg.module}:{{version}}")` },
  );
  const pkg = primary?.pkg || (primary ? packageOf(primary.file) : '');
  if (pkg) blocks.push({ t: 'code', lang: 'kotlin', s: `import ${pkg}.${primary.name}` });

  const section = (title, list) => {
    if (!list.length) return;
    blocks.push({ t: 'declgroup', title, decls: list.map((d) => declBlock(d, ctx, d === primary)) });
  };
  section('Composables', unit.composables);
  section('Types', unit.types);
  section(unit.composables.length || unit.types.length ? 'Functions & properties' : 'API', unit.others);

  return {
    path: `${unit.section}/${unit.slug}`,
    section: cfg.label,
    sectionKey: unit.section,
    title: unit.title,
    description: pageDescription(unit),
    order: 100,
    blocks,
  };
}

export function indexPage(section, units) {
  const cfg = SECTIONS[section];
  const link = (u) => `[**${u.title}**](/docs/${section}/${u.slug}) — ${inline(pageDescription(u))}`;
  const ui = units.filter((u) => u.kind === 'ui');
  const api = units.filter((u) => u.kind !== 'ui');
  const blocks = [{ t: 'p', s: cfg.blurb }];
  if (ui.length) blocks.push({ t: 'h', l: 2, s: 'UI components' }, { t: 'list', ordered: false, items: ui.map(link) });
  if (api.length) blocks.push({ t: 'h', l: 2, s: ui.length ? 'Foundations' : 'API reference' }, { t: 'list', ordered: false, items: api.map(link) });
  return { path: section, section: cfg.label, sectionKey: section, title: cfg.label, description: cfg.blurb.replace(/`/g, ''), order: 0, blocks };
}
