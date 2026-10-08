import { createGetUrl } from 'fumadocs-core/source';

export const appName = 'Kindling';
export const appTagline = 'shadcn/ui-inspired components for Jetpack Compose & Compose Multiplatform';
export const docsRoute = '/docs';
export const docsImageRoute = '/og/docs';
export const docsContentRoute = '/llms.mdx/docs';

export const gitConfig = {
  user: 'ClementBobin',
  repo: 'Kindling',
  branch: 'main',
};

/** Where the website lives inside the repository (used for "edit on GitHub" links). */
export const websiteDir = 'website';

/** Current published library version, injected by CI (falls back to the latest known release). */
export const libraryVersion = process.env.NEXT_PUBLIC_LIBRARY_VERSION?.replace(/^v/, '') || '0.3.0';

/** Base path the site is served from (empty locally, `/Kindling` on GitHub Pages). */
export const basePath = process.env.NEXT_PUBLIC_BASE_PATH ?? '';

/** Prefix a root-relative path with the base path, for plain <iframe>/<a>/<img> that Next does not rewrite. */
export function withBase(path: string) {
  return `${basePath}${path.startsWith('/') ? path : `/${path}`}`;
}

const getContentUrl = createGetUrl(docsContentRoute);

export function getPageMarkdownUrl(page: { slugs: string[]; locale?: string }) {
  const segments = [...page.slugs, 'content.md'];

  return { segments, url: getContentUrl(segments, page.locale) };
}

const getImageUrl = createGetUrl(docsImageRoute);

export function getPageImageUrl(page: { slugs: string[]; locale?: string }) {
  const segments = [...page.slugs, 'image.png'];

  return { segments, url: getImageUrl(segments, page.locale) };
}
