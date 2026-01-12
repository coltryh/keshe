package com.enterprise.dao;

import com.enterprise.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志 Mapper 接口（AOP 日志记录使用）
 */
@Mapper
public interface OperationLogMapper {

    /**
     * 插入操作日志
     */
    int insert(OperationLog log);

    /**
     * 查询所有日志
     */
    List<OperationLog> findAll(@Param("limit") Integer limit);

    /**
     * 根据用户名查询日志
     */
    List<OperationLog> findByUsername(@Param("username") String username);
}
