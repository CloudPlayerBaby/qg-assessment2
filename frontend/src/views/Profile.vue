<template>
  <div class="profile-container">
    <el-row :gutter="24">
      <el-col :span="8">
        <el-card class="info-card">
          <div class="user-avatar">
            <el-avatar :size="120" :src="userInfo?.avatarUrl">
              {{ userInfo?.nickname?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="user-name">{{ userInfo?.nickname || userInfo?.username }}</div>
          <div class="user-role" v-if="userInfo?.identity === 1">管理员</div>
          <div class="user-role" v-else>普通用户</div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <el-form :model="profileForm" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" disabled />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phoneNumber" />
            </el-form-item>
            <el-form-item label="头像">
              <el-upload
                action="#"
                list-type="picture-card"
                :auto-upload="false"
                :limit="1"
                :show-file-list="false"
              >
                <el-avatar :size="80" :src="profileForm.avatarUrl">
                  <el-icon><Plus /></el-icon>
                </el-avatar>
              </el-upload>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="password-card" style="margin-top: 24px;">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>
          <el-form :model="passwordForm" label-width="100px">
            <el-form-item label="原密码">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const userInfo = ref(null)

const profileForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phoneNumber: '',
  avatarUrl: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(() => {
  const savedUserInfo = localStorage.getItem('userInfo')
  if (savedUserInfo) {
    userInfo.value = JSON.parse(savedUserInfo)
    Object.assign(profileForm, userInfo.value)
  }
})

const handleSave = () => {
  ElMessage.success('保存成功')
}

const handleChangePassword = () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次密码输入不一致')
    return
  }
  ElMessage.success('密码修改成功')
}
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
}

.info-card {
  text-align: center;
}

.user-avatar {
  margin-bottom: 16px;
}

.user-name {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.user-role {
  font-size: 14px;
  color: #909399;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
}
</style>
