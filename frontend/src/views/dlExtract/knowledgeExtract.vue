<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>深度学习知识抽取</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新建抽取任务
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="extractType" label="抽取类型">
          <template #default="{ row }">
            <el-tag type="primary">深度学习</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="graphId" label="目标图谱" />
        <el-table-column prop="modelId" label="使用模型" />
        <el-table-column prop="extractedCount" label="已抽取数" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleStart(row)" :disabled="row.status === 'running'">启动</el-button>
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

    <el-dialog v-model="dialogVisible" title="新建抽取任务" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="目标图谱" prop="graphId">
          <el-input-number v-model="form.graphId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="使用模型" prop="modelId">
          <el-input-number v-model="form.modelId" :min="1" style="width: 100%" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getExtractList, createDlExtract, startExtract, deleteExtract } from '@/api/extract'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ name: '', graphId: null, modelId: null, sourceType: 'corpus' })

const rules = { name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }] }
const statusType = (s) => ({ pending: 'info', running: 'warning', completed: 'success', failed: 'danger' }[s] || 'info')
const statusText = (s) => ({ pending: '待执行', running: '执行中', completed: '已完成', failed: '失败' }[s] || s)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getExtractList(query)
    tableData.value = res.data.list.filter(t => t.extractType === 'dl')
    total.value = res.data.total
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
  await createDlExtract(form)
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
</style>
