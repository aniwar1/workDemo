<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>知识图谱管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新建图谱
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索图谱名称" clearable @clear="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="图谱名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="modelId" label="关联模型" />
        <el-table-column prop="nodeCount" label="节点数" />
        <el-table-column prop="edgeCount" label="边数" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'info'">{{ row.status === '1' ? '运行中' : '已停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90">
        <el-form-item label="图谱名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入图谱名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="关联模型" prop="modelId">
          <el-select v-model="form.modelId" placeholder="请选择模型" style="width: 100%">
            <el-option v-for="m in models" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="1">运行中</el-radio>
            <el-radio label="0">已停用</el-radio>
          </el-radio-group>
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
import { getGraphList, addGraph, updateGraph, deleteGraph } from '@/api/kgGraph'
import { getModelList } from '@/api/kgModel'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const models = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ id: null, name: '', description: '', modelId: null, status: '1' })

const rules = { name: [{ required: true, message: '请输入图谱名称', trigger: 'blur' }] }

const loadData = async () => {
  loading.value = true
  try {
    const res = await getGraphList(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadModels = async () => {
  const res = await getModelList({ pageNum: 1, pageSize: 100 })
  models.value = res.data.list
}

const handleAdd = () => {
  Object.assign(form, { id: null, name: '', description: '', modelId: null, status: '1' })
  dialogTitle.value = '新建图谱'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑图谱'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.id) {
    await updateGraph(form.id, form)
    ElMessage.success('修改成功')
  } else {
    await addGraph(form)
    ElMessage.success('新建成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该图谱吗？', '提示')
  await deleteGraph(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => { loadData(); loadModels() })
</script>

<style scoped>
.page-container { padding: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.search-bar .el-input { width: 300px; }
</style>
