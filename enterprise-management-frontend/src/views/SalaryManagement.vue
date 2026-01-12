<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">薪资管理</h1>
    </div>

    <div class="card-container">
      <el-form :inline="true" class="search-form">
        <el-form-item label="选择员工">
          <el-select v-model="selectedEmployeeId" placeholder="请选择员工" style="width: 200px">
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="月份">
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCalculate">计算薪资</el-button>
          <el-button type="success" @click="handleQuery">查询薪资</el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="employeeName" label="员工姓名" />
        <el-table-column prop="month" label="月份" width="120" />
        <el-table-column prop="baseSalary" label="基本工资" width="120">
          <template #default="{ row }">
            ¥{{ row.baseSalary }}
          </template>
        </el-table-column>
        <el-table-column prop="performanceSalary" label="绩效工资" width="120">
          <template #default="{ row }">
            ¥{{ row.performanceSalary }}
          </template>
        </el-table-column>
        <el-table-column prop="bonus" label="奖金" width="100">
          <template #default="{ row }">
            ¥{{ row.bonus }}
          </template>
        </el-table-column>
        <el-table-column prop="deduction" label="扣款" width="100">
          <template #default="{ row }">
            <span style="color: #f56c6c">¥{{ row.deduction }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalSalary" label="实发工资" width="120">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: bold">¥{{ row.totalSalary }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PAID'" type="success">已发放</el-tag>
            <el-tag v-else type="warning">待发放</el-tag>
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

    <!-- 薪资详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="薪资详情" width="600px">
      <el-descriptions :column="2" border v-if="salaryDetail">
        <el-descriptions-item label="员工姓名">{{ salaryDetail.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="月份">{{ salaryDetail.month }}</el-descriptions-item>
        <el-descriptions-item label="基本工资">¥{{ salaryDetail.baseSalary }}</el-descriptions-item>
        <el-descriptions-item label="绩效工资">¥{{ salaryDetail.performanceSalary }}</el-descriptions-item>
        <el-descriptions-item label="奖金">¥{{ salaryDetail.bonus }}</el-descriptions-item>
        <el-descriptions-item label="扣款">¥{{ salaryDetail.deduction }}</el-descriptions-item>
        <el-descriptions-item label="实发工资" :span="2">
          <span style="color: #67c23a; font-size: 18px; font-weight: bold">
            ¥{{ salaryDetail.totalSalary }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag v-if="salaryDetail.status === 'PAID'" type="success">已发放</el-tag>
          <el-tag v-else type="warning">待发放</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { calculateSalary, getSalaryRecords, getEmployeeSalary } from '@/api/salary'
import { getAllEmployees } from '@/api/employee'

const employees = ref([])
const selectedEmployeeId = ref(null)
const selectedMonth = ref(new Date().toISOString().slice(0, 7))
const loading = ref(false)
const detailDialogVisible = ref(false)
const salaryDetail = ref(null)

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10
})

const tableData = ref([])
const total = ref(0)

const loadEmployees = async () => {
  try {
    const res = await getAllEmployees()
    employees.value = res.data
    if (employees.value.length > 0) {
      selectedEmployeeId.value = employees.value[0].id
    }
  } catch (error) {
    console.error('加载员工数据失败', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSalaryRecords(searchForm)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('获取数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleCalculate = async () => {
  if (!selectedEmployeeId.value || !selectedMonth.value) {
    ElMessage.warning('请选择员工和月份')
    return
  }
  try {
    await calculateSalary({
      employeeId: selectedEmployeeId.value,
      month: selectedMonth.value
    })
    ElMessage.success('薪资计算成功')
    fetchData()
  } catch (error) {
    console.error('计算薪资失败', error)
  }
}

const handleQuery = async () => {
  if (!selectedEmployeeId.value || !selectedMonth.value) {
    ElMessage.warning('请选择员工和月份')
    return
  }
  loading.value = true
  try {
    const res = await getEmployeeSalary(selectedEmployeeId.value, selectedMonth.value)
    if (res.data) {
      salaryDetail.value = res.data
      detailDialogVisible.value = true
    } else {
      ElMessage.warning('该月份暂无薪资数据')
    }
  } catch (error) {
    console.error('查询薪资失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadEmployees()
  fetchData()
})
</script>

<style scoped>
@import '@/assets/css/global.css';
</style>
