<template>
  <div class="publish-container">
    <el-card class="publish-card">
      <template #header>
        <div class="card-header">
          <span>编辑失物信息</span>
        </div>
      </template>
      <el-form :model="publishForm" :rules="rules" ref="publishFormRef" label-width="100px">
        <el-form-item label="物品名称" prop="itemName">
          <el-input v-model="publishForm.itemName" placeholder="请输入物品名称" />
        </el-form-item>
        <el-form-item label="丢失地点" prop="lostPlace">
          <el-select v-model="publishForm.lostPlace" placeholder="请选择丢失地点" style="width: 100%;">
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
            v-model="publishForm.lostTime"
            type="datetime"
            placeholder="选择丢失时间"
            style="width: 100%;"
            value-format="YYYY-MM-DDTHH:mm:ss"
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
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">保存</el-button>
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
import { lostApi } from '@/api/lost'
import { fileApi } from '@/api/file'

const router = useRouter()
const route = useRoute()
const publishFormRef = ref(null)
const loading = ref(false)
const uploadLoading = ref(false)
const previewVisible = ref(false)
const previewImage = ref('')
const fileList = ref([])
const lostId = ref(null)

const publishForm = reactive({
  itemName: '',
  lostPlace: '',
  lostTime: '',
  description: '',
  imageUrl: ''
})

const rules = {
  itemName: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  lostPlace: [{ required: true, message: '请选择丢失地点', trigger: 'change' }],
  lostTime: [{ required: true, message: '请选择丢失时间', trigger: 'change' }],
  description: [{ required: true, message: '请输入物品描述', trigger: 'blur' }]
}

const loadLostItem = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('参数错误')
    router.back()
    return
  }
  lostId.value = id
  loading.value = true
  try {
    const res = await lostApi.getLostItemDetail(id)
    Object.assign(publishForm, res.data)
    if (res.data.imageUrl) {
      fileList.value = [{
        name: 'image',
        url: res.data.imageUrl
      }]
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取失物信息失败')
    router.back()
  } finally {
    loading.value = false
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

const handleSubmit = async () => {
  if (!publishFormRef.value) return

  await publishFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      try {
        await lostApi.updateLostItem(lostId.value, publishForm)
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
  loadLostItem()
})
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
</style>
