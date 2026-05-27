import { readdir, readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const repoRoot = path.resolve(__dirname, '..', '..')

const componentsDir = path.join(
  repoRoot,
  'core',
  'src',
  'androidMain',
  'kotlin',
  'dev',
  'kindling',
  'core',
  'components',
)

const outPaths = {
  core: path.join(repoRoot, 'docs-site', 'public', 'content', 'core.json'),
  utils: path.join(repoRoot, 'docs-site', 'public', 'content', 'utils.json'),
  compose: path.join(repoRoot, 'docs-site', 'public', 'content', 'compose.json'),
}

function stripKotlinComments(source) {
  return source
    .replace(/\/\*[\s\S]*?\*\//g, '')
    .replace(/\/\/.*$/gm, '')
}

function stripKdoc(kdocBlock) {
  return kdocBlock
    .split('\n')
    .map((line) => line.replace(/^\s*\*\s?/, ''))
    .join('\n')
    .trim()
}

function parseKdoc(kdocBlock) {
  if (!kdocBlock) return { raw: undefined, summary: undefined, paramDocs: {}, examples: [] }

  const raw = stripKdoc(kdocBlock)
  const lines = raw.split('\n')
  const summaryLines = []
  for (const line of lines) {
    if (!line.trim()) break
    if (line.trim().startsWith('@')) break
    summaryLines.push(line.trim())
  }
  const summary = summaryLines.length ? summaryLines.join(' ') : undefined

  const paramDocs = {}
  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index]
    const match = line.match(/^@param\s+([A-Za-z0-9_]+)\s+(.*)$/)
    if (!match) continue

    const name = match[1]
    let desc = match[2]?.trim() ?? ''
    while (index + 1 < lines.length && lines[index + 1].match(/^\s{2,}\S/)) {
      index += 1
      desc += ` ${lines[index].trim()}`
    }
    paramDocs[name] = desc.trim()
  }

  const examples = []
  const codeBlockRegex = /```(\w+)?\n([\s\S]*?)```/g
  for (const match of raw.matchAll(codeBlockRegex)) {
    const language = match[1] || undefined
    const code = match[2].trimEnd()
    if (!code.trim()) continue
    examples.push({ language, code })
  }

  return { raw, summary, paramDocs, examples }
}

function extractKdocBefore(source, index) {
  const start = source.lastIndexOf('/**', index)
  if (start === -1) return undefined
  const end = source.indexOf('*/', start)
  if (end === -1 || end > index) return undefined
  const between = source.slice(end + 2, index)
  if (between.replace(/\s/g, '').length !== 0) return undefined
  return source.slice(start + 3, end)
}

function extractKdocBeforeKotlinDecl(source, index) {
  const start = source.lastIndexOf('/**', index)
  if (start === -1) return undefined
  const end = source.indexOf('*/', start)
  if (end === -1 || end > index) return undefined

  const between = source.slice(end + 2, index)
  const lines = between.split('\n').map((l) => l.trim()).filter((l) => l.length > 0)
  if (lines.length === 0) return source.slice(start + 3, end)

  const allowedModifiers = new Set([
    'public',
    'private',
    'internal',
    'protected',
    'abstract',
    'sealed',
    'data',
    'open',
    'final',
    'inline',
    'tailrec',
    'suspend',
    'operator',
    'infix',
    'external',
    'const',
    'lateinit',
  ])

  for (const line of lines) {
    if (line.startsWith('@')) continue
    const tokens = line.split(/\s+/).filter(Boolean)
    if (tokens.length === 0) continue
    if (tokens.every((t) => allowedModifiers.has(t))) continue
    return undefined
  }

  return source.slice(start + 3, end)
}

function findMatchingParen(source, openParenIndex) {
  let depth = 0
  for (let index = openParenIndex; index < source.length; index += 1) {
    const ch = source[index]
    if (ch === '(') depth += 1
    else if (ch === ')') {
      depth -= 1
      if (depth === 0) return index
    }
  }
  return -1
}

function findMatchingAngle(source, openIndex) {
  let depth = 0
  for (let index = openIndex; index < source.length; index += 1) {
    const ch = source[index]
    if (ch === '<') depth += 1
    else if (ch === '>') {
      depth -= 1
      if (depth === 0) return index
    }
  }
  return -1
}

