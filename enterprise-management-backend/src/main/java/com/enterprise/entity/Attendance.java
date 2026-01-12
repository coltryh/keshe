package com.enterprise.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate date;  // 考勤日期
    private LocalDateTime checkinTime;  // 签到时间
    private LocalDateTime checkoutTime;  // 签退时间
    private String status;  // NORMAL, LATE, EARLY_LEAVE, ABSENCE
    private String workHours;  // 工作时长
    private LocalDateTime createTime;
}
