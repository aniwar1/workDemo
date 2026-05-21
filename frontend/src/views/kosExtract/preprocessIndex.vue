<template>
  <div class="preprocess-index">
    <router-view />
  </div>
</template>

<script setup>
import { onBeforeMount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onBeforeMount(() => {
  router.replace('/data/dl/preprocess/corpus')
})
</script>

<style scoped>
.preprocess-index {
  height: 100%;
}
</style>
