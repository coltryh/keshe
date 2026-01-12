<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">请假申请</h1>
    </div>

    <div class="card-container">
      <el-button type="primary" @click="handleApply">提交请假申请</el-button>

      <el-divider />

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="employeeName" label="员工姓名" />
        <el-table-column prop="leaveType" label="请假类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.leaveType === 'SICK'" type="danger">病假</el-tag>
            <el-tag v-else-if="row.leaveType === 'PERSONAL'" type="warning">事假</el-tag>
            <el-tag v-else type="info">年假</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="days" label="请假天数" width="100">
          <template #default="{ row }">
            {{ row.days }} 天
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="请假原因" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="warning">待审批</el-tag>
            <el-tag v-else-if="row.status === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING' && isAdmin"
              type="success"
              size="small"
              @click="handleApprove(row, 'APPROVED')"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 'PENDING' && isAdmin"
              type="danger"
              size="small"
              @click="handleApprove(row, 'REJECTED')"
            >
              拒绝
            </el-button>
            <span v-if="row.status !== 'PENDING'">-</span>
          </template>
        </el-table-column>
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

    <!-- 请假申请对话框 -->
    <el-dialog v-model="dialogVisible" title="提交请假申请" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="form.employeeId" placeholder="请选择员工" style="width: 100%">
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <el-option label="病假" value="SICK" />
            <el-option label="事假" value="PERSONAL" />
            <el-option label="年假" value="ANNUAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入请假原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { applyLeave, getLeaveRecords, approveLeave } from '@/api/leave'
import { getAllEmployees } from '@/api/employee'

const employees = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const isAdmin = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  return user.role === 'ADMIN'
})

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10
})

const tableData = ref([])
const total = ref(0)

const form = reactive({
  employeeId: null,
  leaveType: '',
  startTime: '',
  endTime: '',
  reason: ''
})

const rules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

const loadEmployees = async () => {
  try {
    const res = await getAllEmployees()
    employees.value = res.data
  } catch (error) {
    console.error('加载员工数据失败', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getLeaveRecords(searchForm)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('获取数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleApply = () => {
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    await applyLeave(form)
    ElMessage.success('申请提交成功')
    dialogVisible.value = false
    Object.assign(form, {
      employeeId: null,
      leaveType: '',
      startTime: '',
      endTime: '',
      reason: ''
    })
    fetchData()
  } catch (error) {
    console.error('提交申请失败', error)
  }
}

const handleApprove = (row, status) => {
  const approver = JSON.parse(localStorage.getItem('user') || '{}').username || 'admin'
  const comment = status === 'APPROVED' ? '同意' : '拒绝'
  approveLeave(row.id, {
    status,
    approver,
    comment
  }).then(() => {
    ElMessage.success('审批成功')
    fetchData()
  }).catch((error) => {
    console.error('审批失败', error)
  })
}

onMounted(() => {
  loadEmployees()
  fetchData()
})
</script>

<style scoped>
@import '@/assets/css/global.css';
</style>
