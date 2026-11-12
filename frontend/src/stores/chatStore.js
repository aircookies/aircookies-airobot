import { defineStore } from 'pinia'
import { ref } from 'vue'

// 创建store
export const useChatStore = defineStore('chat', () => {

    // 定义模型列表
    const models = ref([
        { id: 1, name: 'deepseek-v4-flash', icon: 'deepseek-logo', description: '更流畅', selected: true },
        { id: 2, name: 'deepseek-v4-pro', icon: 'deepseek-logo', description: '更智能', selected: false },
    ])

    // 选中的模型，默认为第一个
    const selectedModel = ref(models.value[0])

    // 联网搜索状态，默认为false
    const isNetworkSearchSelected = ref(false)

    // 更新选中的模型
    function updateSelectedModel(model) {
        // 将所有模型的selected状态设为false
        models.value.forEach(m => m.selected = false)
        // 将选中的模型的selected状态设为true
        model.selected = true
        // 更新选中的模型
        selectedModel.value = model
    }

    // 更新联网搜索状态
    function updateNetworkSearchStatus(status) {
        isNetworkSearchSelected.value = status
    }

    // 对外暴露相关变量和方法
    return {
        models,
        selectedModel,
        isNetworkSearchSelected,
        updateSelectedModel,
        updateNetworkSearchStatus,
    }
},
{
    // 开启数据持久化
    persist: true,
})
