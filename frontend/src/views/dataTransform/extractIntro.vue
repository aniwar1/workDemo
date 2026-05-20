<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>知识抽取与转化概览</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="intro-card" @click="$router.push('/dl/extract')">
            <el-icon :size="40" color="#409EFF"><Cpu /></el-icon>
            <h3>深度学习知识抽取</h3>
            <p>基于预训练模型进行实体识别、关系抽取</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="intro-card" @click="$router.push('/llm/extract')">
            <el-icon :size="40" color="#67C23A"><MagicStick /></el-icon>
            <h3>LLM知识抽取</h3>
            <p>基于大语言模型进行智能知识抽取</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="intro-card" @click="$router.push('/data/transform')">
            <el-icon :size="40" color="#E6A23C"><DataAnalysis /></el-icon>
            <h3>结构化数据转化</h3>
            <p>将CSV/JSON/XML等结构化数据转换为知识图谱</p>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.page-container { padding: 8px; }
.intro-card { padding: 30px; text-align: center; background: #f9f9f9; border-radius: 8px; cursor: pointer; transition: all 0.3s; }
.intro-card:hover { background: #f0f0f0; transform: translateY(-2px); }
.intro-card h3 { margin: 16px 0 8px; font-size: 16px; }
.intro-card p { font-size: 13px; color: #999; }
</style>
