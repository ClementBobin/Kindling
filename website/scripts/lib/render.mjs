// Turns a documentation unit (see units.mjs) into an MDX page for Fumadocs.

import { kdocToMdx, plain } from './kotlin.mjs';
import { SECTIONS } from './units.mjs';

const SIG_MODS = new Set(['suspend', 'inline', 'operator', 'infix', 'data', 'enum', 'sealed', 'abstract', 'open', 'value', 'annotation', 'const', 'lateinit']);

export const GENERATED_NOTICE =
  '{/* AUTO-GENERATED from KDoc by website/scripts/docgen.mjs. Set `generated: false` in the frontmatter to take over this page by hand. */}';

const q = (s) => JSON.stringify(s);

function sentenceTrim(s, max = 180) {
  const t = plain(s);
  if (t.length <= max) return t;
  const cut = t.slice(0, max);
  const dot = cut.lastIndexOf('. ');
  return dot > 60 ? cut.slice(0, dot + 1) : `${cut.replace(/\s+\S*$/, '')}…`;
}

function mdxInline(s) {
  return kdocToMdx(plain(s) ? s.replace(/\n/g, ' ') : '');
}

function paramSig(p) {
  const kw = p.property ? (p.mutable ? 'var ' : 'val ') : '';
  return `${p.vararg ? 'vararg ' : ''}${kw}${p.name}${p.type ? `: ${p.type}` : ''}${p.default !== null ? ` = ${p.default}` : ''}`;
}

export function signature(d) {
  const mods = d.modifiers.filter((m) => SIG_MODS.has(m)).join(' ');
  const prefix = mods ? `${mods} ` : '';
  const ann = d.composable ? '@Composable\n' : '';

  if (d.keyword === 'fun') {
    const head = `${prefix}fun ${d.generics ? `${d.generics} ` : ''}${d.receiver ? `${d.receiver}.` : ''}${d.name}`;
    const ret = d.returns ? `: ${d.returns}` : '';
    const one = `${head}(${d.params.map(paramSig).join(', ')})${ret}`;
    if (d.params.length <= 1 && one.length <= 90) return ann + one;
    return `${ann}${head}(\n${d.params.map((p) => `    ${paramSig(p)},`).join('\n')}\n)${ret}`;
  }
  if (d.keyword === 'val' || d.keyword === 'var') {
    return `${prefix}${d.keyword} ${d.receiver ? `${d.receiver}.` : ''}${d.name}${d.type ? `: ${d.type}` : ''}`;
  }
  if (d.keyword === 'typealias') return `typealias ${d.name} = ${d.type}`;

  const kw = d.keyword;
  const head = `${prefix}${kw} ${d.name}${d.generics}`;
  const sup = d.supertypes ? ` : ${d.supertypes}` : '';
  if (d.params.length === 0) return `${head}${sup}`;
  const one = `${head}(${d.params.map(paramSig).join(', ')})${sup}`;
  if (d.params.length <= 1 && one.length <= 90) return one;
  return `${head}(\n${d.params.map((p) => `    ${paramSig(p)},`).join('\n')}\n)${sup}`;
}

function oneLine(d) {
  return signature(d).replace(/^@Composable\n/, '').replace(/\s*\n\s*/g, ' ').replace(/,\s*\)/, ')');
}

function typeTable(d) {
  if (!d.params?.length) return '';
  const docParams = d.doc?.params ?? {};
  const type = {};
  for (const p of d.params) {
    const entry = { type: p.type || 'Any' };
    const desc = plain(docParams[p.name] ?? p.doc ?? '');
    if (desc) entry.description = desc;
    if (p.default !== null) entry.default = p.default.length > 90 ? `${p.default.slice(0, 87)}…` : p.default;
    if (p.required) entry.required = true;
    type[p.name] = entry;
  }
  return `<TypeTable type={${JSON.stringify(type)}} />`;
}

function callouts(d) {
  const out = [];
  const dep = d.annotations.find((a) => /^@Deprecated\b/.test(a));
  if (dep) {
    const m = dep.match(/"([^"]*)"/);
    out.push(`<Callout type="warn" title="Deprecated">${m ? mdxInline(m[1]) : 'This API is deprecated.'}</Callout>`);
  }
  if (d.annotations.some((a) => /^@(Experimental\w*|ExperimentalKindlingApi|RequiresOptIn)\b/.test(a))) {
    out.push('<Callout type="info" title="Experimental">This API is experimental and may change without notice.</Callout>');
  }
  return out.join('\n\n');
}

function tags(d) {
  const lines = [];
  for (const t of d.doc?.tags ?? []) {
    if (t.tag === 'return' || t.tag === 'returns') lines.push(`**Returns** ${mdxInline(t.text)}`);
    else if (t.tag === 'throws' || t.tag === 'exception') {
      const m = t.text.match(/^(\S+)\s*(.*)$/);
      if (m) lines.push(`**Throws** \`${m[1]}\`${m[2] ? ` — ${mdxInline(m[2])}` : ''}`);
    } else if (t.tag === 'see') lines.push(`**See also** ${mdxInline(t.text)}`);
    else if (t.tag === 'since') lines.push(`_Since ${plain(t.text)}_`);
  }
  return lines.join('\n\n');
}

function memberList(label, list) {
  if (!list?.length) return '';
  const items = list.slice(0, 60).map((m) => {
    const sum = m.doc?.summary ? ` — ${mdxInline(m.doc.summary)}` : '';
    return `- \`${oneLine(m)}\`${sum}`;
  });
  const more = list.length > 60 ? `\n- …and ${list.length - 60} more` : '';
  return `**${label}**\n\n${items.join('\n')}${more}`;
}

