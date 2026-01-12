package com.enterprise.service;

import com.enterprise.dto.PageRequest;
import com.enterprise.entity.Attendance;

import java.util.Map;

/**
 * 考勤 Service 接口
 */
public interface AttendanceService {

    /**
     * 员工签到
     */
    void checkin(Long employeeId, String employeeName);

    /**
     * 员工签退
     */
    void checkout(Long employeeId);

    /**
     * 分页查询考勤记录
     */
    Map<String, Object> findByPage(PageRequest pageRequest);

    /**
     * 获取考勤统计
     */
    Map<String, Object> getStatistics(Long employeeId, String month);
}