function splitTopLevel(source, delimiter) {
  const parts = []
  let current = ''
  let angle = 0
  let paren = 0
  let square = 0
  for (let index = 0; index < source.length; index += 1) {
    const ch = source[index]
    if (ch === '<') angle += 1
    else if (ch === '>') angle = Math.max(0, angle - 1)
    else if (ch === '(') paren += 1
    else if (ch === ')') paren = Math.max(0, paren - 1)
    else if (ch === '[') square += 1
    else if (ch === ']') square = Math.max(0, square - 1)

    if (ch === delimiter && angle === 0 && paren === 0 && square === 0) {
      parts.push(current)
      current = ''
      continue
    }

    current += ch
  }
  if (current.trim()) parts.push(current)
  return parts
}

function splitTypeAndDefault(typeAndDefault) {
  let angle = 0
  let paren = 0
  let square = 0
  for (let index = 0; index < typeAndDefault.length; index += 1) {
    const ch = typeAndDefault[index]
    if (ch === '<') angle += 1
    else if (ch === '>') angle = Math.max(0, angle - 1)
    else if (ch === '(') paren += 1
    else if (ch === ')') paren = Math.max(0, paren - 1)
    else if (ch === '[') square += 1
    else if (ch === ']') square = Math.max(0, square - 1)

    if (ch === '=' && angle === 0 && paren === 0 && square === 0) {
      return {
        type: typeAndDefault.slice(0, index).trim(),
        defaultValue: typeAndDefault.slice(index + 1).trim(),
      }
    }
  }
  return { type: typeAndDefault.trim(), defaultValue: undefined }
}

function parseParams(paramsText, paramDocs) {
  const params = []
  const parts = splitTopLevel(paramsText, ',')
  for (const part of parts) {
    const raw = part.trim()
    if (!raw) continue

    let cleaned = raw.replace(/^(@\w+\s+)*/, '').trim()

    const isVararg = cleaned.startsWith('vararg ')
    if (isVararg) cleaned = cleaned.slice('vararg '.length).trim()

    while (cleaned.match(/^(public|private|internal|protected|crossinline|noinline|reified)\s+/)) {
      cleaned = cleaned.replace(/^(public|private|internal|protected|crossinline|noinline|reified)\s+/, '')
    }
    if (cleaned.startsWith('val ')) cleaned = cleaned.slice('val '.length).trim()
    if (cleaned.startsWith('var ')) cleaned = cleaned.slice('var '.length).trim()

    const colonIndex = cleaned.indexOf(':')
    if (colonIndex === -1) continue
    const name = cleaned.slice(0, colonIndex).trim()
    const typeAndDefault = cleaned.slice(colonIndex + 1).trim()
    const { type, defaultValue } = splitTypeAndDefault(typeAndDefault)

    params.push({
      name,
      type,
      default: defaultValue,
      description: paramDocs[name],
      isVararg: isVararg || undefined,
    })
  }
  return params
}

function inferTags(fileName, primarySymbol) {
  const name = primarySymbol ?? fileName
  const tags = new Set()
  const add = (...values) => values.forEach((v) => tags.add(v))

  if (name.includes('Input') || name.includes('Textarea') || name.includes('Mask')) add('forms')
  if (name.includes('InputOtp')) add('otp')
  if (name.includes('Button')) add('actions')
  if (name.includes('Dialog') || name.includes('Popover')) add('overlay')
  if (name.includes('Toaster') || name.includes('Toast')) add('feedback')
  if (name.includes('Skeleton') || name.includes('Spinner')) add('loading')
  if (name.includes('Carousel')) add('media')
  if (name.includes('DataTable') || name.includes('Table')) add('data')
  if (name.includes('Date') || name.includes('Calendar')) add('date')
  if (name.includes('Pagination') || name.includes('Stepper')) add('navigation')
  if (name.includes('Card') || name.includes('Layout') || name.includes('AspectRatio')) add('layout')
  if (name.includes('Avatar')) add('identity')
  if (name.includes('Direction')) add('i18n')
  if (name.includes('Combobox')) add('select')
  if (name.includes('Empty')) add('empty-state')

  if (tags.size === 0) tags.add('ui')
  return [...tags].sort()
}

function normalizeParamsText(paramsText) {
  const parts = splitTopLevel(paramsText, ',')
  const normalized = []
  for (const part of parts) {
    const raw = part.trim()
    if (!raw) continue

    let cleaned = raw.replace(/^(@\w+\s+)*/, '').trim()
    while (cleaned.match(/^(public|private|internal|protected|crossinline|noinline|reified)\s+/)) {
      cleaned = cleaned.replace(/^(public|private|internal|protected|crossinline|noinline|reified)\s+/, '')
    }
    if (cleaned.startsWith('val ')) cleaned = cleaned.slice('val '.length).trim()
    if (cleaned.startsWith('var ')) cleaned = cleaned.slice('var '.length).trim()
    normalized.push(cleaned.replace(/\s+/g, ' ').trim())
  }
  return normalized.join(', ')
}

