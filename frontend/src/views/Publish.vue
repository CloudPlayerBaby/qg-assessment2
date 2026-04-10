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
              <el-input
                v-model="lostForm.description"
                type="textarea"
                :rows="4"
                placeholder="请详细描述物品特征"
              />
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
              <el-button type="primary" @click="handleSubmit('lost')" :loading="loading">发布</el-button>
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
              <el-input
                v-model="foundForm.description"
                type="textarea"
                :rows="4"
                placeholder="请详细描述物品特征"
              />
            </el-form-item>
            <el-form-item label="联系方式" prop="contactInfo">
              <el-input v-model="foundForm.contactInfo" placeholder="请输入联系方式" />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="foundForm.applyTop">申请置顶</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit('found')" :loading="loading">发布</el-button>
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

const activeTab = ref('lost')
const lostFormRef = ref(null)
const foundFormRef = ref(null)
const loading = ref(false)
const uploadLoading = ref(false)
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
</style>
