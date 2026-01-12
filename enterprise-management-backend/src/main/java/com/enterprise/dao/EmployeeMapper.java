package com.enterprise.dao;

import com.enterprise.dto.PageRequest;
import com.enterprise.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工 Mapper 接口
 */
@Mapper
public interface EmployeeMapper {

    /**
     * 根据 ID 查询员工
     */
    Employee findById(@Param("id") Long id);

    /**
     * 查询员工列表（分页）
     */
    List<Employee> findByPage(PageRequest pageRequest);

    /**
     * 查询员工总数
     */
    int countTotal(PageRequest pageRequest);

    /**
     * 查询所有员工
     */
    List<Employee> findAll();

    /**
     * 插入员工
     */
    int insert(Employee employee);

    /**
     * 更新员工
     */
    int update(Employee employee);

    /**
     * 删除员工
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据部门 ID 查询员工
     */
    List<Employee> findByDepartmentId(@Param("departmentId") Long departmentId);
}
