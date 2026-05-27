import { useEffect, useMemo, useState } from 'react'
import type { ModuleDoc, ModuleName } from './types'
import { Sidebar } from './components/Sidebar'
import { PageView } from './components/PageView'
import { useHashRoute } from './lib/useHashRoute'
import { searchDocs } from './lib/search'

async function loadJson<T>(path: string): Promise<T> {
  const res = await fetch(path)
  if (!res.ok) throw new Error(`Failed to load ${path}: ${res.status}`)
  return (await res.json()) as T
}

const MODULES: { module: ModuleName; path: string }[] = [
  { module: 'core', path: `${import.meta.env.BASE_URL}content/core.json` },
  { module: 'utils', path: `${import.meta.env.BASE_URL}content/utils.json` },
  { module: 'compose', path: `${import.meta.env.BASE_URL}content/compose.json` },
]

export default function App() {
  const { route, navigate } = useHashRoute()
  const [modules, setModules] = useState<ModuleDoc[]>([])
  const [loadError, setLoadError] = useState<string | null>(null)

  const [query, setQuery] = useState('')
  const [selectedTags, setSelectedTags] = useState<string[]>([])

  useEffect(() => {
    let cancelled = false
    Promise.all(MODULES.map((m) => loadJson<ModuleDoc>(m.path)))
      .then((data) => {
        if (cancelled) return
        setModules(data)
      })
      .catch((e) => {
        if (cancelled) return
        setLoadError(e instanceof Error ? e.message : String(e))
      })
    return () => {
      cancelled = true
    }
  }, [])

  const hits = useMemo(() => searchDocs(modules, query, selectedTags), [modules, query, selectedTags])

  const filteredModules = useMemo(() => {
    if (!query.trim() && selectedTags.length === 0) return modules
    const byModule = new Map<ModuleName, Set<string>>()
    for (const hit of hits) {
      if (!byModule.has(hit.module)) byModule.set(hit.module, new Set())
      byModule.get(hit.module)!.add(hit.page.id)
    }
    return modules.map((m) => ({
      ...m,
      pages: m.pages.filter((p) => byModule.get(m.module)?.has(p.id)),
    }))
  }, [hits, modules, query, selectedTags])

  const onToggleTag = (tag: string) => {
    setSelectedTags((prev) => (prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]))
  }

  return (
    <div className="appShell">
      <Sidebar
        modules={filteredModules}
        route={route}
        query={query}
        selectedTags={selectedTags}
        onQueryChange={setQuery}
        onToggleTag={onToggleTag}
        onNavigate={navigate}
      />
      <main className="mainArea">
        {loadError ? <div className="loadError">Failed to load docs: {loadError}</div> : null}
        <PageView route={route} modules={modules} onNavigate={navigate} />
      </main>
    </div>
  )
}
