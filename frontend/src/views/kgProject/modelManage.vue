<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>图谱模型管理</span>
          <div class="header-actions">
            <el-button size="small" @click="showOwlDialog">
              <el-icon><Upload /></el-icon>导入 OWL
            </el-button>
            <el-button type="primary" size="small" @click="handleAdd">
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
        <el-table-column prop="name" label="模型名称" min-width="160" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="200" />
        <el-table-column label="实体类型" min-width="220">
          <template #default="{ row }">
            <el-tag
              v-for="(t, i) in parseEntityTypes(row.schema)"
              :key="'e-' + i"
              size="small"
              type="primary"
              style="margin-right: 4px; margin-bottom: 2px"
            >{{ t }}</el-tag>
            <span v-if="!parseEntityTypes(row.schema).length" class="empty-text">无</span>
          </template>
        </el-table-column>
        <el-table-column label="关系类型" min-width="220">
          <template #default="{ row }">
            <el-tag
              v-for="(t, i) in parseRelationTypes(row.schema)"
              :key="'r-' + i"
              size="small"
              type="warning"
              style="margin-right: 4px; margin-bottom: 2px"
            >{{ t }}</el-tag>
            <span v-if="!parseRelationTypes(row.schema).length" class="empty-text">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleDuplicate(row)">复制</el-button>
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

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="720px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90">
        <el-form-item label="模型名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入模型名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入模型描述" maxlength="500" show-word-limit />
        </el-form-item>

        <!-- Schema 可视化构建器 -->
        <el-form-item label="实体类型">
          <div class="schema-section">
            <div class="schema-tags-row">
              <el-tag
                v-for="(tag, i) in form.entityTypes"
                :key="'et-' + i"
                closable
                size="default"
                type="primary"
                @close="removeEntityType(i)"
                class="schema-tag"
              >{{ tag }}</el-tag>
              <span v-if="!form.entityTypes.length" class="empty-text">点击下方输入框添加实体类型</span>
            </div>
            <el-select
              v-model="newEntityType"
              placeholder="选择或输入实体类型"
              filterable
              allow-create
              default-first-option
              style="width: 100%; margin-top: 8px"
              @change="addEntityType"
            >
              <el-option v-for="t in unusedEntityOptions" :key="t" :label="t" :value="t" />
            </el-select>
          </div>
        </el-form-item>

        <el-form-item label="关系类型">
          <div class="schema-section">
            <div class="schema-tags-row">
              <el-tag
                v-for="(tag, i) in form.relationTypes"
                :key="'rt-' + i"
                closable
                size="default"
                type="warning"
                @close="removeRelationType(i)"
                class="schema-tag"
              >{{ tag }}</el-tag>
              <span v-if="!form.relationTypes.length" class="empty-text">点击下方输入框添加关系类型</span>
            </div>
            <el-select
              v-model="newRelationType"
              placeholder="选择或输入关系类型"
              filterable
              allow-create
              default-first-option
              style="width: 100%; margin-top: 8px"
              @change="addRelationType"
            >
              <el-option v-for="t in unusedRelationOptions" :key="t" :label="t" :value="t" />
            </el-select>
          </div>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- OWL 导入弹窗 -->
    <el-dialog v-model="owlDialogVisible" title="导入 OWL 本体文件" width="560px" :close-on-click-modal="false">
      <div class="owl-import-container">
        <el-upload
          ref="owlUploadRef"
          :auto-upload="false"
          accept=".owl,.rdf"
          :limit="1"
          :on-change="onOwlFileChange"
          :on-remove="onOwlFileRemove"
          drag
        >
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="upload-text">将 OWL/RDF 文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="upload-tip">支持 .owl 和 .rdf 格式的 OWL 本体文件导入</div>
          </template>
        </el-upload>

        <div v-if="owlPreview" class="owl-preview">
          <div class="preview-header">
            <span class="preview-title">导入预览</span>
            <el-tag size="small" type="primary">{{ owlPreview.entityTypes.length }} 实体类型</el-tag>
            <el-tag size="small" type="warning">{{ owlPreview.relationTypes.length }} 关系类型</el-tag>
          </div>
          <div class="preview-section">
            <div class="preview-label">实体类型：</div>
            <el-tag
              v-for="(t, i) in owlPreview.entityTypes"
              :key="'pe-' + i"
              size="small"
              type="primary"
              style="margin-right: 6px; margin-bottom: 4px"
            >{{ t }}</el-tag>
            <span v-if="!owlPreview.entityTypes.length" class="empty-text">未提取到实体类型</span>
          </div>
          <div class="preview-section">
            <div class="preview-label">关系类型：</div>
            <el-tag
              v-for="(t, i) in owlPreview.relationTypes"
              :key="'pr-' + i"
              size="small"
              type="warning"
              style="margin-right: 6px; margin-bottom: 4px"
            >{{ t }}</el-tag>
            <span v-if="!owlPreview.relationTypes.length" class="empty-text">未提取到关系类型</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="owlDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedOwlFile || owlImportLoading" :loading="owlImportLoading" @click="importOwl">
          {{ selectedOwlFile ? '导入到新模型' : '请先上传文件' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- OWL 导入后的快速创建弹窗 -->
    <el-dialog v-model="owlModelDialogVisible" title="导入 OWL — 创建模型" width="500px" :close-on-click-modal="false">
      <el-form :model="owlForm" label-width="90">
        <el-form-item label="模型名称" required>
          <el-input v-model="owlForm.name" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="owlForm.description" type="textarea" :rows="2" placeholder="可选的模型描述" />
        </el-form-item>
        <el-form-item label="实体类型">
          <el-tag
            v-for="(t, i) in owlPreview?.entityTypes || []"
            :key="'oe-' + i"
            size="small"
            type="primary"
            style="margin-right: 6px; margin-bottom: 4px"
          >{{ t }}</el-tag>
        </el-form-item>
        <el-form-item label="关系类型">
          <el-tag
            v-for="(t, i) in owlPreview?.relationTypes || []"
            :key="'or-' + i"
            size="small"
            type="warning"
            style="margin-right: 6px; margin-bottom: 4px"
          >{{ t }}</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="owlModelDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="owlSaveLoading" @click="handleOwlModelSave">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getModelList, addModel, updateModel, deleteModel } from '@/api/kgModel'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Upload, UploadFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const owlDialogVisible = ref(false)
const owlModelDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const owlUploadRef = ref(null)
const selectedOwlFile = ref(null)
const owlImportLoading = ref(false)
const owlSaveLoading = ref(false)
const owlPreview = ref(null)

const allPresetEntityTypes = ['人物', '机构', '地点', '事件', '概念', '疾病', '症状', '药物', '公司', '产品', '技术', '论文', '作者', '关键词', '期刊']
const allPresetRelationTypes = ['属于', '位于', '关联', '导致', '治疗', '发生在', '创作', '发表', '引用', '属于', '位于', '关联']

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ id: null, name: '', description: '', schema: '', entityTypes: [], relationTypes: [], status: 1 })
const newEntityType = ref('')
const newRelationType = ref('')
const owlForm = reactive({ name: '', description: '' })

