<template>
  <div class="login-container">
    <canvas ref="canvasRef" class="bg-canvas" />
    <div class="login-box">
      <div class="login-header">
        <el-icon :size="40" color="#00f0ff"><Connection /></el-icon>
        <h2>知识图谱管理平台</h2>
        <p>Collaborative Knowledge Graph Construction Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
        <div class="login-tip">
          <span>默认账号: admin / admin123</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const canvasRef = ref(null)

const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form)
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

// --- Particle network background ---
let animId = null
let particles = []

function resize() {
  if (!canvasRef.value) return
  canvasRef.value.width = window.innerWidth
  canvasRef.value.height = window.innerHeight
}

function createParticle() {
  return {
    x: Math.random() * canvasRef.value.width,
    y: Math.random() * canvasRef.value.height,
    vx: (Math.random() - 0.5) * 0.6,
    vy: (Math.random() - 0.5) * 0.6,
    radius: Math.random() * 2 + 1,
    opacity: Math.random() * 0.5 + 0.2
  }
}

function drawFrame() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const w = canvas.width
  const h = canvas.height

  ctx.clearRect(0, 0, w, h)

  // Deep space gradient background
  const bg = ctx.createRadialGradient(w * 0.5, h * 0.4, 0, w * 0.5, h * 0.4, Math.max(w, h) * 0.8)
  bg.addColorStop(0, '#0a0e27')
  bg.addColorStop(0.4, '#0d1440')
  bg.addColorStop(1, '#050820')
  ctx.fillStyle = bg
  ctx.fillRect(0, 0, w, h)

  // Grid lines (subtle tech grid)
  ctx.strokeStyle = 'rgba(0, 240, 255, 0.04)'
  ctx.lineWidth = 1
  const gridSize = 60
  for (let x = 0; x <= w; x += gridSize) {
    ctx.beginPath()
    ctx.moveTo(x, 0)
    ctx.lineTo(x, h)
    ctx.stroke()
  }
  for (let y = 0; y <= h; y += gridSize) {
    ctx.beginPath()
    ctx.moveTo(0, y)
    ctx.lineTo(w, y)
    ctx.stroke()
  }

  // Sweeping scan line
  const scanY = (Date.now() * 0.08) % (h + 100) - 50
  const scanGrad = ctx.createLinearGradient(0, scanY - 30, 0, scanY + 30)
  scanGrad.addColorStop(0, 'rgba(0, 240, 255, 0)')
  scanGrad.addColorStop(0.5, 'rgba(0, 240, 255, 0.06)')
  scanGrad.addColorStop(1, 'rgba(0, 240, 255, 0)')
  ctx.fillStyle = scanGrad
  ctx.fillRect(0, scanY - 30, w, 60)

  // Connect nearby particles
  const maxDist = 140
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < maxDist) {
        const alpha = (1 - dist / maxDist) * 0.25
        ctx.strokeStyle = `rgba(0, 240, 255, ${alpha})`
        ctx.lineWidth = 0.6
        ctx.beginPath()
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.stroke()
      }
    }
  }

  // Draw particles
  particles.forEach(p => {
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
    const hue = 185 + Math.sin(Date.now() * 0.001 + p.x) * 15
    ctx.fillStyle = `hsla(${hue}, 100%, 65%, ${p.opacity})`
    ctx.fill()

    // Glow
    const glow = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.radius * 4)
    glow.addColorStop(0, `hsla(${hue}, 100%, 65%, ${p.opacity * 0.3})`)
    glow.addColorStop(1, 'transparent')
    ctx.fillStyle = glow
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius * 4, 0, Math.PI * 2)
    ctx.fill()

    // Move
    p.x += p.vx
    p.y += p.vy
    if (p.x < 0 || p.x > w) p.vx *= -1
    if (p.y < 0 || p.y > h) p.vy *= -1
  })

  animId = requestAnimationFrame(drawFrame)
}

function init() {
  resize()
  const count = Math.min(Math.floor(window.innerWidth * window.innerHeight / 12000), 100)
  particles = Array.from({ length: count }, createParticle)
  drawFrame()
}

onMounted(() => {
  init()
  window.addEventListener('resize', () => { resize() })
})

onUnmounted(() => {
  if (animId) cancelAnimationFrame(animId)
  window.removeEventListener('resize', resize)
})
</script>

<style scoped>
.login-container {
  position: relative;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.bg-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.login-box {
  position: relative;
  z-index: 10;
  width: 400px;
  background: rgba(10, 14, 39, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 240, 255, 0.15);
  border-radius: 16px;
  padding: 40px;
  box-shadow:
    0 0 40px rgba(0, 240, 255, 0.08),
    0 20px 60px rgba(0, 0, 0, 0.5);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header :deep(.el-icon) {
  filter: drop-shadow(0 0 12px rgba(0, 240, 255, 0.6));
}

.login-header h2 {
  margin: 12px 0 4px;
  font-size: 22px;
  color: #e0f7ff;
  letter-spacing: 2px;
}

.login-header p {
  font-size: 12px;
  color: rgba(0, 240, 255, 0.6);
  letter-spacing: 1px;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(0, 240, 255, 0.04);
  border: 1px solid rgba(0, 240, 255, 0.15);
  box-shadow: none;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover),
.login-form :deep(.el-input__wrapper.is-focus) {
  background: rgba(0, 240, 255, 0.08);
  border-color: rgba(0, 240, 255, 0.4);
  box-shadow: 0 0 16px rgba(0, 240, 255, 0.15);
}

.login-form :deep(.el-input__inner) {
  color: #e0f7ff;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(0, 240, 255, 0.35);
}

.login-form :deep(.el-form-item__error) {
  color: #ff6b6b;
}

.login-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, rgba(0, 200, 255, 0.8), rgba(0, 100, 255, 0.8));
  border: 1px solid rgba(0, 240, 255, 0.3);
  font-weight: 600;
  letter-spacing: 4px;
  transition: all 0.3s;
}

.login-form :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, rgba(0, 220, 255, 0.95), rgba(0, 130, 255, 0.95));
  box-shadow: 0 0 24px rgba(0, 200, 255, 0.35);
  transform: translateY(-1px);
}

.login-tip {
  text-align: center;
  font-size: 12px;
  color: rgba(0, 240, 255, 0.4);
  margin-top: -10px;
  letter-spacing: 0.5px;
}
</style>
