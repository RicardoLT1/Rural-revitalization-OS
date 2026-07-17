<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import L, { type Map as LeafletMap, type Marker } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { ArrowRight, Camera, LandPlot, Layers3, LocateFixed, MapPin, RefreshCw, Search, TrendingUp } from '@lucide/vue'
import { fetchResourceMapPoints } from '../api/business'
import { fetchSystemSettings } from '../api/settings'
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
const defaultCenter = ref<[number, number]>([30.640522, 119.681337])

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
const mapSummary = computed(() => {
  const source = filtered.value
  const count = (status: string) => source.filter((item) => item.investmentStatus === status).length
  return {
    available: count('可招商'),
    negotiating: count('洽谈中'),
    signed: count('已签约'),
    other: source.length - count('可招商') - count('洽谈中') - count('已签约'),
    area: source.reduce((total, item) => total + (Number(item.area) || 0), 0),
    annualEstimate: source.reduce((total, item) => total + (Number(item.annualEstimate) || 0), 0),
    photoReady: source.filter((item) => item.fieldPhotos?.length).length,
    profileReady: source.filter((item) => item.intro && item.owner && item.contact).length,
  }
})
const categoryBreakdown = computed(() => [...new Set(filtered.value.map((item) => item.category).filter(Boolean))]
  .map((name) => ({ name, count: filtered.value.filter((item) => item.category === name).length }))
  .sort((a, b) => b.count - a.count))
const statusTotal = computed(() => Math.max(filtered.value.length, 1))

function formatMetric(value: number) {
  return new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 1 }).format(value)
}

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
    .setView(defaultCenter.value, 14)
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

function centerVillage() {
  initializeMap()
  map?.flyTo(defaultCenter.value, 14, { animate: true, duration: .65 })
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [resources, settings] = await Promise.all([
      fetchResourceMapPoints(),
      fetchSystemSettings().catch(() => null),
    ])
    rows.value = resources
    if (settings && Number.isFinite(settings.mapCenterLat) && Number.isFinite(settings.mapCenterLng)) {
      defaultCenter.value = [settings.mapCenterLat, settings.mapCenterLng]
    }
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
      <button class="icon-button" type="button" title="回到村域中心" @click="centerVillage"><LocateFixed :size="18" /></button>
      <button class="icon-button" type="button" title="刷新地图" @click="load"><RefreshCw :size="18" /></button>
    </section>
    <PageState :loading="loading" :error="error" :empty="!filtered.length" empty-text="当前筛选下没有资源点位" @retry="load">
      <div class="resource-map-workspace">
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

        <section class="map-operations-summary" aria-label="当前地图资源运营概览">
          <header>
            <div><span>当前筛选 · 实时盘点</span><h3>点位运营摘要</h3><p>把地图上的空间分布，转成可用于招商跟进和资料治理的判断依据。</p></div>
            <RouterLink to="/resources">打开资源目录<ArrowRight :size="16" /></RouterLink>
          </header>
          <div class="map-summary-grid">
            <article class="map-stage-card">
              <div class="summary-card-heading"><TrendingUp :size="20" /><div><span>招商阶段</span><strong>{{ mapSummary.available }} 项可继续匹配</strong></div></div>
              <div class="stage-bar" aria-hidden="true">
                <i class="available" :style="{ width: `${mapSummary.available / statusTotal * 100}%` }" />
                <i class="negotiating" :style="{ width: `${mapSummary.negotiating / statusTotal * 100}%` }" />
                <i class="signed" :style="{ width: `${mapSummary.signed / statusTotal * 100}%` }" />
                <i class="other" :style="{ width: `${mapSummary.other / statusTotal * 100}%` }" />
              </div>
              <dl><div><dt>可招商</dt><dd>{{ mapSummary.available }}</dd></div><div><dt>洽谈中</dt><dd>{{ mapSummary.negotiating }}</dd></div><div><dt>已签约</dt><dd>{{ mapSummary.signed }}</dd></div><div><dt>其他</dt><dd>{{ mapSummary.other }}</dd></div></dl>
            </article>

            <article class="map-scale-card">
              <div class="summary-card-heading"><LandPlot :size="20" /><div><span>资源规模</span><strong>当前地图资产量级</strong></div></div>
              <dl><div><dt>合计面积</dt><dd>{{ formatMetric(mapSummary.area) }} <small>㎡</small></dd></div><div><dt>年收益预估</dt><dd>{{ formatMetric(mapSummary.annualEstimate) }} <small>万元</small></dd></div></dl>
              <p>所有数值随类型和关键词筛选实时重算。</p>
            </article>

            <article class="map-quality-card">
              <div class="summary-card-heading"><Camera :size="20" /><div><span>档案质量</span><strong>现场资料完备度</strong></div></div>
              <dl><div><dt>已录入坐标</dt><dd>{{ located.length }} / {{ filtered.length }}</dd></div><div><dt>已有现场照片</dt><dd>{{ mapSummary.photoReady }} / {{ filtered.length }}</dd></div><div><dt>基础档案完整</dt><dd>{{ mapSummary.profileReady }} / {{ filtered.length }}</dd></div></dl>
            </article>

            <article class="map-category-card">
              <div class="summary-card-heading"><Layers3 :size="20" /><div><span>类型构成</span><strong>点击聚焦地图点位</strong></div></div>
              <div class="category-breakdown"><button v-for="item in categoryBreakdown" :key="item.name" type="button" @click="category = item.name"><span>{{ item.name }}</span><b>{{ item.count }}</b></button></div>
            </article>
          </div>
          <footer><MapPin :size="15" /><span>地图坐标来自资源档案；同坐标资源仅做视觉展开，不改变原始经纬度。</span><small>刷新地图即可同步最新资源数据</small></footer>
        </section>
      </div>
    </PageState>
  </div>
</template>
