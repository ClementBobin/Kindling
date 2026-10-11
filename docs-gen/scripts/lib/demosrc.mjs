// Pulls the source of each live demo (showcase/src/**/XDemo.kt) so a docs page can show it next to the preview.
import fs from 'node:fs';
import path from 'node:path';

function walk(dir, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) {
      if (!/^(build|generated|docs)$/.test(e.name)) walk(p, out);
    } else if (e.name.endsWith('.kt')) out.push(p);
  }
  return out;
}

/** Index just past the end of the declaration that starts at `start` (balanced () [] {}, strings and comments skipped). */
function declEnd(t, start) {
  let depth = 0;
  let sawBlock = false;
  let i = start;
  while (i < t.length) {
    const c = t[i];
    if (t.startsWith('//', i)) { i = t.indexOf('\n', i); if (i < 0) return t.length; continue; }
    if (t.startsWith('/*', i)) { i = t.indexOf('*/', i) + 2; continue; }
    if (t.startsWith('"""', i)) { i = t.indexOf('"""', i + 3) + 3; continue; }
    if (c === '"') {
      i++;
      while (i < t.length && t[i] !== '"') i += t[i] === '\\' ? 2 : 1;
      i++;
      continue;
    }
    if (c === "'") { i += t[i + 1] === '\\' ? 4 : 3; continue; }
    if ('([{'.includes(c)) { depth++; if (c === '{') sawBlock = true; }
    else if (')]}'.includes(c)) {
      depth--;
      if (depth === 0 && c === '}' && sawBlock) return i + 1;
    } else if (c === '\n' && depth === 0 && !sawBlock && i > start) {
      // A declaration without a body (val x = ..., data class P(...)) ends at the line break once the next line is back at column 0.
      const next = t.slice(i + 1).match(/^(\s*)(\S)/);
      if (!next || (next[1].length === 0 || next[1].includes('\n')) && !'.{)+?:'.includes(next[2])) return i;
    }
    i++;
  }
  return t.length;
}

function topLevel(text) {
  // name -> { start, end, text, isPrivate }
  const out = new Map();
  const re = /^(?:@\w+(?:\([^)]*\))?\s*\n)*((?:private |internal |public )?(?:fun|val|var|class|object|enum class|data class)\s+(?:<[^>]*>\s*)?(?:\w+\.)?(\w+))/gm;
  let m;
  while ((m = re.exec(text))) {
    const start = m.index;
    const end = declEnd(text, start + m[0].length - m[1].length);
    out.set(m[2], { start, end, text: text.slice(start, end), isPrivate: /^private /.test(m[1]) });
    re.lastIndex = Math.max(re.lastIndex, end);
  }
  return out;
}

export function extractDemoSources(showcaseSrc) {
  const registry = path.join(showcaseSrc, 'dev/kindling/showcase/Registry.kt');
  const reg = fs.existsSync(registry) ? fs.readFileSync(registry, 'utf8') : '';
  const wanted = [...reg.matchAll(/\bdemo\(\s*"([a-z0-9-]+)"\s*,\s*"[^"]*"\s*\)\s*\{\s*(\w+)\(\)/g)].map((m) => [m[1], m[2]]);

  const files = walk(showcaseSrc).map((f) => {
    const text = fs.readFileSync(f, 'utf8');
    return { text, imports: [...text.matchAll(/^import\s+([\w.]+)\s*$/gm)].map((m) => m[1]), decls: topLevel(text) };
  });

  const result = new Map();
  for (const [id, fn] of wanted) {
    const file = files.find((f) => f.decls.has(fn));
    if (!file) continue;
    const parts = new Map();
    const queue = [fn];
    while (queue.length) {
      const name = queue.shift();
      if (parts.has(name)) continue;
      const d = file.decls.get(name);
      if (!d) continue;
      parts.set(name, d);
      for (const [other, od] of file.decls) {
        if ((od.isPrivate || /Demo$/.test(other)) && !parts.has(other) && new RegExp(`\\b${other}\\b`).test(d.text)) queue.push(other);
      }
    }
    // helpers first, the demo last, in source order
    const body = [...parts.entries()].sort((a, b) => (a[0] === fn ? 1 : b[0] === fn ? -1 : a[1].start - b[1].start)).map(([, d]) => d.text.trim()).join('\n\n');
    const imports = file.imports
      .filter((i) => i.startsWith('dev.kindling.') && !i.startsWith('dev.kindling.showcase'))
      .filter((i) => new RegExp(`\\b${i.split('.').pop()}\\b`).test(body))
      .sort();
    const head = imports.length ? `${imports.map((i) => `import ${i}`).join('\n')}\n// (androidx.compose imports omitted)\n\n` : '';
    result.set(id, `${head}${body}\n`);
  }
  return result;
}
