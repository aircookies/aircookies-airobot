<template>
    <Layout>
        <!-- 主内容区域 -->
        <template #main-content>
            <div class="flex items-center justify-center flex-1">
                <div class="max-w-3xl w-full select-none">
                    <div class="text-center mb-10">
                        <div class="flex items-center justify-center mb-3">
                            <SvgIcon name="ai-robot-logo" customCss="w-10 h-10 text-gray-700 mr-3" />
                            <h2 class="text-2xl text-gray-800">我是小哈 AI 机器人，很高兴见到你！</h2>
                        </div>
                        <p class="text-gray-500">我可以帮你写代码、写作各种创意内容，请把你的任务交给我吧~</p>
                    </div>

                    <!-- 聊天输入框 -->
                     <ChatInputBox v-model="userMessage" @sendMessage="sendMessage"></ChatInputBox>
                </div>
            </div>
        </template>
    </Layout>
</template>


<script setup>
import Layout from '@/layouts/Layout.vue'
import SvgIcon from '@/components/SvgIcon.vue'
import ChatInputBox from '@/components/ChatInputBox.vue'
import { ref, watch } from 'vue';
import { newChat } from '@/api/chat';
import { useRouter } from 'vue-router';

// 引入路由实例
const router = useRouter();

// 用户输入的消息
const userMessage = ref('')

// 侦听属性，确认子组件中textarea中变更的消息,是否能够传递给父组件
// watch(userMessage, (newText) => {
//     console.log(`子组件传递的值: ${newText}`)
// })

// 发送消息 - 跳转到对话聊天页并发送消息
const sendMessage = (payload) => {
    if (!userMessage.value.trim()) return;

    console.log('选中的模型:', payload.selectedModel)
    console.log('联网搜索选中状态:', payload.isNetworkSearch)

    // 临时保存消息的值，因为子组件中的userMessage会被清空
    const userMessageTemp = userMessage.value;
    console.log('用户发送的消息: ' + userMessageTemp)

    // 请求对话新建接口
    newChat(userMessage.value).then(res => {
        if (res.data.success) {
            // 跳转到聊天对话页面
            router.push({
                name: 'ChatPage', // 必须使用命名路由来跳转
                params: {
                    chatId: res.data.data.uuid // Url 中的 UUID
                },
                state: {
                    firstMessage: userMessageTemp // 将用户在首页填入的消息，传递给 “聊天对话页”
                }
            })
        }
    })
}
</script>
