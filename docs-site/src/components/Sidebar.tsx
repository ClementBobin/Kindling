import { useEffect, useMemo, useRef } from 'react'
import type { DocPage, ModuleDoc, ModuleName } from '../types'
import type { Route } from '../lib/useHashRoute'
import { collectTags, pageMatchesQuery, pageMatchesTags } from '../lib/search'
import './sidebar.css'

function moduleLabel(module: ModuleName) {
  if (module === 'core') return 'Core'
  if (module === 'utils') return 'Utils'
  return 'Compose'
}

function routeKey(route: Route) {
  if (route.kind === 'home') return 'home'
  if (route.kind === 'module') return `module:${route.module}`
  return `page:${route.module}:${route.pageId}`
}

export function Sidebar(props: {
  modules: ModuleDoc[]
  route: Route
  query: string
  selectedTags: string[]
  onQueryChange: (next: string) => void
  onToggleTag: (tag: string) => void
  onNavigate: (next: Route) => void
}) {
  const inputRef = useRef<HTMLInputElement | null>(null)

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault()
        inputRef.current?.focus()
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [])

  const allTags = useMemo(() => collectTags(props.modules), [props.modules])

  const filteredModules = useMemo(() => {
    const q = props.query
    return props.modules
      .map((mod) => {
        const pages = mod.pages.filter((p) => pageMatchesTags(p, props.selectedTags) && pageMatchesQuery(p, q).ok)
        return { mod, pages }
      })
      .filter((x) => x.pages.length > 0)
  }, [props.modules, props.query, props.selectedTags])

  return (
    <aside className="sidebar">
      <div className="brand" role="banner">
        <button className="brandButton" onClick={() => props.onNavigate({ kind: 'home' })} type="button">
          <span className="brandMark" aria-hidden="true">
            K
          </span>
          <span className="brandText">
            <span className="brandTitle">Kindling</span>
            <span className="brandSubtitle">Docs</span>
          </span>
        </button>
        <a className="brandLink" href="https://github.com/ClementBobin/Kindling" target="_blank" rel="noreferrer">
          GitHub
        </a>
      </div>

      <div className="search">
        <input
          ref={inputRef}
          value={props.query}
          onChange={(e) => props.onQueryChange(e.target.value)}
          className="searchInput"
          type="search"
          placeholder="Search components, APIs, tags…"
          aria-label="Search"
        />
        <div className="searchHint">Ctrl/⌘ K</div>
      </div>

      <div className="tags" aria-label="Tags">
        {allTags.slice(0, 18).map(({ tag, count }) => {
          const active = props.selectedTags.includes(tag)
          return (
            <button
              key={tag}
              type="button"
              className={active ? 'tag tagActive' : 'tag'}
              onClick={() => props.onToggleTag(tag)}
              aria-pressed={active}
              title={`${count} page(s)`}
            >
              {tag}
            </button>
          )
        })}
      </div>

      <nav className="nav" aria-label="Navigation">
        <button
          type="button"
          className={routeKey(props.route) === 'home' ? 'navTop navTopActive' : 'navTop'}
          onClick={() => props.onNavigate({ kind: 'home' })}
        >
          Overview
        </button>

        {filteredModules.map(({ mod, pages }) => (
          <div key={mod.module} className="navGroup">
            <button
              type="button"
              className={routeKey(props.route) === `module:${mod.module}` ? 'navGroupTitle navGroupTitleActive' : 'navGroupTitle'}
              onClick={() => props.onNavigate({ kind: 'module', module: mod.module })}
            >
              {moduleLabel(mod.module)}
              <span className="navCount">{pages.length}</span>
            </button>
            <div className="navItems">
              {pages.slice(0, 70).map((page) => (
                <NavItem
                  key={`${mod.module}:${page.id}`}
                  page={page}
                  active={props.route.kind === 'page' && props.route.module === mod.module && props.route.pageId === page.id}
                  onClick={() => props.onNavigate({ kind: 'page', module: mod.module, pageId: page.id })}
                />
              ))}
            </div>
          </div>
        ))}
      </nav>
    </aside>
  )
}

function NavItem(props: { page: DocPage; active: boolean; onClick: () => void }) {
  const tag = props.page.tags[0]
  return (
    <button type="button" className={props.active ? 'navItem navItemActive' : 'navItem'} onClick={props.onClick}>
      <span className="navItemTitle">{props.page.title}</span>
      {tag ? <span className="navItemTag">{tag}</span> : null}
    </button>
  )
}

