import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'
import Login from '@/views/login/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/home/index.vue'), meta: { title: '系统首页', icon: 'House' } },
    ]
  },
  {
    path: '/system',
    component: Layout,
    meta: { title: '平台管理', icon: 'Setting' },
    children: [
      { path: 'user', name: 'UserManage', component: () => import('@/views/system/userManage.vue'), meta: { title: '用户管理' } },
      { path: 'role', name: 'RoleManage', component: () => import('@/views/system/roleManage.vue'), meta: { title: '角色管理' } },
      { path: 'password', name: 'PasswordEdit', component: () => import('@/views/system/passwordEdit.vue'), meta: { title: '修改密码' } },
    ]
  },
  {
    path: '/kg',
    component: Layout,
    meta: { title: '图谱项目管理', icon: 'Connection' },
    children: [
      { path: 'graph', name: 'GraphManage', component: () => import('@/views/kgProject/graphManage.vue'), meta: { title: '知识图谱管理' } },
      { path: 'model', name: 'ModelManage', component: () => import('@/views/kgProject/modelManage.vue'), meta: { title: '图谱模型管理' } },
    ]
  },
  {
    path: '/data',
    component: Layout,
    meta: { title: '知识抽取与转化', icon: 'DataAnalysis' },
    redirect: '/data/transform',
    children: [
      { path: 'transform', name: 'StructTransform', component: () => import('@/views/dataTransform/structTransform.vue'), meta: { title: '（半）结构化数据转化' } },

      {
        path: 'kos',
        name: 'KosKnowledgeExtract',
        component: () => import('@/views/kosExtract/index.vue'),
        meta: { title: '基于KOS知识抽取' },
        redirect: '/data/kos/dl',
        children: [
          {
            path: 'dl',
            name: 'DlExtractMain',
            component: () => import('@/views/kosExtract/dlIndex.vue'),
            meta: { title: '深度学习知识抽取' },
            redirect: '/data/kos/dl/preprocess',
            children: [
              {
                path: 'preprocess',
                name: 'PreprocessMain',
                component: () => import('@/views/kosExtract/preprocessIndex.vue'),
                meta: { title: '数据标注与预处理' },
                redirect: '/data/kos/dl/preprocess/corpus',
                children: [
                  { path: 'corpus', name: 'CorpusManage', component: () => import('@/views/dlExtract/corpusManage.vue'), meta: { title: '语料管理' } },
                  { path: 'auth', name: 'AnnotationAuth', component: () => import('@/views/dlExtract/annotationAuth.vue'), meta: { title: '标注授权' } },
                  { path: 'annotate', name: 'DataAnnotate', component: () => import('@/views/dlExtract/dataAnnotate.vue'), meta: { title: '数据标注' } },
                  { path: 'manage', name: 'AnnotationManage', component: () => import('@/views/dlExtract/annotationManage.vue'), meta: { title: '标注管理' } },
                ]
              },
              {
                path: 'training',
                name: 'TrainingMain',
                component: () => import('@/views/kosExtract/trainingIndex.vue'),
                meta: { title: '模型训练与评估' },
                redirect: '/data/kos/dl/training/train',
                children: [
                  { path: 'train', name: 'ModelTrainManage', component: () => import('@/views/dlExtract/modelTrainManage.vue'), meta: { title: '模型训练管理' } },
                  { path: 'effect', name: 'ModelEffect', component: () => import('@/views/dlExtract/modelEffect.vue'), meta: { title: '训练效果' } },
                ]
              },
              { path: 'extract', name: 'KnowledgeExtract', component: () => import('@/views/dlExtract/extractIndex.vue'), meta: { title: '知识抽取' } },
            ]
          },
        ]
      },

      { path: 'llm', name: 'LlmKnowledgeExtract', component: () => import('@/views/llmExtract/index.vue'), meta: { title: '基于LLM知识抽取' } },

      { path: 'multimodal', name: 'MultimodalUpdate', component: () => import('@/views/multimodal/multimodalUpdate.vue'), meta: { title: '多模态数据更新' } },
    ]
  },

  {
    path: '/explore',
    component: Layout,
    meta: { title: '图谱实例管理', icon: 'Search' },
    children: [
      { path: 'graph', name: 'GraphExplore', component: () => import('@/views/graphExplore/index.vue'), meta: { title: '知识图谱探索' } },
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path !== '/login') {
    const token = localStorage.getItem('token')
    if (!token) {
      next('/login')
    } else if (to.path === '/system/role') {
      const roleCode = JSON.parse(localStorage.getItem('userInfo') || '{}').roleCode
      if (roleCode !== 'ADMIN') {
        next('/home')
      } else {
        next()
      }
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
