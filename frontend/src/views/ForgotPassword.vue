<template>
  <div class="forgot-container">
    <div class="forgot-box">
      <div class="forgot-header">
        <h1>校园AI失物招领平台</h1>
        <p>重置密码</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" class="forgot-form">
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入注册邮箱"
            size="large"
            prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="form.captcha"
              placeholder="请输入验证码"
              size="large"
              prefix-icon="Key"
              class="captcha-input"
            />
            <el-button
              type="primary"
              size="large"
              native-type="button"
              :disabled="countdown > 0 || !form.email"
              :loading="sendingCode"
              @click="handleSendCode"
              class="captcha-btn"
            >
              {{ countdown > 0 ? `${countdown}s后重发` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleReset"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="reset-btn" native-type="button" @click="handleReset" :loading="loading">
            重置密码
          </el-button>
        </el-form-item>
      </el-form>
      <div class="forgot-footer">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
const sendingCode = ref(false)
let timer = null

const form = reactive({
  email: '',
  captcha: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSendCode = async () => {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  sendingCode.value = true
  try {
    await userApi.sendVerificationCode(form.email)
    ElMessage.success('验证码已发送，请查收邮件')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error(error)
  } finally {
    sendingCode.value = false
  }
}

const handleReset = async () => {
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  loading.value = true
  try {
    await userApi.resetPassword(form)
    ElMessage.success('密码重置成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.forgot-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.forgot-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.forgot-header {
  text-align: center;
  margin-bottom: 30px;
}

.forgot-header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.forgot-header p {
  font-size: 14px;
  color: #999;
}

.forgot-form {
  margin-top: 30px;
}

.reset-btn {
  width: 100%;
}

.forgot-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.forgot-footer a {
  color: #667eea;
  text-decoration: none;
}

.forgot-footer a:hover {
  text-decoration: underline;
}

.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.captcha-input {
  flex: 1;
}

.captcha-btn {
  width: 120px;
  flex-shrink: 0;
}
</style>
