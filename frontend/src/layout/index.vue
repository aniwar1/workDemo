<template>
  <div class="layout-container">
    <!-- Sidebar -->
    <div class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <el-icon :size="28"><Connection /></el-icon>
        <span v-if="!isCollapsed">KG Platform</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :router="true"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/home">
          <el-icon><House /></el-icon>
          <template #title>系统首页</template>
        </el-menu-item>

        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>平台管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/password">修改密码</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/kg">
          <template #title>
            <el-icon><Connection /></el-icon>
            <span>图谱项目管理</span>
          </template>
          <el-menu-item index="/kg/graph">知识图谱管理</el-menu-item>
          <el-menu-item index="/kg/model">图谱模型管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/data">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>知识抽取与转化</span>
          </template>

          <el-menu-item index="/data/transform">（半）结构化数据转化</el-menu-item>
          <el-menu-item index="/data/kos">基于KOS知识抽取</el-menu-item>
          <el-sub-menu index="/data/dl">
            <template #title>深度学习知识抽取</template>
            <el-sub-menu index="/data/dl/preprocess">
              <template #title>数据标注与预处理</template>
              <el-menu-item index="/data/dl/preprocess/corpus">语料管理</el-menu-item>
              <el-menu-item index="/data/dl/preprocess/auth">标注授权</el-menu-item>
              <el-menu-item index="/data/dl/preprocess/annotate">数据标注</el-menu-item>
              <el-menu-item index="/data/dl/preprocess/manage">标注管理</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="/data/dl/training">
              <template #title>模型训练与评估</template>
              <el-menu-item index="/data/dl/training/train">模型训练管理</el-menu-item>
              <el-menu-item index="/data/dl/training/effect">训练效果</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/data/dl/extract">知识抽取</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/data/llm">基于LLM知识抽取</el-menu-item>
          <el-menu-item index="/data/multimodal">多模态数据更新</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/explore">
          <template #title>
            <el-icon><Search /></el-icon>
            <span>图谱实例管理</span>
          </template>
          <el-menu-item index="/explore/graph">知识图谱探索</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>

    <!-- Main area -->
    <div class="main-container">
      <!-- Header -->
      <div class="header">
        <div class="header-left">
          <el-button text @click="toggleSidebar">
            <el-icon :size="20"><Fold /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title">{{ currentRoute.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || 'Admin' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- Content -->
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapsed = ref(false)
const currentRoute = computed(() => route)

const activeMenu = computed(() => route.path)
const isAdmin = computed(() => userStore.isAdmin)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示')
    await logout().catch(() => {})
    userStore.logout()
    router.push('/login')
  } else if (command === 'password') {
    router.push('/system/password')
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  background: #304156;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #3d4a5c;
}

.sidebar :deep(.el-menu) {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #e6e6e6;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-info:hover {
  background: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #333;
}

.content {
  flex: 1;
  overflow: auto;
  padding: 16px;
}
</style>
