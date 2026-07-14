<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import L, { type Map as LeafletMap, type Marker } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { ArrowRight, Layers3, MapPin, RefreshCw, Search } from '@lucide/vue'
import { fetchResourceMapPoints } from '../api/business'
import PageState from '../components/PageState.vue'
import StatusBadge from '../components/StatusBadge.vue'
import type { ResourceItem } from '../types/business'

type DisplayPoint = { item: ResourceItem; lat: number; lng: number }

const router = useRouter()
const mapElement = ref<HTMLElement | null>(null)
const rows = ref<ResourceItem[]>([])
const selected = ref<ResourceItem | null>(null)
const category = ref('ALL')
const keyword = ref('')
const loading = ref(true)
const error = ref('')

let map: LeafletMap | null = null
let markerLayer: L.LayerGroup | null = null
const markers = new Map<string, Marker>()
const displayCoordinates = new Map<string, [number, number]>()

const categories = computed(() => ['ALL', ...new Set(rows.value.map((item) => item.category).filter(Boolean))])
const filtered = computed(() => rows.value.filter((item) => {
  const term = keyword.value.trim().toLowerCase()
  return (category.value === 'ALL' || item.category === category.value)
    && (!term || `${item.name} ${item.address}`.toLowerCase().includes(term))
}))
const located = computed(() => filtered.value.filter((item) => Number.isFinite(Number(item.lat)) && Number.isFinite(Number(item.lng))))

function spreadOverlappingPoints(items: ResourceItem[]): DisplayPoint[] {
  const groups = new Map<string, ResourceItem[]>()
  items.forEach((item) => {
    const key = `${Number(item.lat).toFixed(6)},${Number(item.lng).toFixed(6)}`
    groups.set(key, [...(groups.get(key) || []), item])
  })
  return [...groups.values()].flatMap((group) => group.map((item, index) => {
    if (group.length === 1) return { item, lat: Number(item.lat), lng: Number(item.lng) }
    const angle = (Math.PI * 2 * index) / group.length
    const radius = .00011 + Math.floor(index / 8) * .00005
    return {
      item,
      lat: Number(item.lat) + Math.sin(angle) * radius,
      lng: Number(item.lng) + Math.cos(angle) * radius,
    }
  }))
}

function statusTone(status?: string) {
  if (status === '可招商') return 'available'
  if (status === '洽谈中') return 'negotiating'
  if (status === '已签约') return 'signed'
  return 'muted'
}

function createTooltip(item: ResourceItem) {
  const card = document.createElement('article')
  card.className = 'map-hover-card'

  const meta = document.createElement('div')
  const categoryText = document.createElement('span')
  categoryText.textContent = item.category || '乡村资源'
  const status = document.createElement('b')
  status.className = statusTone(item.investmentStatus)
  status.textContent = item.investmentStatus || '状态待完善'
  meta.append(categoryText, status)

  const title = document.createElement('h4')
  title.textContent = item.name
  const address = document.createElement('p')
  address.textContent = item.address || '地址待完善'

  const facts = document.createElement('dl')
  const area = document.createElement('div')
  const areaLabel = document.createElement('dt')
  areaLabel.textContent = '面积'
  const areaValue = document.createElement('dd')
  areaValue.textContent = item.area == null ? '--' : `${item.area} ㎡`
  area.append(areaLabel, areaValue)
  const income = document.createElement('div')
  const incomeLabel = document.createElement('dt')
  incomeLabel.textContent = '年收益预估'
  const incomeValue = document.createElement('dd')
  incomeValue.textContent = item.annualEstimate == null ? '--' : `${item.annualEstimate} 万元`
  income.append(incomeLabel, incomeValue)
  facts.append(area, income)

  const hint = document.createElement('small')
  hint.textContent = '点击点位查看完整资源档案 →'
  card.append(meta, title, address, facts, hint)
  return card
}

function initializeMap() {
  if (map || !mapElement.value) return
  map = L.map(mapElement.value, { zoomControl: false, minZoom: 12, maxZoom: 19 })
  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors',
  }).addTo(map)
  L.control.zoom({ position: 'bottomright' }).addTo(map)
  L.control.scale({ position: 'bottomleft', imperial: false }).addTo(map)
  markerLayer = L.layerGroup().addTo(map)
}