function extractReturnTypeAfter(source, closeParenIndex) {
  let index = closeParenIndex + 1
  while (index < source.length && /\s/.test(source[index])) index += 1
  if (source[index] !== ':') return ''

  index += 1
  while (index < source.length && /\s/.test(source[index])) index += 1

  let end = index
  while (end < source.length) {
    const ch = source[end]
    if (ch === '=' || ch === '{' || ch === '\n' || ch === '\r') break
    end += 1
  }

  const type = source.slice(index, end).trim()
  return type ? `: ${type}` : ''
}

function extractLineModifiers(source, keywordIndex) {
  const lineStart = Math.max(0, source.lastIndexOf('\n', keywordIndex - 1) + 1)
  const prefix = source.slice(lineStart, keywordIndex).trim()
  if (!prefix) return ''
  const allowed = new Set(['abstract', 'sealed', 'data', 'open'])
  const tokens = prefix.split(/\s+/).filter((t) => allowed.has(t))
  return tokens.length ? `${tokens.join(' ')} ` : ''
}

function apiNameFor(receiver, shortName) {
  if (receiver && /^[A-Z][A-Za-z0-9_]*$/.test(receiver)) return `${receiver}.${shortName}`
  return shortName
}

function scanTopLevelApi(source) {
  const entries = []

  const funKeywordRegex = /\bfun\b/g
  for (const match of source.matchAll(funKeywordRegex)) {
    const funStart = match.index

    let cursor = funStart + 3
    while (cursor < source.length && /\s/.test(source[cursor])) cursor += 1
    if (source[cursor] === '<') {
      const close = findMatchingAngle(source, cursor)
      if (close === -1) continue
      cursor = close + 1
      while (cursor < source.length && /\s/.test(source[cursor])) cursor += 1
    }

    const openParenIndex = source.indexOf('(', cursor)
    if (openParenIndex === -1) continue
    const closeParenIndex = findMatchingParen(source, openParenIndex)
    if (closeParenIndex === -1) continue

    const nameSegment = source.slice(cursor, openParenIndex).replace(/\s+/g, ' ').trim()
    if (!nameSegment) continue
    const nameToken = nameSegment.split(/\s+/).at(-1)
    if (!nameToken) continue
    const dotIndex = nameToken.lastIndexOf('.')
    const receiver = dotIndex === -1 ? undefined : nameToken.slice(0, dotIndex)
    const shortName = dotIndex === -1 ? nameToken : nameToken.slice(dotIndex + 1)
    if (!shortName.match(/^[A-Za-z_][A-Za-z0-9_]*$/)) continue
    const name = apiNameFor(receiver, shortName)

    const kdocBlock = extractKdocBeforeKotlinDecl(source, funStart)
    if (!kdocBlock) continue

    const kdoc = parseKdoc(kdocBlock)
    const paramsText = source.slice(openParenIndex + 1, closeParenIndex)
    const params = parseParams(paramsText, kdoc.paramDocs)
    const header = source.slice(funStart, openParenIndex).replace(/\s+/g, ' ').trim()
    const signature = `${header}(${normalizeParamsText(paramsText)})${extractReturnTypeAfter(source, closeParenIndex)}`

    entries.push({
      kind: 'fun',
      name,
      api: {
        name,
        signature,
        summary: kdoc.summary,
        kdoc: kdoc.raw,
        params,
        examples: kdoc.examples,
      },
    })
  }

  const typeRegex = /\b(class|interface|object)\s+([A-Za-z0-9_]+)(\s*<[^>{}\n]+>)?/g
  for (const match of source.matchAll(typeRegex)) {
    const kind = match[1]
    const name = match[2]
    const typeStart = match.index
    const kdocBlock = extractKdocBeforeKotlinDecl(source, typeStart)
    if (!kdocBlock) continue

    const kdoc = parseKdoc(kdocBlock)
    let signature = `${extractLineModifiers(source, typeStart)}${kind} ${name}${match[3] ?? ''}`.replace(
      /\s+/g,
      ' ',
    )

    const openParenIndex = source.indexOf('(', typeStart)
    const braceIndex = source.indexOf('{', typeStart)
    const colonIndex = source.indexOf(':', typeStart)
    const stopIndexCandidates = [braceIndex, colonIndex].filter((i) => i !== -1)
    const stopIndex = stopIndexCandidates.length ? Math.min(...stopIndexCandidates) : -1

    if (kind === 'class' && openParenIndex !== -1 && (stopIndex === -1 || openParenIndex < stopIndex)) {
      const closeParenIndex = findMatchingParen(source, openParenIndex)
      if (closeParenIndex !== -1) {
        const paramsText = source.slice(openParenIndex + 1, closeParenIndex)
        const params = parseParams(paramsText, kdoc.paramDocs)
        signature = `${extractLineModifiers(source, typeStart)}class ${name}${match[3] ?? ''}(${normalizeParamsText(paramsText)})`.replace(
          /\s+/g,
          ' ',
        )
        entries.push({
          kind: 'class',
          name,
          api: {
            name,
            signature,
            summary: kdoc.summary,
            kdoc: kdoc.raw,
            params,
            examples: kdoc.examples,
          },
        })
        continue
      }
    }

    entries.push({
      kind,
      name,
      api: {
        name,
        signature,
        summary: kdoc.summary,
        kdoc: kdoc.raw,
        params: [],
        examples: kdoc.examples,
      },
    })
  }

  return entries
}

