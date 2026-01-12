<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">AI 智能分析</h1>
    </div>

    <el-row :gutter="20">
      <!-- AI 智能问答 -->
      <el-col :span="12">
        <div class="card-container">
          <h3 style="margin-bottom: 20px">AI 智能助手</h3>
          <div class="chat-container">
            <div class="chat-messages" ref="chatMessagesRef">
              <div
                v-for="(msg, index) in chatMessages"
                :key="index"
                :class="['message', msg.role]"
              >
                <div class="message-content">{{ msg.content }}</div>
              </div>
              <div v-if="loading" class="message assistant">
                <div class="message-content">正在思考中...</div>
              </div>
            </div>
            <div class="chat-input">
              <el-input
                v-model="question"
                placeholder="请输入您的问题..."
                @keyup.enter="sendQuestion"
              >
                <template #append>
                  <el-button @click="sendQuestion" :loading="loading">发送</el-button>
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 智能分析工具 -->
      <el-col :span="12">
        <div class="card-container">
          <h3 style="margin-bottom: 20px">智能分析工具</h3>

          <el-form label-width="120px">
            <el-form-item label="流失风险分析">
              <el-select v-model="selectedEmployeeId" placeholder="请选择员工" style="width: 200px">
                <el-option
                  v-for="emp in employees"
                  :key="emp.id"
                  :label="emp.name"
                  :value="emp.id"
                />
              </el-select>
              <el-button type="primary" @click="analyzeTurnover" :loading="analyzing">
                分析
              </el-button>
            </el-form-item>

            <el-form-item label="薪资分析">
              <el-select v-model="selectedDeptId" placeholder="请选择部门" style="width: 200px">
                <el-option label="技术部" :value="1" />
                <el-option label="市场部" :value="2" />
                <el-option label="人事部" :value="3" />
              </el-select>
              <el-button type="success" @click="analyzeSalary" :loading="analyzing">
                分析
              </el-button>
            </el-form-item>

            <el-form-item label="智能报表">
              <el-button type="warning" @click="generateReport('employee')" :loading="analyzing">
                生成员工报表
              </el-button>
              <el-button type="info" @click="generateReport('salary')" :loading="analyzing">
                生成薪资报表
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 分析结果展示 -->
          <div v-if="analysisResult" class="analysis-result">
            <h4 style="margin-bottom: 15px">分析结果</h4>
            <el-descriptions :column="1" border>
              <el-descriptions-item
                v-for="(value, key) in analysisResult"
                :key="key"
                :label="formatLabel(key)"
              >
                {{ formatValue(key, value) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 常见问题 -->
    <div class="card-container" style="margin-top: 20px">
      <h3 style="margin-bottom: 20px">常见问题</h3>
      <el-space wrap>
        <el-tag
          v-for="q in commonQuestions"
          :key="q"
          @click="question = q"
          style="cursor: pointer"
          size="large"
        >
          {{ q }}
        </el-tag>
      </el-space>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { aiChat, analyzeTurnover, analyzeSalaryData, generateReport } from '@/api/ai'
import { getAllEmployees } from '@/api/employee'

const employees = ref([])
const selectedEmployeeId = ref(null)
const selectedDeptId = ref(null)
const question = ref('')
const chatMessages = ref([
  { role: 'assistant', content: '您好！我是企业管理 AI 助手，有什么可以帮助您的吗？' }
])
const loading = ref(false)
const analyzing = ref(false)
const analysisResult = ref(null)
const chatMessagesRef = ref(null)

const commonQuestions = [
  '如何管理考勤？',
  '薪资如何计算？',
  '请假流程是什么？',
  '如何提高员工满意度？',
  '怎么降低员工流失率？'
]

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

const sendQuestion = async () => {
  if (!question.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }

  chatMessages.value.push({ role: 'user', content: question.value })
  const q = question.value
  question.value = ''
  loading.value = true

  scrollToBottom()

  try {
    const res = await aiChat(q)
    chatMessages.value.push({ role: 'assistant', content: res.data })
  } catch (error) {
    chatMessages.value.push({
      role: 'assistant',
      content: '抱歉，AI 服务暂时不可用，请稍后再试。'
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const analyzeTurnoverFunc = async () => {
  if (!selectedEmployeeId.value) {
    ElMessage.warning('请选择员工')
    return
  }

  analyzing.value = true
  try {
    const res = await analyzeTurnover(selectedEmployeeId.value)
    analysisResult.value = res.data
    ElMessage.success('分析完成')
  } catch (error) {
    console.error('分析失败', error)
  } finally {
    analyzing.value = false
  }
}

const analyzeSalary = async () => {
  if (!selectedDeptId.value) {
    ElMessage.warning('请选择部门')
    return
  }

  analyzing.value = true
  try {
    const res = await analyzeSalaryData(selectedDeptId.value)
    analysisResult.value = res.data
    ElMessage.success('分析完成')
  } catch (error) {
    console.error('分析失败', error)
  } finally {
    analyzing.value = false
  }
}

const generateReport = async (type) => {
  analyzing.value = true
  try {
    const res = await generateReport(type)
    analysisResult.value = res.data
    ElMessage.success('报表生成成功')
  } catch (error) {
    console.error('生成报表失败', error)
  } finally {
    analyzing.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

const formatLabel = (key) => {
  const labelMap = {
    employeeId: '员工ID',
    employeeName: '员工姓名',
    lateCount: '迟到次数',
    absenceCount: '缺勤次数',
    riskScore: '风险评分',
    riskLevel: '风险等级',
    advice: '建议',
    departmentId: '部门ID',
    employeeCount: '员工数量',
    avgSalary: '平均薪资',
    distribution: '薪资分布',
    totalEmployees: '员工总数',
    activeEmployees: '在职员工',
    reportType: '报表类型'
  }
  return labelMap[key] || key
}

const formatValue = (key, value) => {
  if (key === 'avgSalary' && typeof value === 'number') {
    return `¥${value.toFixed(2)}`
  }
  if (key === 'riskLevel') {
    const levelMap = {
      '低': '🟢 低风险',
      '中': '🟡 中风险',
      '高': '🔴 高风险'
    }
    return levelMap[value] || value
  }
  return value
}

onMounted(() => {
  loadEmployees()
})
</script>

<style scoped>
@import '@/assets/css/global.css';

.chat-container {
  display: flex;
  flex-direction: column;
  height: 400px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 15px;
}

.message {
  margin-bottom: 15px;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 8px;
  word-wrap: break-word;
}

.message.user .message-content {
  background-color: #409eff;
  color: #fff;
}

.message.assistant .message-content {
  background-color: #fff;
  color: #333;
  border: 1px solid #e4e7ed;
}

.chat-input {
  display: flex;
  gap: 10px;
}

.analysis-result {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.analysis-result h4 {
  margin: 0 0 15px 0;
  color: #333;
}
</style>
