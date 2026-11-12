<template>
    <div :class="containerClass">
        <div class="bg-gray-100 rounded-3xl px-4 py-3 mx-4 border border-gray-200 flex flex-col w-2xl select-none">
            <textarea :placeholder="props.placeholder"
                class="bg-transparent border-none outline-none w-full text-sm resize-none min-h-6" rows="2"
                v-model="userMessage"
                @input="autoResize" 
                ref="textareaRef"
                @keydown.enter="handleEnter"
                ></textarea>

            <!-- 下方容器 -->
            <div class="flex mt-3">
                <div class="flex gap-2">
                    <div class="flex gap-2 relative" ref="leftContainerRef">
                        <!-- 大模型下拉框 -->
                        <div v-show="props.showModelDropdown" class="border border-gray-300 px-2 py-1 rounded-3xl flex items-center justify-center hover:bg-gray-200 cursor-pointer"
                            ref="selectRef" @click="toggleModelDropdown(event)">
                            <SvgIcon :name="currSelectedModel.icon" customCss="w-5 h-5 mr-1.5" />
                            <span class="text-gray-800 text-xs">{{ currSelectedModel.name }}</span>
                            <SvgIcon name="down-arrow"
                                customCss="w-5 h-5 ml-1 text-gray-800 transform transition-transform duration-300"
                                :class="isModelDropdownOpen ? 'rotate-180' : ''" />
                        </div>

                        <!-- 下拉框菜单 -->
                        <div v-if="isModelDropdownOpen" ref="dropdownRef"
                            :class="['absolute', 'left-0', 'w-48', 'bg-white', 'rounded-lg', 'shadow-lg', 'border', 'border-gray-200', 'z-10', 'overflow-hidden', dropdownPosition]">
                            <div v-for="model in models" :key="model.id"
                                class="px-3 py-2 hover:bg-gray-100 cursor-pointer flex items-center justify-between"
                                @click="selectModel(model)">
                                <div class="flex items-center">
                                    <SvgIcon :name="model.icon" customCss="w-5 h-5 mr-2" />
                                    <div class="flex flex-col text-xs">
                                        <div class="text-gray-800">{{ model.name }}</div>
                                        <div class="text-gray-500">{{ model.description }}</div>
                                    </div>
                                </div>
                                <!-- 右侧对号 -->
                                <SvgIcon v-if="model.selected" name="check" customCss="w-3 h-3 text-gray-600" />
                            </div>
                        </div>

                        <!-- 联网搜索 -->
                        <div v-show="props.showNetworkSearch" class="ml-3 border px-2 py-1 rounded-3xl flex items-center justify-center cursor-pointer"
                            :class="isNetworkSearchSelected ? 'border-[#ceddee] bg-[#DBEAFE] hover:bg-[#C3DAF8]' : 'border-gray-300 hover:bg-gray-200'"
                            @click="toggleNetworkSearch">
                            <SvgIcon name="network" customCss="w-5 h-5 mr-1.5"
                                :class="isNetworkSearchSelected ? 'text-[#4D6BFE]' : 'text-gray-500'" />
                            <span class="text-xs mr-1"
                                :class="isNetworkSearchSelected ? 'text-[#4D6BFE]' : 'text-gray-800'">联网搜索</span>
                        </div>
                    </div>
                </div>
                <div class="grow"></div>

                <a-tooltip placement="top">
                    <!-- Tooltip 提示文字 -->
                    <template #title>
                        <span>请输入你的问题</span>
                    </template>

                    <!-- 发送按钮 -->
                    <button class="flex items-center justify-center bg-[#4d6bfe] rounded-full w-8 h-8 border border-[#4d6bfe] hover:bg-[#3b5bef] transition-colors
                        disabled:opacity-50
                        disabled:cursor-not-allowed" @click="handleSendMessage" :disabled="!userMessage.trim()">
                        <ArrowUpOutlined style="color: white;"></ArrowUpOutlined>
                    </button>
                </a-tooltip>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ArrowUpOutlined } from '@ant-design/icons-vue';
import SvgIcon from '@/components/SvgIcon.vue'
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import { useChatStore } from '@/stores/chatStore'

// 获取chatStore
const chatStore = useChatStore()

// 模型列表
const models = computed(() => chatStore.models)

// 下拉菜单父容器引用
const leftContainerRef = ref(null);

