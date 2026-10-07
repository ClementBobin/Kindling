import Link from 'next/link';
import { appName, appTagline, libraryVersion } from '@/lib/shared';

const modules = [
  {
    name: 'core',
    href: '/docs/components',
    blurb: 'shadcn/ui-style Compose components: Button, Dialog, DataTable, Carousel, Stepper, Toaster and more.',
  },
  {
    name: 'utils',
    href: '/docs/utils',
    blurb: 'Coroutine utilities (Debouncer, Throttler, Flow extensions) and formatting helpers.',
  },
  {
    name: 'compose',
    href: '/docs/compose',
    blurb: 'Typed navigation (KNavHost) and a structured KViewModel base class.',
  },
  {
    name: 'android',
    href: '/docs/android',
    blurb: 'Native device helpers, a Ktor-based KHttpClient, and session/token storage.',
  },
];

export default function HomePage() {
  return (
    <main className="mx-auto flex w-full max-w-5xl flex-1 flex-col gap-16 px-6 py-20">
      <section className="flex flex-col items-center gap-6 text-center">
        <span className="rounded-full border px-3 py-1 text-xs text-fd-muted-foreground">
          v{libraryVersion} · Apache-2.0
        </span>
        <h1 className="text-4xl font-bold tracking-tight sm:text-6xl">
          Build beautiful Compose UIs, <span className="text-fd-primary">faster</span>.
        </h1>
        <p className="max-w-2xl text-lg text-fd-muted-foreground">
          {appName} is {appTagline}. Fully theme-aware via Material3, with live previews right in the docs.
        </p>
        <div className="flex flex-wrap justify-center gap-3">
          <Link
            href="/docs"
            className="rounded-md bg-fd-primary px-5 py-2.5 text-sm font-medium text-fd-primary-foreground hover:opacity-90"
          >
            Get started
          </Link>
          <Link
            href="/docs/components"
            className="rounded-md border px-5 py-2.5 text-sm font-medium hover:bg-fd-accent"
          >
            Browse components
          </Link>
        </div>
      </section>

      <section className="grid gap-4 sm:grid-cols-2">
        {modules.map((m) => (
          <Link
            key={m.name}
            href={m.href}
            className="group rounded-xl border bg-fd-card p-5 transition-colors hover:bg-fd-accent"
          >
            <code className="text-sm font-semibold">:{m.name}</code>
            <p className="mt-2 text-sm text-fd-muted-foreground">{m.blurb}</p>
          </Link>
        ))}
      </section>
    </main>
  );
}
