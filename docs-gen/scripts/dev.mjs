#!/usr/bin/env node
// One command for working on the docs site:
//
//   node docs-gen/scripts/dev.mjs            regenerate docs on every change AND run the dev server (rebuilds + reloads)
//   node docs-gen/scripts/dev.mjs --no-serve only keep the generated Kotlin up to date (use with your own Gradle run)
//   node docs-gen/scripts/dev.mjs --history  keep per-version pages while watching (slower, ~7s per change)
//
// While watching, version history is skipped for speed, which rewrites the generated files without it.
// Run `node docs-gen/scripts/docgen.mjs` once more before committing to restore it (CI checks this).
//
// Watches docs-gen/pages, docs-gen/extra, the library sources (KDoc) and the showcase demos/registry.
// Open http://localhost:8080 ; #/dev shows every demo at once (pick a width to test narrow layouts).

import fs from 'node:fs';
import path from 'node:path';
import { spawn, spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const serve = !process.argv.includes('--no-serve');
const history = process.argv.includes('--history');
const docgen = path.join(root, 'docs-gen/scripts/docgen.mjs');

const WATCH = ['docs-gen/pages', 'docs-gen/extra', 'core/src', 'utils/src', 'compose/src', 'android/src', 'showcase/src/wasmJsMain/kotlin/dev/kindling/showcase'];
// Generated output and build dirs must not retrigger the generator.
const IGNORE = /(\/generated\/|\/build\/|\/\.gradle\/|\.docgen)/;

let running = false;
let again = false;
function regenerate(why) {
  if (running) { again = true; return; }
  running = true;
  const t = Date.now();
  const r = spawnSync('node', history ? [docgen] : [docgen, '--no-history'], { cwd: root, encoding: 'utf8' });
  running = false;
  const line = (r.stdout || '').trim().split('\n').pop();
  console.log(`[docs] ${why ? `${why}: ` : ''}${r.status === 0 ? line : `FAILED\n${r.stderr}`} (${Date.now() - t}ms)`);
  if (again) { again = false; regenerate('changes during run'); }
}

let timer;
const schedule = (why) => { clearTimeout(timer); timer = setTimeout(() => regenerate(why), 400); };

regenerate('initial');
for (const rel of WATCH) {
  const dir = path.join(root, rel);
  if (!fs.existsSync(dir)) continue;
  // recursive watch is supported on macOS, Windows and Linux (Node 20+)
  fs.watch(dir, { recursive: true }, (_event, file) => {
    if (!file || IGNORE.test(`/${file}`) || !/\.(kt|md|kts)$/.test(file)) return;
    schedule(path.join(rel, file));
  });
}
console.log(`[docs] watching ${WATCH.length} folders`);

if (serve) {
  const gradle = path.join(root, process.platform === 'win32' ? 'gradlew.bat' : 'gradlew');
  // -t (continuous): Gradle rebuilds when the generated Kotlin or any source changes; the dev server reloads the page.
  const child = spawn(gradle, [':showcase:wasmJsBrowserDevelopmentRun', '-t'], { cwd: root, stdio: 'inherit' });
  child.on('exit', (code) => process.exit(code ?? 0));
  process.on('SIGINT', () => child.kill('SIGINT'));
}
