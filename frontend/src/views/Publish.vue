<template>
  <div class="publish-container">
    <el-card class="publish-card">
      <template #header>
        <div class="card-header">
          <span>发布信息</span>
        </div>
      </template>
      <el-tabs v-model="activeTab" class="publish-tabs">
        <el-tab-pane label="发布失物" name="lost">
          <el-form :model="lostForm" :rules="lostRules" ref="lostFormRef" label-width="100px">
            <el-form-item label="物品名称" prop="itemName">
              <el-input v-model="lostForm.itemName" placeholder="请输入物品名称" />
            </el-form-item>
            <el-form-item label="丢失地点" prop="lostPlace">
              <el-select v-model="lostForm.lostPlace" placeholder="请选择丢失地点" style="width: 100%;">
                <el-option label="教学楼" value="教学楼" />
                <el-option label="食堂" value="食堂" />
                <el-option label="图书馆" value="图书馆" />
                <el-option label="宿舍" value="宿舍" />
                <el-option label="操场" value="操场" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="丢失时间" prop="lostTime">
              <el-date-picker
                v-model="lostForm.lostTime"
                type="datetime"
                placeholder="选择丢失时间"
                style="width: 100%;"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
            <el-form-item label="物品描述" prop="description">
              <el-row :gutter="20" style="width: 100%;">
                <el-col :span="aiSuggestionText ? 12 : 24">
                  <el-input
                    v-model="lostForm.description"
                    type="textarea"
                    :rows="6"
                    placeholder="请详细描述物品特征"
                  />
                  <el-button 
                    type="success" 
                    size="small" 
                    link 
                    @click="polishDescription('lost')" 
                    :loading="polishing"
                    style="margin-top: 5px;"
                  >
                    <el-icon><MagicStick /></el-icon>
                    {{ hasPolished ? '重新描述' : (lostForm.imageUrl ? 'AI根据描述分析图片' : 'AI 润色描述') }}
                  </el-button>
                  <el-button 
                    type="primary" 
                    size="small" 
                    link 
                    @click="searchLostItem" 
                    :loading="searching"
                    style="margin-top: 5px; margin-left: 10px;"
                  >
                    <el-icon><Search /></el-icon>
                    AI搜索
                  </el-button>
                </el-col>
                <el-col v-if="aiSuggestionText || searchResultText" :span="12">
                  <div v-if="aiSuggestionText" class="ai-suggestion-box">
                    <div class="suggestion-header">
                      <span>AI 建议描述</span>
                      <el-button type="primary" size="small" @click="applySuggestion('lost')">采用建议</el-button>
                    </div>
                    <div class="suggestion-content">{{ aiSuggestionText }}</div>
                  </div>
                  <div v-if="searchResultText" class="ai-search-box">
                    <div class="suggestion-header">
                      <span>AI 搜索结果</span>
                    </div>
                    <div class="suggestion-content">{{ searchResultText }}</div>
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
                :file-list="lostFileList"
                :on-change="(file, list) => handleImageChange(file, list, 'lost')"
                :on-preview="handlePictureCardPreview"
                :on-remove="(file, list) => handleRemove(file, list, 'lost')"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="lostForm.applyTop">申请置顶</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit('lost')" :loading="loading" :disabled="uploadLoading">发布</el-button>
              <el-button @click="handleReset('lost')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="发布拾物" name="found">
          <el-form :model="foundForm" :rules="foundRules" ref="foundFormRef" label-width="100px">
            <el-form-item label="物品名称" prop="itemName">
              <el-input v-model="foundForm.itemName" placeholder="请输入物品名称" />
            </el-form-item>
            <el-form-item label="拾取地点" prop="foundPlace">
              <el-select v-model="foundForm.foundPlace" placeholder="请选择拾取地点" style="width: 100%;">
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
                v-model="foundForm.foundTime"
                type="datetime"
                placeholder="选择拾取时间"
                style="width: 100%;"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
            <el-form-item label="物品描述" prop="description">
              <el-row :gutter="20" style="width: 100%;">
                <el-col :span="aiSuggestionText || searchResultText ? 12 : 24">
                  <el-input
                    v-model="foundForm.description"
                    type="textarea"
                    :rows="6"
                    placeholder="请详细描述物品特征"
                  />
                  <el-button 
                    type="success" 
                    size="small" 
                    link 
                    @click="polishDescription('found')" 
                    :loading="polishing"
                    style="margin-top: 5px;"
                  >
                    <el-icon><MagicStick /></el-icon>
                    {{ hasPolished ? '重新描述' : (foundForm.imageUrl ? 'AI根据描述分析图片' : 'AI 润色描述') }}
                  </el-button>
                  <el-button 
                    type="primary" 
                    size="small" 
                    link 
                    @click="searchFoundItem" 
                    :loading="searchingFound"
                    style="margin-top: 5px; margin-left: 10px;"
                  >
                    <el-icon><Search /></el-icon>
                    AI搜索
                  </el-button>
                </el-col>
                <el-col v-if="aiSuggestionText || searchResultText" :span="12">
                  <div v-if="aiSuggestionText" class="ai-suggestion-box">
                    <div class="suggestion-header">
                      <span>AI 建议描述</span>
                      <el-button type="primary" size="small" @click="applySuggestion('found')">采用建议</el-button>
                    </div>
                    <div class="suggestion-content">{{ aiSuggestionText }}</div>
                  </div>
                  <div v-if="searchResultText" class="ai-search-box">
                    <div class="suggestion-header">
                      <span>AI 搜索结果</span>
                    </div>
                    <div class="suggestion-content">{{ searchResultText }}</div>
                  </div>
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item label="联系方式" prop="contactInfo">
              <el-input v-model="foundForm.contactInfo" placeholder="请输入联系方式" />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="foundForm.applyTop">申请置顶</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit('found')" :loading="loading" :disabled="uploadLoading">发布</el-button>
              <el-button @click="handleReset('found')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { lostApi } from '@/api/lost'
