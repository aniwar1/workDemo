<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>（半）结构化数据转化</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新建转换任务
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
        <el-table-column prop="sourceType" label="源格式" />
        <el-table-column prop="targetFormat" label="目标格式" />
        <el-table-column prop="recordCount" label="记录数" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleExecute(row)" :disabled="row.status === 'running'">执行</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="源格式" prop="sourceType">
          <el-select v-model="form.sourceType" placeholder="请选择源格式" style="width: 100%">
            <el-option label="CSV" value="csv" />
            <el-option label="JSON" value="json" />
            <el-option label="XML" value="xml" />
            <el-option label="SQL" value="sql" />
            <el-option label="Excel" value="excel" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标格式" prop="targetFormat">
          <el-select v-model="form.targetFormat" placeholder="请选择目标格式" style="width: 100%">
            <el-option label="知识图谱JSON" value="kg_json" />
            <el-option label="RDF" value="rdf" />
            <el-option label="OWL" value="owl" />
          </el-select>
        </el-form-item>
        <el-form-item label="输入路径" prop="inputPath">
          <el-input v-model="form.inputPath" placeholder="请输入输入文件路径" />
        </el-form-item>
        <el-form-item label="输出路径" prop="outputPath">
          <el-input v-model="form.outputPath" placeholder="请输入输出文件路径" />
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
import { getTransformList, addTransform, executeTransform, deleteTransform } from '@/api/dataTransform'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ id: null, name: '', sourceType: '', targetFormat: '', inputPath: '', outputPath: '' })

const rules = { name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }] }

const statusType = (s) => ({ pending: 'info', running: 'warning', completed: 'success', failed: 'danger' }[s] || 'info')
const statusText = (s) => ({ pending: '待执行', running: '执行中', completed: '已完成', failed: '失败' }[s] || s)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTransformList(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, name: '', sourceType: '', targetFormat: '', inputPath: '', outputPath: '' })
  dialogTitle.value = '新建转换任务'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑转换任务'
  dialogVisible.value = true
}

const handleExecute = async (row) => {
  await ElMessageBox.confirm('确定执行该转换任务吗？', '提示')
  await executeTransform(row.id)
  ElMessage.success('任务已启动')
  loadData()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.id) {
    // update
    ElMessage.success('修改成功')
  } else {
    await addTransform(form)
    ElMessage.success('新建成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该任务吗？', '提示')
  await deleteTransform(row.id)
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
</style>
