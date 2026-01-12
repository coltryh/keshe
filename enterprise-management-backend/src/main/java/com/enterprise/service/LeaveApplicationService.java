package com.enterprise.service;

import com.enterprise.dto.PageRequest;
import com.enterprise.entity.LeaveApplication;

import java.util.List;
import java.util.Map;

/**
 * 请假申请 Service 接口
 */
public interface LeaveApplicationService {

    /**
     * 提交请假申请
     */
    void apply(LeaveApplication leaveApplication);

    /**
     * 审批请假申请
     */
    void approve(Long id, String status, String approver, String comment);

    /**
     * 分页查询请假申请
     */
    Map<String, Object> findByPage(PageRequest pageRequest);

    /**
     * 查询员工的请假申请
     */
    List<LeaveApplication> findByEmployeeId(Long employeeId);
}
