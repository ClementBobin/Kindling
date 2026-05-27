import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(() => {
  return {
    base: process.env.VITE_BASE ?? '/',
    plugins: [react()],
  }
})
