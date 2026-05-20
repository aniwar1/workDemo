<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>数据标注</span>
      </template>
      <el-empty description="请先在标注授权中分配任务后在此进行标注">
        <el-button type="primary" @click="$router.push('/dl/annotation-auth')">去分配任务</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.page-container { padding: 8px; }
</style>
