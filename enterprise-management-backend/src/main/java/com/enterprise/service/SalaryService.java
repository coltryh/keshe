package com.enterprise.service;

import com.enterprise.dto.PageRequest;
import com.enterprise.entity.Salary;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 薪资 Service 接口
 */
public interface SalaryService {

    /**
     * 计算并生成薪资
     */
    void calculateSalary(Long employeeId, String month);

    /**
     * 分页查询薪资记录
     */
    Map<String, Object> findByPage(PageRequest pageRequest);

    /**
     * 查询员工某月薪资
     */
    Salary findByEmployeeAndMonth(Long employeeId, String month);
}
