'use client';

import { useTheme } from 'next-themes';
import { useEffect, useState } from 'react';
import { withBase } from '@/lib/shared';

interface ShowcaseProps {
  /** Component id registered in the Compose showcase app (e.g. `badge`, `dialog`). */
  component: string;
  /** Optional demo id when a component exposes several demos. */
  demo?: string;
  /** Iframe height in px. */
  height?: number;
}

/**
 * Embeds the live Compose Multiplatform (Wasm) showcase for a single component.
 *
 * The showcase app is built from the `:showcase` Gradle module and copied to
 * `website/public/showcase/` by CI (see .github/workflows/docs.yml). It reads
 * `component`, `demo` and `theme` from the query string.
 */
export function Showcase({ component, demo, height = 280 }: ShowcaseProps) {
  const { resolvedTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  useEffect(() => setMounted(true), []);

  const theme = mounted && resolvedTheme === 'dark' ? 'dark' : 'light';
  const params = new URLSearchParams({ component, theme });
  if (demo) params.set('demo', demo);
  const src = `${withBase('/showcase/index.html')}?${params.toString()}`;

  return (
    <div className="not-prose overflow-hidden rounded-lg border bg-fd-card">
      {mounted ? (
        <iframe
          // Re-mount on theme change so the Wasm app picks up the new scheme.
          key={theme}
          src={src}
          title={`${component} live preview`}
          loading="lazy"
          style={{ height }}
          className="w-full border-0"
        />
      ) : (
        <div style={{ height }} className="flex items-center justify-center text-sm text-fd-muted-foreground">
          Loading preview…
        </div>
      )}
      <div className="flex items-center justify-between border-t px-3 py-1.5 text-xs text-fd-muted-foreground">
        <span>Live Compose for Web preview</span>
        <a href={src} target="_blank" rel="noreferrer" className="underline underline-offset-2 hover:text-fd-foreground">
          Open full screen
        </a>
      </div>
    </div>
  );
}
