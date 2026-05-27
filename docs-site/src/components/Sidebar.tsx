import { type CSSProperties, type ReactNode, useEffect, useMemo, useRef, useState } from 'react'
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
  if (route.kind === 'package') return `package:${route.module}:${route.packagePath.join('/')}`
  return `page:${route.module}:${route.pageId}`
}

type PackageTreeNode = {
  label: string
  path: string[]
  children: Map<string, PackageTreeNode>
  pages: DocPage[]
}

function splitPackagePath(page: DocPage): string[] {
  return (page.packagePath ?? '').split('/').filter(Boolean)
}

function buildPackageTree(pages: DocPage[]): PackageTreeNode {
  const root: PackageTreeNode = { label: '', path: [], children: new Map(), pages: [] }
  for (const page of pages) {
    const segments = splitPackagePath(page)
    let node = root
    for (let i = 0; i < segments.length; i += 1) {
      const seg = segments[i]
      if (!node.children.has(seg)) {
        node.children.set(seg, { label: seg, path: [...node.path, seg], children: new Map(), pages: [] })
      }
      node = node.children.get(seg)!
    }
    node.pages.push(page)
  }
  return root
}

function compressTree(node: PackageTreeNode): PackageTreeNode {
  const nextChildren = new Map<string, PackageTreeNode>()
  for (const [key, child] of node.children.entries()) {
    let c = compressTree(child)
    while (c.pages.length === 0 && c.children.size === 1) {
      const only = [...c.children.values()][0]
      c = {
        label: `${c.label}/${only.label}`,
        path: only.path,
        pages: only.pages,
        children: only.children,
      }
    }
    nextChildren.set(key, c)
  }
  return { ...node, children: nextChildren, pages: node.pages.slice().sort((a, b) => a.title.localeCompare(b.title)) }
}

function collectForcedOpen(route: Route, pages: DocPage[]): Set<string> {
  const forced = new Set<string>()
  if (route.kind === 'package') {
    for (let i = 1; i <= route.packagePath.length; i += 1) forced.add(route.packagePath.slice(0, i).join('/'))
    return forced
  }
  if (route.kind === 'page') {
    const found = pages.find((p) => p.id === route.pageId)
    const segs = found ? splitPackagePath(found) : []
    for (let i = 1; i <= segs.length; i += 1) forced.add(segs.slice(0, i).join('/'))
  }
  return forced
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

  const [openPackages, setOpenPackages] = useState<Set<string>>(() => new Set())

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
              <PackageTree
                module={mod.module}
                pages={pages.slice(0, 200)}
                route={props.route}
                openPackages={openPackages}
                forcedOpen={collectForcedOpen(props.route, pages)}
                onToggle={(key) =>
                  setOpenPackages((prev) => {
                    const next = new Set(prev)
                    if (next.has(key)) next.delete(key)
                    else next.add(key)
                    return next
                  })
                }
                onNavigate={props.onNavigate}
              />
            </div>
          </div>
        ))}
      </nav>
    </aside>
  )
}

function NavItem(props: { page: DocPage; active: boolean; onClick: () => void; style?: CSSProperties }) {
  const tag = props.page.tags[0]
  return (
    <button
      type="button"
      className={props.active ? 'navItem navItemActive' : 'navItem'}
      onClick={props.onClick}
      style={props.style}
    >
      <span className="navItemTitle">{props.page.title}</span>
      {tag ? <span className="navItemTag">{tag}</span> : null}
    </button>
  )
}

function PackageTree(props: {
  module: ModuleName
  pages: DocPage[]
  route: Route
  openPackages: Set<string>
  forcedOpen: Set<string>
  onToggle: (key: string) => void
  onNavigate: (next: Route) => void
}) {
  const tree = useMemo(() => compressTree(buildPackageTree(props.pages)), [props.pages])
  const isOpen = (key: string) => props.forcedOpen.has(key) || props.openPackages.has(key)

  const pageCount = (node: PackageTreeNode): number => {
    let count = node.pages.length
    for (const c of node.children.values()) count += pageCount(c)
    return count
  }

  const renderNode = (node: PackageTreeNode, depth: number) => {
    const rows: ReactNode[] = []
    const key = node.path.join('/')
    if (node.label) {
      const open = isOpen(key)
      rows.push(
        <button
          key={`pkg:${key}`}
          type="button"
          className={open ? 'navPkg navPkgOpen' : 'navPkg'}
          style={{ paddingLeft: 10 + depth * 14 }}
          onClick={() => props.onToggle(key)}
          aria-expanded={open}
        >
          <span className="navPkgArrow" aria-hidden="true">
            ▸
          </span>
          <span className="navPkgLabel">{node.label}</span>
          <span className="navPkgCount">{pageCount(node)}</span>
        </button>,
      )
      if (!open) return rows
    }

    for (const page of node.pages) {
      rows.push(
        <NavItem
          key={`page:${props.module}:${page.id}`}
          page={page}
          active={props.route.kind === 'page' && props.route.module === props.module && props.route.pageId === page.id}
          onClick={() => props.onNavigate({ kind: 'page', module: props.module, pageId: page.id })}
          style={{ paddingLeft: 10 + depth * 14 + (node.label ? 14 : 0) }}
        />,
      )
    }

    const children = [...node.children.values()].sort((a, b) => a.label.localeCompare(b.label))
    for (const child of children) rows.push(...renderNode(child, node.label ? depth + 1 : depth))
    return rows
  }

  return <>{renderNode(tree, 0)}</>
}
