import request from '@/utils/request'

// AI 智能问答
export const aiChat = (question) => {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: { question }
  })
}

// 员工流失风险分析
export const analyzeTurnover = (employeeId) => {
  return request({
    url: '/ai/analyze/turnover',
    method: 'post',
    params: { employeeId }
  })
}

// 薪资合理性分析
export const analyzeSalary = (departmentId) => {
  return request({
    url: '/ai/analyze/salary',
    method: 'post',
    params: { departmentId }
  })
}

// 生成智能报表
export const generateReport = (type) => {
  return request({
    url: '/ai/report/generate',
    method: 'post',
    params: { type }
  })
}
