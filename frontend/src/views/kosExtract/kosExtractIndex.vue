<template>
  <div class="kos-extract-page">
    <!-- 左侧：输入面板 -->
    <div class="panel panel-input">
      <div class="panel-header">
        <span>输入文本</span>
        <el-button text size="small" @click="loadSample">加载示例</el-button>
      </div>
      <el-input
        v-model="form.text"
        type="textarea"
        :rows="16"
        placeholder="请输入或粘贴待抽取的文本内容..."
        resize="none"
      />
      <div class="text-stats">
        <span>字数：{{ form.text.length }}</span>
      </div>
    </div>

    <!-- 右侧：配置 + 输出面板 -->
    <div class="panel panel-result">
      <!-- 工具栏 -->
      <div class="controls-bar">
        <el-form :inline="true" :model="form" label-width="68">
          <el-form-item label="目标图谱">
            <el-select v-model="form.graphId" placeholder="选择图谱" clearable style="width: 160px" @change="onGraphChange">
              <el-option v-for="g in graphList" :key="g.id" :label="g.name" :value="g.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="语言">
            <el-select v-model="form.language" style="width: 90px">
              <el-option label="中文" value="zh" />
              <el-option label="英文" value="en" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型">
            <el-select v-model="form.model" placeholder="默认" clearable style="width: 130px">
              <el-option label="qwen-plus" value="qwen-plus" />
              <el-option label="qwen-max" value="qwen-max" />
              <el-option label="qwen-turbo" value="qwen-turbo" />
              <el-option label="qwen-long" value="qwen-long" />
            </el-select>
          </el-form-item>
          <el-form-item label="KOS Schema">
            <el-select v-model="form.schemaType" placeholder="默认通用" clearable style="width: 140px">
              <el-option label="通用" value="" />
              <el-option label="人物关系" value="人物,人物类型,关系类型" />
              <el-option label="企业知识" value="公司,产品,技术,人物,地理位置" />
              <el-option label="科研领域" value="论文,作者,机构,关键词,期刊" />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="control-actions">
          <el-button :loading="loading" :disabled="!form.text" type="primary" @click="handleExtract">
            {{ loading ? '抽取中...' : '抽取' }}
          </el-button>
        </div>
      </div>

      <!-- 结果区域 -->
      <el-tabs v-model="activeTab" class="result-tabs" @tab-click="onTabClick">
        <el-tab-pane label="抽取结果" name="result">
          <div class="results" v-if="result || errorMsg">
            <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="true" @close="errorMsg = ''" />

            <div v-if="result" class="result-stats">
              <el-tag type="success">实体 {{ result.entities?.length || 0 }}</el-tag>
              <el-tag type="warning">关系 {{ result.relations?.length || 0 }}</el-tag>
            </div>

            <div v-if="result?.entities?.length" class="result-section">
              <div class="section-title">
                <span>实体列表</span>
                <el-tag v-if="entityTypeStats.length > 0" size="small" style="margin-left: 8px">
                  {{ entityTypeStats.slice(0, 3).map(e => `${e.type}(${e.count})`).join(' / ') }}
                </el-tag>
              </div>
              <el-table :data="result.entities" stripe size="small" max-height="220" :scrollbar-always-on="true">
                <el-table-column prop="name" label="名称" min-width="130" show-overflow-tooltip />
                <el-table-column prop="type" label="类型" width="130">
                  <template #default="{ row }">
                    <el-tag size="small" type="info">{{ row.type }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="属性" min-width="150">
                  <template #default="{ row }">
                    <span class="attr-text">{{ formatAttrs(row.attributes) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div v-if="result?.relations?.length" class="result-section">
              <div class="section-title">
                <span>关系列表</span>
                <el-tag v-if="relationTypeStats.length > 0" size="small" style="margin-left: 8px" type="warning">
                  {{ relationTypeStats.slice(0, 3).map(r => `${r.type}(${r.count})`).join(' / ') }}
                </el-tag>
              </div>
              <el-table :data="result.relations" stripe size="small" max-height="180" :scrollbar-always-on="true">
                <el-table-column prop="source" label="源实体" width="130" show-overflow-tooltip />
                <el-table-column prop="type" label="关系" width="120">
                  <template #default="{ row }">
                    <el-tag size="small" type="warning">{{ row.type }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="target" label="目标实体" width="130" show-overflow-tooltip />
                <el-table-column label="属性" min-width="120">
                  <template #default="{ row }">
                    <span class="attr-text">{{ formatAttrs(row.attributes) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <el-empty v-if="result && !result.entities?.length && !result.relations?.length" description="未抽取出任何实体或关系" />
          </div>

          <div v-else class="results-empty">
            <el-empty description="抽取结果将在此展示">
              <template #image>
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="9" stroke="#dcdfe6" stroke-width="1.5"/>
                  <circle cx="12" cy="7" r="2" fill="#dcdfe6"/>
                  <circle cx="7.5" cy="14" r="2" fill="#dcdfe6"/>
                  <circle cx="16.5" cy="14" r="2" fill="#dcdfe6"/>
                  <line x1="12" y1="9" x2="8" y2="12.5" stroke="#dcdfe6" stroke-width="1.5"/>
                  <line x1="12" y1="9" x2="16" y2="12.5" stroke="#dcdfe6" stroke-width="1.5"/>
                </svg>
              </template>
            </el-empty>
          </div>
        </el-tab-pane>

        <el-tab-pane label="图谱预览" name="graph">
          <div class="graph-preview-container">
            <div v-if="graphLoading" class="graph-loading">
              <el-icon class="is-loading" :size="32"><Loading /></el-icon>
              <span>加载图谱数据中...</span>
            </div>
            <div v-else-if="!form.graphId" class="graph-empty-hint">
              <el-empty description="请先选择目标图谱">
                <template #image>
                  <el-icon :size="48" color="#dcdfe6"><Connection /></el-icon>
                </template>
              </el-empty>
            </div>
            <div v-else-if="!graphData || (graphData.nodes && graphData.nodes.length === 0)" class="graph-empty-hint">
              <el-empty description="该图谱暂无数据，请先进行知识抽取">
                <template #image>
                  <el-icon :size="48" color="#dcdfe6"><Connection /></el-icon>
                </template>
              </el-empty>
            </div>
            <div v-else class="graph-display" ref="graphContainerRef">
              <div ref="chartRef" class="echarts-wrapper"></div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { kosExtract, kosExtractSave, getKosGraphList, getKosGraphData } from '@/api/kosExtract'
import { ElMessage } from 'element-plus'
import { Connection, Loading } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const loading = ref(false)
const result = ref(null)
const errorMsg = ref('')
const activeTab = ref('result')
const graphList = ref([])
const hasExtractedData = ref(false)
const graphData = ref(null)  // 后端返回的图谱数据
const graphLoading = ref(false)
const chartRef = ref(null)
const graphContainerRef = ref(null)
let chartInstance = null
let resizeObserver = null

const form = reactive({
  text: '',
  language: 'zh',
  model: '',
  schemaType: '',
  graphId: null,
})

const entityTypeStats = computed(() => {
  if (!result.value?.entities?.length) return []
  const map = {}
  result.value.entities.forEach(e => {
    const t = e.type || '未知'
    map[t] = (map[t] || 0) + 1
  })
  return Object.entries(map).map(([type, count]) => ({ type, count })).sort((a, b) => b.count - a.count)
})

const relationTypeStats = computed(() => {
  if (!result.value?.relations?.length) return []
  const map = {}
  result.value.relations.forEach(r => {
    const t = r.type || '未知'
    map[t] = (map[t] || 0) + 1
  })
  return Object.entries(map).map(([type, count]) => ({ type, count })).sort((a, b) => b.count - a.count)
})

const handleExtract = async () => {
  if (!form.text.trim()) {
    ElMessage.warning('请输入文本内容')
    return
  }
  loading.value = true
  errorMsg.value = ''
  result.value = null
  hasExtractedData.value = false

  try {
    const payload = {
      text: form.text,
      extractType: 'all',
      language: form.language,
      model: form.model || null,
      schema: form.schemaType || null,
    }

    let res
    if (form.graphId) {
      res = await kosExtractSave(payload, form.graphId)
    } else {
      res = await kosExtract(payload)
    }

    if (res.code === 200) {
      result.value = res.data
      if (form.graphId) {
        hasExtractedData.value = true
        activeTab.value = 'graph'
        await nextTick()
        fetchAndRenderGraph()
      }
    } else {
      errorMsg.value = res.message || '抽取失败'
    }
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e.message || '抽取请求失败'
  } finally {
    loading.value = false
  }
}

const loadSample = () => {
  form.text = `张明是阿里巴巴集团的高级算法工程师，2018年毕业于清华大学计算机系，获博士学位。他师从李开复教授，主要研究方向为自然语言处理和知识图谱构建。

2019年，张明加入字节跳动担任算法研究员，期间主导了抖音推荐系统的优化项目。2021年，他跳槽到阿里巴巴，负责淘宝搜索排序算法的研发。

李开复是创新工场的创始人兼CEO，曾任谷歌中国区总裁，是人工智能领域的知名专家。他创办的创新工场投资了多家人工智能初创公司。`
  ElMessage.success('已加载示例文本')
}

const loadGraphList = async () => {
  try {
    const res = await getKosGraphList({ pageSize: 100 })
    if (res.code === 200) {
      graphList.value = res.data.list || []
    }
  } catch (e) {
    console.warn('Failed to load graph list', e)
  }
}

const onGraphChange = () => {
  hasExtractedData.value = false
}

const formatAttrs = (attrs) => {
  if (!attrs || typeof attrs !== 'object') return '-'
  const entries = Object.entries(attrs)
  if (entries.length === 0) return '-'
  return entries.map(([k, v]) => `${k}: ${v}`).join('; ')
}

const ENTITY_COLORS = {
  '人物': '#5470c8', '公司': '#fac858', '产品': '#91cc75',
  '地点': '#3ba272', '组织': '#ee6666', '机构': '#73c0de',
  '技术': '#fc8452', '事件': '#9a60b4', '论文': '#48b8d0',
  '关键词': '#c9e5a5', '期刊': '#f5c2c2', '默认': '#48b8d0'
}

const getNodeColor = (nodeType) => ENTITY_COLORS[nodeType] || ENTITY_COLORS['默认']

const buildGraphOption = (entities, relations) => {
  const uniqueEntityNames = [...new Set(entities.map(e => e.name))]
  const displayEntities = entities.filter((e, i) => uniqueEntityNames.indexOf(e.name) === i)

  const chartNodes = displayEntities.map((e, idx) => ({
    id: idx,
    name: e.name.length > 14 ? e.name.slice(0, 14) + '...' : e.name,
    fullName: e.name,
    nodeType: e.type || '默认',
    itemStyle: { color: getNodeColor(e.type) },
    symbolSize: 44
  }))

  const nameToIdx = {}
  displayEntities.forEach((e, idx) => { nameToIdx[e.name] = idx })

  const chartLinks = relations
    .filter(r => nameToIdx[r.source] !== undefined && nameToIdx[r.target] !== undefined)
    .map(r => ({
      source: nameToIdx[r.source],
      target: nameToIdx[r.target],
      name: r.type || '关系',
      lineStyle: { color: '#aaa', width: 1.5, curveness: 0.2 }
    }))

  const categories = [...new Set(displayEntities.map(e => e.type || '默认'))].map(t => ({ name: t }))

  return {
    backgroundColor: '#fafafa',
    tooltip: {
      trigger: 'item',
      formatter: (p) => {
        if (p.dataType === 'node') {
          return `<b>${p.data.fullName || p.data.name}</b><br/>类型: ${p.data.nodeType}`
        }
        return `${p.data.name}`
      }
    },
    legend: {
      data: [...new Set(displayEntities.map(e => e.type || '默认'))],
      top: 8, right: 12, textStyle: { fontSize: 11 }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      roam: true,
      draggable: true,
      label: {
        show: true,
        position: 'right',
        fontSize: 11,
        color: '#333',
        formatter: (p) => p.data.name
      },
      lineStyle: { curveness: 0.2 },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 3 }
      },
      categories,
      data: chartNodes,
      links: chartLinks,
      force: {
        repulsion: 120,
        edgeLength: [80, 200],
        layoutAnimation: true
      }
    }]
  }
}

const fetchAndRenderGraph = async () => {
  if (!form.graphId) return
  try {
    const res = await getKosGraphData(form.graphId)
    if (res.code === 200) {
      graphData.value = res.data
      hasExtractedData.value = true
      await nextTick()
      renderGraphPreviewFromGraphData()
    }
  } catch (e) {
    console.error('获取图谱数据失败', e)
  } finally {
    graphLoading.value = false
  }
}

const renderGraphPreviewFromGraphData = () => {
  if (!chartRef.value || !graphData.value) return
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }

  chartInstance = echarts.init(chartRef.value)
  const option = buildGraphOptionFromData(graphData.value.nodes || [], graphData.value.relations || [])
  chartInstance.setOption(option, true)

  chartInstance.on('click', (params) => {
    if (params.dataType === 'node') {
      const fullName = params.data.fullName || params.data.name
      ElMessage.info(`节点: ${fullName}`)
    }
  })
}

const buildGraphOptionFromData = (nodes, relations) => {
  const uniqueNames = [...new Set(nodes.map(n => n.name))]
  const displayNodes = nodes.filter((n, i) => uniqueNames.indexOf(n.name) === i)

  const chartNodes = displayNodes.map((n, idx) => ({
    id: idx,
    name: n.name.length > 14 ? n.name.slice(0, 14) + '...' : n.name,
    fullName: n.name,
    nodeType: n.type || '默认',
    itemStyle: { color: getNodeColor(n.type) },
    symbolSize: 44
  }))

  const nameToIdx = {}
  displayNodes.forEach((n, idx) => { nameToIdx[n.name] = idx })

  const chartLinks = relations
    .filter(r => nameToIdx[r.source] !== undefined && nameToIdx[r.target] !== undefined)
    .map(r => ({
      source: nameToIdx[r.source],
      target: nameToIdx[r.target],
      name: r.type || '关系',
      lineStyle: { color: '#aaa', width: 1.5, curveness: 0.2 }
    }))

  const categories = [...new Set(displayNodes.map(n => n.type || '默认'))].map(t => ({ name: t }))

  return {
    backgroundColor: '#fafafa',
    tooltip: {
      trigger: 'item',
      formatter: (p) => {
        if (p.dataType === 'node') {
          return `<b>${p.data.fullName || p.data.name}</b><br/>类型: ${p.data.nodeType}`
        }
        return `${p.data.name}`
      }
    },
    legend: {
      data: [...new Set(displayNodes.map(n => n.type || '默认'))],
      top: 8, right: 12, textStyle: { fontSize: 11 }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      roam: true,
      draggable: true,
      label: {
        show: true,
        position: 'right',
        fontSize: 11,
        color: '#333',
        formatter: (p) => p.data.name
      },
      lineStyle: { curveness: 0.2 },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 3 }
      },
      categories,
      data: chartNodes,
      links: chartLinks,
      force: {
        repulsion: 120,
        edgeLength: [80, 200],
        layoutAnimation: true
      }
    }]
  }
}

watch(activeTab, (newTab) => {
  if (newTab === 'graph') {
    graphLoading.value = true
    nextTick().then(() => {
      if (form.graphId) {
        fetchAndRenderGraph()
      } else {
        graphLoading.value = false
      }
    })
  }
})

const onTabClick = (tab) => {
  if (tab.paneName === 'graph') {
    graphLoading.value = true
    if (form.graphId) {
      fetchAndRenderGraph()
    } else {
      graphLoading.value = false
    }
  }
}

onMounted(() => {
  loadGraphList()

  resizeObserver = new ResizeObserver(() => {
    chartInstance?.resize()
  })
  if (graphContainerRef.value) {
    resizeObserver.observe(graphContainerRef.value)
  }
})

onUnmounted(() => {
  resizeObserver?.disconnect()
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style scoped>
.kos-extract-page {
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
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.control-actions {
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
  display: flex;
  align-items: center;
}

.attr-text {
  font-size: 12px;
  color: #606266;
  word-break: break-all;
}

.graph-preview-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.graph-loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
  font-size: 14px;
}

.graph-empty-hint {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.graph-display {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.echarts-wrapper {
  flex: 1;
  min-height: 400px;
  border-radius: 6px;
  overflow: hidden;
}
</style>
