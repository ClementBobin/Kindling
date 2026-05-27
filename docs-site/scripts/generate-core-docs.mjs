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

const outPath = path.join(repoRoot, 'docs-site', 'public', 'content', 'core.json')

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

async function main() {
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

      const kdocBlock = extractKdocBefore(source, funStart)
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

  await writeFile(outPath, JSON.stringify(doc, null, 2) + '\n', 'utf8')
  console.log(`Wrote ${path.relative(repoRoot, outPath)}`)
}

main().catch((err) => {
  console.error(err)
  process.exitCode = 1
})
