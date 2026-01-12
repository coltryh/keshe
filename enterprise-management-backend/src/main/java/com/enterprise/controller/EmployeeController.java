package com.enterprise.controller;

import com.enterprise.dto.PageRequest;
import com.enterprise.dto.Result;
import com.enterprise.entity.Employee;
import com.enterprise.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工管理 Controller
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 获取员工列表（分页）
     * GET /api/employees?pageNum=1&pageSize=10&keyword=xxx
     */
    @GetMapping
    public Result<?> getEmployeeList(PageRequest pageRequest) {
        try {
            return Result.success(employeeService.findByPage(pageRequest));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有员工（下拉选择用）
     * GET /api/employees/all
     */
    @GetMapping("/all")
    public Result<?> getAllEmployees() {
        try {
            return Result.success(employeeService.findAll());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取员工详情
     * GET /api/employees/{id}
     */
    @GetMapping("/{id}")
    public Result<?> getEmployeeById(@PathVariable Long id) {
        try {
            return Result.success(employeeService.findById(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建员工
     * POST /api/employees
     */
    @PostMapping
    public Result<?> createEmployee(@RequestBody Employee employee) {
        try {
            employeeService.create(employee);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新员工
     * PUT /api/employees/{id}
     */
    @PutMapping("/{id}")
    public Result<?> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        try {
            employee.setId(id);
            employeeService.update(employee);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除员工
     * DELETE /api/employees/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据部门 ID 查询员工
     * GET /api/employees/department/{departmentId}
     */
    @GetMapping("/department/{departmentId}")
    public Result<?> getEmployeesByDepartment(@PathVariable Long departmentId) {
        try {
            return Result.success(employeeService.findByDepartmentId(departmentId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
