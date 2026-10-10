// Minimal, dependency-free Kotlin declaration + KDoc scanner.
//
// It is NOT a full Kotlin parser. It understands exactly what the docs need:
// top-level (and member) declarations, their visibility, parameters with
// defaults, return types, enum entries, companion members, and the KDoc that
// precedes them. Strings and comments are masked first so brace/paren matching
// is reliable.

const MODIFIERS =
  'public|private|internal|protected|open|abstract|final|sealed|data|enum|annotation|inline|value|expect|actual|suspend|operator|infix|tailrec|external|const|lateinit|override|inner|fun(?=\\s+interface)';
const DECL_RE = new RegExp(
  `^((?:(?:${MODIFIERS})\\s+)*)(fun\\s+interface|fun|class|interface|object|val|var|typealias)\\b\\s*`,
);

/** Replace comments and string contents by spaces (keeping newlines + length) and collect KDoc blocks. */
export function mask(src) {
  const out = src.split('');
  const docs = [];
  const comments = [];
  const n = src.length;
  let i = 0;
  const blank = (from, to) => {
    for (let k = from; k < to; k++) if (out[k] !== '\n') out[k] = ' ';
  };
  while (i < n) {
    const c = src[i];
    const d = src[i + 1];
    if (c === '/' && d === '/') {
      let j = i;
      while (j < n && src[j] !== '\n') j++;
      comments.push({ start: i, end: j });
      blank(i, j);
      i = j;
    } else if (c === '/' && d === '*') {
      // Kotlin block comments nest.
      let depth = 1;
      let j = i + 2;
      while (j < n && depth > 0) {
        if (src[j] === '/' && src[j + 1] === '*') {
          depth++;
          j += 2;
        } else if (src[j] === '*' && src[j + 1] === '/') {
          depth--;
          j += 2;
        } else j++;
      }
      if (src[i + 2] === '*' && src[i + 3] !== '/') docs.push({ start: i, end: j, text: src.slice(i, j) });
      comments.push({ start: i, end: j });
      blank(i, j);
      i = j;
    } else if (c === '"' && src.startsWith('"""', i)) {
      let j = src.indexOf('"""', i + 3);
      j = j === -1 ? n : j + 3;
      blank(i + 1, j - 1);
      i = j;
    } else if (c === '"') {
      let j = i + 1;
      while (j < n && src[j] !== '"' && src[j] !== '\n') {
        if (src[j] === '\\') j++;
        j++;
      }
      blank(i + 1, j);
      i = j + 1;
    } else if (c === "'") {
      let j = i + 1;
      while (j < n && src[j] !== "'" && src[j] !== '\n') {
        if (src[j] === '\\') j++;
        j++;
      }
      blank(i + 1, j);
      i = j + 1;
    } else i++;
  }
  return { masked: out.join(''), docs, comments };
}

function matching(masked, open, from) {
  const close = { '(': ')', '{': '}', '[': ']' }[open];
  let depth = 0;
  for (let i = from; i < masked.length; i++) {
    if (masked[i] === open) depth++;
    else if (masked[i] === close && --depth === 0) return i;
  }
  return -1;
}

/** Ranges of `s` split on top-level commas (ignores nested ()[]{}<> and the `->` arrow). `s` must be masked. */
function splitTopRanges(s) {
  const ranges = [];
  let depth = 0;
  let from = 0;
  for (let i = 0; i < s.length; i++) {
    const c = s[i];
    if ('([{<'.includes(c)) depth++;
    else if (')]}'.includes(c) || (c === '>' && s[i - 1] !== '-' && s[i - 1] !== '=')) depth--;
    if (c === ',' && depth === 0) {
      ranges.push([from, i]);
      from = i + 1;
    }
  }
  ranges.push([from, s.length]);
  return ranges;
}

function blankRanges(text, ranges) {
  const out = text.split('');
  for (const { start, end } of ranges) for (let k = start; k < end; k++) if (out[k] !== '\n') out[k] = ' ';
  return out.join('');
}

function indexTop(s, ch) {
  let depth = 0;
  for (let i = 0; i < s.length; i++) {
    const c = s[i];
    if ('([{<'.includes(c)) depth++;
    else if (')]}'.includes(c) || (c === '>' && s[i - 1] !== '-' && s[i - 1] !== '=')) depth--;
    else if (depth === 0 && c === ch) {
      if (ch === '=' && (s[i + 1] === '=' || s[i - 1] === '!' || s[i - 1] === '<' || s[i - 1] === '>')) continue;
      return i;
    }
  }
  return -1;
}

const clean = (s) => s.replace(/\s+/g, ' ').trim();

