import request from '@/utils/request'

// 计算薪资
export const calculateSalary = (params) => {
  return request({
    url: '/salary/calculate',
    method: 'post',
    params
  })
}

// 获取薪资记录
export const getSalaryRecords = (params) => {
  return request({
    url: '/salary/records',
    method: 'get',
    params
  })
}

// 获取员工薪资
export const getEmployeeSalary = (employeeId, month) => {
  return request({
    url: `/salary/employee/${employeeId}`,
    method: 'get',
    params: { month }
  })
}
