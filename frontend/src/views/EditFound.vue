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
          <el-input
            v-model="publishForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述物品特征"
          />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="publishForm.contactInfo" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { foundApi } from '@/api/found'

const router = useRouter()
const route = useRoute()
const publishFormRef = ref(null)
const loading = ref(false)
const foundId = ref(null)

const publishForm = reactive({
  itemName: '',
  foundPlace: '',
  foundTime: '',
  description: '',
  contactInfo: ''
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

.publish-card {
  margin-top: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style>
