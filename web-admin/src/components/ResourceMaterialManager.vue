<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { Camera, Check, Download, Eye, FileText, Pencil, RefreshCw, ShieldCheck, Star, Trash2, Upload, X } from '@lucide/vue'
import {
  deleteResourceMaterial,
  fetchResourceMaterialContent,
  fetchResourceMaterials,
  replaceResourceMaterial,
  setResourceMaterialCover,
  updateResourceMaterial,
  uploadResourceMaterial,
} from '../api/business'
import type { ResourceMaterial, ResourceMaterialCategory } from '../types/business'
import ImagePreview from './ImagePreview.vue'

const props = withDefaults(defineProps<{
  resourceId: string
  title: string
  legacyPhotos?: string[]
  canMaintain?: boolean
  canAdmin?: boolean
}>(), {
  legacyPhotos: () => [],
  canMaintain: false,
  canAdmin: false,
})

const categories: Array<{ value: ResourceMaterialCategory; label: string }> = [
  { value: 'FIELD_PHOTO', label: '现场照片' },
  { value: 'OWNERSHIP', label: '权属材料' },
  { value: 'INVESTMENT', label: '招商材料' },
  { value: 'APPROVAL', label: '审批附件' },
  { value: 'OTHER', label: '其他证明' },
]

const materials = ref<ResourceMaterial[]>([])
const loading = ref(true)
const error = ref('')
const notice = ref('')
const activeCategory = ref<'ALL' | ResourceMaterialCategory>('ALL')
const showUpload = ref(false)
const uploading = ref(false)
const uploadFile = ref<File | null>(null)
const uploadInput = ref<HTMLInputElement | null>(null)
const uploadForm = reactive<{ category: ResourceMaterialCategory; title: string; description: string }>({
  category: 'FIELD_PHOTO',
  title: '',
  description: '',
})
const editing = ref<ResourceMaterial | null>(null)
const editForm = reactive({ title: '', description: '' })
const saving = ref(false)
const replacementTarget = ref<ResourceMaterial | null>(null)
const replacementInput = ref<HTMLInputElement | null>(null)
const previewing = ref<ResourceMaterial | null>(null)
const previewUrls = reactive<Record<string, string>>({})
let requestId = 0

const isImage = (item: ResourceMaterial) => item.image === true || item.contentType?.startsWith('image/')
const cover = computed(() => materials.value.find(item => item.cover && isImage(item)) || materials.value.find(isImage) || null)
const visibleMaterials = computed(() => activeCategory.value === 'ALL'
  ? materials.value
  : materials.value.filter(item => item.category === activeCategory.value))
const photoCount = computed(() => materials.value.filter(item => item.category === 'FIELD_PHOTO' && isImage(item)).length)
const documentCount = computed(() => materials.value.filter(item => !isImage(item)).length)
const selectedPreviewUrl = computed(() => previewing.value ? previewUrls[previewing.value.id] : '')

function categoryLabel(value: string) {
  return categories.find(item => item.value === value)?.label || '其他证明'
}

function categoryCount(value: ResourceMaterialCategory) {
  return materials.value.filter(item => item.category === value).length
}

function formatBytes(value: number) {
  if (!Number.isFinite(value) || value <= 0) return '0 B'
  if (value < 1024) return `${value} B`
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '--'
}

function clearPreviewUrls() {
  Object.values(previewUrls).forEach(url => URL.revokeObjectURL(url))
  Object.keys(previewUrls).forEach(key => delete previewUrls[key])
}

async function prepareImage(item: ResourceMaterial, resourceId: string, currentRequest: number) {
  if (!isImage(item) || previewUrls[item.id]) return
  try {
    const blob = await fetchResourceMaterialContent(resourceId, item.id)
    if (currentRequest !== requestId) return
    previewUrls[item.id] = URL.createObjectURL(blob)
  } catch {
    // The list remains useful when a single protected preview cannot be loaded.
  }
}