export function parseParams(text, { allowVal = false } = {}) {
  const { masked, comments } = mask(text);
  const items = splitTopRanges(masked)
    .map(([a, b]) => {
      const cs = comments.filter((c) => c.start >= a && c.end <= b);
      const docC = cs.find((c) => text.startsWith('/**', c.start));
      const doc = docC ? parseKDoc(text.slice(docC.start, docC.end)) : null;
      const raw = blankRanges(
        text.slice(a, b),
        cs.map((c) => ({ start: c.start - a, end: c.end - a })),
      ).trim();
      return { raw, doc };
    })
    .filter((x) => x.raw);
  return items.map(({ raw, doc }) => {
    let p = raw.replace(/^(?:@[\w.]+(?:\([^)]*\))?\s*)+/, '').trim();
    let mutable = null;
    let prop = false;
    p = p.replace(/^((?:(?:vararg|noinline|crossinline|public|private|internal|protected|override|open)\s+)*)/, (m) => m);
    const kw = p.match(/^(?:(?:vararg|noinline|crossinline|public|private|internal|protected|override|open)\s+)*(val|var)\s+/);
    if (kw) {
      prop = true;
      mutable = kw[1] === 'var';
      p = p.replace(/^((?:(?:vararg|noinline|crossinline|public|private|internal|protected|override|open)\s+)*)(val|var)\s+/, '$1');
    }
    const vararg = /(^|\s)vararg\s/.test(p);
    p = p.replace(/^((?:(?:vararg|noinline|crossinline|public|private|internal|protected|override|open)\s+)*)/, '');
    const colon = indexTop(p, ':');
    const name = (colon === -1 ? p : p.slice(0, colon)).trim();
    let rest = colon === -1 ? '' : p.slice(colon + 1);
    let def = null;
    const eq = indexTop(rest, '=');
    if (eq !== -1) {
      def = clean(rest.slice(eq + 1));
      rest = rest.slice(0, eq);
    }
    const isPrivate = /(^|\s)(private|internal)\s+(val|var)/.test(raw);
    return {
      name,
      doc: doc?.summary ?? '',
      type: clean(rest),
      default: def,
      required: def === null && !vararg,
      vararg,
      property: prop,
      mutable,
      hidden: isPrivate && allowVal,
    };
  });
}

function stripGenerics(s) {
  s = s.trimStart();
  if (!s.startsWith('<')) return { generics: '', rest: s };
  let depth = 0;
  for (let i = 0; i < s.length; i++) {
    if (s[i] === '<') depth++;
    else if (s[i] === '>' && s[i - 1] !== '-' && --depth === 0) {
      return { generics: s.slice(0, i + 1), rest: s.slice(i + 1).trimStart() };
    }
  }
  return { generics: '', rest: s };
}

function lastDotTop(s) {
  let depth = 0;
  let idx = -1;
  for (let i = 0; i < s.length; i++) {
    const c = s[i];
    if ('<('.includes(c)) depth++;
    else if (')'.includes(c) || (c === '>' && s[i - 1] !== '-')) depth--;
    else if (c === '.' && depth === 0) idx = i;
  }
  return idx;
}

