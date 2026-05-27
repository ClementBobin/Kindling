export type CodeBlock = {
  language?: string
  code: string
  title?: string
}

export type ApiParam = {
  name: string
  type: string
  default?: string
  description?: string
  isVararg?: boolean
  enum?: EnumDoc
}

export type EnumDoc = {
  name: string
  values: string[]
  summary?: string
}

export type ApiEntry = {
  name: string
  signature?: string
  summary?: string
  kdoc?: string
  params: ApiParam[]
  examples: CodeBlock[]
  enums?: EnumDoc[]
}

export type DocPage = {
  id: string
  title: string
  primary: string
  packagePath?: string
  summary?: string
  tags: string[]
  sourcePath?: string
  api: ApiEntry[]
}

export type ModuleName = 'core' | 'utils' | 'compose'

export type ModuleDoc = {
  module: ModuleName
  title: string
  artifact: string
  description: string
  pages: DocPage[]
}
