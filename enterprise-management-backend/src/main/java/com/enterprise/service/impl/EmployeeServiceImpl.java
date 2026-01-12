package com.enterprise.service.impl;

import com.enterprise.dao.EmployeeMapper;
import com.enterprise.dto.PageRequest;
import com.enterprise.entity.Employee;
import com.enterprise.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工 Service 实现类（依赖注入）
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public Employee findById(Long id) {
        return employeeMapper.findById(id);
    }

    @Override
    public Map<String, Object> findByPage(PageRequest pageRequest) {
        int offset = (pageRequest.getPageNum() - 1) * pageRequest.getPageSize();
        pageRequest.setPageNum(offset);

        List<Employee> list = employeeMapper.findByPage(pageRequest);
        int total = employeeMapper.countTotal(pageRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public List<Employee> findAll() {
        return employeeMapper.findAll();
    }

    @Override
    public void create(Employee employee) {
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setStatus("ACTIVE");
        employeeMapper.insert(employee);
    }

    @Override
    public void update(Employee employee) {
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.update(employee);
    }

    @Override
    public void deleteById(Long id) {
        employeeMapper.deleteById(id);
    }

    @Override
    public List<Employee> findByDepartmentId(Long departmentId) {
        return employeeMapper.findByDepartmentId(departmentId);
    }
}
