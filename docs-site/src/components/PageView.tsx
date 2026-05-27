import { useEffect, useMemo, useRef, useState } from 'react'
import type { EnumDoc, ModuleDoc, ModuleName } from '../types'
import type { Route } from '../lib/useHashRoute'
import { CodeBlock } from './CodeBlock'
import './page.css'

function moduleLabel(module: ModuleName) {
  if (module === 'core') return 'Core'
  if (module === 'utils') return 'Utils'
  return 'Compose'
}

function githubBlobUrl(sourcePath?: string) {
  if (!sourcePath) return undefined
  return `https://github.com/ClementBobin/Kindling/blob/main/${sourcePath}`
}

type CrumbItem = {
  label: string
  onClick?: () => void
}

function EllipsisCrumb(props: { items: CrumbItem[] }) {
  const ref = useRef<HTMLDetailsElement | null>(null)
  if (props.items.length === 0) return null
  return (
    <details className="crumbDropdown" ref={ref}>
      <summary className="crumbButton" aria-label="Show full breadcrumb">
        …
      </summary>
      <div className="crumbMenu" role="menu">
        {props.items.map((c, idx) => (
          <button
            key={`${c.label}:${idx}`}
            type="button"
            className="crumbMenuItem"
            onClick={() => {
              c.onClick?.()
              if (ref.current) ref.current.open = false
            }}
            role="menuitem"
          >
            {c.label}
          </button>
        ))}
      </div>
    </details>
  )
}

function Breadcrumbs(props: { items: CrumbItem[] }) {
  const items = props.items
  if (items.length <= 3) {
    return (
      <div className="crumbs">
        {items.map((c, idx) => (
          <span key={`${c.label}:${idx}`} className="crumbWrap">
            {idx > 0 ? <span className="sep">/</span> : null}
            {c.onClick ? (
              <button type="button" className="crumbButton" onClick={c.onClick}>
                {c.label}
              </button>
            ) : (
              <span className="crumb">{c.label}</span>
            )}
          </span>
        ))}
      </div>
    )
  }

  const first = items[0]
  const last = items[items.length - 1]
  const hidden = items.slice(1, -1)

  return (
    <div className="crumbs">
      <span className="crumbWrap">
        {first.onClick ? (
          <button type="button" className="crumbButton" onClick={first.onClick}>
            {first.label}
          </button>
        ) : (
          <span className="crumb">{first.label}</span>
        )}
      </span>
      <span className="sep">/</span>
      <EllipsisCrumb items={hidden} />
      <span className="sep">/</span>
      <span className="crumbWrap">
        {last.onClick ? (
          <button type="button" className="crumbButton" onClick={last.onClick}>
            {last.label}
          </button>
        ) : (
          <span className="crumb">{last.label}</span>
        )}
      </span>
    </div>
  )
}

function collectEnums(entry: { params: { enum?: EnumDoc }[]; enums?: EnumDoc[] }): EnumDoc[] {
  const out: EnumDoc[] = []
  const seen = new Set<string>()
  const add = (e: EnumDoc | undefined) => {
    if (!e) return
    if (seen.has(e.name)) return
    seen.add(e.name)
    out.push(e)
  }

  for (const p of entry.params) add(p.enum)
  for (const e of entry.enums ?? []) add(e)
  return out
}

export function PageView(props: { route: Route; modules: ModuleDoc[]; onNavigate: (next: Route) => void }) {
  const route = props.route
  const core = props.modules.find((m) => m.module === 'core')
  const utils = props.modules.find((m) => m.module === 'utils')
  const compose = props.modules.find((m) => m.module === 'compose')

  if (route.kind === 'home') {
    return <HomeView core={core} utils={utils} compose={compose} onNavigate={props.onNavigate} />
  }

  const mod = props.modules.find((m) => m.module === route.module)
  if (!mod) return <NotFound />

  if (route.kind === 'module') {
    return <ModuleView module={mod} onNavigate={props.onNavigate} />
  }

  if (route.kind === 'package') {
    return <PackageView module={mod} packagePath={route.packagePath} onNavigate={props.onNavigate} />
  }

  const page = mod.pages.find((p) => p.id === route.pageId)
  if (!page) return <NotFound />
  return <DocPageView module={mod} pageId={page.id} onNavigate={props.onNavigate} />
}

