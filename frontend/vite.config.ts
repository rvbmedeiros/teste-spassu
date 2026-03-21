import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: process.env.VITE_API_BASE_URL || 'http://localhost:8083',
        changeOrigin: true,
      },
      '/flows': {
        target: process.env.VITE_API_BASE_URL || 'http://localhost:8083',
        changeOrigin: true,
      },
      '/ws': {
        target: (process.env.VITE_WS_BASE_URL || 'http://localhost:8082').replace(/^http/, 'ws'),
        changeOrigin: true,
        ws: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/tests/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'lcov'],
      thresholds: {
        lines: 80,
        branches: 80,
        statements: 80,
      },
      exclude: [
        'dist/**',
        'vite.config.ts',
        'src/tests/**',
        'src/main.ts',
        'src/env.d.ts',
        'src/router/**',
      ],
    },
  },
})
