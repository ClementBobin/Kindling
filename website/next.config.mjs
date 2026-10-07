import { createMDX } from 'fumadocs-mdx/next';

const withMDX = createMDX();

// GitHub Pages serves project sites from /<repo>. CI sets NEXT_PUBLIC_BASE_PATH=/Kindling,
// local dev leaves it empty so http://localhost:3000 just works.
const basePath = process.env.NEXT_PUBLIC_BASE_PATH ?? '';

/** @type {import('next').NextConfig} */
const config = {
  output: 'export',
  reactStrictMode: true,
  basePath,
  trailingSlash: true,
  images: { unoptimized: true },
};

export default withMDX(config);