import { foundApi } from '@/api/found'
import { fileApi } from '@/api/file'
import { aiApi } from '@/api/ai'
import { Plus, MagicStick, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

const activeTab = ref('lost')
const lostFormRef = ref(null)
const foundFormRef = ref(null)
const loading = ref(false)
const polishing = ref(false)
const searching = ref(false)
const searchingFound = ref(false)
const uploadLoading = ref(false)
const aiSuggestionText = ref('')
const searchResultText = ref('')
const hasPolished = ref(false)

const applySuggestion = (type) => {
  const form = type === 'lost' ? lostForm : foundForm
  form.description = aiSuggestionText.value
  aiSuggestionText.value = ''
  ElMessage.success('已采用 AI 建议描述')
}

const polishDescription = async (type) => {
  const form = type === 'lost' ? lostForm : foundForm
  if (!form.description.trim()) {
    ElMessage.warning('请先输入一些描述内容')
    return
  }
  
  // 检查AI使用限制
  try {
    const limitRes = await aiApi.checkLimit()
    if (!limitRes.data) {
      ElMessage.warning('您今天的AI使用次数已达上限（20次），请明天再试')
      return
    }
  } catch (error) {
    console.error('检查AI限制失败:', error)
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
        description: form.description,
        imageUrl: form.imageUrl,
        type: type
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

const searchLostItem = async () => {
  if (!lostForm.description.trim()) {
    ElMessage.warning('请先输入一些描述内容')
    return
  }
  
  // 检查AI使用限制
  try {
    const limitRes = await aiApi.checkLimit()
    if (!limitRes.data) {
      ElMessage.warning('您今天的AI使用次数已达上限（20次），请明天再试')
      return
    }
  } catch (error) {
    console.error('检查AI限制失败:', error)
  }
  
  searching.value = true
  searchResultText.value = ''
  aiSuggestionText.value = ''
  
  try {
    const response = await fetch('/api/ai/search/found', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      },
      body: JSON.stringify({
        description: lostForm.description,
        type: 'lost'
      })
    })

    if (!response.ok) {
      const errorText = await response.text()
      let errorMessage = 'AI 搜索请求失败'
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
        throw new Error(errorData.message || 'AI 搜索失败')
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
            searchResultText.value += content
          }
        } else if (line.trim() && !line.startsWith(':')) {
          searchResultText.value += line
        }
      }
    }
    
    ElMessage.success('搜索完成')
  } catch (error) {
    console.error('AI 搜索失败:', error)
    ElMessage.error('搜索失败，请重试')
  } finally {
    searching.value = false
  }
}

