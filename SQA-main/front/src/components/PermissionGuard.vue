<template>
  <span v-if="hasPermission(permission)">
    <slot></slot>
  </span>
</template>

<script setup>
import { hasPermission } from '@/utils/permission'

defineProps({
  permission: {
    type: String,
    required: true
  }
})
</script>