// 下拉菜单状态
const isModelDropdownOpen = ref(false)

// 下拉框容器引用
const selectRef = ref(null)

// 下拉菜单引用
const dropdownRef = ref(null)
// 下拉菜单位置
const dropdownPosition = ref('top-8') // 默认下方

// 当前选择的模型，使用store中的选中模型
const currSelectedModel = computed(() => chatStore.selectedModel)

// 下拉菜单显示/隐藏
const toggleModelDropdown = (event) => {
    isModelDropdownOpen.value = !isModelDropdownOpen.value

    // 如果下拉菜单显示，则计算位置
    if (isModelDropdownOpen.value) {
        nextTick(() => {
            if (leftContainerRef.value && dropdownRef.value) {
                // 返回元素相对于视图窗口的位置和尺寸信息
                const buttonRect = leftContainerRef.value.getBoundingClientRect();
                // 下拉菜单的实际高度
                const dropdownHeight = dropdownRef.value.offsetHeight;

                // 检查下方空间是否足够
                // 可用空间 = window.innerHeight - buttonRect.bottom
                // 所需空间 = dropdownHeight
                // 如果可用空间 < 所需空间，说明下方空间不足
                if (window.innerHeight - buttonRect.bottom < dropdownHeight) {
                    // 下方空间不足，显示在上方
                    dropdownPosition.value = 'bottom-10';
                } else {
                    // 下方空间足够，显示在下方
                    dropdownPosition.value = 'top-8';
                }
            }
        })
    }
}

// 是否启用联网搜索
const isNetworkSearchSelected = computed(() => chatStore.isNetworkSearchSelected)

// 定义 emits
const emit = defineEmits(['update:modelValue', 'sendMessage'])

// 文本域引用
const textareaRef = ref(null)

// 点击外部区域关闭下拉菜单
const handleClickOutside = (event) => {
    if (selectRef.value && !selectRef.value.contains(event.target)) {
        isModelDropdownOpen.value = false
    }
}

// 选择模型
const selectModel = (model) => {
    // 更新store中选中的模型
    chatStore.updateSelectedModel(model)

    // 关闭下拉菜单
    isModelDropdownOpen.value = false;
}

// 切换联网搜索选中状态
const toggleNetworkSearch = () => {
    // 更新store中的状态
    chatStore.updateNetworkSearchStatus(!chatStore.isNetworkSearchSelected)
}

// 接收父组件传递的属性
const props = defineProps({
    // textarea 中用户输入的用户消息
    modelValue: {
        type: String,
        required: true
    },
    containerClass: { // 自定义根容器样式
        type: String,
        default: ''
    },
    placeholder: {
        type: String,
        default: '给机器人发送消息'
    },
    showModelDropdown: {
        type: Boolean,
        default: true
    },
    showNetworkSearch: {
        type: Boolean,
        default: true
    },
})

// 计算属性，用于 v-model 的双向绑定
const userMessage = computed({
    get() {
        return props.modelValue;
    },
    set(value) {
        emit('update:modelValue', value);
    }
})

// 处理发送消息
const handleSendMessage = () => {
    // 若消息为空
    if (!userMessage.value.trim()) {
        message.warning('消息不能为空');
        return
    }

    emit('sendMessage', {
        selectedModel: chatStore.selectedModel,
        isNetworkSearch: chatStore.isNetworkSearchSelected
    });
    // 清空输入框
    userMessage.value = '';
}

// 自动调整高度
const autoResize = () => {
    const textarea = textareaRef.value;
    if (textarea) {
        // 重置高度以获取正确的滚动高度
        textarea.style.height = 'auto'

        // 计算新高度，但最大不超过 300px
        const newHeight = Math.min(textarea.scrollHeight, 300);
        textarea.style.height = newHeight + 'px';

        // 如果内容超出 300px，则启用滚动
        textarea.style.overflowY = textarea.scrollHeight > 300 ? 'auto' : 'hidden';
    }
}

// 监听 textarea 回车事件
const handleEnter = (event) => {
    // console.log('回车键被按下', event.target.value)
    // 主动调用发送消息方法
    handleSendMessage()
}

// 挂载时添加事件监听器
onMounted(() => {
    document.addEventListener('click', handleClickOutside)
})

// 卸载时移除事件监听器
onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
})
</script>