async function generateCore() {
  const entries = await readdir(componentsDir)
  const files = entries
    .filter((name) => name.endsWith('.kt'))
    .filter((name) => name !== 'global.kt' && name !== 'Global.kt')
    .sort((a, b) => a.localeCompare(b))

  const pages = []

  for (const file of files) {
    const filePath = path.join(componentsDir, file)
    const source = await readFile(filePath, 'utf8')

    const api = []
    const funRegex = /(^|\s)fun\s+(K[A-Za-z0-9_]+)\s*\(/g
    for (const match of source.matchAll(funRegex)) {
      const name = match[2]
      const funStart = match.index + match[0].lastIndexOf('fun')
      const openParenIndex = source.indexOf('(', funStart)
      if (openParenIndex === -1) continue
      const closeParenIndex = findMatchingParen(source, openParenIndex)
      if (closeParenIndex === -1) continue

      const kdocBlock = extractKdocBeforeKotlinDecl(source, funStart)
      const kdoc = parseKdoc(kdocBlock)
      const paramsText = source.slice(openParenIndex + 1, closeParenIndex)
      const params = parseParams(paramsText, kdoc.paramDocs)
      const signature = `fun ${name}(${paramsText.replace(/\s+/g, ' ').trim()})`

      api.push({
        name,
        signature,
        summary: kdoc.summary,
        kdoc: kdoc.raw,
        params,
        examples: kdoc.examples,
      })
    }

    if (api.length === 0) continue

    const primary = api[0].name
    const title = primary
    pages.push({
      id: primary,
      title,
      primary,
      summary: api[0].summary,
      tags: inferTags(file.replace(/\.kt$/, ''), primary),
      sourcePath: path.relative(repoRoot, filePath).replaceAll(path.sep, '/'),
      api,
    })
  }

  const doc = {
    module: 'core',
    title: 'Core',
    artifact: 'io.github.clementbobin.kindling:core',
    description:
      'Shadcn/ui-style Jetpack Compose UI components, fully theme-aware via Material3.',
    pages: pages.sort((a, b) => a.title.localeCompare(b.title)),
  }

  await writeFile(outPaths.core, JSON.stringify(doc, null, 2) + '\n', 'utf8')
  console.log(`Wrote ${path.relative(repoRoot, outPaths.core)}`)
}

async function generateUtils() {
  const utilsDir = path.join(repoRoot, 'utils', 'src', 'main', 'kotlin', 'dev', 'kindling', 'library', 'utils')
  const files = [path.join(utilsDir, 'Debouncer.kt'), path.join(utilsDir, 'PublicApi.kt')]

  const allEntries = []
  for (const filePath of files) {
    const source = await readFile(filePath, 'utf8')
    for (const e of scanTopLevelApi(source)) {
      allEntries.push({
        ...e,
        sourcePath: path.relative(repoRoot, filePath).replaceAll(path.sep, '/'),
      })
    }
  }

  const byName = new Map()
  for (const entry of allEntries) {
    if (!byName.has(entry.api.name)) byName.set(entry.api.name, entry)
  }

  const pick = (name) => {
    const found = byName.get(name)
    if (!found) return undefined
    return {
      ...found.api,
      name: found.api.name,
    }
  }

  const debouncerApi = [pick('Debouncer'), pick('KDebounce')].filter(Boolean)
  const throttlerApi = [pick('Throttler'), pick('KThrottle')].filter(Boolean)
  const flowApi = [pick('kDebounceLeading'), pick('kThrottleFirst')].filter(Boolean)

  const pages = []
  if (debouncerApi.length) {
    pages.push({
      id: 'Debouncer',
      title: 'Debouncer',
      primary: 'Debouncer',
      summary: debouncerApi[0].summary,
      tags: ['coroutines', 'flow'],
      sourcePath: byName.get('Debouncer')?.sourcePath,
      api: debouncerApi,
    })
  }
  if (throttlerApi.length) {
    pages.push({
      id: 'Throttler',
      title: 'Throttler',
      primary: 'Throttler',
      summary: throttlerApi[0].summary,
      tags: ['coroutines', 'flow'],
      sourcePath: byName.get('Throttler')?.sourcePath,
      api: throttlerApi,
    })
  }
  if (flowApi.length) {
    pages.push({
      id: 'FlowExtensions',
      title: 'Flow extensions',
      primary: 'Flow',
      summary: flowApi[0].summary,
      tags: ['coroutines', 'flow'],
      sourcePath: byName.get('kDebounceLeading')?.sourcePath,
      api: flowApi,
    })
  }

  const doc = {
    module: 'utils',
    title: 'Utils',
    artifact: 'io.github.clementbobin.kindling:utils',
    description: 'Coroutine utilities for debouncing, throttling, and Flow helpers.',
    pages: pages.sort((a, b) => a.title.localeCompare(b.title)),
  }

  await writeFile(outPaths.utils, JSON.stringify(doc, null, 2) + '\n', 'utf8')
  console.log(`Wrote ${path.relative(repoRoot, outPaths.utils)}`)
}

async function generateCompose() {
  const composeDir = path.join(repoRoot, 'compose', 'src', 'main', 'kotlin', 'dev', 'kindling', 'compose')

  const typedNavPath = path.join(composeDir, 'Destination.kt')
  const kViewModelPath = path.join(composeDir, 'KViewModel.kt')
  const kScreenPath = path.join(composeDir, 'KScreen.kt')

  const typedNavSource = await readFile(typedNavPath, 'utf8')
  const typedNavEntries = scanTopLevelApi(typedNavSource)
  const typedNavAllow = new Set(['Destination', 'KNavHost', 'NavController.navigate', 'NavController.popBackTo'])
  const typedNavApi = typedNavEntries
    .filter((e) => typedNavAllow.has(e.api.name))
    .map((e) => e.api)

  const kViewModelSource = await readFile(kViewModelPath, 'utf8')
  const kViewModelApi = scanTopLevelApi(kViewModelSource)
    .filter((e) => e.api.name === 'KViewModel' && e.kind === 'class')
    .map((e) => e.api)

  const kScreenSource = await readFile(kScreenPath, 'utf8')
  const kScreenApi = scanTopLevelApi(kScreenSource)
    .filter((e) => e.api.name === 'KScreen')
    .map((e) => e.api)

  const pages = []
  if (typedNavApi.length) {
    pages.push({
      id: 'TypedNavigation',
      title: 'Typed navigation',
      primary: 'Destination',
      summary: typedNavApi[0].summary,
      tags: ['navigation', 'compose'],
      sourcePath: path.relative(repoRoot, typedNavPath).replaceAll(path.sep, '/'),
      api: typedNavApi,
    })
  }
  if (kViewModelApi.length) {
    pages.push({
      id: 'KViewModel',
      title: 'KViewModel',
      primary: 'KViewModel',
      summary: kViewModelApi[0].summary,
      tags: ['architecture', 'compose'],
      sourcePath: path.relative(repoRoot, kViewModelPath).replaceAll(path.sep, '/'),
      api: kViewModelApi,
    })
  }
  if (kScreenApi.length) {
    pages.push({
      id: 'KScreen',
      title: 'KScreen',
      primary: 'KScreen',
      summary: kScreenApi[0].summary,
      tags: ['architecture', 'compose'],
      sourcePath: path.relative(repoRoot, kScreenPath).replaceAll(path.sep, '/'),
      api: kScreenApi,
    })
  }

  const doc = {
    module: 'compose',
    title: 'Compose',
    artifact: 'io.github.clementbobin.kindling:compose',
    description: 'Typed navigation helpers and a structured ViewModel base for Compose apps.',
    pages: pages.sort((a, b) => a.title.localeCompare(b.title)),
  }

  await writeFile(outPaths.compose, JSON.stringify(doc, null, 2) + '\n', 'utf8')
  console.log(`Wrote ${path.relative(repoRoot, outPaths.compose)}`)
}

async function main() {
  await generateCore()
  await generateUtils()
  await generateCompose()
}

main().catch((err) => {
  console.error(err)
  process.exitCode = 1
})
