<template>
  <div class="extract-page">
    <div class="panel panel-input">
      <div class="panel-header">
        <span>输入文本</span>
      </div>
      <el-input
        v-model="form.text"
        type="textarea"
        :rows="20"
        placeholder="请输入或粘贴待抽取的文本内容..."
        resize="none"
      />
      <div class="text-stats">
        <span>字数：{{ form.text.length }}</span>
      </div>
    </div>

    <div class="panel panel-result">
      <div class="controls-bar">
        <el-form :inline="true" :model="form" label-width="80">
          <el-form-item label="语言">
            <el-select v-model="form.language" style="width: 100px">
              <el-option label="中文" value="zh" />
              <el-option label="英文" value="en" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型">
            <el-select v-model="form.model" placeholder="默认模型" clearable style="width: 140px">
              <el-option label="qwen-plus" value="qwen-plus" />
              <el-option label="qwen-max" value="qwen-max" />
              <el-option label="qwen-turbo" value="qwen-turbo" />
              <el-option label="qwen-long" value="qwen-long" />
            </el-select>
          </el-form-item>
          <el-form-item label="保存图谱">
            <el-select v-model="form.graphId" placeholder="不保存" clearable style="width: 160px">
              <el-option
                v-for="g in graphList"
                :key="g.id"
                :label="g.name"
                :value="g.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <el-button type="primary" :loading="loading" :disabled="!form.text" @click="handleExtract">
          {{ loading ? '抽取中...' : '抽取' }}
        </el-button>
      </div>

      <el-tabs v-model="activeTab" class="result-tabs">
        <el-tab-pane label="抽取结果" name="result">
          <div class="results" v-if="result || errorMsg">
            <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="true" @close="errorMsg = ''" />
            <div v-if="result" class="result-stats">
              <el-tag type="warning">关系 {{ result.relations?.length || 0 }}</el-tag>
            </div>
            <div v-if="result?.relations?.length" class="result-section">
              <div class="section-title">关系列表</div>
              <el-table :data="result.relations" stripe size="small" max-height="300">
                <el-table-column prop="source" label="源实体" width="140" />
                <el-table-column prop="type" label="关系" width="120">
                  <template #default="{ row }">
                    <el-tag size="small" type="warning">{{ row.type }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="target" label="目标实体" width="140" />
                <el-table-column label="属性" min-width="120">
                  <template #default="{ row }">
                    <span class="attr-text">{{ formatAttrs(row.attributes) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-empty v-if="result && !result.relations?.length" description="未抽取出任何关系" />
          </div>
          <div v-else class="results-empty">
            <el-empty description="抽取结果将在此展示">
              <template #image>
                <el-icon :size="48" color="#dcdfe6"><MagicStick /></el-icon>
              </template>
            </el-empty>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { extractDirect, extractDirectSave } from '@/api/extract'
import { getGraphList } from '@/api/kgGraph'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'

const loading = ref(false)
const result = ref(null)
const errorMsg = ref('')
const activeTab = ref('result')
const graphList = ref([])

const form = reactive({
  text: '',
  extractType: 'relation',
  language: 'zh',
  model: '',
  graphId: null,
})

const handleExtract = async () => {
  if (!form.text.trim()) {
    ElMessage.warning('请输入文本内容')
    return
  }
  loading.value = true
  errorMsg.value = ''
  result.value = null

  try {
    let res
    if (form.graphId) {
      res = await extractDirectSave({
        text: form.text,
        extractType: 'relation',
        language: form.language,
        model: form.model || null,
        graphId: form.graphId,
      })
    } else {
      res = await extractDirect({
        text: form.text,
        extractType: 'relation',
        language: form.language,
        model: form.model || null,
      })
    }

    if (res.code === 200) {
      result.value = res.data
    } else {
      errorMsg.value = res.message || '抽取失败'
    }
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e.message || '抽取请求失败'
  } finally {
    loading.value = false
  }
}

const loadGraphList = async () => {
  try {
    const res = await getGraphList({ pageSize: 100 })
    if (res.code === 200) {
      graphList.value = res.data.list || []
    }
  } catch (e) {
    console.warn('Failed to load graph list', e)
  }
}

const formatAttrs = (attrs) => {
  if (!attrs || typeof attrs !== 'object') return '-'
  return Object.entries(attrs)
    .map(([k, v]) => `${k}: ${v}`)
    .join('; ')
}

onMounted(() => {
  loadGraphList()
})
</script>

<style scoped>
.extract-page {
  display: flex;
  gap: 12px;
  height: calc(100vh - 140px);
  padding: 8px;
}

.panel {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-input {
  flex: 0 0 38%;
  max-width: 38%;
}

.panel-result {
  flex: 1;
  min-width: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}

.text-stats {
  font-size: 12px;
  color: #909399;
  text-align: right;
  margin-top: 6px;
}

.controls-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.result-tabs {
  flex: 1;
  min-height: 0;
  margin-top: 12px;
  display: flex;
  flex-direction: column;
}

.result-tabs :deep(.el-tabs__content) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.result-tabs :deep(.el-tab-pane) {
  height: 100%;
  overflow-y: auto;
}

.results {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.results-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-stats {
  display: flex;
  align-items: center;
  gap: 10px;
}

.result-section {
  background: #fafafa;
  border-radius: 6px;
  padding: 12px;
}

.section-title {
  font-weight: 600;
  font-size: 13px;
  color: #303133;
  margin-bottom: 8px;
}

.attr-text {
  font-size: 12px;
  color: #606266;
  word-break: break-all;
}
</style>
