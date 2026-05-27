import type { DocPage, ModuleDoc, ModuleName } from '../types'

export type SearchHit = {
  module: ModuleName
  page: DocPage
  score: number
  reason: string
}

function normalize(value: string) {
  return value.toLowerCase().trim()
}

function scoreText(haystack: string, needle: string) {
  const h = normalize(haystack)
  const n = normalize(needle)
  if (!n) return 0
  if (h === n) return 100
  if (h.startsWith(n)) return 70
  if (h.includes(n)) return 40
  return 0
}

export function pageMatchesQuery(page: DocPage, query: string) {
  const q = normalize(query)
  if (!q) return { ok: true, score: 0, reason: '' }

  const scores = [
    { score: scoreText(page.title, q), reason: 'title' },
    { score: scoreText(page.primary, q), reason: 'primary' },
    { score: scoreText(page.summary ?? '', q), reason: 'summary' },
    { score: page.tags.some((t) => normalize(t).includes(q)) ? 25 : 0, reason: 'tag' },
    {
      score: page.api.some((a) => normalize(a.name).includes(q) || normalize(a.signature ?? '').includes(q))
        ? 20
        : 0,
      reason: 'api',
    },
  ]

  const best = scores.reduce((acc, cur) => (cur.score > acc.score ? cur : acc), { score: 0, reason: '' })
  return { ok: best.score > 0, score: best.score, reason: best.reason }
}

export function pageMatchesTags(page: DocPage, selectedTags: string[]) {
  if (selectedTags.length === 0) return true
  const pageTags = new Set(page.tags.map(normalize))
  return selectedTags.every((t) => pageTags.has(normalize(t)))
}

export function searchDocs(modules: ModuleDoc[], query: string, selectedTags: string[]) {
  const hits: SearchHit[] = []
  for (const mod of modules) {
    for (const page of mod.pages) {
      if (!pageMatchesTags(page, selectedTags)) continue
      const match = pageMatchesQuery(page, query)
      if (!match.ok) continue
      hits.push({ module: mod.module, page, score: match.score, reason: match.reason })
    }
  }
  return hits.sort((a, b) => b.score - a.score || a.page.title.localeCompare(b.page.title))
}

export function collectTags(modules: ModuleDoc[]) {
  const counts = new Map<string, number>()
  for (const mod of modules) {
    for (const page of mod.pages) {
      for (const tag of page.tags) counts.set(tag, (counts.get(tag) ?? 0) + 1)
    }
  }
  return [...counts.entries()]
    .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
    .map(([tag, count]) => ({ tag, count }))
}

