package com.enterprise.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 请假申请实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApplication {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String leaveType;  // SICK, PERSONAL, ANNUAL
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double days;  // 请假天数
    private String reason;
    private String status;  // PENDING, APPROVED, REJECTED
    private String approver;  // 审批人
    private String approvalComment;  // 审批意见
    private LocalDateTime approvalTime;
    private LocalDateTime createTime;
}
