// Release history for the docs: each git tag is unpacked (cached) so its sources can be scanned like HEAD.
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';

const MODULES = ['core', 'utils', 'compose', 'android'];
export const label = (tag) => tag.replace(/^v/, '');

export function semverCmp(a, b) {
  const pa = label(a).split(/[.-]/).map((x) => parseInt(x, 10) || 0);
  const pb = label(b).split(/[.-]/).map((x) => parseInt(x, 10) || 0);
  for (let i = 0; i < Math.max(pa.length, pb.length); i++) {
    const d = (pa[i] ?? 0) - (pb[i] ?? 0);
    if (d) return d;
  }
  return 0;
}

const git = (repoRoot, ...args) => execFileSync('git', args, { cwd: repoRoot, maxBuffer: 1 << 30 });

/** Release tags (x.y.z, optional leading v), newest first. Empty when git or tags are unavailable. */
export function listTags(repoRoot) {
  try {
    return git(repoRoot, 'tag', '--list')
      .toString()
      .split('\n')
      .map((t) => t.trim())
      .filter((t) => /^v?\d+\.\d+(\.\d+)?$/.test(t))
      .sort((a, b) => semverCmp(b, a));
  } catch {
    return [];
  }
}

/** Unpack the library modules of `tag` under cacheDir/<tag>; returns the root, or null if there is nothing to scan. */
export function checkoutTag(repoRoot, tag, cacheDir) {
  const dest = path.join(cacheDir, tag);
  if (fs.existsSync(path.join(dest, '.ok'))) return dest;
  try {
    const tree = git(repoRoot, 'ls-tree', '--name-only', tag).toString().split('\n');
    const mods = MODULES.filter((m) => tree.includes(m));
    if (!mods.length) return null;
    fs.rmSync(dest, { recursive: true, force: true });
    fs.mkdirSync(dest, { recursive: true });
    execFileSync('tar', ['-x', '-C', dest], { input: git(repoRoot, 'archive', '--format=tar', tag, ...mods), maxBuffer: 1 << 30 });
    fs.writeFileSync(path.join(dest, '.ok'), '');
    return dest;
  } catch {
    return null;
  }
}