function HomeView(props: {
  core?: ModuleDoc
  utils?: ModuleDoc
  compose?: ModuleDoc
  onNavigate: (next: Route) => void
}) {
  const [dsl, setDsl] = useState<'kotlin' | 'groovy'>('kotlin')

  const kotlinInstall = `repositories {\n    mavenCentral()\n    maven("https://jitpack.io") // fallback for core and utils\n}\n\ndependencies {\n    implementation("io.github.clementbobin.kindling:core:0.3.0")\n    implementation("io.github.clementbobin.kindling:utils:0.3.0")\n    implementation("io.github.clementbobin.kindling:compose:0.3.0")\n}`

  const groovyInstall = `repositories {\n    mavenCentral()\n    maven { url 'https://jitpack.io' }\n}\n\ndependencies {\n    implementation 'io.github.clementbobin.kindling:core:0.3.0'\n    implementation 'io.github.clementbobin.kindling:utils:0.3.0'\n    implementation 'io.github.clementbobin.kindling:compose:0.3.0'\n}`

  return (
    <div className="content">
      <header className="hero">
        <div className="heroTop">
          <div className="heroTitleWrap">
            <h1 className="heroTitle">Kindling</h1>
            <p className="heroSubtitle">A Kotlin multi-module component library for Jetpack Compose.</p>
          </div>
          <div className="badges" aria-label="Badges">
            <a className="badge" href="https://central.sonatype.com/search?q=io.github.clementbobin.kindling" target="_blank" rel="noreferrer">
              Maven Central
            </a>
            <a className="badge" href="https://jitpack.io/#ClementBobin/Kindling" target="_blank" rel="noreferrer">
              JitPack
            </a>
            <a className="badge" href="https://github.com/ClementBobin/Kindling/actions/workflows/ci.yml" target="_blank" rel="noreferrer">
              CI
            </a>
          </div>
        </div>

        <div className="cards">
          <ModuleCard module="core" title="Core" description={props.core?.description ?? 'UI components'} onClick={() => props.onNavigate({ kind: 'module', module: 'core' })} />
          <ModuleCard module="utils" title="Utils" description={props.utils?.description ?? 'Coroutine utilities'} onClick={() => props.onNavigate({ kind: 'module', module: 'utils' })} />
          <ModuleCard module="compose" title="Compose" description={props.compose?.description ?? 'Navigation + ViewModel base'} onClick={() => props.onNavigate({ kind: 'module', module: 'compose' })} />
        </div>
      </header>

      <section className="section">
        <div className="sectionHeader">
          <h2>Installation</h2>
          <div className="tabs" role="tablist" aria-label="Gradle DSL">
            <button className={dsl === 'kotlin' ? 'tab tabActive' : 'tab'} type="button" role="tab" aria-selected={dsl === 'kotlin'} onClick={() => setDsl('kotlin')}>
              Kotlin DSL
            </button>
            <button className={dsl === 'groovy' ? 'tab tabActive' : 'tab'} type="button" role="tab" aria-selected={dsl === 'groovy'} onClick={() => setDsl('groovy')}>
              Groovy DSL
            </button>
          </div>
        </div>

        <CodeBlock language={dsl === 'kotlin' ? 'kotlin' : 'groovy'} code={dsl === 'kotlin' ? kotlinInstall : groovyInstall} />
        <p className="note">
          <strong>Note:</strong> <code>:compose</code> is Android-only and is published to Maven Central. <code>:core</code> and <code>:utils</code> are also available on JitPack as a fallback.
        </p>
      </section>

      <section className="section">
        <h2>Requirements</h2>
        <div className="grid">
          <Req title="minSdk" value="26+" />
          <Req title="JDK" value="17+" />
          <Req title="Kotlin" value="2.2.0+" />
          <Req title="Compose BOM" value="2025.05.00+" />
        </div>
      </section>
    </div>
  )
}

function ModuleCard(props: { module: ModuleName; title: string; description: string; onClick: () => void }) {
  return (
    <button type="button" className="card" onClick={props.onClick}>
      <div className="cardTop">
        <div className="cardTitle">{props.title}</div>
        <div className="pill">{props.module}</div>
      </div>
      <div className="cardDesc">{props.description}</div>
    </button>
  )
}

function Req(props: { title: string; value: string }) {
  return (
    <div className="req">
      <div className="reqTitle">{props.title}</div>
      <div className="reqValue">{props.value}</div>
    </div>
  )
}

