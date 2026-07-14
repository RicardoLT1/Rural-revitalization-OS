<script setup lang="ts">
import { Camera, X, ZoomIn } from '@lucide/vue'
import { ref } from 'vue'

defineProps<{ images?: string[]; title: string }>()
const selected = ref('')
</script>

<template>
  <div v-if="images?.length" class="resource-photo-grid">
    <button v-for="(photo, index) in images" :key="photo" type="button" @click="selected = photo"><img :src="photo" :alt="`${title}现场照片 ${index + 1}`" /><span><ZoomIn :size="15" />查看大图</span></button>
  </div>
  <div v-else class="resource-media-empty"><Camera :size="21" />尚未上传现场照片</div>
  <div v-if="selected" class="image-preview-layer" @click.self="selected = ''"><figure><button type="button" title="关闭预览" @click="selected = ''"><X :size="20" /></button><img :src="selected" :alt="`${title}现场照片大图`" /></figure></div>
</template>
