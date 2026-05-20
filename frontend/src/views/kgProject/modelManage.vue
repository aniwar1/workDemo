<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>图谱模型管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新建模型
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索模型名称" clearable @clear="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模型名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="schema" label="实体类型">
          <template #default="{ row }">
            <el-tag v-for="(t, i) in parseSchema(row.schema)" :key="i" size="small" style="margin-right: 4px">{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'info'">{{ row.status === '1' ? '启用' : '禁用' }}</el-tag>
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
        <el-form-item label="模型名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="实体类型" prop="entityTypes">
          <el-select v-model="form.entityTypes" multiple placeholder="选择或输入实体类型" style="width: 100%" allow-create filterable default-first-option>
            <el-option v-for="t in defaultEntityTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">禁用</el-radio>
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
import { getModelList, addModel, updateModel, deleteModel } from '@/api/kgModel'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const defaultEntityTypes = ['人物', '机构', '地点', '事件', '概念', '疾病', '症状', '药物']

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ id: null, name: '', description: '', schema: '', entityTypes: [], status: '1' })

const rules = { name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }] }

const parseSchema = (schema) => {
  if (!schema) return []
  try { const s = JSON.parse(schema); return s.entityTypes || [] } catch { return [] }
}

const buildSchema = (entityTypes) => JSON.stringify({ entityTypes })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getModelList(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, name: '', description: '', schema: '', entityTypes: [], status: '1' })
  dialogTitle.value = '新建模型'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  const types = parseSchema(row.schema)
  Object.assign(form, { ...row, entityTypes: types })
  dialogTitle.value = '编辑模型'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const data = { ...form, schema: buildSchema(form.entityTypes) }
  if (form.id) {
    await updateModel(form.id, data)
    ElMessage.success('修改成功')
  } else {
    await addModel(data)
    ElMessage.success('新建成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该模型吗？', '提示')
  await deleteModel(row.id)
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