function ModuleView(props: { module: ModuleDoc; onNavigate: (next: Route) => void }) {
  return (
    <div className="content">
      <header className="pageHeader">
        <Breadcrumbs items={[{ label: moduleLabel(props.module.module) }]} />
        <h1 className="pageTitle">{props.module.title}</h1>
        <p className="pageSubtitle">{props.module.description}</p>
        <div className="meta">
          <span className="metaKey">Artifact</span>
          <code className="metaValue">{props.module.artifact}</code>
        </div>
      </header>

      <section className="section">
        <h2>Pages</h2>
        <div className="pageList">
          {props.module.pages.map((p) => (
            <button
              key={p.id}
              type="button"
              className="pageRow"
              onClick={() => props.onNavigate({ kind: 'page', module: props.module.module, pageId: p.id })}
            >
              <div className="pageRowTitle">{p.title}</div>
              <div className="pageRowMeta">
                {p.tags.slice(0, 3).map((t) => (
                  <span key={t} className="pageTag">
                    {t}
                  </span>
                ))}
              </div>
            </button>
          ))}
        </div>
      </section>
    </div>
  )
}

function PackageView(props: { module: ModuleDoc; packagePath: string[]; onNavigate: (next: Route) => void }) {
  const prefix = props.packagePath.filter(Boolean).join('/')
  const pages = props.module.pages.filter((p) => {
    const pp = p.packagePath ?? ''
    if (!prefix) return pp === ''
    return pp === prefix || pp.startsWith(`${prefix}/`)
  })

  const crumbs: CrumbItem[] = [
    { label: moduleLabel(props.module.module), onClick: () => props.onNavigate({ kind: 'module', module: props.module.module }) },
  ]
  for (let i = 0; i < props.packagePath.length; i += 1) {
    const seg = props.packagePath[i]
    const path = props.packagePath.slice(0, i + 1)
    crumbs.push({ label: seg, onClick: () => props.onNavigate({ kind: 'package', module: props.module.module, packagePath: path }) })
  }

  const title = prefix || moduleLabel(props.module.module)

  return (
    <div className="content">
      <header className="pageHeader">
        <Breadcrumbs items={crumbs} />
        <h1 className="pageTitle">{title}</h1>
        <p className="pageSubtitle">{pages.length} page(s)</p>
      </header>

      <section className="section">
        <h2>Pages</h2>
        <div className="pageList">
          {pages.map((p) => (
            <button
              key={p.id}
              type="button"
              className="pageRow"
              onClick={() => props.onNavigate({ kind: 'page', module: props.module.module, pageId: p.id })}
            >
              <div className="pageRowTitle">{p.title}</div>
              <div className="pageRowMeta">
                {p.tags.slice(0, 3).map((t) => (
                  <span key={t} className="pageTag">
                    {t}
                  </span>
                ))}
              </div>
            </button>
          ))}
        </div>
      </section>
    </div>
  )
}