const rules = {
  name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
}

const parseEntityTypes = (schema) => {
  if (!schema) return []
  try {
    const s = JSON.parse(schema)
    return Array.isArray(s.entityTypes) ? s.entityTypes : []
  } catch {
    return []
  }
}

const parseRelationTypes = (schema) => {
  if (!schema) return []
  try {
    const s = JSON.parse(schema)
    return Array.isArray(s.relationTypes) ? s.relationTypes : []
  } catch {
    return []
  }
}

const buildSchema = (entityTypes, relationTypes) => JSON.stringify({ entityTypes, relationTypes })

const unusedEntityOptions = computed(() => {
  return allPresetEntityTypes.filter(t => !form.entityTypes.includes(t))
})

const unusedRelationOptions = computed(() => {
  return allPresetRelationTypes.filter(t => !form.relationTypes.includes(t))
})

const addEntityType = (val) => {
  if (val && typeof val === 'string' && !form.entityTypes.includes(val)) {
    form.entityTypes.push(val.trim())
  }
  newEntityType.value = ''
}

const removeEntityType = (index) => {
  form.entityTypes.splice(index, 1)
}

const addRelationType = (val) => {
  if (val && typeof val === 'string' && !form.relationTypes.includes(val)) {
    form.relationTypes.push(val.trim())
  }
  newRelationType.value = ''
}

