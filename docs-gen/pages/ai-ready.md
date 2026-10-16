---
title: AI-ready
description: Give your AI assistant the Kindling docs so it can use, extend and generate components.
section: Getting started
order: 3
---

The design of Kindling makes it easy for AI tools to work with your code. Its open code and consistent API let models read, understand, and even generate new components. An assistant can learn how the components work, suggest improvements, or create new components that integrate with your existing design.

## Copy page

Every page has a **Copy page** button at the top right. It copies the page as clean Markdown (description, code, props and the source of each live demo) so you can paste it into any chat. Its menu also offers:

- **View as Markdown**: open the raw Markdown in a new tab.
- **Copy for AI**: the Markdown with a short intro telling the model what it is reading.
- **Open in Claude / ChatGPT / v0 / Scira**: copies the page and opens a new chat with it.

## llms.txt

The site publishes an index of every page at `/llms.txt`, following the [llms.txt](https://llmstxt.org) convention, so tools that crawl docs can discover them.

## Tips for good results

- Paste the page of the component you are using, not the whole site.
- Say which version you use; pick it in the version menu before copying.
- Ask for components that read colours from `MaterialTheme.colorScheme`, as every Kindling component does.
- Ask the assistant to follow an existing component as a template: `KButton`, `KInput` and `KDialog` are good references.
