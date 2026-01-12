package com.enterprise.dao;

import com.enterprise.dto.PageRequest;
import com.enterprise.entity.Salary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 薪资 Mapper 接口
 */
@Mapper
public interface SalaryMapper {

    /**
     * 根据 ID 查询薪资记录
     */
    Salary findById(@Param("id") Long id);

    /**
     * 查询薪资记录（分页）
     */
    List<Salary> findByPage(PageRequest pageRequest);

    /**
     * 查询薪资总数
     */
    int countTotal(PageRequest pageRequest);

    /**
     * 查询员工某月薪资
     */
    Salary findByEmployeeAndMonth(@Param("employeeId") Long employeeId, @Param("month") String month);

    /**
     * 插入薪资记录
     */
    int insert(Salary salary);

    /**
     * 更新薪资记录
     */
    int update(Salary salary);

    /**
     * 删除薪资记录
     */
    int deleteById(@Param("id") Long id);
}
