<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>知识图谱项目管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新建图谱
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索项目名称" clearable @clear="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="name" label="项目名称" />
        <el-table-column prop="description" label="项目描述" show-overflow-tooltip />
        <el-table-column prop="projectManager" label="项目负责人" />
        <el-table-column prop="modelName" label="模型名称" />
        <el-table-column prop="storageEngine" label="存储引擎" />
        <el-table-column prop="storageEngineConfigured" label="是否已配置存储引擎" width="140">
          <template #default="{ row }">
            <el-tag :type="row.storageEngineConfigured ? 'success' : 'danger'" size="small">
              {{ row.storageEngineConfigured ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="graphSpaceCreated" label="是否已创建图空间" width="140">
          <template #default="{ row }">
            <el-tag :type="row.graphSpaceCreated ? 'success' : 'danger'" size="small">
              {{ row.graphSpaceCreated ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="项目负责人" prop="projectManager">
          <el-input v-model="form.projectManager" placeholder="请输入项目负责人" />
        </el-form-item>
        <el-form-item label="模型名称" prop="modelId">
          <el-select v-model="form.modelId" placeholder="请选择模型" style="width: 100%">
            <el-option v-for="m in models" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="存储引擎" prop="storageEngine">
          <el-select v-model="form.storageEngine" placeholder="请选择存储引擎" style="width: 100%">
            <el-option label="NebulaGraph" value="nebula" />
            <el-option label="JanusGraph" value="janus" />
            <el-option label="TuGraph" value="tugraph" />
            <el-option label="Neo4j" value="neo4j" />
            <el-option label="HugeGraph" value="hugegraph" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否已配置存储引擎" prop="storageEngineConfigured">
          <el-switch v-model="form.storageEngineConfigured" />
        </el-form-item>
        <el-form-item label="是否已创建图空间" prop="graphSpaceCreated">
          <el-switch v-model="form.graphSpaceCreated" />
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
const form = reactive({ id: null, name: '', description: '', projectManager: '', modelId: null, storageEngine: '', storageEngineConfigured: false, graphSpaceCreated: false })

const rules = { name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }] }

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
  Object.assign(form, { id: null, name: '', description: '', projectManager: '', modelId: null, storageEngine: '', storageEngineConfigured: false, graphSpaceCreated: false })
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