function sourceLink(d, ctx) {
  return `[View source](https://github.com/${ctx.repo}/blob/${ctx.branch}/${d.file}#L${d.line})`;
}

function declSection(d, ctx, level, { isPrimary }) {
  const parts = [];
  parts.push(`${'#'.repeat(level)} ${d.name}`);
  const c = callouts(d);
  if (c) parts.push(c);
  // The primary declaration's summary is the page description and its body is the page intro,
  // so repeating them under its own heading would just duplicate the text.
  if (!isPrimary) {
    if (d.doc?.summary) parts.push(kdocToMdx(d.doc.summary));
    if (d.doc?.body) parts.push(kdocToMdx(d.doc.body));
  }
  parts.push(`\`\`\`kotlin\n${signature(d)}\n\`\`\``);
  const tt = typeTable(d);
  if (tt) parts.push(tt);

  if (d.entries?.length) {
    parts.push(
      `**Values**\n\n${d.entries.map((e) => `- \`${e.name}\`${e.doc?.summary ? ` — ${mdxInline(e.doc.summary)}` : ''}`).join('\n')}`,
    );
  }
  const presets = memberList('Presets', d.companion);
  if (presets) parts.push(presets);
  const members = memberList('Members', d.members);
  if (members) parts.push(members);

  const t = tags(d);
  if (t) parts.push(t);
  parts.push(sourceLink(d, ctx));
  return parts.join('\n\n');
}

export function pageDescription(unit) {
  const s = unit.primary?.doc?.summary;
  return s ? sentenceTrim(s) : `${unit.title} API reference.`;
}

function importLine(unit) {
  const d = unit.primary;
  if (!d) return '';
  const pkg = packageOf(d.file);
  return pkg ? `import ${pkg}.${d.name}` : '';
}

function packageOf(file) {
  const m = file.match(/\/kotlin\/(dev\/kindling\/[^]*)\/[^/]+\.kt$/);
  return m ? m[1].replace(/\//g, '.') : '';
}

export function renderUnit(unit, ctx) {
  const cfg = SECTIONS[unit.section];
  const description = pageDescription(unit);
  const primary = unit.primary;
  const out = [];

  out.push(
    `---\ntitle: ${q(unit.title)}\ndescription: ${q(description)}\ngenerated: true\nmodule: ${q(cfg.module)}\n---`,
  );
  out.push(GENERATED_NOTICE);

  const hasShowcase = unit.kind === 'ui' && ctx.showcaseIds.has(unit.slug);
  const examples = unit.examples;
  const first = examples[0];

  if (primary?.doc?.body) out.push(kdocToMdx(primary.doc.body));

  // Hand-written or AI-drafted prose lives in docs-extra/<section>/<slug>.mdx and survives regeneration.
  const extra = ctx.extra?.(unit.section, unit.slug);
  if (extra) out.push(extra.trim());

  if (hasShowcase) {
    const code = first ? `\n\n\`\`\`${first.lang}\n${first.code}\n\`\`\`\n\n` : '\n\n_No code example in the KDoc yet._\n\n';
    out.push(
      `<Tabs items={['Preview', 'Code']}>\n  <Tab value="Preview">\n    <Showcase component=${q(unit.slug)} />\n  </Tab>\n  <Tab value="Code">${code}  </Tab>\n</Tabs>`,
    );
  } else if (first) {
    out.push(`## Usage\n\n\`\`\`${first.lang}\n${first.code}\n\`\`\``);
  }
  const rest = examples.slice(1);
  if (rest.length) {
    out.push(
      `## More examples\n\n${rest.map((e) => `${e.from ? `**${e.from}**\n\n` : ''}\`\`\`${e.lang}\n${e.code}\n\`\`\``).join('\n\n')}`,
    );
  }

  const imp = importLine(unit);
  out.push(
    `## Installation\n\n\`\`\`kotlin\nimplementation("io.github.clementbobin.kindling:${cfg.module}:${ctx.version}")\n\`\`\`${imp ? `\n\n\`\`\`kotlin\n${imp}\n\`\`\`` : ''}`,
  );

  const section = (title, list) => {
    if (!list.length) return;
    const blocks = list.map((d) => declSection(d, ctx, 3, { isPrimary: d === primary }));
    out.push(`## ${title}\n\n${blocks.join('\n\n---\n\n')}`);
  };
  section('Composables', unit.composables);
  section('Types', unit.types);
  section(unit.composables.length || unit.types.length ? 'Functions & properties' : 'API', unit.others);

  return `${out.join('\n\n')}\n`;
}

export function renderIndex(section, units, ctx) {
  const cfg = SECTIONS[section];
  const card = (u) =>
    `  <Card title=${q(u.title)} href=${q(`/docs/${section}/${u.slug}`)}>\n    ${mdxInline(pageDescription(u)) || ' '}\n  </Card>`;
  const ui = units.filter((u) => u.kind === 'ui');
  const api = units.filter((u) => u.kind !== 'ui');

  const parts = [
    `---\ntitle: ${q(cfg.label)}\ndescription: ${q(cfg.blurb.replace(/`/g, ''))}\ngenerated: true\nmodule: ${q(cfg.module)}\n---`,
    GENERATED_NOTICE,
    `${cfg.blurb}`,
  ];
  if (ui.length) parts.push(`## UI components\n\n<Cards>\n${ui.map(card).join('\n')}\n</Cards>`);
  if (api.length) parts.push(`## ${ui.length ? 'Foundations' : 'API reference'}\n\n<Cards>\n${api.map(card).join('\n')}\n</Cards>`);
  return `${parts.join('\n\n')}\n`;
}