async function load() {
  const currentRequest = ++requestId
  const resourceId = props.resourceId
  loading.value = true
  error.value = ''
  previewing.value = null
  clearPreviewUrls()
  try {
    const result = await fetchResourceMaterials(resourceId)
    if (currentRequest !== requestId) return
    materials.value = result
    await Promise.allSettled(result.filter(isImage).map(item => prepareImage(item, resourceId, currentRequest)))
  } catch (reason) {
    if (currentRequest !== requestId) return
    materials.value = []
    error.value = reason instanceof Error ? reason.message : '材料档案读取失败'
  } finally {
    if (currentRequest === requestId) loading.value = false
  }
}

function selectUploadFile(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0] || null
  if (file && file.size > 10 * 1024 * 1024) {
    error.value = '单个文件不能超过 10 MB'
    uploadFile.value = null
    return
  }
  uploadFile.value = file
  if (file && !uploadForm.title) uploadForm.title = file.name.replace(/\.[^.]+$/, '')
}

function resetUpload() {
  showUpload.value = false
  uploadFile.value = null
  uploadForm.category = 'FIELD_PHOTO'
  uploadForm.title = ''
  uploadForm.description = ''
  if (uploadInput.value) uploadInput.value.value = ''
}

async function submitUpload() {
  if (!uploadFile.value) {
    error.value = '请先选择需要归档的文件'
    return
  }
  uploading.value = true
  error.value = ''
  try {
    await uploadResourceMaterial(props.resourceId, {
      file: uploadFile.value,
      category: uploadForm.category,
      title: uploadForm.title,
      description: uploadForm.description,
    })
    resetUpload()
    notice.value = '材料已安全归档，操作记录已写入审计日志'
    await load()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '材料上传失败'
  } finally {
    uploading.value = false
    window.setTimeout(() => { notice.value = '' }, 3200)
  }
}

function openMetadata(item: ResourceMaterial) {
  editing.value = item
  editForm.title = item.title
  editForm.description = item.description || ''
}

async function saveMetadata() {
  if (!editing.value || !editForm.title.trim()) return
  saving.value = true
  error.value = ''
  try {
    await updateResourceMaterial(props.resourceId, editing.value.id, {
      title: editForm.title.trim(),
      description: editForm.description.trim(),
    })
    editing.value = null
    notice.value = '材料说明已更新'
    await load()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '材料说明保存失败'
  } finally {
    saving.value = false
    window.setTimeout(() => { notice.value = '' }, 2600)
  }
}

function chooseReplacement(item: ResourceMaterial) {
  replacementTarget.value = item
  if (replacementInput.value) {
    replacementInput.value.value = ''
    replacementInput.value.click()
  }
}

async function replaceFile(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  const target = replacementTarget.value
  if (!file || !target) return
  if (file.size > 10 * 1024 * 1024) {
    error.value = '单个文件不能超过 10 MB'
    return
  }
  if (!window.confirm(`即将替换“${target.title}”的原文件。旧文件将停止对外提供，是否继续？`)) return
  saving.value = true
  error.value = ''
  try {
    await replaceResourceMaterial(props.resourceId, target.id, file)
    notice.value = '材料文件已替换，原文件已软删除留痕'
    await load()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '材料替换失败'
  } finally {
    saving.value = false
    replacementTarget.value = null
    window.setTimeout(() => { notice.value = '' }, 3200)
  }
}

async function makeCover(item: ResourceMaterial) {
  saving.value = true
  error.value = ''
  try {
    await setResourceMaterialCover(props.resourceId, item.id)
    notice.value = `“${item.title}”已设为资源封面`
    await load()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '封面设置失败'
  } finally {
    saving.value = false
    window.setTimeout(() => { notice.value = '' }, 2600)
  }
}

async function remove(item: ResourceMaterial) {
  if (!window.confirm(`即将删除“${item.title}”。删除后所有管理员都无法再预览或下载该材料，但审计记录会保留。是否继续？`)) return
  saving.value = true
  error.value = ''
  try {
    await deleteResourceMaterial(props.resourceId, item.id)
    if (previewing.value?.id === item.id) previewing.value = null
    notice.value = '材料已软删除，审计记录已保留'
    await load()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '材料删除失败'
  } finally {
    saving.value = false
    window.setTimeout(() => { notice.value = '' }, 3000)
  }
}

