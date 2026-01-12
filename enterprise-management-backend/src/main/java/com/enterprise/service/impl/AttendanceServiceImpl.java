package com.enterprise.service.impl;

import com.enterprise.dao.AttendanceMapper;
import com.enterprise.dao.EmployeeMapper;
import com.enterprise.dto.PageRequest;
import com.enterprise.entity.Attendance;
import com.enterprise.entity.Employee;
import com.enterprise.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考勤 Service 实现类
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public void checkin(Long employeeId, String employeeName) {
        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }

        LocalDate today = LocalDate.now();
        Attendance existAttendance = attendanceMapper.findByEmployeeAndDate(employeeId, today);
        if (existAttendance != null) {
            throw new RuntimeException("今天已经签到过了");
        }

        LocalDateTime now = LocalDateTime.now();
        // 判断是否迟到（9:00后算迟到）
        String status = now.getHour() >= 9 && now.getMinute() > 0 ? "LATE" : "NORMAL";

        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setEmployeeName(employeeName);
        attendance.setDate(today);
        attendance.setCheckinTime(now);
        attendance.setStatus(status);
        attendance.setCreateTime(LocalDateTime.now());

        attendanceMapper.insert(attendance);
    }

    @Override
    public void checkout(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceMapper.findByEmployeeAndDate(employeeId, today);
        if (attendance == null) {
            throw new RuntimeException("请先签到");
        }
        if (attendance.getCheckoutTime() != null) {
            throw new RuntimeException("今天已经签退过了");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckoutTime(now);

        // 判断是否早退（18:00前算早退）
        if (now.getHour() < 18) {
            attendance.setStatus("EARLY_LEAVE");
        }

        // 计算工作时长
        long minutes = java.time.Duration.between(attendance.getCheckinTime(), now).toMinutes();
        long hours = minutes / 60;
        long mins = minutes % 60;
        attendance.setWorkHours(hours + "小时" + mins + "分钟");

        attendanceMapper.update(attendance);
    }

    @Override
    public Map<String, Object> findByPage(PageRequest pageRequest) {
        int offset = (pageRequest.getPageNum() - 1) * pageRequest.getPageSize();
        pageRequest.setPageNum(offset);

        List<Attendance> list = attendanceMapper.findByPage(pageRequest);
        int total = attendanceMapper.countTotal(pageRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public Map<String, Object> getStatistics(Long employeeId, String month) {
        return attendanceMapper.getStatistics(employeeId, month);
    }
}
