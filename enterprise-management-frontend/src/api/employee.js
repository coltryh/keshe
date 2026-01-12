import request from '@/utils/request'

// 获取员工列表
export const getEmployeeList = (params) => {
  return request({
    url: '/employees',
    method: 'get',
    params
  })
}

// 获取所有员工
export const getAllEmployees = () => {
  return request({
    url: '/employees/all',
    method: 'get'
  })
}

// 获取员工详情
export const getEmployeeById = (id) => {
  return request({
    url: `/employees/${id}`,
    method: 'get'
  })
}

// 创建员工
export const createEmployee = (data) => {
  return request({
    url: '/employees',
    method: 'post',
    data
  })
}

// 更新员工
export const updateEmployee = (id, data) => {
  return request({
    url: `/employees/${id}`,
    method: 'put',
    data
  })
}

// 删除员工
export const deleteEmployee = (id) => {
  return request({
    url: `/employees/${id}`,
    method: 'delete'
  })
}
