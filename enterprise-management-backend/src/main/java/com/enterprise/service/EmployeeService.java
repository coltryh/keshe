package com.enterprise.service;

import com.enterprise.dto.PageRequest;
import com.enterprise.entity.Employee;

import java.util.List;
import java.util.Map;

/**
 * 员工 Service 接口
 */
public interface EmployeeService {

    /**
     * 根据 ID 查询员工
     */
    Employee findById(Long id);

    /**
     * 分页查询员工列表
     */
    Map<String, Object> findByPage(PageRequest pageRequest);

    /**
     * 查询所有员工
     */
    List<Employee> findAll();

    /**
     * 创建员工
     */
    void create(Employee employee);

    /**
     * 更新员工
     */
    void update(Employee employee);

    /**
     * 删除员工
     */
    void deleteById(Long id);

    /**
     * 根据部门 ID 查询员工
     */
    List<Employee> findByDepartmentId(Long departmentId);
}
