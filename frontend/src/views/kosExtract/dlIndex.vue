<template>
  <div class="dl-index">
    <router-view />
  </div>
</template>

<script setup>
import { onBeforeMount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onBeforeMount(() => {
  router.replace('/data/dl/preprocess')
})
</script>

<style scoped>
.dl-index {
  height: 100%;
}
</style>
