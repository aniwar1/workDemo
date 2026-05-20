<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>图谱模型管理</span>
          <div class="header-actions">
            <el-button size="small" @click="showOwlDialog">导入 OWL</el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新建模型
            </el-button>
          </div>
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
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleDuplicate(row)">复制</el-button>
            <el-button link type="danger" @click="handleClear(row)">清空</el-button>
            <el-button link type="info" @click="handleExportOwl(row)">导出</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px">
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
        <el-form-item label="关系类型">
          <el-select v-model="form.relationTypes" multiple placeholder="选择或输入关系类型" style="width: 100%" allow-create filterable default-first-option>
            <el-option v-for="t in defaultRelationTypes" :key="t" :label="t" :value="t" />
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

    <el-dialog v-model="owlDialogVisible" title="导入 OWL 文件" width="500px">
      <el-upload ref="owlUploadRef" :auto-upload="false" accept=".owl" :limit="1" :on-change="onOwlFileChange">
        <el-button type="primary">选择 OWL 文件</el-button>
        <template #tip>
          <div class="upload-tip">支持 .owl 格式的 OWL 本体文件导入</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="owlDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="importOwl">导入</el-button>
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
const owlDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const owlUploadRef = ref(null)
const selectedOwlFile = ref(null)

const defaultEntityTypes = ['人物', '机构', '地点', '事件', '概念', '疾病', '症状', '药物']
const defaultRelationTypes = ['属于', '位于', '关联', '导致', '治疗', '发生在', '属于']

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ id: null, name: '', description: '', schema: '', entityTypes: [], relationTypes: [], status: '1' })

const rules = { name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }] }

const parseSchema = (schema) => {
  if (!schema) return []
  try { const s = JSON.parse(schema); return s.entityTypes || [] } catch { return [] }
}

const buildSchema = (entityTypes, relationTypes) => JSON.stringify({ entityTypes, relationTypes })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getModelList(query)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, name: '', description: '', schema: '', entityTypes: [], relationTypes: [], status: '1' })
  dialogTitle.value = '新建模型'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  const types = parseSchema(row.schema)
  Object.assign(form, { ...row, entityTypes: types, relationTypes: [] })
  dialogTitle.value = '编辑模型'
  dialogVisible.value = true
}

const handleDuplicate = async (row) => {
  const newForm = { ...row, id: null, name: row.name + ' (副本)', schema: row.schema }
  delete newForm.id
  await addModel(newForm)
  ElMessage.success('模型已复制')
  loadData()
}

const handleClear = async (row) => {
  await ElMessageBox.confirm('确定清空该模型的所有实体和关系定义吗？', '提示')
  const emptySchema = JSON.stringify({ entityTypes: [], relationTypes: [] })
  await updateModel(row.id, { ...row, schema: emptySchema })
  ElMessage.success('模型已清空')
  loadData()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const data = { ...form, schema: buildSchema(form.entityTypes, form.relationTypes) }
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

const handleExportOwl = (row) => {
  const owl = generateOwl(row)
  const blob = new Blob([owl], { type: 'application/xml' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `model-${row.id}.owl`
  a.click()
  ElMessage.success('OWL 文件已导出')
}

const generateOwl = (row) => {
  const types = parseSchema(row.schema)
  const classes = types.map(t => `    <owl:Class rdf:about="#${t}"/>`).join('\n')
  return `<?xml version="1.0"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:owl="http://www.w3.org/2002/07/owl#"
         xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
  <owl:Ontology rdf:about="${row.name || 'KGModel'}"/>
${classes}
</rdf:RDF>`
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该模型吗？', '提示')
  await deleteModel(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const showOwlDialog = () => {
  selectedOwlFile.value = null
  owlDialogVisible.value = true
}

const onOwlFileChange = (file) => {
  selectedOwlFile.value = file.raw
}

const importOwl = () => {
  if (!selectedOwlFile.value) {
    ElMessage.warning('请先选择 OWL 文件')
    return
  }
  ElMessage.success('OWL 文件导入功能已触发')
  owlDialogVisible.value = false
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; }
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.search-bar .el-input { width: 300px; }
.upload-tip { font-size: 12px; color: #999; margin-top: 4px; }
</style>
