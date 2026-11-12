import { createRouter, createWebHistory } from 'vue-router'

// 声明路由
const routes = [
    {
        path: '/',
        name: 'Index',
        component: () => import('@/views/Index.vue'),
        meta: { // meta信息
            title: '首页' // 页面标题
        }
    },
    {
        path: '/chat/:chatId', // 路由地址
        name: 'ChatPage',
        component: () => import('@/views/ChatPage.vue'), // 对应组件
        meta: { // meta信息
            title: '对话聊天页面' // 页面标题
        }
    },
    {
        path: '/customer-service/chat',
        name: 'CustomerServiceChatPage',
        component: () => import('@/views/CunstomerServiceChatPage.vue'),
        meta: { // meta信息
            title: '智能客服聊天页面' // 页面标题
        }
    },
]

// 创建路由实例
const router = createRouter({
    // 指定路由模式，hash 模式指的是 URL 的路径是通过 hash 符号（#）进行标识
    history: createWebHistory(),
    routes
})

export default router
