<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>基于LLM知识抽取</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新建LLM抽取任务
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="extractType" label="抽取类型">
          <template #default>
            <el-tag type="success">LLM</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="graphId" label="目标图谱" />
        <el-table-column prop="extractedCount" label="已抽取数" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleStart(row)" :disabled="row.status === 'running'">启动</el-button>
            <el-button link type="warning" @click="handleStop(row)" :disabled="row.status !== 'running'">停止</el-button>
            <el-button link type="success" @click="handleViewResult(row)">查看结果</el-button>
            <el-button link type="info" @click="handleExport(row)">导出</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" title="新建LLM抽取任务" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="目标图谱" prop="graphId">
          <el-input-number v-model="form.graphId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="LLM模型" prop="modelId">
          <el-select v-model="form.modelId" placeholder="请选择LLM模型" style="width: 100%">
            <el-option label="GPT-4" :value="1" />
            <el-option label="GPT-3.5" :value="2" />
            <el-option label="Claude-3" :value="3" />
            <el-option label="文心一言" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据来源类型" prop="sourceType">
          <el-select v-model="form.sourceType" placeholder="请选择数据来源" style="width: 100%">
            <el-option label="语料" value="corpus" />
            <el-option label="文本" value="text" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultDialogVisible" title="抽取结果" width="700px">
      <div v-if="!resultData" class="loading">加载中...</div>
      <div v-else>
        <el-alert type="info" :closable="false" style="margin-bottom: 12px">
          共抽取 {{ resultData.task?.extractedCount || 0 }} 个实体/关系
        </el-alert>
        <el-empty v-if="!resultData.graphData || resultData.graphData.length === 0" description="暂无详细图数据" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getExtractList, createLlmExtract, startExtract, stopExtract, getExtractResult, deleteExtract } from '@/api/extract'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const resultDialogVisible = ref(false)
const formRef = ref(null)
const resultData = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ name: '', graphId: null, modelId: null, sourceType: 'corpus' })

const rules = { name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }] }
const statusType = (s) => ({ pending: 'info', running: 'warning', completed: 'success', failed: 'danger', stopped: 'info' }[s] || 'info')
const statusText = (s) => ({ pending: '待执行', running: '执行中', completed: '已完成', failed: '失败', stopped: '已停止' }[s] || s)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getExtractList(query)
    tableData.value = (res.data.list || []).filter(t => t.extractType === 'llm')
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { name: '', graphId: null, modelId: null, sourceType: 'corpus' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  await createLlmExtract(form)
  ElMessage.success('创建成功')
  dialogVisible.value = false
  loadData()
}

const handleStart = async (row) => {
  await ElMessageBox.confirm('确定启动该抽取任务吗？', '提示')
  await startExtract(row.id)
  ElMessage.success('任务已启动')
  loadData()
}

const handleStop = async (row) => {
  await stopExtract(row.id)
  ElMessage.success('任务已停止')
  loadData()
}

const handleViewResult = async (row) => {
  resultDialogVisible.value = true
  resultData.value = null
  try {
    const res = await getExtractResult(row.id)
    resultData.value = res.data
  } catch {
    ElMessage.error('加载结果失败')
  }
}

const handleExport = (row) => {
  const data = { taskId: row.id, name: row.name, extractedCount: row.extractedCount, exportTime: new Date().toISOString() }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `llm-extract-result-${row.id}.json`
  a.click()
  ElMessage.success('已导出')
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该任务吗？', '提示')
  await deleteExtract(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.loading { text-align: center; padding: 40px; color: #999; }
</style>
