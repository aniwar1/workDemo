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
        <el-table-column prop="fileType" label="文件类型">
          <template #default="{ row }">
            <el-tag size="small">{{ (row.fileType || '').toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
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

        <el-form-item label="文件上传" v-if="!form.id">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".txt,.json,.csv,.xml"
            :on-change="onFileChange"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="upload-tip">支持 txt, json, csv, xml 格式</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="关联图谱" prop="graphId">
          <el-input-number v-model="form.graphId" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="uploading">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCorpusList, uploadCorpusFile, updateCorpus, deleteCorpus } from '@/api/corpus'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const uploadRef = ref(null)
const uploading = ref(false)
const selectedFile = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ id: null, name: '', graphId: null })

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
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, name: '', graphId: null })
  selectedFile.value = null
  dialogTitle.value = '上传语料'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { id: row.id, name: row.name, graphId: row.graphId })
  dialogTitle.value = '编辑语料'
  dialogVisible.value = true
}

const onFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleSubmit = async () => {
  if (!form.id && !selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  uploading.value = true
  try {
    if (form.id) {
      await updateCorpus(form.id, { name: form.name, graphId: form.graphId })
      ElMessage.success('修改成功')
    } else {
      const fd = new FormData()
      fd.append('file', selectedFile.value)
      fd.append('name', form.name)
      fd.append('graphId', form.graphId || '')
      await uploadCorpusFile(fd)
      ElMessage.success('上传成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    uploading.value = false
  }
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
.upload-tip { font-size: 12px; color: #999; margin-top: 4px; }
</style>