function readHeader(masked, original, start) {
  let i = start;
  let paren = 0;
  let bracket = 0;
  while (i < masked.length) {
    const c = masked[i];
    if (c === '(') paren++;
    else if (c === ')') paren--;
    else if (c === '[') bracket++;
    else if (c === ']') bracket--;
    else if (paren === 0 && bracket === 0) {
      if (c === '{') break;
      if (c === '=' && masked[i + 1] !== '=' && !'!<>='.includes(masked[i - 1] ?? '')) break;
      if (c === '\n') {
        const nxt = (masked.slice(i + 1).match(/^\s*(\S+)/) ?? [])[1] ?? '';
        if (!/^(:|,|where\b|=|\{|->|&&|\|\|)/.test(nxt)) break;
      }
    }
    i++;
  }
  return { text: original.slice(start, i), end: i };
}

const lineNo = (src, idx) => src.slice(0, idx).split('\n').length;

/**
 * Scan one source blob and return the public declarations found at its top level.
 * `baseLine` lets recursive scans of member bodies report correct line numbers.
 */
export function scan(original, { baseLine = 1, includeNonPublic = false } = {}) {
  const { masked, docs } = mask(original);
  const decls = [];
  const lines = masked.split('\n');
  let offset = 0;
  let depth = 0;
  let pendingDoc = null;
  let annotations = [];
  let skipUntil = -1;

  for (let li = 0; li < lines.length; li++) {
    const ml = lines[li];
    const lineStart = offset;
    const startDepth = depth;
    for (const ch of ml) {
      if (ch === '{') depth++;
      else if (ch === '}') depth--;
    }
    offset += ml.length + 1;
    if (lineStart < skipUntil) continue;

    const trimmed = ml.trim();
    const docHere = docs.find((d) => d.start >= lineStart && d.start < lineStart + ml.length + 1);
    if (startDepth === 0 && docHere && !trimmed) {
      pendingDoc = docHere;
      continue;
    }
    if (startDepth !== 0 || !trimmed) continue;
    if (/^(package|import)\b/.test(trimmed)) {
      pendingDoc = null;
      annotations = [];
      continue;
    }
    if (trimmed.startsWith('@')) {
      // Annotation (possibly multi-line). Remember it, keep the pending doc.
      const at = lineStart + ml.indexOf('@');
      let end = at;
      const open = masked.indexOf('(', at);
      const nl = masked.indexOf('\n', at);
      if (open !== -1 && (nl === -1 || open < nl) && !/^@[\w.:]+\s+\S/.test(trimmed.replace(/\(.*$/, ''))) {
        const close = matching(masked, '(', open);
        end = close === -1 ? nl : close + 1;
      } else {
        // inline annotation(s) followed by a declaration on the same line
        const m = trimmed.match(/^((?:@[\w.:]+(?:\([^)]*\))?\s*)+)(.*)$/);
        if (m && m[2]) {
          annotations.push(m[1].trim());
          const declStart = lineStart + ml.indexOf(m[2]);
          processDecl(declStart);
          continue;
        }
        end = nl === -1 ? masked.length : nl;
      }
      annotations.push(original.slice(at, end).trim());
      skipUntil = end;
      continue;
    }
    if (DECL_RE.test(trimmed)) {
      processDecl(lineStart + ml.indexOf(trimmed));
      continue;
    }
    pendingDoc = null;
    annotations = [];
  }

  function processDecl(start) {
    const doc = pendingDoc;
    const anns = annotations;
    pendingDoc = null;
    annotations = [];
    const { text: header, end } = readHeader(masked, original, start);
    // Lines inside the header (multi-line constructor/parameter lists) are not declarations.
    skipUntil = Math.max(skipUntil, end);
    const m = header.trimStart().match(DECL_RE);
    if (!m) return;
    const mods = m[1].trim().split(/\s+/).filter(Boolean);
    const keyword = m[2].replace(/\s+/g, ' ');
    const isPublic = !mods.includes('private') && !mods.includes('internal');
    if (!isPublic && !includeNonPublic) return;
    let rest = header.trimStart().slice(m[0].length);
    const decl = {
      keyword,
      modifiers: mods,
      annotations: anns,
      visibility: mods.find((x) => ['public', 'private', 'internal', 'protected'].includes(x)) ?? 'public',
      doc: doc ? parseKDoc(doc.text) : null,
      line: baseLine + lineNo(original, start) - 1,
      name: '',
      params: [],
      returns: '',
      generics: '',
      receiver: '',
      type: '',
      members: [],
      entries: [],
      supertypes: '',
      signature: '',
    };
    const composable = anns.some((a) => /^@Composable\b/.test(a));
    decl.composable = composable;

    if (keyword === 'fun' ) {
      const g = stripGenerics(rest);
      decl.generics = g.generics;
      rest = g.rest;
      const open = rest.indexOf('(');
      if (open === -1) return;
      const prefix = rest.slice(0, open).trim();
      const dot = lastDotTop(prefix);
      decl.receiver = dot === -1 ? '' : prefix.slice(0, dot).trim();
      decl.name = (dot === -1 ? prefix : prefix.slice(dot + 1)).trim();
      const absOpen = start + header.indexOf('(', header.indexOf(rest.slice(0, 3)) >= 0 ? 0 : 0);
      void absOpen;
      const closeRel = matching(rest, '(', open);
      decl.params = parseParams(rest.slice(open + 1, closeRel === -1 ? undefined : closeRel));
      const tail = closeRel === -1 ? '' : rest.slice(closeRel + 1).trim();
      decl.returns = tail.startsWith(':') ? clean(tail.slice(1).replace(/\bwhere\b[\s\S]*$/, '')) : '';
      decl.signature = clean(
        `${decl.modifiers.filter((x) => x !== 'public').join(' ')} fun ${decl.generics} ${decl.receiver ? `${decl.receiver}.` : ''}${decl.name}`,
      );
    } else if (keyword === 'val' || keyword === 'var') {
      const g = stripGenerics(rest);
      rest = g.rest;
      const colon = indexTop(rest, ':');
      const head = clean(colon === -1 ? rest : rest.slice(0, colon));
      const dot = lastDotTop(head);
      decl.receiver = dot === -1 ? '' : head.slice(0, dot).trim();
      decl.name = dot === -1 ? head : head.slice(dot + 1);
      decl.type = colon === -1 ? '' : clean(rest.slice(colon + 1));
    } else if (keyword === 'typealias') {
      decl.name = (rest.match(/^\w+/) ?? [''])[0];
      decl.type = clean(rest.slice(rest.indexOf('=') + 1));
    } else {
      // class / interface / object / fun interface
      decl.name = (rest.match(/^\w+/) ?? [''])[0];
      rest = rest.slice(decl.name.length);
      const g = stripGenerics(rest);
      decl.generics = g.generics;
      rest = g.rest.replace(/^(?:(?:public|private|internal|protected)\s+)?(?:@[\w.]+(?:\([^)]*\))?\s*)*(?:constructor\s*)?(?=\()/, '');
      if (rest.startsWith('(')) {
        const close = matching(rest, '(', 0);
        // Keep private ctor properties too: they are still required arguments (e.g. KDebouncer's `scope`).
        decl.params = parseParams(rest.slice(1, close === -1 ? undefined : close), { allowVal: true });
        rest = close === -1 ? '' : rest.slice(close + 1);
      }
      decl.supertypes = clean(rest.replace(/^\s*:/, '')).replace(/\{[\s\S]*$/, '').trim();
      // Body: members, enum entries, companion members.
      const bodyOpen = masked.indexOf('{', end - 1) === end ? end : masked[end] === '{' ? end : -1;
      if (bodyOpen !== -1) {
        const bodyClose = matching(masked, '{', bodyOpen);
        const body = original.slice(bodyOpen + 1, bodyClose === -1 ? undefined : bodyClose);
        const bodyLine = baseLine + lineNo(original, bodyOpen) - 1;
        if (mods.includes('enum')) decl.entries = parseEnumEntries(body);
        const comp = findCompanion(body);
        const own = scan(comp ? body.replace(comp.text, ' '.repeat(comp.text.length)) : body, {
          baseLine: bodyLine,
        }).filter((d) => !d.modifiers.includes('override'));
        decl.members = own;
        if (comp) decl.companion = scan(comp.body, { baseLine: bodyLine }).filter((d) => !d.modifiers.includes('override'));
      }
    }
    if (decl.name) decls.push(decl);
  }

  return decls;
}

function findCompanion(body) {
  const { masked } = mask(body);
  const m = masked.match(/\bcompanion\s+object\b[^{\n]*\{/);
  if (!m) return null;
  const open = m.index + m[0].length - 1;
  const close = matching(masked, '{', open);
  if (close === -1) return null;
  return { text: body.slice(m.index, close + 1), body: body.slice(open + 1, close) };
}

function parseEnumEntries(body) {
  const { masked } = mask(body);
  const semi = (() => {
    let depth = 0;
    for (let i = 0; i < masked.length; i++) {
      const c = masked[i];
      if ('({['.includes(c)) depth++;
      else if (')}]'.includes(c)) depth--;
      else if (c === ';' && depth === 0) return i;
    }
    return masked.length;
  })();
  const region = masked.slice(0, semi);
  const entries = [];
  const regionOrig = body.slice(0, semi);
  const re = /(?:\/\*\*([\s\S]*?)\*\/\s*)?(?:@\w+(?:\([^)]*\))?\s*)*\b([A-Z][A-Za-z0-9_]*)\b/g;
  // Walk top-level comma separated chunks to avoid matching inside constructor args.
  let depth = 0;
  let chunkStart = 0;
  const chunks = [];
  for (let i = 0; i <= region.length; i++) {
    const c = region[i];
    if (i === region.length || (c === ',' && depth === 0)) {
      chunks.push([chunkStart, i]);
      chunkStart = i + 1;
    } else if ('({['.includes(c)) depth++;
    else if (')}]'.includes(c)) depth--;
  }
  for (const [a, b] of chunks) {
    const chunk = regionOrig.slice(a, b);
    re.lastIndex = 0;
    const mm = re.exec(chunk);
    if (!mm) continue;
    entries.push({ name: mm[2], doc: mm[1] ? parseKDoc(`/**${mm[1]}*/`) : null });
  }
  return entries;
}

// ─────────────────────────────────────────────────────────────────────────────
//  KDoc
// ─────────────────────────────────────────────────────────────────────────────

// Parse a raw KDoc block (slash-star-star ... star-slash) into { summary, body, examples, params, tags }.
export function parseKDoc(raw) {
  const text = raw
    .replace(/^\/\*\*/, '')
    .replace(/\*\/$/, '')
    .split('\n')
    .map((l) => l.replace(/^\s*\* ?/, ''))
    .join('\n')
    .replace(/^\n+|\n+$/g, '');

  const lines = text.split('\n');
  const main = [];
  const tags = [];
  let fence = false;
  let cur = null;
  for (const line of lines) {
    if (/^\s*```/.test(line)) fence = !fence;
    const tag = !fence && line.match(/^@(\w+)\s*(.*)$/);
    if (tag) {
      cur = { tag: tag[1], text: tag[2] };
      tags.push(cur);
    } else if (cur && !fence) cur.text += `\n${line}`;
    else if (cur && fence) cur.text += `\n${line}`;
    else main.push(line);
  }

  // Pull fenced blocks that sit under an "Example"/"Usage" heading out of the body.
  const examples = [];
  const body = [];
  let i = 0;
  while (i < main.length) {
    const line = main[i];
    const h = line.match(/^(#{1,6})\s*(.*?):?\s*$/);
    if (h && /^(example|examples|usage|example usage|sample)\b/i.test(h[2])) {
      i++;
      const level = h[1].length;
      while (i < main.length) {
        const l = main[i];
        const nh = l.match(/^(#{1,6})\s/);
        if (nh && nh[1].length <= level) break;
        if (/^\s*```/.test(l)) {
          const lang = l.replace(/^\s*```/, '').trim() || 'kotlin';
          const code = [];
          i++;
          while (i < main.length && !/^\s*```/.test(main[i])) code.push(main[i++]);
          i++;
          examples.push({ lang, code: dedent(code.join('\n')).replace(/\s+$/, '') });
        } else {
          if (l.trim()) body.push(l);
          i++;
        }
      }
      continue;
    }
    body.push(line);
    i++;
  }

  const bodyText = body.join('\n').replace(/\n{3,}/g, '\n\n').trim();
  const paras = bodyText.split(/\n\s*\n/);
  const summary = (paras[0] ?? '').replace(/\n/g, ' ').trim();
  const rest = paras.slice(1).join('\n\n').trim();

  const params = {};
  const props = {};
  const other = [];
  for (const t of tags) {
    if (t.tag === 'param' || t.tag === 'property') {
      const m = t.text.match(/^(\w+)\s*([\s\S]*)$/);
      if (m) (t.tag === 'param' ? params : props)[m[1]] = m[2].replace(/\s*\n\s*/g, ' ').trim();
    } else other.push({ tag: t.tag, text: t.text.replace(/\s*\n\s*/g, ' ').trim() });
  }
  return { summary, body: rest, examples, params: { ...props, ...params }, tags: other };
}

function dedent(s) {
  const lines = s.split('\n');
  const indents = lines.filter((l) => l.trim()).map((l) => l.match(/^\s*/)[0].length);
  const min = indents.length ? Math.min(...indents) : 0;
  return lines.map((l) => l.slice(min).replace(/\s+$/, '')).join('\n');
}

/** Convert KDoc markup to MDX-safe markdown. Code fences/inline code are left untouched. */
export function kdocToMdx(text) {
  if (!text) return '';
  const out = [];
  let fence = false;
  for (const line of text.split('\n')) {
    if (/^\s*```/.test(line)) {
      fence = !fence;
      out.push(line);
      continue;
    }
    if (fence) {
      out.push(line);
      continue;
    }
    // split into code / prose by backticks
    const parts = line.split(/(`[^`]*`)/g);
    out.push(
      parts
        .map((p, idx) => {
          if (idx % 2 === 1) return p;
          return p
            .replace(/\[([^\]\n]+)\]\[([^\]\n]+)\]/g, (_, label) => `\`${label}\``)
            .replace(/\[([\w.<>?]+)\](?!\()/g, (_, ref) => `\`${ref.split('.').filter((s) => /^[A-Z]/.test(s) || s === ref).slice(-2).join('.') || ref}\``)
            .replace(/\{/g, '\\{')
            .replace(/\}/g, '\\}')
            .replace(/</g, '\\<');
        })
        .join(''),
    );
  }
  return out.join('\n');
}

/** Single-line plain text (no markdown) for TypeTable descriptions and card blurbs. */
export function plain(text) {
  return (text ?? '')
    .replace(/\[([^\]]+)\]\[[^\]]+\]/g, '$1')
    .replace(/\[([\w.<>?]+)\]/g, (_, r) => r.split('.').pop())
    .replace(/`/g, '')
    .replace(/\s+/g, ' ')
    .trim();
}
