import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// Naive UI 导入
import naive from 'naive-ui'

import '@/styles/theme.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(pinia)

// 配置ElementPlus全局选项
app.use(ElementPlus, {
  message: {
    // ElMessage默认显示3秒
    duration: 3000,
    // 最多同时显示3个消息
    max: 3
  }
})

// 使用 Naive UI
app.use(naive)

app.mount('#app')
