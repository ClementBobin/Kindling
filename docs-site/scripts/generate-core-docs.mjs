import { readdir, readFile, stat, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const repoRoot = path.resolve(__dirname, '..', '..')

const coreRootDir = path.join(repoRoot, 'core', 'src', 'androidMain', 'kotlin', 'dev', 'kindling', 'core')

const outPaths = {
  core: path.join(repoRoot, 'docs-site', 'public', 'content', 'core.json'),
  utils: path.join(repoRoot, 'docs-site', 'public', 'content', 'utils.json'),
  compose: path.join(repoRoot, 'docs-site', 'public', 'content', 'compose.json'),
}

async function isDirectory(dirPath) {
  try {
    const s = await stat(dirPath)
    return s.isDirectory()
  } catch {
    return false
  }
}

async function firstExistingDir(candidates) {
  for (const dirPath of candidates) {
    if (await isDirectory(dirPath)) return dirPath
  }
  return undefined
}

async function listKotlinFiles(rootDir) {
  const out = []
  const stack = [rootDir]
  while (stack.length) {
    const dirPath = stack.pop()
    const entries = await readdir(dirPath, { withFileTypes: true })
    for (const e of entries) {
      const fullPath = path.join(dirPath, e.name)
      if (e.isDirectory()) stack.push(fullPath)
      else if (e.isFile() && e.name.endsWith('.kt')) out.push(fullPath)
    }
  }
  return out.sort((a, b) => a.localeCompare(b))
}

function dedupeByLowercasePath(filePaths) {
  const byLower = new Map()
  for (const p of filePaths) {
    const key = p.toLowerCase()
    const existing = byLower.get(key)
    if (!existing) byLower.set(key, p)
    else if (path.basename(p) === 'Global.kt') byLower.set(key, p)
  }
  return [...byLower.values()].sort((a, b) => a.localeCompare(b))
}

function stripKdoc(kdocBlock) {
  return kdocBlock
    .split('\n')
    .map((line) => line.replace(/^\s*\*\s?/, ''))
    .join('\n')
    .trim()
}

function parseKdoc(kdocBlock) {
  if (!kdocBlock) return { raw: undefined, summary: undefined, paramDocs: {}, enumDocs: {}, examples: [] }

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
  const enumDocs = {}
  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index]
    const paramMatch = line.match(/^@param\s+([A-Za-z0-9_]+)\s+(.*)$/)
    const propMatch = line.match(/^@property\s+([A-Za-z0-9_]+)\s+(.*)$/)
    const enumMatch = line.match(/^@enum\s+([A-Za-z0-9_]+)(?:\s+(.*))?$/)

    if (paramMatch || propMatch) {
      const match = paramMatch ?? propMatch
      const name = match[1]
      let desc = match[2]?.trim() ?? ''
      while (index + 1 < lines.length && lines[index + 1].match(/^\s{2,}\S/)) {
        index += 1
        desc += ` ${lines[index].trim()}`
      }
      paramDocs[name] = desc.trim()
      continue
    }

    if (enumMatch) {
      const name = enumMatch[1]
      let desc = enumMatch[2]?.trim() ?? ''
      while (index + 1 < lines.length && lines[index + 1].match(/^\s{2,}\S/)) {
        index += 1
        desc += ` ${lines[index].trim()}`
      }
      enumDocs[name] = desc.trim()
    }
  }

  const examples = []
  const codeBlockRegex = /```(\w+)?\n([\s\S]*?)```/g
  for (const match of raw.matchAll(codeBlockRegex)) {
    const language = match[1] || undefined
    const code = match[2].trimEnd()
    if (!code.trim()) continue
    examples.push({ language, code })
  }

  return { raw, summary, paramDocs, enumDocs, examples }
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

