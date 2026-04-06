<template>
  <div class="publish-container">
    <el-card class="publish-card">
      <template #header>
        <div class="card-header">
          <span>发布信息</span>
        </div>
      </template>
      <el-form :model="publishForm" :rules="rules" ref="publishFormRef" label-width="100px">
        <el-form-item label="信息类型" prop="type">
          <el-radio-group v-model="publishForm.type">
            <el-radio label="lost">丢失物品</el-radio>
            <el-radio label="found">拾取物品</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="物品名称" prop="itemName">
          <el-input v-model="publishForm.itemName" placeholder="请输入物品名称" />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-select v-model="publishForm.location" placeholder="请选择地点" style="width: 100%;">
            <el-option label="教学楼" value="教学楼" />
            <el-option label="食堂" value="食堂" />
            <el-option label="图书馆" value="图书馆" />
            <el-option label="宿舍" value="宿舍" />
            <el-option label="操场" value="操场" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间" prop="time">
          <el-date-picker
            v-model="publishForm.time"
            type="datetime"
            placeholder="选择时间"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="物品描述" prop="description">
          <el-input
            v-model="publishForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述物品特征"
          />
        </el-form-item>
        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="publishForm.contact" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="publishForm.isTop" />
          <span class="tip">置顶需要管理员审核，置顶时间24小时</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">发布</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const publishFormRef = ref(null)
const loading = ref(false)

const publishForm = reactive({
  type: 'lost',
  itemName: '',
  location: '',
  time: '',
  description: '',
  contact: '',
  isTop: false
})

const rules = {
  type: [{ required: true, message: '请选择信息类型', trigger: 'change' }],
  itemName: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  location: [{ required: true, message: '请选择地点', trigger: 'change' }],
  time: [{ required: true, message: '请选择时间', trigger: 'change' }],
  description: [{ required: true, message: '请输入物品描述', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系方式', trigger: 'blur' }]
}

const handleSubmit = async () => {
  await publishFormRef.value.validate()
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('发布成功，等待审核')
    handleReset()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  publishFormRef.value?.resetFields()
}
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.tip {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}
</style>