function renderMarkers(fit = true) {
  if (!map || !markerLayer) return
  markerLayer.clearLayers()
  markers.clear()
  displayCoordinates.clear()
  const points = spreadOverlappingPoints(located.value)
  points.forEach(({ item, lat, lng }, index) => {
    const tone = statusTone(item.investmentStatus)
    const icon = L.divIcon({
      className: 'resource-map-marker-shell',
      html: `<span class="resource-map-marker ${tone}"><i>${index + 1}</i></span>`,
      iconSize: [38, 46],
      iconAnchor: [19, 42],
      tooltipAnchor: [0, -38],
    })
    const marker = L.marker([lat, lng], { icon, title: item.name, keyboard: true, riseOnHover: true })
      .bindTooltip(createTooltip(item), {
        direction: 'top',
        offset: [0, -4],
        opacity: 1,
        className: 'resource-map-tooltip',
      })
      .on('mouseover focus', () => {
        selected.value = item
        marker.openTooltip()
      })
      .on('mouseout blur', () => marker.closeTooltip())
      .on('click', () => router.push(`/resources/${item.id}`))
      .addTo(markerLayer!)
    markers.set(item.id, marker)
    displayCoordinates.set(item.id, [lat, lng])
  })

  if (!points.length) return
  if (fit) {
    map.fitBounds(L.latLngBounds(points.map((point) => [point.lat, point.lng])), {
      padding: [58, 58],
      maxZoom: 17,
      animate: true,
    })
  }
}

function focusResource(item: ResourceItem) {
  selected.value = item
  const coordinate = displayCoordinates.get(item.id)
  const marker = markers.get(item.id)
  if (!map || !coordinate || !marker) return
  map.flyTo(coordinate, Math.max(map.getZoom(), 17), { animate: true, duration: .55 })
  marker.openTooltip()
}

function openResource(item: ResourceItem) {
  router.push(`/resources/${item.id}`)
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    rows.value = await fetchResourceMapPoints()
    selected.value = rows.value.find((item) => item.lat != null && item.lng != null) || rows.value[0] || null
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '资源地图读取失败'
  } finally {
    loading.value = false
    await nextTick()
    if (!error.value && filtered.value.length) {
      initializeMap()
      map?.invalidateSize()
      renderMarkers()
    }
  }
}

watch(located, async () => {
  await nextTick()
  if (!map && mapElement.value) initializeMap()
  if (selected.value && !located.value.some((item) => item.id === selected.value?.id)) selected.value = located.value[0] || null
  renderMarkers()
}, { deep: true })

onMounted(load)
onBeforeUnmount(() => {
  map?.remove()
  map = null
  markerLayer = null
  markers.clear()
  displayCoordinates.clear()
})
</script>

<template>
  <div class="business-page resource-map-page">
    <section class="page-intro map-page-intro">
      <div><p>真实底图 · 资源定位</p><h2>资源地图</h2></div>
      <div class="count-summary"><strong>{{ located.length }}</strong><span>个有效点位</span></div>
    </section>
    <section class="map-toolbar">
      <div class="map-categories"><button v-for="item in categories" :key="item" type="button" :class="{ active: category === item }" @click="category = item">{{ item === 'ALL' ? '全部资源' : item }}</button></div>
      <label><Search :size="18" /><input v-model="keyword" placeholder="搜索资源或地址" /></label>
      <button class="icon-button" type="button" title="刷新地图" @click="load"><RefreshCw :size="18" /></button>
    </section>
    <PageState :loading="loading" :error="error" :empty="!filtered.length" empty-text="当前筛选下没有资源点位" @retry="load">
      <div class="resource-map-layout">
        <section class="real-resource-map-shell" aria-label="可缩放的真实资源地图">
          <div ref="mapElement" class="real-resource-map" />
          <div class="map-usage-hint"><MapPin :size="15" /><span>悬停查看资源信息，点击点位进入档案</span></div>
          <div class="map-legend"><span><i />可招商</span><span><i />洽谈或其他状态</span><small>重叠坐标已在原位置附近展开</small></div>
        </section>
        <aside class="map-resource-list">
          <header><div><span>当前结果</span><h3>资源点位</h3></div><Layers3 :size="22" /></header>
          <div class="map-list-scroll">
            <button v-for="item in filtered" :key="item.id" type="button" :class="{ active: selected?.id === item.id }" @mouseenter="focusResource(item)" @focus="focusResource(item)" @click="openResource(item)">
              <i><MapPin :size="17" /></i>
              <div><strong>{{ item.name }}</strong><span>{{ item.address || '地址待完善' }}</span></div>
              <StatusBadge :status="item.investmentStatus" />
              <ArrowRight :size="16" />
            </button>
          </div>
        </aside>
      </div>
    </PageState>
  </div>
</template>
