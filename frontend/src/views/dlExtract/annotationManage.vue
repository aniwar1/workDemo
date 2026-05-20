<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>标注管理</span>
          <el-button size="small" @click="handleExport">导出结果</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索内容" clearable @clear="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 140px" @change="loadData">
          <el-option label="待标注" value="pending" />
          <el-option label="已完成" value="completed" />
          <el-option label="已审核" value="approved" />
          <el-option label="已驳回" value="rejected" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="taskId" label="任务ID" width="100" />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="annotation" label="标注结果" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="annotation-preview">{{ formatAnnotation(row.annotation) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="annotatorId" label="标注人">
          <template #default="{ row }">用户 #{{ row.annotatorId }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="recordStatusType(row.status)">{{ recordStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="success" @click="handleReview(row, 'approve')" :disabled="row.status === 'approved'">通过</el-button>
            <el-button link type="danger" @click="handleReview(row, 'reject')" :disabled="row.status === 'rejected'">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecordList, reviewRecord } from '@/api/annotation'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })

const recordStatusType = (s) => ({ pending: 'info', completed: 'success', approved: 'success', rejected: 'danger' }[s] || 'info')
const recordStatusText = (s) => ({ pending: '待标注', completed: '已完成', approved: '已通过', rejected: '已驳回' }[s] || s)

const formatAnnotation = (ann) => {
  if (!ann) return '-'
  try {
    const obj = typeof ann === 'string' ? JSON.parse(ann) : ann
    const entities = obj.entities || []
    return entities.map(e => `${e.text}[${e.label}]`).join(', ') || '-'
  } catch {
    return ann
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRecordList(query)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleReview = async (row, action) => {
  const msg = action === 'approve' ? '确定通过该标注吗？' : '确定驳回该标注吗？'
  await ElMessageBox.confirm(msg, '审核')
  await reviewRecord(row.id, action)
  ElMessage.success(action === 'approve' ? '已通过' : '已驳回')
  loadData()
}

const handleExport = () => {
  const rows = tableData.value.map(r => ({
    id: r.id, taskId: r.taskId, content: r.content,
    annotation: r.annotation, status: r.status, createTime: r.createTime
  }))
  const csv = ['ID,任务ID,内容,标注结果,状态,时间']
  rows.forEach(r => {
    csv.push([r.id, r.taskId, `"${(r.content || '').replace(/"/g, '""')}"`, `"${(r.annotation || '').replace(/"/g, '""')}"`, r.status, r.createTime].join(','))
  })
  const blob = new Blob([csv.join('\n')], { type: 'text/csv' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `annotation-export-${Date.now()}.csv`
  a.click()
  ElMessage.success('导出成功')
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.search-bar .el-input { width: 260px; }
.annotation-preview { font-size: 12px; color: #666; }
</style>
