package com.enterprise.controller;

import com.enterprise.dto.PageRequest;
import com.enterprise.dto.Result;
import com.enterprise.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 考勤管理 Controller
 */
@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 员工签到
     * POST /api/attendance/checkin
     */
    @PostMapping("/checkin")
    public Result<?> checkin(@RequestParam Long employeeId, @RequestParam String employeeName) {
        try {
            attendanceService.checkin(employeeId, employeeName);
            return Result.success("签到成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 员工签退
     * POST /api/attendance/checkout
     */
    @PostMapping("/checkout")
    public Result<?> checkout(@RequestParam Long employeeId) {
        try {
            attendanceService.checkout(employeeId);
            return Result.success("签退成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询考勤记录（分页）
     * GET /api/attendance/records?pageNum=1&pageSize=10
     */
    @GetMapping("/records")
    public Result<?> getAttendanceRecords(PageRequest pageRequest) {
        try {
            return Result.success(attendanceService.findByPage(pageRequest));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 考勤统计
     * GET /api/attendance/statistics?employeeId=1&month=2024-01
     */
    @GetMapping("/statistics")
    public Result<?> getStatistics(@RequestParam Long employeeId, @RequestParam String month) {
        try {
            return Result.success(attendanceService.getStatistics(employeeId, month));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
