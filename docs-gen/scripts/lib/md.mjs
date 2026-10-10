// Markdown -> neutral "blocks" consumed by the Kotlin docs site (see GeneratedDocs emit in docgen.mjs).
//
// Block shapes:
//   {t:'h', l, s} {t:'p', s} {t:'code', lang, s} {t:'list', ordered, items:[s]} {t:'quote', s}
//   {t:'props', rows:[{name,type,def,desc,req}]} {t:'demo', id} {t:'playground', id} {t:'gallery'} {t:'hr'} {t:'src', url}
// Inline text (`code`, **bold**, [links](url)) stays as raw Markdown and is parsed in Kotlin.

export function parseFrontmatter(text) {
  const m = text.match(/^---\n([\s\S]*?)\n---\n?/);
  if (!m) return { meta: {}, body: text };
  const meta = {};
  for (const line of m[1].split('\n')) {
    const kv = line.match(/^(\w+):\s*(.*)$/);
    if (kv) meta[kv[1]] = kv[2].replace(/^"(.*)"$/, '$1');
  }
  return { meta, body: text.slice(m[0].length) };
}

export function mdToBlocks(md) {
  const lines = md.replace(/\r/g, '').split('\n');
  const out = [];
  let para = [];
  const flush = () => {
    if (para.length) out.push({ t: 'p', s: para.join(' ').replace(/\s+/g, ' ').trim() });
    para = [];
  };
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const fence = line.match(/^\s*```(\w*)/);
    if (fence) {
      flush();
      const code = [];
      i++;
      while (i < lines.length && !/^\s*```/.test(lines[i])) code.push(lines[i++]);
      out.push({ t: 'code', lang: fence[1] || 'kotlin', s: code.join('\n') });
      continue;
    }
    if (/^\s*<!--/.test(line) || /^\s*\{\/\*.*\*\/\}\s*$/.test(line)) {
      flush();
      continue;
    }
    const dir = line.match(/^\s*\{\{(demo|gallery|props|playground):?(.*)\}\}\s*$/);
    if (dir) {
      flush();
      if (dir[1] === 'demo') out.push({ t: 'demo', id: dir[2].trim() });
      else if (dir[1] === 'playground') out.push({ t: 'playground', id: dir[2].trim() });
      else if (dir[1] === 'gallery') out.push({ t: 'gallery' });
      else {
        const [n, type, def, ...d] = dir[2].split('|');
        const req = n.endsWith('*');
        const row = { name: n.replace('*', '').trim(), type: (type ?? '').trim(), def: (def ?? '').trim(), desc: d.join('|').trim(), req };
        const last = out[out.length - 1];
        if (last?.t === 'props') last.rows.push(row);
        else out.push({ t: 'props', rows: [row] });
      }
      continue;
    }
    const h = line.match(/^(#{1,6})\s+(.*?)\s*$/);
    if (h) {
      flush();
      out.push({ t: 'h', l: h[1].length, s: h[2] });
      continue;
    }
    if (/^\s*(---|\*\*\*)\s*$/.test(line)) {
      flush();
      out.push({ t: 'hr' });
      continue;
    }
    if (/^\s*>/.test(line)) {
      flush();
      const q = [];
      while (i < lines.length && /^\s*>/.test(lines[i])) q.push(lines[i++].replace(/^\s*>\s?/, ''));
      i--;
      out.push({ t: 'quote', s: q.join(' ').trim() });
      continue;
    }
    const li = line.match(/^\s*([-*]|\d+\.)\s+(.*)$/);
    if (li) {
      flush();
      const ordered = /\d/.test(li[1]);
      const items = [li[2]];
      while (i + 1 < lines.length) {
        const nx = lines[i + 1];
        const m2 = nx.match(/^\s*([-*]|\d+\.)\s+(.*)$/);
        if (m2) {
          items.push(m2[2]);
          i++;
        } else if (/^\s{2,}\S/.test(nx)) {
          items[items.length - 1] += ` ${nx.trim()}`;
          i++;
        } else break;
      }
      out.push({ t: 'list', ordered, items });
      continue;
    }
    if (!line.trim()) {
      flush();
      continue;
    }
    para.push(line.trim());
  }
  flush();
  return out;
}
