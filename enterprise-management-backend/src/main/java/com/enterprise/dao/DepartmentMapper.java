package com.enterprise.dao;

import com.enterprise.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门 Mapper 接口
 */
@Mapper
public interface DepartmentMapper {

    /**
     * 根据 ID 查询部门
     */
    Department findById(@Param("id") Long id);

    /**
     * 查询所有部门
     */
    List<Department> findAll();

    /**
     * 插入部门
     */
    int insert(Department department);

    /**
     * 更新部门
     */
    int update(Department department);

    /**
     * 删除部门
     */
    int deleteById(@Param("id") Long id);
}
