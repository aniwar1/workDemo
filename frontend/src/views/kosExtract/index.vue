<template>
  <div class="kos-index">
    <router-view />
  </div>
</template>

<script setup>
import { onBeforeMount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onBeforeMount(() => {
  router.replace('/data/dl')
})
</script>

<style scoped>
.kos-index {
  height: 100%;
}
</style>
