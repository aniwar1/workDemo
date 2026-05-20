<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>标注管理</span>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="taskId" label="任务ID" />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="annotation" label="标注结果" show-overflow-tooltip />
        <el-table-column prop="annotatorId" label="标注人" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'completed' ? 'success' : 'info'">{{ row.status === 'completed' ? '已完成' : '待标注' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRecordList } from '@/api/annotation'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRecordList(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 8px; }
</style>
