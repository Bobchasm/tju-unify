<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import auth from './api/auth'
import { startMemoReminderScheduler, stopMemoReminderScheduler } from './composables/useMemoReminders'

const router = useRouter()

onMounted(() => {
  if (auth.isAuthenticated()) {
    startMemoReminderScheduler()
  }
})

watch(
  () => router.currentRoute.value.fullPath,
  () => {
    if (auth.isAuthenticated()) {
      startMemoReminderScheduler()
    } else {
      stopMemoReminderScheduler()
    }
  }
)

onBeforeUnmount(() => {
  stopMemoReminderScheduler()
})
</script>
