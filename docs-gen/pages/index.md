---
title: Introduction
description: shadcn/ui-inspired components, typed navigation and utilities for Jetpack Compose and Compose Multiplatform.
section: Getting started
order: 1
---

Kindling is a Kotlin multi-module library for Jetpack Compose. The components are modelled on [shadcn/ui](https://ui.shadcn.com): small, composable, and fully theme-aware through Material3. Every component reads its colours from `MaterialTheme.colorScheme`, so light and dark schemes work out of the box.

This whole site is a Compose Multiplatform (Wasm) app built with Kindling itself, so every preview below is the real component running live.

{{demo:badge}}

## Modules

- [core](/docs/components): Compose components such as Button, Input, Dialog, Carousel, DataTable, Stepper and Toaster.
- [utils](/docs/utils): coroutine utilities (`Debouncer`, `Throttler`, `debounceLeading`, `throttleFirst`) and formatting helpers.
- [compose](/docs/compose): typed navigation (`Destination`, `KNavHost`) and the `KViewModel` base class.
- [android](/docs/android): native device helpers, a Ktor-based `KHttpClient`, and session/token storage.

## AI-ready

Kindling is built to be easy for AI tools to work with. The components are open code with a consistent API, so a model can read how they work, suggest improvements, and generate new components that fit your existing theme. See [AI-ready](/docs/ai-ready) for how to give your assistant the docs.

## How these docs are made

API pages are generated from the KDoc in the source code, so they stay in sync with the library. Hand-written pages (like this one) live next to them. See [Contributing to the docs](/docs/contributing-docs).
