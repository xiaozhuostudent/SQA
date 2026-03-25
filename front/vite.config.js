import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import VueDevTools from 'vite-plugin-vue-devtools'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    VueDevTools()
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  define: {
    // 修复 sockjs-client 在浏览器中的 global is not defined 错误
    global: 'globalThis'
  },
  server: {
    port: 3001,
    proxy: {
      // 统一处理：所有后端API都统一使用/api前缀，直接转发即可
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ai-service': {
        target: 'http://localhost:5051',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ai-service/, '')
      },
      // 添加 WebSocket 代理
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true
      }
    }
  }
})