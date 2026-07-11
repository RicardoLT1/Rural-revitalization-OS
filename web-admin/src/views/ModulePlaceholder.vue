<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Construction } from '@lucide/vue'

const route = useRoute()
const title = computed(() => String(route.meta.title || '业务模块'))
</script>

<template><section class="module-placeholder"><Construction :size="28" /><span>阶段 3 · 下一批</span><h2>{{ title }}</h2><p>页面入口与权限边界已经建立，真实接口、表格状态和业务操作将在本阶段后续批次接入。</p></section></template>
