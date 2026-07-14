<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Layers3, MapPin, RefreshCw, Search } from '@lucide/vue'
import { fetchResourceMapPoints } from '../api/business'
import PageState from '../components/PageState.vue'
import StatusBadge from '../components/StatusBadge.vue'
import type { ResourceItem } from '../types/business'

const router = useRouter()
const rows = ref<ResourceItem[]>([])
const selected = ref<ResourceItem | null>(null)
const category = ref('ALL')
const keyword = ref('')
const loading = ref(true)
const error = ref('')

const categories = computed(() => ['ALL', ...new Set(rows.value.map((item) => item.category).filter(Boolean))])
const filtered = computed(() => rows.value.filter((item) => {
  const term = keyword.value.trim().toLowerCase()
  return (category.value === 'ALL' || item.category === category.value) && (!term || `${item.name} ${item.address}`.toLowerCase().includes(term))
}))
const located = computed(() => filtered.value.filter((item) => item.lat != null && item.lng != null))
const bounds = computed(() => {
  const latitudes = located.value.map((item) => Number(item.lat))
  const longitudes = located.value.map((item) => Number(item.lng))
  return { minLat: Math.min(...latitudes, 0), maxLat: Math.max(...latitudes, 1), minLng: Math.min(...longitudes, 0), maxLng: Math.max(...longitudes, 1) }
})

function pointStyle(item: ResourceItem) {
  const latRange = Math.max(bounds.value.maxLat - bounds.value.minLat, .0001)
  const lngRange = Math.max(bounds.value.maxLng - bounds.value.minLng, .0001)
  return {
    left: `${10 + (Number(item.lng) - bounds.value.minLng) / lngRange * 80}%`,
    top: `${88 - (Number(item.lat) - bounds.value.minLat) / latRange * 76}%`,
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    rows.value = await fetchResourceMapPoints()
    selected.value = rows.value.find((item) => item.lat != null && item.lng != null) || rows.value[0] || null
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '资源地图读取失败'
  } finally { loading.value = false }
}

onMounted(load)
</script>

<template>
  <div class="business-page resource-map-page">
    <section class="page-intro"><div><p>真实点位 · 村域视角</p><h2>资源地图</h2></div><div class="count-summary"><strong>{{ located.length }}</strong><span>个有效点位</span></div></section>
    <section class="map-toolbar"><div class="map-categories"><button v-for="item in categories" :key="item" type="button" :class="{ active: category === item }" @click="category = item">{{ item === 'ALL' ? '全部资源' : item }}</button></div><label><Search :size="16" /><input v-model="keyword" placeholder="搜索资源或地址" /></label><button class="icon-button" type="button" title="刷新地图" @click="load"><RefreshCw :size="16" /></button></section>
    <PageState :loading="loading" :error="error" :empty="!filtered.length" empty-text="当前筛选下没有资源点位" @retry="load">
      <div class="resource-map-layout">
        <section class="village-map" aria-label="村域资源点位图">
          <div class="map-grid" /><div class="terrain terrain-one" /><div class="terrain terrain-two" />
          <svg class="map-river" viewBox="0 0 800 560" preserveAspectRatio="none" aria-hidden="true"><path d="M-20 390 C140 300 230 470 390 352 S650 210 840 285" /></svg>
          <div class="map-label north-label">北部山林</div><div class="map-label center-label">青耘村核心区</div><div class="map-label south-label">溪畔片区</div>
          <button v-for="item in located" :key="item.id" class="resource-marker" :class="{ active: selected?.id === item.id }" :style="pointStyle(item)" type="button" :title="item.name" @click="selected = item"><span><MapPin :size="15" /></span><b>{{ item.name }}</b></button>
          <div class="map-legend"><span><i />可招商</span><span><i />其他状态</span><small>依据资源经纬度相对分布</small></div>
        </section>
        <aside class="map-resource-list">
          <header><div><span>当前结果</span><h3>资源点位</h3></div><Layers3 :size="20" /></header>
          <div class="map-list-scroll"><button v-for="item in filtered" :key="item.id" type="button" :class="{ active: selected?.id === item.id }" @click="selected = item"><i><MapPin :size="15" /></i><div><strong>{{ item.name }}</strong><span>{{ item.address || '地址待完善' }}</span></div><StatusBadge :status="item.investmentStatus" /></button></div>
          <article v-if="selected" class="map-resource-card"><div><span>{{ selected.category }}</span><StatusBadge :status="selected.investmentStatus" /></div><h3>{{ selected.name }}</h3><p>{{ selected.intro || '暂无资源介绍' }}</p><dl><div><dt>面积</dt><dd>{{ selected.area == null ? '--' : `${selected.area} ㎡` }}</dd></div><div><dt>年收益预估</dt><dd>{{ selected.annualEstimate == null ? '--' : `${selected.annualEstimate} 万元` }}</dd></div></dl><button type="button" @click="router.push(`/resources/${selected.id}`)">打开资源档案<ArrowRight :size="15" /></button></article>
        </aside>
      </div>
    </PageState>
  </div>
</template>
