import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
// 注册 SVG Icon
import 'virtual:svg-icons-register'
// 引入 pinia 插件
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

// 注册 pinia 插件
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const app = createApp(App)

app.use(router)
app.use(pinia)
app.mount('#app')
