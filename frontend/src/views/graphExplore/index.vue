<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>知识图谱探索</span>
      </template>

      <div class="explore-toolbar">
        <el-select v-model="currentGraphId" placeholder="请选择图谱" @change="loadGraphData" style="width: 300px">
          <el-option v-for="g in graphs" :key="g.id" :label="g.name" :value="g.id" />
        </el-select>
        <el-input v-model="searchKeyword" placeholder="搜索节点名称" clearable style="width: 200px" @clear="loadGraphData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadGraphData">查询</el-button>
      </div>

      <div class="graph-display">
        <div class="graph-info">
          <span>节点数: {{ nodes.length }}</span>
          <span>边数: {{ edges.length }}</span>
        </div>
        <div class="graph-canvas-placeholder">
          <el-empty v-if="!currentGraphId" description="请先选择一个图谱" />
          <div v-else class="node-list">
            <el-tag v-for="n in nodes" :key="n.id" size="large" style="margin: 6px">{{ n.nodeName }}</el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getGraphs, getGraphData, searchNodes } from '@/api/graphExplore'

const currentGraphId = ref(null)
const searchKeyword = ref('')
const graphs = ref([])
const nodes = ref([])
const edges = ref([])

const loadGraphs = async () => {
  const res = await getGraphs()
  graphs.value = res.data
}

const loadGraphData = async () => {
  if (!currentGraphId.value) return
  try {
    const res = await getGraphData(currentGraphId.value)
    nodes.value = res.data.nodes || []
    edges.value = res.data.edges || []
  } catch (e) {
    nodes.value = []
    edges.value = []
  }
}

onMounted(() => { loadGraphs() })
</script>

<style scoped>
.page-container { padding: 8px; }
.explore-toolbar { display: flex; gap: 10px; margin-bottom: 16px; align-items: center; }
.graph-display { border: 1px solid #eee; border-radius: 8px; min-height: 400px; }
.graph-info { padding: 12px 16px; background: #f5f7fa; border-bottom: 1px solid #eee; display: flex; gap: 20px; font-size: 14px; color: #666; }
.graph-canvas-placeholder { padding: 20px; min-height: 350px; }
.node-list { display: flex; flex-wrap: wrap; gap: 8px; }
</style>
