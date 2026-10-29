// 2026/10/29 gongml [需求 待补充] 前端入口
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import perm from './directives/perm'
import './styles/index.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.directive('perm', perm)
app.mount('#app')
