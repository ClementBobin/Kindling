import './codeblock.css'

export function CodeBlock(props: { code: string; language?: string }) {
  return (
    <pre className="codeblock" data-language={props.language ?? ''}>
      <code>{props.code}</code>
    </pre>
  )
}

