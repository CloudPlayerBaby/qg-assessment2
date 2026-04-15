<template>
  <div class="publish-container">
    <el-card class="publish-card">
      <template #header>
        <div class="card-header">
          <span>编辑拾物信息</span>
        </div>
      </template>
      <el-form :model="publishForm" :rules="rules" ref="publishFormRef" label-width="100px">
        <el-form-item label="物品名称" prop="itemName">
          <el-input v-model="publishForm.itemName" placeholder="请输入物品名称" />
        </el-form-item>
        <el-form-item label="拾取地点" prop="foundPlace">
          <el-select v-model="publishForm.foundPlace" placeholder="请选择拾取地点" style="width: 100%;">
            <el-option label="教学楼" value="教学楼" />
            <el-option label="食堂" value="食堂" />
            <el-option label="图书馆" value="图书馆" />
            <el-option label="宿舍" value="宿舍" />
            <el-option label="操场" value="操场" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="拾取时间" prop="foundTime">
          <el-date-picker
            v-model="publishForm.foundTime"
            type="datetime"
            placeholder="选择拾取时间"
            style="width: 100%;"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="物品描述" prop="description">
          <el-row :gutter="20" style="width: 100%;">
            <el-col :span="aiSuggestionText ? 12 : 24">
              <el-input
                v-model="publishForm.description"
                type="textarea"
                :rows="6"
                placeholder="请详细描述物品特征"
              />
              <el-button 
                type="success" 
                size="small" 
                link 
                @click="polishDescription" 
                :loading="polishing"
                style="margin-top: 5px;"
              >
                <el-icon><MagicStick /></el-icon>
                {{ hasPolished ? '重新描述' : (publishForm.imageUrl ? 'AI根据描述分析图片' : 'AI 润色描述') }}
              </el-button>
            </el-col>
            <el-col v-if="aiSuggestionText" :span="12">
              <div class="ai-suggestion-box">
                <div class="suggestion-header">
                  <span>AI 建议描述</span>
                  <el-button type="primary" size="small" @click="applySuggestion">采用建议</el-button>
                </div>
                <div class="suggestion-content">{{ aiSuggestionText }}</div>
              </div>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="1"
            :show-file-list="true"
            :file-list="fileList"
            :on-change="handleImageChange"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="publishForm.contactInfo" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading" :disabled="uploadLoading">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="[previewImage]"
      @close="previewVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { foundApi } from '@/api/found'
import { fileApi } from '@/api/file'
import { Plus, MagicStick } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const publishFormRef = ref(null)
const loading = ref(false)
const polishing = ref(false)
const foundId = ref(null)
const aiSuggestionText = ref('')
const hasPolished = ref(false)
const uploadLoading = ref(false)
const fileList = ref([])
const previewVisible = ref(false)
const previewImage = ref('')

const applySuggestion = () => {
  publishForm.description = aiSuggestionText.value
  aiSuggestionText.value = ''
  ElMessage.success('已采用 AI 建议描述')
}

const polishDescription = async () => {
  if (!publishForm.description.trim()) {
    ElMessage.warning('请先输入一些描述内容')
    return
  }
  
  polishing.value = true
  aiSuggestionText.value = ''
  
  try {
    const response = await fetch('/api/ai/polish', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      },
      body: JSON.stringify({
        description: publishForm.description,
        imageUrl: publishForm.imageUrl,
        type: 'found'
      })
    })

    if (!response.ok) {
      const errorText = await response.text()
      let errorMessage = 'AI 润色请求失败'
      try {
        const errorData = JSON.parse(errorText)
        errorMessage = errorData.message || errorMessage
      } catch (e) {
        errorMessage = errorText || errorMessage
      }
      throw new Error(errorMessage)
    }

    // 检查响应类型，如果是 JSON 报错（即使状态码是 200）
    const contentType = response.headers.get('Content-Type')
    if (contentType && contentType.includes('application/json')) {
      const errorData = await response.json()
      if (errorData.code !== 200) {
        throw new Error(errorData.message || 'AI 润色失败')
      }
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value, { stream: true })
      const lines = chunk.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const content = line.replace('data:', '').trim()
          if (content) {
            aiSuggestionText.value += content
          }
        } else if (line.trim() && !line.startsWith(':')) {
          aiSuggestionText.value += line
        }
      }
    }
    
    ElMessage.success('润色建议已生成')
    hasPolished.value = true
  } catch (error) {
    console.error('AI 润色失败:', error)
    ElMessage.error('润色失败，请重试')
  } finally {
    polishing.value = false
  }
}

const handleImageChange = async (file, uploadFileList) => {
  if (file.raw) {
    uploadLoading.value = true
    try {
      const res = await fileApi.upload(file.raw)
      publishForm.imageUrl = res.data
      fileList.value = uploadFileList.map(f => {
        if (f.uid === file.uid) {
          return { ...f, url: res.data }
        }
        return f
      })
      ElMessage.success('图片上传成功')
    } catch (error) {
      console.error(error)
      ElMessage.error('图片上传失败')
    } finally {
      uploadLoading.value = false
    }
  }
}

const handlePictureCardPreview = (file) => {
  previewImage.value = file.url || publishForm.imageUrl
  previewVisible.value = true
}

const handleRemove = (file, uploadFileList) => {
  publishForm.imageUrl = ''
  fileList.value = uploadFileList
}

const publishForm = reactive({
  itemName: '',
  foundPlace: '',
  foundTime: '',
  description: '',
  contactInfo: '',
  imageUrl: ''
})

const rules = {
  itemName: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  foundPlace: [{ required: true, message: '请选择拾取地点', trigger: 'change' }],
  foundTime: [{ required: true, message: '请选择拾取时间', trigger: 'change' }],
  description: [{ required: true, message: '请输入物品描述', trigger: 'blur' }],
  contactInfo: [{ required: true, message: '请输入联系方式', trigger: 'blur' }]
}

const loadFoundItem = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('参数错误')
    router.back()
    return
  }
  foundId.value = id
  loading.value = true
  try {
    const res = await foundApi.getFoundItemDetail(id)
    Object.assign(publishForm, res.data)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取拾物信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!publishFormRef.value) return

  await publishFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      try {
        await foundApi.updateFoundItem(foundId.value, publishForm)
        ElMessage.success('保存成功')
        router.back()
      } catch (error) {
        console.error('保存失败:', error)
        if (error.response && error.response.data && error.response.data.message) {
          ElMessage.error(error.response.data.message)
        } else {
          ElMessage.error('保存失败')
        }
      } finally {
        loading.value = false
      }
    } else {
      console.log('表单验证失败:', fields)
    }
  })
}

const handleCancel = () => {
  router.back()
}

onMounted(() => {
  loadFoundItem()
})
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;
}

.ai-suggestion-box {
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 8px;
  padding: 15px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: #67c23a;
  font-weight: bold;
  font-size: 14px;
}

.suggestion-content {
  flex: 1;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.publish-card {
  margin-top: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style>
