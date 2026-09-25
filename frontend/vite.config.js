import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    // In sviluppo le chiamate /api vanno al backend Spring Boot: stessa origine, niente CORS da configurare
    proxy: { '/api': 'http://localhost:8080' },
  },
})