const searchFoundItem = async () => {
  if (!foundForm.description.trim()) {
    ElMessage.warning('请先输入一些描述内容')
    return
  }
  
  // 检查AI使用限制
  try {
    const limitRes = await aiApi.checkLimit()
    if (!limitRes.data) {
      ElMessage.warning('您今天的AI使用次数已达上限（20次），请明天再试')
      return
    }
  } catch (error) {
    console.error('检查AI限制失败:', error)
  }
  
  searchingFound.value = true
  searchResultText.value = ''
  aiSuggestionText.value = ''
  
  try {
    const response = await fetch('/api/ai/search/lost', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      },
      body: JSON.stringify({
        description: foundForm.description,
        type: 'found'
      })
    })

    if (!response.ok) {
      const errorText = await response.text()
      let errorMessage = 'AI 搜索请求失败'
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
        throw new Error(errorData.message || 'AI 搜索失败')
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
            searchResultText.value += content
          }
        } else if (line.trim() && !line.startsWith(':')) {
          searchResultText.value += line
        }
      }
    }
    
    ElMessage.success('搜索完成')
  } catch (error) {
    console.error('AI 搜索失败:', error)
    ElMessage.error('搜索失败，请重试')
  } finally {
    searchingFound.value = false
  }
}

const previewVisible = ref(false)
const previewImage = ref('')
const lostFileList = ref([])
const foundFileList = ref([])

const lostForm = reactive({
  itemName: '',
  lostPlace: '',
  lostTime: '',
  description: '',
  imageUrl: '',
  applyTop: false
})

const foundForm = reactive({
  itemName: '',
  foundPlace: '',
  foundTime: '',
  description: '',
  contactInfo: '',
  applyTop: false
})

const lostRules = {
  itemName: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  lostPlace: [{ required: true, message: '请选择丢失地点', trigger: 'change' }],
  lostTime: [{ required: true, message: '请选择丢失时间', trigger: 'change' }],
  description: [{ required: true, message: '请输入物品描述', trigger: 'blur' }]
}

const foundRules = {
  itemName: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  foundPlace: [{ required: true, message: '请选择拾取地点', trigger: 'change' }],
  foundTime: [{ required: true, message: '请选择拾取时间', trigger: 'change' }],
  description: [{ required: true, message: '请输入物品描述', trigger: 'blur' }],
  contactInfo: [{ required: true, message: '请输入联系方式', trigger: 'blur' }]
}

const handleImageChange = async (file, uploadFileList, type) => {
  if (file.raw) {
    uploadLoading.value = true
    try {
      const res = await fileApi.upload(file.raw)
      if (type === 'lost') {
        lostForm.imageUrl = res.data
        lostFileList.value = uploadFileList.map(f => {
          if (f.uid === file.uid) {
            return { ...f, url: res.data }
          }
          return f
        })
      }
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
  previewImage.value = file.url || lostForm.imageUrl
  previewVisible.value = true
}

const handleRemove = (file, uploadFileList, type) => {
  if (type === 'lost') {
    lostForm.imageUrl = ''
    lostFileList.value = uploadFileList
  }
}

const handleSubmit = async (type) => {
  const formRef = type === 'lost' ? lostFormRef.value : foundFormRef.value
  const formData = type === 'lost' ? lostForm : foundForm

  if (!formRef) return

  await formRef.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      try {
        const data = { ...formData }
        data.applyTop = data.applyTop ? 1 : 0
        
        if (type === 'lost') {
          await lostApi.postLostItem(data)
        } else {
          await foundApi.postFoundItem(data)
        }
        ElMessage.success('发布成功')
        handleReset(type)
      } catch (error) {
        console.error('发布失败:', error)
        if (error.response && error.response.data && error.response.data.message) {
          ElMessage.error(error.response.data.message)
        } else {
          ElMessage.error('发布失败')
        }
      } finally {
        loading.value = false
      }
    } else {
      console.log('表单验证失败:', fields)
    }
  })
}

const handleReset = (type) => {
  const formRef = type === 'lost' ? lostFormRef.value : foundFormRef.value
  if (formRef) {
    formRef.resetFields()
  }
  aiSuggestionText.value = ''
  searchResultText.value = ''
  hasPolished.value = false
  if (type === 'lost') {
    lostForm.imageUrl = ''
    lostForm.applyTop = false
    lostFileList.value = []
  } else {
    foundForm.applyTop = false
  }
}
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;
}

.publish-card {
  margin-top: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.publish-tabs {
  margin-top: 10px;
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

.ai-search-box {
  background-color: #ecf5ff;
  border: 1px solid #c6e2ff;
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

.ai-search-box .suggestion-header {
  color: #409eff;
}

.suggestion-content {
  flex: 1;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
