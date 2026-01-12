import request from '@/utils/request'

// 签到
export const checkin = (data) => {
  return request({
    url: '/attendance/checkin',
    method: 'post',
    params: data
  })
}

// 签退
export const checkout = (data) => {
  return request({
    url: '/attendance/checkout',
    method: 'post',
    params: data
  })
}

// 获取考勤记录
export const getAttendanceRecords = (params) => {
  return request({
    url: '/attendance/records',
    method: 'get',
    params
  })
}

// 获取考勤统计
export const getAttendanceStatistics = (params) => {
  return request({
    url: '/attendance/statistics',
    method: 'get',
    params
  })
}
