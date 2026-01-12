package com.enterprise.service.impl;

import com.enterprise.dao.AttendanceMapper;
import com.enterprise.dao.EmployeeMapper;
import com.enterprise.dao.SalaryMapper;
import com.enterprise.dto.PageRequest;
import com.enterprise.entity.Employee;
import com.enterprise.entity.Salary;
import com.enterprise.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 薪资 Service 实现类
 */
@Service
public class SalaryServiceImpl implements SalaryService {

    @Autowired
    private SalaryMapper salaryMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Override
    public void calculateSalary(Long employeeId, String month) {
        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }

        Salary existSalary = salaryMapper.findByEmployeeAndMonth(employeeId, month);
        if (existSalary != null) {
            throw new RuntimeException("该月薪资已生成");
        }

        // 基本工资
        BigDecimal baseSalary = employee.getSalary() != null ? employee.getSalary() : new BigDecimal("5000");

        // 绩效工资（固定 2000）
        BigDecimal performanceSalary = new BigDecimal("2000");

        // 奖金（固定 1000）
        BigDecimal bonus = new BigDecimal("1000");

        // 根据考勤计算扣款
        Map<String, Object> statistics = attendanceMapper.getStatistics(employeeId, month);
        Integer lateCount = ((Number) statistics.getOrDefault("lateCount", 0)).intValue();
        Integer absenceCount = ((Number) statistics.getOrDefault("absenceCount", 0)).intValue();

        // 迟到一次扣 50，缺勤一次扣 200
        BigDecimal deduction = new BigDecimal(lateCount * 50 + absenceCount * 200);

        // 计算实发工资
        BigDecimal totalSalary = baseSalary.add(performanceSalary).add(bonus).subtract(deduction);

        Salary salary = new Salary();
        salary.setEmployeeId(employeeId);
        salary.setEmployeeName(employee.getName());
        salary.setMonth(month);
        salary.setBaseSalary(baseSalary);
        salary.setPerformanceSalary(performanceSalary);
        salary.setBonus(bonus);
        salary.setDeduction(deduction);
        salary.setTotalSalary(totalSalary);
        salary.setStatus("PENDING");
        salary.setCreateTime(LocalDateTime.now());
        salary.setUpdateTime(LocalDateTime.now());

        salaryMapper.insert(salary);
    }

    @Override
    public Map<String, Object> findByPage(PageRequest pageRequest) {
        int offset = (pageRequest.getPageNum() - 1) * pageRequest.getPageSize();
        pageRequest.setPageNum(offset);

        List<Salary> list = salaryMapper.findByPage(pageRequest);
        int total = salaryMapper.countTotal(pageRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public Salary findByEmployeeAndMonth(Long employeeId, String month) {
        return salaryMapper.findByEmployeeAndMonth(employeeId, month);
    }
}
