<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>模型训练效果</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="12">
          <div class="metric-card">
            <h4>准确率 (Accuracy)</h4>
            <div class="metric-value">0.9123</div>
            <el-progress :percentage="91" color="#67C23A" />
          </div>
        </el-col>
        <el-col :span="12">
          <div class="metric-card">
            <h4>精确率 (Precision)</h4>
            <div class="metric-value">0.8956</div>
            <el-progress :percentage="89" color="#409EFF" />
          </div>
        </el-col>
        <el-col :span="12">
          <div class="metric-card">
            <h4>召回率 (Recall)</h4>
            <div class="metric-value">0.8877</div>
            <el-progress :percentage="88" color="#E6A23C" />
          </div>
        </el-col>
        <el-col :span="12">
          <div class="metric-card">
            <h4>F1 分数 (F1 Score)</h4>
            <div class="metric-value">0.8916</div>
            <el-progress :percentage="89" color="#F56C6C" />
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
.metric-card { padding: 20px; background: #f9f9f9; border-radius: 8px; }
.metric-card h4 { margin: 0 0 12px; font-size: 14px; color: #666; }
.metric-value { font-size: 32px; font-weight: bold; color: #333; margin-bottom: 16px; }
</style>
