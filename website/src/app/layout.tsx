import { GeistMono } from 'geist/font/mono';
import { GeistSans } from 'geist/font/sans';
import type { Metadata } from 'next';
import { Provider } from '@/components/provider';
import { appName, appTagline } from '@/lib/shared';
import './global.css';

export const metadata: Metadata = {
  // CI sets NEXT_PUBLIC_SITE_URL (e.g. https://clementbobin.github.io/Kindling) so OG image URLs resolve.
  metadataBase: new URL(process.env.NEXT_PUBLIC_SITE_URL ?? 'http://localhost:3000'),
  title: { default: `${appName} — Compose UI components`, template: `%s | ${appName}` },
  description: appTagline,
};

export default function Layout({ children }: LayoutProps<'/'>) {
  return (
    <html lang="en" className={`${GeistSans.variable} ${GeistMono.variable}`} suppressHydrationWarning>
      <body className="flex flex-col min-h-screen font-sans"><Provider>{children}</Provider></body>
    </html>
  );
}
