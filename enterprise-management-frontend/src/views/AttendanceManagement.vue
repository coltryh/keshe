<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">考勤管理</h1>
    </div>

    <div class="card-container">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="checkin-card">
              <div class="checkin-title">签到打卡</div>
              <div class="checkin-time">{{ currentTime }}</div>
              <el-button type="primary" size="large" @click="handleCheckin" :disabled="checkedIn">
                {{ checkedIn ? '已签到' : '签到' }}
              </el-button>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card shadow="hover">
            <div class="checkin-card">
              <div class="checkin-title">签退打卡</div>
              <div class="checkin-time">{{ currentTime }}</div>
              <el-button type="warning" size="large" @click="handleCheckout" :disabled="!checkedIn || checkedOut">
                {{ checkedOut ? '已签退' : '签退' }}
              </el-button>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-title">本月考勤统计</div>
              <div class="stat-item">
                <span>出勤天数：</span>
                <span class="stat-value">{{ statistics.normalCount || 0 }} 天</span>
              </div>
              <div class="stat-item">
                <span>迟到次数：</span>
                <span class="stat-value warning">{{ statistics.lateCount || 0 }} 次</span>
              </div>
              <div class="stat-item">
                <span>缺勤次数：</span>
                <span class="stat-value danger">{{ statistics.absenceCount || 0 }} 次</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-divider />

      <div class="search-form">
        <el-form :inline="true">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="请输入员工姓名" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchData">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="employeeName" label="员工姓名" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="checkinTime" label="签到时间" width="180" />
        <el-table-column prop="checkoutTime" label="签退时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'NORMAL'" type="success">正常</el-tag>
            <el-tag v-else-if="row.status === 'LATE'" type="warning">迟到</el-tag>
            <el-tag v-else-if="row.status === 'EARLY_LEAVE'" type="warning">早退</el-tag>
            <el-tag v-else type="danger">缺勤</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workHours" label="工作时长" width="120" />
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { checkin, checkout, getAttendanceRecords, getAttendanceStatistics } from '@/api/attendance'
import { getAllEmployees } from '@/api/employee'

const currentTime = ref('')
const checkedIn = ref(false)
const checkedOut = ref(false)
const statistics = ref({})

const searchForm = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 10
})

const tableData = ref([])
const total = ref(0)
const currentEmployee = ref(null)

let timer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

const fetchData = async () => {
  try {
    const res = await getAttendanceRecords(searchForm)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('获取数据失败', error)
  }
}

const fetchStatistics = async () => {
  try {
    if (currentEmployee.value) {
      const month = new Date().toISOString().slice(0, 7)
      const res = await getAttendanceStatistics({
        employeeId: currentEmployee.value.id,
        month
      })
      statistics.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

const loadEmployees = async () => {
  try {
    const res = await getAllEmployees()
    if (res.data && res.data.length > 0) {
      currentEmployee.value = res.data[0]
      await fetchStatistics()
    }
  } catch (error) {
    console.error('加载员工数据失败', error)
  }
}

const handleCheckin = async () => {
  if (!currentEmployee.value) {
    ElMessage.warning('请先选择员工')
    return
  }
  try {
    await checkin({
      employeeId: currentEmployee.value.id,
      employeeName: currentEmployee.value.name
    })
    ElMessage.success('签到成功')
    checkedIn.value = true
    fetchData()
    fetchStatistics()
  } catch (error) {
    console.error('签到失败', error)
  }
}

const handleCheckout = async () => {
  if (!currentEmployee.value) {
    ElMessage.warning('请先选择员工')
    return
  }
  try {
    await checkout({
      employeeId: currentEmployee.value.id
    })
    ElMessage.success('签退成功')
    checkedOut.value = true
    fetchData()
    fetchStatistics()
  } catch (error) {
    console.error('签退失败', error)
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  fetchData()
  loadEmployees()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
@import '@/assets/css/global.css';

.checkin-card {
  text-align: center;
  padding: 20px 0;
}

.checkin-title {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
}

.checkin-time {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 20px;
}

.stat-card {
  padding: 10px;
}

.stat-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-value {
  font-weight: bold;
  color: #67c23a;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.danger {
  color: #f56c6c;
}
</style>