const removeRelationType = (index) => {
  form.relationTypes.splice(index, 1)
}

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
  Object.assign(form, { id: null, name: '', description: '', schema: '', entityTypes: [], relationTypes: [], status: 1 })
  newEntityType.value = ''
  newRelationType.value = ''
  dialogTitle.value = '新建模型'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  const entityTypes = parseEntityTypes(row.schema)
  const relationTypes = parseRelationTypes(row.schema)
  Object.assign(form, {
    id: row.id,
    name: row.name,
    description: row.description || '',
    schema: row.schema,
    entityTypes: [...entityTypes],
    relationTypes: [...relationTypes],
    status: row.status
  })
  newEntityType.value = ''
  newRelationType.value = ''
  dialogTitle.value = '编辑模型'
  dialogVisible.value = true
}

const handleDuplicate = async (row) => {
  const newForm = {
    name: row.name + ' (副本)',
    description: row.description || '',
    schema: row.schema,
    status: row.status
  }
  await addModel(newForm)
  ElMessage.success('模型已复制')
  loadData()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const data = {
    name: form.name,
    description: form.description,
    schema: buildSchema(form.entityTypes, form.relationTypes),
    status: form.status
  }
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
  await ElMessageBox.confirm(`确定删除模型「${row.name}」吗？此操作不可恢复。`, '确认删除', { type: 'warning' })
  await deleteModel(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleExportOwl = (row) => {
  const entityTypes = parseEntityTypes(row.schema)
  const relationTypes = parseRelationTypes(row.schema)

  const classes = entityTypes.map(t => `    <owl:Class rdf:about="#${t}"/>`).join('\n')
  const properties = relationTypes.map(t => `    <owl:ObjectProperty rdf:about="#${t}"/>`).join('\n')
  const classSubClasses = entityTypes.map(t => `    <owl:Class rdf:about="#${t}">\n      <rdfs:subClassOf rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>\n    </owl:Class>`).join('\n')

  const owl = `<?xml version="1.0"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:owl="http://www.w3.org/2002/07/owl#"
         xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
         xmlns:xsd="http://www.w3.org/2001/XMLSchema#">
  <owl:Ontology rdf:about="${row.name || 'KGModel'}"/>
${classSubClasses}
${properties}
</rdf:RDF>`

  const blob = new Blob([owl], { type: 'application/xml;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${row.name || 'model-' + row.id}.owl`
  a.click()
  URL.revokeObjectURL(a.href)
  ElMessage.success('OWL 文件已导出')
}

const parseOwlContent = (content) => {
  const parser = new DOMParser()
  const doc = parser.parseFromString(content, 'text/xml')

  const entityTypes = new Set()
  const relationTypes = new Set()

  doc.querySelectorAll('Class').forEach(cls => {
    const about = cls.getAttribute('rdf:about') || cls.getAttribute('rdfID')
    if (about && !about.includes('owl:') && !about.includes('#Thing')) {
      const name = about.split('#').pop() || about
      if (name && name.trim()) entityTypes.add(name.trim())
    }
    const subClassOf = cls.querySelector('subClassOf')
    if (subClassOf) {
      const resource = subClassOf.getAttribute('rdf:resource')
      if (resource && !resource.includes('owl#Thing')) {
        const name = resource.split('#').pop()
        if (name && name.trim()) entityTypes.add(name.trim())
      }
    }
  })

  doc.querySelectorAll('ObjectProperty, DatatypeProperty').forEach(prop => {
    const about = prop.getAttribute('rdf:about') || prop.getAttribute('rdfID')
    if (about) {
      const name = about.split('#').pop() || about
      if (name && name.trim()) relationTypes.add(name.trim())
    }
  })

  doc.querySelectorAll('[rdf\\:about*="#"]').forEach(el => {
    const about = el.getAttribute('rdf:about')
    if (about) {
      const name = about.split('#').pop()
      const localName = el.getAttribute('rdf:ID')
      if (localName) entityTypes.add(localName)
    }
  })

  const parseResult = {
    entityTypes: Array.from(entityTypes).slice(0, 50),
    relationTypes: Array.from(relationTypes).slice(0, 50)
  }
  return parseResult
}

const showOwlDialog = () => {
  selectedOwlFile.value = null
  owlPreview.value = null
  if (owlUploadRef.value) owlUploadRef.value.clearFiles()
  owlDialogVisible.value = true
}

const onOwlFileChange = async (file) => {
  selectedOwlFile.value = file.raw
  try {
    const text = await file.raw.text()
    owlPreview.value = parseOwlContent(text)
  } catch (e) {
    owlPreview.value = { entityTypes: [], relationTypes: [] }
    ElMessage.warning('文件解析失败，请确认是有效的 OWL/RDF 格式')
  }
}

const onOwlFileRemove = () => {
  selectedOwlFile.value = null
  owlPreview.value = null
}

const importOwl = async () => {
  if (!selectedOwlFile.value) {
    ElMessage.warning('请先上传 OWL 文件')
    return
  }
  if (!owlPreview.value || (owlPreview.value.entityTypes.length === 0 && owlPreview.value.relationTypes.length === 0)) {
    ElMessage.warning('未从文件中提取到有效的实体或关系类型')
    return
  }
  owlDialogVisible.value = false
  owlForm.name = selectedOwlFile.value.name.replace(/\.(owl|rdf)$/i, '') + ' (导入)'
  owlForm.description = '由 OWL 文件导入的图谱模型'
  owlModelDialogVisible.value = true
}

const handleOwlModelSave = async () => {
  if (!owlForm.name.trim()) {
    ElMessage.warning('请输入模型名称')
    return
  }
  owlSaveLoading.value = true
  try {
    const data = {
      name: owlForm.name.trim(),
      description: owlForm.description,
      schema: buildSchema(owlPreview.value.entityTypes, owlPreview.value.relationTypes),
      status: 1
    }
    await addModel(data)
    ElMessage.success(`模型「${owlForm.name}」创建成功`)
    owlModelDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('导入失败: ' + (e.message || '未知错误'))
  } finally {
    owlSaveLoading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; }
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.search-bar .el-input { width: 300px; }
.upload-tip { font-size: 12px; color: #999; margin-top: 4px; line-height: 1.4; }

.schema-section { width: 100%; }
.schema-tags-row { min-height: 36px; display: flex; flex-wrap: wrap; align-items: center; gap: 6px; }
.schema-tag { font-size: 13px; }

.empty-text { font-size: 12px; color: #c0c4cc; font-style: italic; }

.owl-import-container { display: flex; flex-direction: column; gap: 16px; }
.upload-icon { font-size: 48px; color: #909399; margin-bottom: 8px; }
.upload-text { color: #606266; font-size: 14px; }
.upload-text em { color: #409eff; font-style: normal; cursor: pointer; }

.owl-preview {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px 16px;
  background: #fafbfc;
}
.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.preview-title { font-weight: 600; font-size: 14px; color: #303133; }
.preview-section { margin-bottom: 8px; }
.preview-section:last-child { margin-bottom: 0; }
.preview-label { font-size: 12px; color: #909399; margin-bottom: 6px; font-weight: 500; }
</style>
