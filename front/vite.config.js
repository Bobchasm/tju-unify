import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:7070',
        changeOrigin: true,
        secure: false
      },
      '/unify-api': {
        target: 'http://localhost:7070',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
