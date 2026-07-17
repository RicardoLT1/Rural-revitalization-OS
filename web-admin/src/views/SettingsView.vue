<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { BellRing, Check, Clock3, MapPinned, Save, Settings2, ShieldCheck } from '@lucide/vue'
import PageState from '../components/PageState.vue'
import { fetchSystemSettings, saveSystemSettings, type SystemSettings } from '../api/settings'

const settings = ref<SystemSettings | null>(null)
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const notice = ref('')

const weekDays = [
  { value: 1, label: '周一' }, { value: 2, label: '周二' }, { value: 3, label: '周三' },
  { value: 4, label: '周四' }, { value: 5, label: '周五' }, { value: 6, label: '周六' },
  { value: 7, label: '周日' },
]

function formatTime(value?: string) {
  return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '尚未更新'
}

async function load() {
  loading.value = true
  error.value = ''
  try { settings.value = await fetchSystemSettings() }
  catch (reason) { error.value = reason instanceof Error ? reason.message : '设置读取失败' }
  finally { loading.value = false }
}

async function save() {
  if (!settings.value) return
  saving.value = true
  error.value = ''
  try {
    settings.value = await saveSystemSettings(settings.value)
    notice.value = '系统设置已保存，村域名称与通知策略已同步生效'
    window.dispatchEvent(new CustomEvent('xiangyun:settings-updated', { detail: settings.value }))
    window.dispatchEvent(new CustomEvent('xiangyun:notifications-updated'))
    window.setTimeout(() => { notice.value = '' }, 3200)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '设置保存失败'
  } finally { saving.value = false }
}

onMounted(load)
</script>

<template>
  <div class="business-page settings-page">
    <div v-if="notice" class="toast-notice"><Check :size="16" />{{ notice }}</div>
    <section class="page-intro"><div><p>平台治理 · 村域规则</p><h2>系统设置</h2></div><span class="settings-trust"><ShieldCheck :size="16" />仅管理员可修改，变更自动进入审计中心</span></section>
    <PageState :loading="loading" :error="settings ? '' : error" :empty="!settings" empty-text="没有可维护的村域设置" @retry="load">
      <template v-if="settings">
        <section class="settings-identity">
          <div class="settings-seal">乡</div>
          <div><span>当前村域身份</span><h3>{{ settings.villageName }}</h3><p>{{ settings.platformName }} · 村域 {{ settings.villageId }}</p></div>
          <dl><div><dt>审批时限</dt><dd>{{ settings.approvalTimeoutHours }} 小时</dd></div><div><dt>周报生成</dt><dd>{{ weekDays.find(item => item.value === settings?.weeklyReportDay)?.label }}</dd></div><div><dt>系统版本</dt><dd>{{ settings.systemVersion }}</dd></div></dl>
        </section>

        <div class="settings-layout">
          <main class="settings-form-stack">
            <section class="settings-section">
              <header><Settings2 :size="19" /><div><span>基础信息</span><h3>平台与村域名称</h3></div></header>
              <div class="settings-form-grid">
                <label><span>平台名称</span><input v-model.trim="settings.platformName" maxlength="64" /><small>显示在管理端品牌区和系统信息中</small></label>
                <label><span>村域名称</span><input v-model.trim="settings.villageName" maxlength="128" /><small>保存后同步更新村域基础档案</small></label>
                <label class="wide"><span>运营联系电话</span><input v-model.trim="settings.contactPhone" maxlength="32" placeholder="用于管理端内部联络" /></label>
              </div>
            </section>

            <section class="settings-section">
              <header><MapPinned :size="19" /><div><span>空间基准</span><h3>默认地图中心点</h3></div></header>
              <div class="settings-form-grid">
                <label><span>中心纬度</span><input v-model.number="settings.mapCenterLat" type="number" min="-90" max="90" step="0.000001" /></label>
                <label><span>中心经度</span><input v-model.number="settings.mapCenterLng" type="number" min="-180" max="180" step="0.000001" /></label>
              </div>
              <p class="settings-field-note"><MapPinned :size="14" />资源地图的“回到村域中心”将使用此坐标，具体资源点仍使用各自档案坐标。</p>
            </section>

            <section class="settings-section">
              <header><Clock3 :size="19" /><div><span>业务规则</span><h3>审批与周报节奏</h3></div></header>
              <div class="settings-form-grid">
                <label><span>审批超时阈值（小时）</span><input v-model.number="settings.approvalTimeoutHours" type="number" min="1" max="168" /><small>超过阈值后进入风险提醒</small></label>
                <label><span>每周周报生成日</span><select v-model.number="settings.weeklyReportDay"><option v-for="item in weekDays" :key="item.value" :value="item.value">{{ item.label }}</option></select><small>用于后续自动提醒周报确认</small></label>
              </div>
            </section>

            <section class="settings-section">
              <header><BellRing :size="19" /><div><span>通知策略</span><h3>哪些运营事件需要提醒</h3></div></header>
              <div class="settings-switch-list">
                <label><div><strong>待审批事项</strong><span>新的待处理流程进入通知中心</span></div><input v-model="settings.workflowNotificationEnabled" type="checkbox" /><i /></label>
                <label><div><strong>超时风险</strong><span>待办超过截止时间时生成风险提醒</span></div><input v-model="settings.riskNotificationEnabled" type="checkbox" /><i /></label>
              </div>
            </section>
          </main>

          <aside class="settings-release-card">
            <span>配置责任</span><h3>一次保存，同步三处</h3><p>村域名称进入基础档案，业务阈值用于风险判断，通知开关决定管理端消息范围。</p>
            <dl><div><dt>最后修改人</dt><dd>{{ settings.updatedBy || 'system' }}</dd></div><div><dt>最后更新时间</dt><dd>{{ formatTime(settings.updatedAt) }}</dd></div><div><dt>生效范围</dt><dd>当前村域</dd></div></dl>
            <button class="primary-button" type="button" :disabled="saving" @click="save"><Save :size="17" />{{ saving ? '正在保存...' : '保存并立即生效' }}</button>
            <small v-if="error">{{ error }}</small>
          </aside>
        </div>
      </template>
    </PageState>
  </div>
</template>
