import { useEffect, useMemo, useState } from 'react'
import type { ModuleName } from '../types'

export type Route =
  | { kind: 'home' }
  | { kind: 'module'; module: ModuleName }
  | { kind: 'page'; module: ModuleName; pageId: string }

function parseHash(hash: string): Route {
  const cleaned = (hash || '').replace(/^#\/?/, '').trim()
  if (!cleaned) return { kind: 'home' }

  const [moduleRaw, pageIdRaw] = cleaned.split('/').filter(Boolean)
  if (moduleRaw !== 'core' && moduleRaw !== 'utils' && moduleRaw !== 'compose') return { kind: 'home' }
  if (!pageIdRaw) return { kind: 'module', module: moduleRaw }
  return { kind: 'page', module: moduleRaw, pageId: decodeURIComponent(pageIdRaw) }
}

export function useHashRoute() {
  const [hash, setHash] = useState(() => window.location.hash)

  useEffect(() => {
    const onChange = () => setHash(window.location.hash)
    window.addEventListener('hashchange', onChange)
    return () => window.removeEventListener('hashchange', onChange)
  }, [])

  const route = useMemo(() => parseHash(hash), [hash])

  const navigate = (next: Route) => {
    if (next.kind === 'home') window.location.hash = '#/'
    else if (next.kind === 'module') window.location.hash = `#/${next.module}`
    else window.location.hash = `#/${next.module}/${encodeURIComponent(next.pageId)}`
  }

  return { route, navigate }
}

