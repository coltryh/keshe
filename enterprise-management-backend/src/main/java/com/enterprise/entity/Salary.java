package com.enterprise.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 薪资实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Salary {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String month;  // 薪资月份，格式：2024-01
    private BigDecimal baseSalary;  // 基本工资
    private BigDecimal performanceSalary;  // 绩效工资
    private BigDecimal bonus;  // 奖金
    private BigDecimal deduction;  // 扣款
    private BigDecimal totalSalary;  // 实发工资
    private String status;  // PENDING, PAID
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
