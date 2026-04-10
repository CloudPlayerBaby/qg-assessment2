<template>
  <el-container class="admin-container">
    <el-aside width="220px" class="admin-aside">
      <div class="admin-logo">
        <el-icon :size="24"><Setting /></el-icon>
        <span>管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="admin-menu"
      >
        <el-menu-item index="/admin/user-manage">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/post-manage">
          <el-icon><Document /></el-icon>
          <span>信息管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/top-manage">
          <el-icon><Document /></el-icon>
          <span>置顶审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/report">
          <el-icon><Warning /></el-icon>
          <span>举报管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/statistics">
          <el-icon><DataLine /></el-icon>
          <span>平台统计</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="admin-main-container">
      <el-header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>管理后台</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <router-link to="/">返回前台</router-link>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Warning, User, Document } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.admin-container {
  height: calc(100vh - 64px);
}

.admin-aside {
  background-color: #304156;
  color: white;
}

.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
}

.admin-menu {
  border: none;
  background-color: #304156;
}

.admin-menu:not(.el-menu--collapse) {
  width: 220px;
}

.admin-menu :deep(.el-menu-item) {
  color: #bfcbd9;
}

.admin-menu :deep(.el-menu-item.is-active) {
  color: #409eff;
  background-color: #263445;
}

.admin-menu :deep(.el-menu-item:hover) {
  background-color: #263445;
}

.admin-menu :deep(.el-icon) {
  color: inherit;
}

.admin-main-container {
  background-color: #f0f2f5;
}

.admin-header {
  background-color: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

.header-right a {
  color: #667eea;
  text-decoration: none;
}

.admin-main {
  padding: 24px;
}
</style>
