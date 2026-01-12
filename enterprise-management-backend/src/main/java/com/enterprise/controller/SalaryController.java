package com.enterprise.controller;

import com.enterprise.dto.PageRequest;
import com.enterprise.dto.Result;
import com.enterprise.entity.Salary;
import com.enterprise.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 薪资管理 Controller
 */
@RestController
@RequestMapping("/api/salary")
public class SalaryController {

    @Autowired
    private SalaryService salaryService;

    /**
     * 计算薪资
     * POST /api/salary/calculate?employeeId=1&month=2024-01
     */
    @PostMapping("/calculate")
    public Result<?> calculateSalary(@RequestParam Long employeeId, @RequestParam String month) {
        try {
            salaryService.calculateSalary(employeeId, month);
            return Result.success("薪资计算成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询薪资记录（分页）
     * GET /api/salary/records?pageNum=1&pageSize=10
     */
    @GetMapping("/records")
    public Result<?> getSalaryRecords(PageRequest pageRequest) {
        try {
            return Result.success(salaryService.findByPage(pageRequest));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询员工某月薪资
     * GET /api/salary/employee/{employeeId}?month=2024-01
     */
    @GetMapping("/employee/{employeeId}")
    public Result<?> getEmployeeSalary(@PathVariable Long employeeId, @RequestParam String month) {
        try {
            Salary salary = salaryService.findByEmployeeAndMonth(employeeId, month);
            return Result.success(salary);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