function stripKotlinComments(source) {
  return source.replace(/\/\*[\s\S]*?\*\//g, '').replace(/\/\/.*$/gm, '')
}

function parseParams(paramsText, paramDocs) {
  const params = []
  const parts = splitTopLevel(stripKotlinComments(paramsText), ',')
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

function parseEnumValues(enumText) {
  if (!enumText) return []
  const cleaned = enumText.replace(/\.\s*$/, '').trim()
  if (!cleaned) return []
  const parts = cleaned.includes('|') ? cleaned.split('|') : cleaned.split(',')
  if (parts.length > 1) return parts.map((p) => p.trim()).filter(Boolean)
  return cleaned
    .split(/\s+/)
    .map((p) => p.trim())
    .filter(Boolean)
}

function findMatchingBrace(source, openBraceIndex) {
  let depth = 0
  for (let index = openBraceIndex; index < source.length; index += 1) {
    const ch = source[index]
    if (ch === '{') depth += 1
    else if (ch === '}') {
      depth -= 1
      if (depth === 0) return index
    }
  }
  return -1
}

function findTopLevelSemicolon(source) {
  let angle = 0
  let paren = 0
  let square = 0
  let brace = 0
  for (let index = 0; index < source.length; index += 1) {
    const ch = source[index]
    if (ch === '<') angle += 1
    else if (ch === '>') angle = Math.max(0, angle - 1)
    else if (ch === '(') paren += 1
    else if (ch === ')') paren = Math.max(0, paren - 1)
    else if (ch === '[') square += 1
    else if (ch === ']') square = Math.max(0, square - 1)
    else if (ch === '{') brace += 1
    else if (ch === '}') brace = Math.max(0, brace - 1)

    if (ch === ';' && angle === 0 && paren === 0 && square === 0 && brace === 0) return index
  }
  return -1
}

function extractEnumDocsFromSource(source, enumsByName) {
  const enumRegex = /\benum\s+class\s+([A-Za-z0-9_]+)/g
  for (const match of source.matchAll(enumRegex)) {
    const name = match[1]
    if (enumsByName.has(name)) continue

    const enumStart = match.index
    const lineStart = Math.max(0, source.lastIndexOf('\n', enumStart - 1) + 1)
    const prefix = source.slice(lineStart, enumStart)
    if (prefix.includes('private')) continue

    const openBraceIndex = source.indexOf('{', enumStart)
    if (openBraceIndex === -1) continue
    const closeBraceIndex = findMatchingBrace(source, openBraceIndex)
    if (closeBraceIndex === -1) continue

    const kdocBlock = extractKdocBeforeKotlinDecl(source, enumStart)
    const kdoc = parseKdoc(kdocBlock)

    const body = source.slice(openBraceIndex + 1, closeBraceIndex)
    const semicolonIndex = findTopLevelSemicolon(body)
    const constantsPart = (semicolonIndex === -1 ? body : body.slice(0, semicolonIndex))
      .replace(/\/\/.*$/gm, '')
      .replace(/\/\*[\s\S]*?\*\//g, '')

    const constants = splitTopLevel(constantsPart, ',')
      .map((c) => c.trim())
      .filter(Boolean)
      .map((c) => {
        let cleaned = c.replace(/^(@\w+\s+)*/, '').trim()
        const m = cleaned.match(/^([A-Za-z_][A-Za-z0-9_]*)/)
        return m ? m[1] : undefined
      })
      .filter(Boolean)

    if (constants.length === 0) continue
    enumsByName.set(name, { name, values: constants, summary: kdoc.summary })
  }
}

function inferParamEnum(param, kdocEnums, enumsByName) {
  const explicit = kdocEnums?.[param.name]
  const typeName = param.type.replace(/\?$/, '').split('<', 1)[0].trim().split('.').at(-1)
  const byType = typeName ? enumsByName.get(typeName) : undefined

  if (explicit) {
    const values = parseEnumValues(explicit)
    const name = byType?.name ?? typeName ?? param.name
    return {
      name,
      values: values.length ? values : (byType?.values ?? []),
      summary: byType?.summary,
    }
  }

  if (byType) {
    return { name: byType.name, values: byType.values, summary: byType.summary }
  }

  return undefined
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

function pageIdSuffixForFile(filePath) {
  const rel = path.relative(repoRoot, filePath).replaceAll(path.sep, '/').replace(/\.kt$/, '')
  return rel.replaceAll('/', '__')
}

function toPageIdUnique(idCandidate, suffix, usedIds) {
  let id = idCandidate
  if (!usedIds.has(id)) {
    usedIds.add(id)
    return id
  }
  id = `${idCandidate}@${suffix}`
  if (!usedIds.has(id)) {
    usedIds.add(id)
    return id
  }
  let index = 2
  while (usedIds.has(`${idCandidate}@${suffix}#${index}`)) index += 1
  id = `${idCandidate}@${suffix}#${index}`
  usedIds.add(id)
  return id
}

function packagePathFromFile(rootDir, filePath) {
  const rel = path.relative(rootDir, filePath).replaceAll(path.sep, '/')
  const dir = path.posix.dirname(rel)
  return dir === '.' ? '' : dir
}

async function pagesFromKotlinFiles(module, rootDir, filePaths, enumsByName) {
  const usedIds = new Set()
  const pages = []

  for (const filePath of filePaths) {
    const source = await readFile(filePath, 'utf8')
    const entries = scanTopLevelApi(source, enumsByName)
    const api = entries.map((e) => e.api).filter(Boolean)
    if (api.length === 0) continue

    const primary = api[0]?.name ?? path.basename(filePath, '.kt')
    const id = toPageIdUnique(primary, pageIdSuffixForFile(filePath), usedIds)
    const packagePath = packagePathFromFile(rootDir, filePath)

    pages.push({
      id,
      title: primary,
      primary,
      packagePath: packagePath || undefined,
      summary: api[0]?.summary,
      tags: module === 'core' ? inferTags(path.basename(filePath, '.kt'), primary) : [],
      sourcePath: path.relative(repoRoot, filePath).replaceAll(path.sep, '/'),
      api,
    })
  }

  return pages.sort((a, b) => a.title.localeCompare(b.title))
}

function normalizeParamsText(paramsText) {
  const parts = splitTopLevel(stripKotlinComments(paramsText), ',')
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

function scanTopLevelApi(source, enumsByName = new Map()) {
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
    const params = parseParams(paramsText, kdoc.paramDocs).map((p) => ({
      ...p,
      enum: inferParamEnum(p, kdoc.enumDocs, enumsByName),
    }))
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
        enums: Object.entries(kdoc.enumDocs ?? {})
          .map(([enumName, rawValues]) => {
            const known = enumsByName.get(enumName)
            const values = rawValues ? parseEnumValues(rawValues) : (known?.values ?? [])
            if (!values.length) return undefined
            return { name: enumName, values, summary: known?.summary }
          })
          .filter(Boolean),
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
        const params = parseParams(paramsText, kdoc.paramDocs).map((p) => ({
          ...p,
          enum: inferParamEnum(p, kdoc.enumDocs, enumsByName),
        }))
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
            enums: Object.entries(kdoc.enumDocs ?? {})
              .map(([enumName, rawValues]) => {
                const known = enumsByName.get(enumName)
                const values = rawValues ? parseEnumValues(rawValues) : (known?.values ?? [])
                if (!values.length) return undefined
                return { name: enumName, values, summary: known?.summary }
              })
              .filter(Boolean),
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
        enums: Object.entries(kdoc.enumDocs ?? {})
          .map(([enumName, rawValues]) => {
            const known = enumsByName.get(enumName)
            const values = rawValues ? parseEnumValues(rawValues) : (known?.values ?? [])
            if (!values.length) return undefined
            return { name: enumName, values, summary: known?.summary }
          })
          .filter(Boolean),
      },
    })
  }

  return entries
}

async function generateCore() {
  const coreDir = await firstExistingDir([coreRootDir])
  if (!coreDir) throw new Error(`Core docs dir not found: ${coreRootDir}`)

  const allFiles = dedupeByLowercasePath(await listKotlinFiles(coreDir))

  const enumsByName = new Map()
  for (const filePath of allFiles) {
    const source = await readFile(filePath, 'utf8')
    extractEnumDocsFromSource(source, enumsByName)
  }

  const pages = await pagesFromKotlinFiles(
    'core',
    coreDir,
    allFiles.filter((p) => {
      const base = path.basename(p)
      return base !== 'global.kt' && base !== 'Global.kt'
    }),
    enumsByName,
  )

  const doc = {
    module: 'core',
    title: 'Core',
    artifact: 'io.github.clementbobin.kindling:core',
    description:
      'Shadcn/ui-style Jetpack Compose UI components, fully theme-aware via Material3.',
    pages,
  }

  await writeFile(outPaths.core, JSON.stringify(doc, null, 2) + '\n', 'utf8')
  console.log(`Wrote ${path.relative(repoRoot, outPaths.core)}`)
}

async function generateUtils() {
  const utilsDir = await firstExistingDir([
    path.join(repoRoot, 'utils', 'src', 'main', 'kotlin', 'dev', 'kindling', 'utils'),
    path.join(repoRoot, 'utils', 'src', 'main', 'kotlin', 'dev', 'kindling', 'library', 'utils'),
  ])
  if (!utilsDir) throw new Error('Utils docs dir not found under utils/src/main/kotlin/dev/kindling/(utils|library/utils)')

  const allFiles = dedupeByLowercasePath(await listKotlinFiles(utilsDir))
  const enumsByName = new Map()
  for (const filePath of allFiles) {
    const source = await readFile(filePath, 'utf8')
    extractEnumDocsFromSource(source, enumsByName)
  }

  const pages = await pagesFromKotlinFiles('utils', utilsDir, allFiles, enumsByName)

  const doc = {
    module: 'utils',
    title: 'Utils',
    artifact: 'io.github.clementbobin.kindling:utils',
    description: 'Coroutine utilities for debouncing, throttling, and Flow helpers.',
    pages,
  }

  await writeFile(outPaths.utils, JSON.stringify(doc, null, 2) + '\n', 'utf8')
  console.log(`Wrote ${path.relative(repoRoot, outPaths.utils)}`)
}

async function generateCompose() {
  const composeDir = await firstExistingDir([
    path.join(repoRoot, 'compose', 'src', 'main', 'kotlin', 'dev', 'kindling', 'compose'),
  ])
  if (!composeDir) throw new Error('Compose docs dir not found under compose/src/main/kotlin/dev/kindling/compose')

  const allFiles = dedupeByLowercasePath(await listKotlinFiles(composeDir))
  const enumsByName = new Map()
  for (const filePath of allFiles) {
    const source = await readFile(filePath, 'utf8')
    extractEnumDocsFromSource(source, enumsByName)
  }

  const pages = await pagesFromKotlinFiles('compose', composeDir, allFiles, enumsByName)

  const doc = {
    module: 'compose',
    title: 'Compose',
    artifact: 'io.github.clementbobin.kindling:compose',
    description: 'Typed navigation helpers and a structured ViewModel base for Compose apps.',
    pages,
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
