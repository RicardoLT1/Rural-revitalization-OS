<script setup lang="ts">
withDefaults(defineProps<{ label?: string; hint?: string }>(), { hint: '即将上线' })
</script>

<template>
  <button class="coming-soon-control" type="button" disabled title="该功能将在后续版本开放">
    <slot>{{ label }}</slot><small>{{ hint }}</small>
  </button>
</template>
