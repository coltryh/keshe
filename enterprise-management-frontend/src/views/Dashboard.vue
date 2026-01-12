<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: item.color }">
              <el-icon :size="30"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/attendance')">
              <el-icon><Clock /></el-icon> 签到打卡
            </el-button>
            <el-button type="success" @click="$router.push('/leave')">
              <el-icon><Document /></el-icon> 请假申请
            </el-button>
            <el-button type="warning" @click="$router.push('/salary')">
              <el-icon><Money /></el-icon> 薪资查询
            </el-button>
            <el-button type="info" @click="$router.push('/ai')">
              <el-icon><MagicStick /></el-icon> AI 分析
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统公告</span>
          </template>
          <div class="notice-list">
            <div class="notice-item" v-for="i in 3" :key="i">
              <el-icon class="notice-icon"><Bell /></el-icon>
              <span>欢迎使用智能企业管理系统 v{{ i }}.0</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, Clock, Money, Document, MagicStick, Bell } from '@element-plus/icons-vue'

const stats = ref([
  { title: '员工总数', value: 0, icon: User, color: '#409eff' },
  { title: '今日出勤', value: 0, icon: Clock, color: '#67c23a' },
  { title: '待审批', value: 0, icon: Document, color: '#e6a23c' },
  { title: '本月薪资', value: '¥0', icon: Money, color: '#f56c6c' }
])

onMounted(() => {
  // 这里可以调用 API 获取实际数据
  stats.value[0].value = 15
  stats.value[1].value = 12
  stats.value[2].value = 3
  stats.value[3].value = '¥125,000'
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-title {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.quick-actions .el-button {
  width: 100%;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.notice-icon {
  color: #409eff;
}
</style>
