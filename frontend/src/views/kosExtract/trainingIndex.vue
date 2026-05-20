<template>
  <div class="training-index">
    <router-view />
  </div>
</template>

<script setup>
import { onBeforeMount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onBeforeMount(() => {
  router.replace('/data/kos/dl/training/train')
})
</script>

<style scoped>
.training-index {
  height: 100%;
}
</style>
