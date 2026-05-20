<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>模型训练效果</span>
          <div class="header-actions">
            <el-select v-model="selectedTaskId" placeholder="选择训练任务" clearable @change="loadMetrics" style="width: 200px">
              <el-option v-for="t in trainTasks" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
            <el-button size="small" @click="exportReport" :disabled="!metrics">导出报告</el-button>
          </div>
        </div>
      </template>

      <div v-if="!selectedTaskId" class="empty-state">
        <el-empty description="请先选择一个训练任务查看效果" />
      </div>

      <div v-else>
        <el-row :gutter="16" style="margin-bottom: 20px">
          <el-col :span="6">
            <div class="metric-card metric-accuracy">
              <div class="metric-label">准确率 Accuracy</div>
              <div class="metric-value">{{ metrics?.accuracy?.toFixed(4) || '-' }}</div>
              <el-progress :percentage="metrics ? Math.round(metrics.accuracy * 100) : 0" :color="accuracyColor" :stroke-width="8" />
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-card metric-precision">
              <div class="metric-label">精确率 Precision</div>
              <div class="metric-value">{{ metrics?.precisionVal?.toFixed(4) || '-' }}</div>
              <el-progress :percentage="metrics ? Math.round(metrics.precisionVal * 100) : 0" color="#409EFF" :stroke-width="8" />
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-card metric-recall">
              <div class="metric-label">召回率 Recall</div>
              <div class="metric-value">{{ metrics?.recall?.toFixed(4) || '-' }}</div>
              <el-progress :percentage="metrics ? Math.round(metrics.recall * 100) : 0" color="#E6A23C" :stroke-width="8" />
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-card metric-f1">
              <div class="metric-label">F1 分数 F1 Score</div>
              <div class="metric-value">{{ metrics?.f1Score?.toFixed(4) || '-' }}</div>
              <el-progress :percentage="metrics ? Math.round(metrics.f1Score * 100) : 0" color="#F56C6C" :stroke-width="8" />
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="16" style="margin-bottom: 20px">
          <el-col :span="8">
            <div class="info-card">
              <div class="info-label">训练时长</div>
              <div class="info-value">{{ metrics?.trainTime ? metrics.trainTime + 's' : '-' }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-card">
              <div class="info-label">训练状态</div>
              <el-tag :type="statusType(metrics?.status)" style="font-size:14px">{{ statusText(metrics?.status) }}</el-tag>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-card">
              <div class="info-label">创建时间</div>
              <div class="info-value">{{ metrics?.createTime || '-' }}</div>
            </div>
          </el-col>
        </el-row>

        <el-card shadow="never" title="指标对比" style="margin-top: 16px">
          <template #header>指标对比</template>
          <el-select v-model="compareTasks" multiple placeholder="选择任务进行对比" style="width: 400px; margin-bottom: 16px" @change="loadComparison">
            <el-option v-for="t in trainTasks" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <div v-if="comparisonTasks.length > 0" class="comparison-table">
            <el-table :data="comparisonTasks" stripe size="small">
              <el-table-column prop="name" label="任务名称" />
              <el-table-column prop="accuracy" label="准确率">
                <template #default="{ row }">{{ row.accuracy?.toFixed(4) || '-' }}</template>
              </el-table-column>
              <el-table-column prop="precisionVal" label="精确率">
                <template #default="{ row }">{{ row.precisionVal?.toFixed(4) || '-' }}</template>
              </el-table-column>
              <el-table-column prop="recall" label="召回率">
                <template #default="{ row }">{{ row.recall?.toFixed(4) || '-' }}</template>
              </el-table-column>
              <el-table-column prop="f1Score" label="F1分数">
                <template #default="{ row }">{{ row.f1Score?.toFixed(4) || '-' }}</template>
              </el-table-column>
              <el-table-column prop="trainTime" label="训练时长">
                <template #default="{ row }">{{ row.trainTime ? row.trainTime + 's' : '-' }}</template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTrainList, getMetrics } from '@/api/modelTrain'

const selectedTaskId = ref(null)
const trainTasks = ref([])
const metrics = ref(null)
const compareTasks = ref([])
const comparisonTasks = ref([])

const accuracyColor = (p) => p > 90 ? '#67C23A' : p > 75 ? '#409EFF' : '#E6A23C'

const statusType = (s) => ({ pending: 'info', running: 'warning', completed: 'success', failed: 'danger' }[s] || 'info')
const statusText = (s) => ({ pending: '待执行', running: '执行中', completed: '已完成', failed: '失败' }[s] || s)

const loadTasks = async () => {
  const res = await getTrainList({ pageNum: 1, pageSize: 50 })
  trainTasks.value = res.data?.list || []
  if (trainTasks.value.length > 0) {
    selectedTaskId.value = trainTasks.value[0].id
    loadMetrics()
  }
}

const loadMetrics = async () => {
  if (!selectedTaskId.value) return
  try {
    const res = await getMetrics(selectedTaskId.value)
    metrics.value = res.data
  } catch (e) {
    ElMessage.error('加载指标失败')
  }
}

const loadComparison = async () => {
  comparisonTasks.value = []
  for (const id of compareTasks.value) {
    const res = await getMetrics(id)
    if (res.data) comparisonTasks.value.push(res.data)
  }
}

const exportReport = () => {
  const report = {
    task: metrics.value,
    timestamp: new Date().toISOString(),
    comparison: comparisonTasks.value
  }
  const blob = new Blob([JSON.stringify(report, null, 2)], { type: 'application/json' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `model-report-${selectedTaskId.value}.json`
  a.click()
  ElMessage.success('报告已导出')
}

onMounted(() => loadTasks())
</script>

<style scoped>
.page-container { padding: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.empty-state { display: flex; justify-content: center; padding: 60px 0; }
.metric-card { padding: 20px; background: #f9f9f9; border-radius: 8px; text-align: center; }
.metric-label { font-size: 13px; color: #888; margin-bottom: 8px; }
.metric-value { font-size: 28px; font-weight: bold; margin-bottom: 12px; }
.info-card { padding: 16px; background: #f5f7fa; border-radius: 8px; text-align: center; }
.info-label { font-size: 12px; color: #999; margin-bottom: 6px; }
.info-value { font-size: 16px; font-weight: 500; color: #333; }
</style>
