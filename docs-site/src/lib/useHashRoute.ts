import { useEffect, useMemo, useState } from 'react'
import type { ModuleName } from '../types'

export type Route =
  | { kind: 'home' }
  | { kind: 'module'; module: ModuleName }
  | { kind: 'package'; module: ModuleName; packagePath: string[] }
  | { kind: 'page'; module: ModuleName; pageId: string }

function parseHash(hash: string): Route {
  const cleaned = (hash || '').replace(/^#\/?/, '').trim()
  if (!cleaned) return { kind: 'home' }

  const parts = cleaned.split('/').filter(Boolean)
  const moduleRaw = parts[0]
  if (moduleRaw !== 'core' && moduleRaw !== 'utils' && moduleRaw !== 'compose') return { kind: 'home' }
  if (parts.length === 1) return { kind: 'module', module: moduleRaw }

  if (parts[1] === 'pkg') {
    const packagePath = parts.slice(2).map((p) => decodeURIComponent(p)).filter(Boolean)
    return { kind: 'package', module: moduleRaw, packagePath }
  }

  const pageIdRaw = parts.slice(1).join('/')
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
    else if (next.kind === 'package') {
      const suffix = next.packagePath.map((p) => encodeURIComponent(p)).join('/')
      window.location.hash = suffix ? `#/${next.module}/pkg/${suffix}` : `#/${next.module}`
    }
    else window.location.hash = `#/${next.module}/${encodeURIComponent(next.pageId)}`
  }

  return { route, navigate }
}
