<template>
  <div class="home-page">
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.value }}</div>
            </div>
            <el-icon :size="36" :color="item.color"><component :is="item.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>图谱趋势</span>
          </template>
          <div class="chart-placeholder">
            <p>图谱数量趋势图（基于统计数据）</p>
            <div class="trend-bars">
              <div v-for="(v, i) in chartData.graphTrend" :key="i" class="bar-item">
                <div class="bar" :style="{ height: v * 3 + 'px' }"></div>
                <span class="bar-label">Q{{ i + 1 }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" plain @click="$router.push('/kg/graph')">新建图谱</el-button>
            <el-button type="success" plain @click="$router.push('/data/dl/preprocess/corpus')">上传语料</el-button>
            <el-button type="warning" plain @click="$router.push('/data/dl/training/train')">模型训练</el-button>
            <el-button type="info" plain @click="$router.push('/data/llm')">LLM抽取</el-button>
            <el-button type="danger" plain @click="$router.push('/explore/graph')">图谱探索</el-button>
            <el-button plain @click="$router.push('/system/user')">用户管理</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboard } from '@/api/home'

const statCards = ref([
  { title: '知识图谱', value: 0, icon: 'Connection', color: '#409EFF' },
  { title: '图谱模型', value: 0, icon: 'Box', color: '#67C23A' },
  { title: '训练任务', value: 0, icon: 'Cpu', color: '#E6A23C' },
  { title: '语料数据', value: 0, icon: 'Document', color: '#F56C6C' }
])

const chartData = ref({
  graphTrend: [12, 19, 15, 22, 18, 25, 30],
  taskTrend: [8, 12, 10, 15, 14, 18, 20]
})

onMounted(async () => {
  try {
    const res = await getDashboard()
    const d = res.data
    statCards.value[0].value = d.graphCount || 0
    statCards.value[1].value = d.modelCount || 0
    statCards.value[2].value = d.trainTaskCount || 0
    statCards.value[3].value = d.corpusCount || 0
    if (d.graphTrend) chartData.value.graphTrend = d.graphTrend
    if (d.taskTrend) chartData.value.taskTrend = d.taskTrend
  } catch (e) {
    console.error('Failed to load dashboard data')
  }
})
</script>

<style scoped>
.home-page { padding: 8px; }
.stat-card { margin-bottom: 0; }
.stat-content { display: flex; justify-content: space-between; align-items: center; }
.stat-title { font-size: 14px; color: #999; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
.chart-placeholder { height: 220px; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.trend-bars { display: flex; align-items: flex-end; gap: 12px; margin-top: 20px; }
.bar-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.bar { width: 40px; background: linear-gradient(to top, #409EFF, #79bbff); border-radius: 4px 4px 0 0; transition: height 0.3s; }
.bar-label { font-size: 12px; color: #999; }
.quick-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
</style>
