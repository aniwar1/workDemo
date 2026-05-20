<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>模型训练管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新建训练任务
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索任务名称" clearable @clear="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="modelType" label="模型类型" />
        <el-table-column prop="corpusId" label="训练语料" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accuracy" label="Accuracy">
          <template #default="{ row }">{{ row.accuracy ? row.accuracy.toFixed(4) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="f1Score" label="F1 Score">
          <template #default="{ row }">{{ row.f1Score ? row.f1Score.toFixed(4) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="trainTime" label="训练时间(s)" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleStart(row)" :disabled="row.status === 'running'">启动</el-button>
            <el-button link type="warning" @click="handleStop(row)" :disabled="row.status !== 'running'">停止</el-button>
            <el-button link type="info" @click="handleLogs(row)">日志</el-button>
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

    <el-dialog v-model="dialogVisible" title="新建训练任务" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="模型类型" prop="modelType">
          <el-select v-model="form.modelType" placeholder="请选择模型类型" style="width: 100%">
            <el-option label="BERT-NER" value="bert_ner" />
            <el-option label="BiLSTM-CRF" value="bilstm_crf" />
            <el-option label="RoBERTa" value="roberta" />
          </el-select>
        </el-form-item>
        <el-form-item label="训练语料" prop="corpusId">
          <el-input-number v-model="form.corpusId" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logsDialogVisible" title="训练日志" width="600px">
      <pre class="logs-pre">{{ logsContent }}</pre>
      <template #footer>
        <el-button @click="logsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTrainList, addTrain, startTrain, stopTrain, getTrainLogs, deleteTrain } from '@/api/modelTrain'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const logsDialogVisible = ref(false)
const formRef = ref(null)
const logsContent = ref('')

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ name: '', modelType: '', corpusId: null })

const rules = { name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }] }
const statusType = (s) => ({ pending: 'info', running: 'warning', completed: 'success', failed: 'danger', stopped: 'info' }[s] || 'info')
const statusText = (s) => ({ pending: '待执行', running: '执行中', completed: '已完成', failed: '失败', stopped: '已停止' }[s] || s)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTrainList(query)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { name: '', modelType: '', corpusId: null })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  await addTrain(form)
  ElMessage.success('创建成功')
  dialogVisible.value = false
  loadData()
}

const handleStart = async (row) => {
  await ElMessageBox.confirm('确定启动该训练任务吗？', '提示')
  await startTrain(row.id)
  ElMessage.success('任务已启动')
  loadData()
}

const handleStop = async (row) => {
  await ElMessageBox.confirm('确定停止该训练任务吗？', '提示')
  await stopTrain(row.id)
  ElMessage.success('任务已停止')
  loadData()
}

const handleLogs = async (row) => {
  logsContent.value = ''
  logsDialogVisible.value = true
  try {
    const res = await getTrainLogs(row.id)
    logsContent.value = res.data || '暂无日志'
  } catch {
    logsContent.value = '加载日志失败'
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该任务吗？', '提示')
  await deleteTrain(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.search-bar .el-input { width: 300px; }
.logs-pre { background: #1e1e1e; color: #d4d4d4; padding: 16px; border-radius: 6px; font-size: 12px; line-height: 1.6; max-height: 400px; overflow-y: auto; white-space: pre-wrap; word-break: break-all; }
</style>
