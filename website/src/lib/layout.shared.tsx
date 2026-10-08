import type { BaseLayoutProps } from 'fumadocs-ui/layouts/shared';
import { appName, gitConfig, libraryVersion } from './shared';

export function baseOptions(): BaseLayoutProps {
  return {
    nav: {
      title: (
        <span className="inline-flex items-center gap-2 font-semibold tracking-tight">
          <span aria-hidden>🔥</span>
          {appName}
          <span className="rounded-md border px-1.5 py-0.5 text-[10px] font-medium text-fd-muted-foreground">
            v{libraryVersion}
          </span>
        </span>
      ),
    },
    githubUrl: `https://github.com/${gitConfig.user}/${gitConfig.repo}`,
    links: [
      { text: 'Documentation', url: '/docs', active: 'nested-url' },
      { text: 'Components', url: '/docs/components', active: 'nested-url' },
      {
        text: 'Showcase',
        url: '/docs/showcase',
        active: 'url',
      },
    ],
  };
}
