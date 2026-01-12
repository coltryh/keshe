import request from '@/utils/request'

// 提交请假申请
export const applyLeave = (data) => {
  return request({
    url: '/leave/apply',
    method: 'post',
    data
  })
}

// 审批请假申请
export const approveLeave = (id, params) => {
  return request({
    url: `/leave/${id}/approve`,
    method: 'put',
    params
  })
}

// 获取请假记录
export const getLeaveRecords = (params) => {
  return request({
    url: '/leave/records',
    method: 'get',
    params
  })
}

// 获取员工请假记录
export const getEmployeeLeaveRecords = (employeeId) => {
  return request({
    url: `/leave/employee/${employeeId}`,
    method: 'get'
  })
}
