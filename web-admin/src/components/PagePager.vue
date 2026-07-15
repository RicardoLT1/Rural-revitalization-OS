<script setup lang="ts">
import { ChevronLeft, ChevronRight } from '@lucide/vue'

const props = defineProps<{
  page: number
  pageSize: number
  total: number
  totalPages: number
}>()

const emit = defineEmits<{ change: [page: number] }>()

function go(nextPage: number) {
  if (nextPage < 1 || nextPage > props.totalPages || nextPage === props.page) return
  emit('change', nextPage)
}
</script>

<template>
  <nav v-if="total > 0" class="table-pager" aria-label="列表分页">
    <p><strong>{{ total }}</strong> 条记录 <span>·</span> 每页 {{ pageSize }} 条</p>
    <div>
      <button type="button" :disabled="page <= 1" aria-label="上一页" @click="go(page - 1)">
        <ChevronLeft :size="16" />上一页
      </button>
      <span><b>{{ page }}</b> / {{ Math.max(totalPages, 1) }}</span>
      <button type="button" :disabled="page >= totalPages" aria-label="下一页" @click="go(page + 1)">
        下一页<ChevronRight :size="16" />
      </button>
    </div>
  </nav>
</template>
