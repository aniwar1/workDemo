<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>语料管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>上传语料
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索语料名称" clearable @clear="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="语料名称" />
        <el-table-column prop="fileType" label="文件类型" />
        <el-table-column prop="fileSize" label="文件大小">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="graphId" label="关联图谱" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'info'">{{ row.status === '1' ? '可用' : '不可用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90">
        <el-form-item label="语料名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入语料名称" />
        </el-form-item>
        <el-form-item label="文件路径" prop="filePath">
          <el-input v-model="form.filePath" placeholder="请输入文件路径" />
        </el-form-item>
        <el-form-item label="文件类型" prop="fileType">
          <el-select v-model="form.fileType" placeholder="请选择文件类型" style="width: 100%">
            <el-option label="TXT" value="txt" />
            <el-option label="JSON" value="json" />
            <el-option label="CSV" value="csv" />
            <el-option label="XML" value="xml" />
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
import { getCorpusList, addCorpus, updateCorpus, deleteCorpus } from '@/api/corpus'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ id: null, name: '', filePath: '', fileType: '', graphId: null })

const rules = { name: [{ required: true, message: '请输入语料名称', trigger: 'blur' }] }

const formatSize = (size) => {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(1) + ' MB'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCorpusList(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, name: '', filePath: '', fileType: '', graphId: null })
  dialogTitle.value = '上传语料'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑语料'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.id) {
    await updateCorpus(form.id, form)
    ElMessage.success('修改成功')
  } else {
    await addCorpus(form)
    ElMessage.success('上传成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该语料吗？', '提示')
  await deleteCorpus(row.id)
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
