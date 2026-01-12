package com.enterprise.dao;

import com.enterprise.dto.PageRequest;
import com.enterprise.entity.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤 Mapper 接口
 */
@Mapper
public interface AttendanceMapper {

    /**
     * 根据 ID 查询考勤记录
     */
    Attendance findById(@Param("id") Long id);

    /**
     * 查询考勤记录（分页）
     */
    List<Attendance> findByPage(PageRequest pageRequest);

    /**
     * 查询考勤总数
     */
    int countTotal(PageRequest pageRequest);

    /**
     * 查询员工某天的考勤记录
     */
    Attendance findByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    /**
     * 插入考勤记录
     */
    int insert(Attendance attendance);

    /**
     * 更新考勤记录
     */
    int update(Attendance attendance);

    /**
     * 删除考勤记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询员工考勤统计
     */
    Map<String, Object> getStatistics(@Param("employeeId") Long employeeId, @Param("month") String month);
}