function DocPageView(props: { module: ModuleDoc; pageId: string; onNavigate: (next: Route) => void }) {
  const page = props.module.pages.find((p) => p.id === props.pageId)
  const tocRef = useRef<HTMLDivElement | null>(null)
  const [activeAnchor, setActiveAnchor] = useState<string | null>(null)

  const anchors = useMemo(() => {
    if (!page) return []
    return page.api.map((a) => ({ id: `api-${a.name}`, label: a.name }))
  }, [page])

  useEffect(() => {
    if (!page) return

    const headings = anchors
      .map((a) => document.getElementById(a.id))
      .filter((h): h is HTMLElement => Boolean(h))

    if (headings.length === 0) return

    const obs = new IntersectionObserver(
      (entries) => {
        const visible = entries
          .filter((e) => e.isIntersecting)
          .sort((a, b) => (a.boundingClientRect.top ?? 0) - (b.boundingClientRect.top ?? 0))[0]
        if (visible?.target?.id) setActiveAnchor(visible.target.id)
      },
      { rootMargin: '-20% 0px -70% 0px', threshold: [0.01, 0.2, 0.6] },
    )

    for (const h of headings) obs.observe(h)
    return () => obs.disconnect()
  }, [anchors, page])

  useEffect(() => {
    if (!tocRef.current) return
    if (!activeAnchor) return
    const active = tocRef.current.querySelector<HTMLAnchorElement>(`a[href="#${CSS.escape(activeAnchor)}"]`)
    active?.scrollIntoView({ block: 'nearest' })
  }, [activeAnchor])

  if (!page) return <NotFound />

  const sourceUrl = githubBlobUrl(page.sourcePath)
  const packageSegments = (page.packagePath ?? '').split('/').filter(Boolean)
  const crumbs: CrumbItem[] = [
    { label: moduleLabel(props.module.module), onClick: () => props.onNavigate({ kind: 'module', module: props.module.module }) },
    ...packageSegments.map((seg, idx) => ({
      label: seg,
      onClick: () =>
        props.onNavigate({ kind: 'package', module: props.module.module, packagePath: packageSegments.slice(0, idx + 1) }),
    })),
    { label: page.title },
  ]

  return (
    <div className="content contentWithToc">
      <div className="main">
        <header className="pageHeader">
          <Breadcrumbs items={crumbs} />
          <h1 className="pageTitle">{page.title}</h1>
          {page.summary ? <p className="pageSubtitle">{page.summary}</p> : null}
          <div className="tagRow">
            {page.tags.map((t) => (
              <span key={t} className="pageTag">
                {t}
              </span>
            ))}
            {sourceUrl ? (
              <a className="sourceLink" href={sourceUrl} target="_blank" rel="noreferrer">
                Source
              </a>
            ) : null}
          </div>
        </header>

        <section className="section">
          {page.api.map((a, idx) => {
            const enums = collectEnums(a)
            return (
            <div key={a.name} className={idx === 0 ? 'apiBlock' : 'apiBlock apiBlockSep'}>
              <h2 id={`api-${a.name}`} className="apiTitle">
                {a.name}
              </h2>
              {a.signature ? <CodeBlock language="kotlin" code={a.signature} /> : null}
              {a.summary ? <p className="apiSummary">{a.summary}</p> : null}

              {a.params.length ? (
                <div className="subSection">
                  <h3 className="subTitle">Props</h3>
                  <div className="tableWrap">
                    <table className="table">
                      <thead>
                        <tr>
                          <th>Prop</th>
                          <th>Type</th>
                          <th>Default</th>
                          <th>Description</th>
                        </tr>
                      </thead>
                      <tbody>
                        {a.params.map((p) => (
                          <tr key={p.name}>
                            <td>
                              <code>{p.name}</code>
                            </td>
                            <td>
                              <code>{p.type}</code>
                            </td>
                            <td>{p.default ? <code>{p.default}</code> : <span className="muted">—</span>}</td>
                            <td className="cellDesc">{p.description ?? <span className="muted">—</span>}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              ) : null}

              {enums.length ? (
                <div className="subSection">
                  <h3 className="subTitle">Enums</h3>
                  <div className="tableWrap">
                    <table className="table">
                      <thead>
                        <tr>
                          <th>Enum</th>
                          <th>Values</th>
                          <th>Description</th>
                        </tr>
                      </thead>
                      <tbody>
                        {enums.map((e) => (
                          <tr key={e.name}>
                            <td>
                              <code>{e.name}</code>
                            </td>
                            <td className="cellValues">
                              {e.values.length ? (
                                <span className="valuesWrap">
                                  {e.values.map((v) => (
                                    <code key={v}>{v}</code>
                                  ))}
                                </span>
                              ) : (
                                <span className="muted">—</span>
                              )}
                            </td>
                            <td className="cellDesc">{e.summary ?? <span className="muted">—</span>}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              ) : null}

              {a.examples.length ? (
                <div className="subSection">
                  <h3 className="subTitle">Examples</h3>
                  <div className="examples">
                    {a.examples.map((ex, idx) => (
                      <div key={idx} className="example">
                        <CodeBlock language={ex.language ?? 'kotlin'} code={ex.code} />
                      </div>
                    ))}
                  </div>
                </div>
              ) : null}
            </div>
          )})}
        </section>
      </div>

      <aside className="toc" aria-label="On this page" ref={tocRef}>
        <div className="tocTitle">On this page</div>
        <div className="tocItems">
          {anchors.map((a) => (
            <a key={a.id} href={`#${a.id}`} className={activeAnchor === a.id ? 'tocItem tocItemActive' : 'tocItem'}>
              {a.label}
            </a>
          ))}
        </div>
      </aside>
    </div>
  )
}

function NotFound() {
  return (
    <div className="content">
      <header className="pageHeader">
        <h1 className="pageTitle">Not found</h1>
        <p className="pageSubtitle">This page does not exist (or the docs are still loading).</p>
      </header>
    </div>
  )
}
