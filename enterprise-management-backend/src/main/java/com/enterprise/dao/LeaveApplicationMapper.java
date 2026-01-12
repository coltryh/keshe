package com.enterprise.dao;

import com.enterprise.dto.PageRequest;
import com.enterprise.entity.LeaveApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 请假申请 Mapper 接口
 */
@Mapper
public interface LeaveApplicationMapper {

    /**
     * 根据 ID 查询请假申请
     */
    LeaveApplication findById(@Param("id") Long id);

    /**
     * 查询请假申请列表（分页）
     */
    List<LeaveApplication> findByPage(PageRequest pageRequest);

    /**
     * 查询请假申请总数
     */
    int countTotal(PageRequest pageRequest);

    /**
     * 查询员工的请假申请
     */
    List<LeaveApplication> findByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 插入请假申请
     */
    int insert(LeaveApplication leaveApplication);

    /**
     * 更新请假申请
     */
    int update(LeaveApplication leaveApplication);

    /**
     * 删除请假申请
     */
    int deleteById(@Param("id") Long id);
}
