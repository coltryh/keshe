package com.enterprise.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    private Long id;
    private String name;
    private String gender;  // 男, 女
    private Integer age;
    private Long departmentId;
    private String departmentName;
    private String position;  // 职位
    private String phone;
    private String email;
    private LocalDate hireDate;  // 入职日期
    private String status;  // ACTIVE, RESIGNED
    private BigDecimal salary;  // 基本工资
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