async function download(item: ResourceMaterial) {
  error.value = ''
  try {
    const blob = await fetchResourceMaterialContent(props.resourceId, item.id, true)
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = item.originalName || item.title
    anchor.click()
    window.setTimeout(() => URL.revokeObjectURL(url), 1000)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '材料下载失败'
  }
}

watch(() => props.resourceId, load, { immediate: true })
onBeforeUnmount(() => {
  requestId += 1
  clearPreviewUrls()
})
</script>

<template>
  <section class="detail-section material-manager">
    <div class="section-title material-section-title">
      <div><span>材料档案</span><h4>现场与证明材料</h4></div>
      <div class="material-heading-actions"><small>{{ materials.length }} 份已归档</small><button v-if="canMaintain" class="material-upload-trigger" type="button" @click="showUpload = true"><Upload :size="15" />上传材料</button></div>
    </div>

    <div v-if="notice" class="material-notice"><Check :size="15" />{{ notice }}</div>
    <div v-if="error" class="material-error"><span>{{ error }}</span><button type="button" @click="load"><RefreshCw :size="14" />重试</button></div>

    <div class="material-ledger-hero">
      <button v-if="cover && previewUrls[cover.id]" class="material-cover-card" type="button" @click="previewing = cover">
        <img :src="previewUrls[cover.id]" :alt="`${title}资源封面`" />
        <span><Star :size="13" />资源封面</span>
        <strong>{{ cover.title }}</strong>
      </button>
      <div v-else class="material-cover-empty"><Camera :size="25" /><strong>封面待归档</strong><span>上传第一张现场照片后自动设为封面</span></div>
      <div class="material-ledger-stats">
        <span>受控材料空间</span>
        <strong>{{ materials.length ? '档案已建立' : '等待建档' }}</strong>
        <p>文件经鉴权接口访问，替换与删除均保留操作记录。</p>
        <div><b>{{ photoCount }}</b><small>现场图片</small><b>{{ documentCount }}</b><small>证明文件</small></div>
      </div>
    </div>

    <nav class="material-category-strip" aria-label="材料分类筛选">
      <button type="button" :class="{ active: activeCategory === 'ALL' }" @click="activeCategory = 'ALL'"><span>全部材料</span><b>{{ materials.length }}</b></button>
      <button v-for="item in categories" :key="item.value" type="button" :class="{ active: activeCategory === item.value }" @click="activeCategory = item.value"><span>{{ item.label }}</span><b>{{ categoryCount(item.value) }}</b></button>
    </nav>

    <div v-if="loading" class="material-loading"><i /><i /><i /></div>
    <div v-else-if="visibleMaterials.length" class="material-file-list">
      <article v-for="item in visibleMaterials" :key="item.id" :class="{ cover: item.cover }">
        <button v-if="isImage(item) && previewUrls[item.id]" class="material-thumbnail" type="button" title="预览大图" @click="previewing = item"><img :src="previewUrls[item.id]" :alt="item.title" /><Eye :size="14" /></button>
        <div v-else class="material-document-icon"><FileText :size="21" /></div>
        <div class="material-file-copy">
          <div><strong>{{ item.title }}</strong><span v-if="item.cover"><Star :size="11" />当前封面</span></div>
          <p>{{ item.description || '尚未填写材料说明' }}</p>
          <small>{{ categoryLabel(item.category) }} · {{ formatBytes(item.fileSize) }} · {{ item.uploadedByName || '管理员' }} · {{ formatDate(item.createdAt) }}</small>
          <code>SHA-256 {{ item.sha256?.slice(0, 12) || '--' }}…</code>
        </div>
        <div class="material-file-actions">
          <button v-if="isImage(item)" type="button" title="预览" @click="previewing = item"><Eye :size="14" />预览</button>
          <button type="button" title="下载" @click="download(item)"><Download :size="14" />下载</button>
          <button v-if="canMaintain" type="button" title="编辑说明" @click="openMetadata(item)"><Pencil :size="14" />说明</button>
          <button v-if="canAdmin && item.category === 'FIELD_PHOTO' && isImage(item) && !item.cover" type="button" title="设置资源封面" @click="makeCover(item)"><Star :size="14" />设封面</button>
          <button v-if="canAdmin" type="button" title="替换文件" @click="chooseReplacement(item)"><RefreshCw :size="14" />替换</button>
          <button v-if="canAdmin" class="danger" type="button" title="删除材料" @click="remove(item)"><Trash2 :size="14" />删除</button>
        </div>
      </article>
    </div>
    <div v-else class="material-empty"><ShieldCheck :size="24" /><strong>{{ activeCategory === 'ALL' ? '尚未建立正式材料档案' : `暂无${categoryLabel(activeCategory)}` }}</strong><span>{{ canMaintain ? '上传后可预览、下载并追踪维护记录。' : '当前分类尚无可查看材料。' }}</span><button v-if="canMaintain" type="button" @click="showUpload = true"><Upload :size="14" />归档第一份材料</button></div>

    <div v-if="legacyPhotos.length" class="legacy-materials"><header><div><span>历史资料</span><strong>旧版现场照片（只读）</strong></div><small>{{ legacyPhotos.length }} 张</small></header><ImagePreview :images="legacyPhotos" :title="title" /></div>

    <input ref="replacementInput" class="visually-hidden-file" type="file" accept=".jpg,.jpeg,.png,.webp,.pdf,.doc,.docx,.xls,.xlsx" @change="replaceFile" />

    <div v-if="showUpload" class="dialog-layer material-dialog-layer" @click.self="resetUpload">
      <form class="entity-dialog material-dialog" @submit.prevent="submitUpload">
        <header><div><span>受控文件归档</span><h3>上传资源材料</h3></div><button class="icon-button" type="button" title="关闭" @click="resetUpload"><X :size="18" /></button></header>
        <div class="material-dialog-body">
          <label class="material-drop-field"><Upload :size="23" /><strong>{{ uploadFile?.name || '选择需要归档的文件' }}</strong><span>支持 JPG、PNG、WEBP、PDF、Word、Excel，单文件不超过 10 MB</span><input ref="uploadInput" required type="file" accept=".jpg,.jpeg,.png,.webp,.pdf,.doc,.docx,.xls,.xlsx" @change="selectUploadFile" /></label>
          <div class="form-grid two"><label><span>材料分类</span><select v-model="uploadForm.category"><option v-for="item in categories" :key="item.value" :value="item.value">{{ item.label }}</option></select></label><label><span>档案标题</span><input v-model="uploadForm.title" maxlength="128" placeholder="例如：院落南侧现场图" /></label></div>
          <label><span>材料说明</span><textarea v-model="uploadForm.description" maxlength="512" rows="3" placeholder="说明拍摄位置、文件用途或有效期" /></label>
          <p><ShieldCheck :size="14" />下载与预览需要管理员身份，原始存储路径不会暴露给浏览器。</p>
        </div>
        <footer><button class="secondary-button" type="button" @click="resetUpload">取消</button><button class="primary-button dialog-submit" type="submit" :disabled="uploading">{{ uploading ? '正在安全归档…' : '确认上传' }}</button></footer>
      </form>
    </div>

    <div v-if="editing" class="dialog-layer material-dialog-layer" @click.self="editing = null">
      <form class="entity-dialog material-dialog compact" @submit.prevent="saveMetadata">
        <header><div><span>维护材料元数据</span><h3>{{ editing.title }}</h3></div><button class="icon-button" type="button" title="关闭" @click="editing = null"><X :size="18" /></button></header>
        <div class="material-dialog-body"><label><span>档案标题</span><input v-model="editForm.title" required maxlength="128" /></label><label><span>材料说明</span><textarea v-model="editForm.description" maxlength="512" rows="4" /></label></div>
        <footer><button class="secondary-button" type="button" @click="editing = null">取消</button><button class="primary-button dialog-submit" type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存说明' }}</button></footer>
      </form>
    </div>

    <div v-if="previewing && selectedPreviewUrl" class="image-preview-layer material-preview-layer" @click.self="previewing = null">
      <figure><button type="button" title="关闭预览" @click="previewing = null"><X :size="20" /></button><img :src="selectedPreviewUrl" :alt="previewing.title" /><figcaption><strong>{{ previewing.title }}</strong><span>{{ previewing.description || previewing.originalName }}</span></figcaption></figure>
    </div>
  </section>
</template>
