package com.enterprise.controller;

import com.enterprise.dto.PageRequest;
import com.enterprise.dto.Result;
import com.enterprise.entity.LeaveApplication;
import com.enterprise.service.LeaveApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 请假申请 Controller
 */
@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    @Autowired
    private LeaveApplicationService leaveApplicationService;

    /**
     * 提交请假申请
     * POST /api/leave/apply
     */
    @PostMapping("/apply")
    public Result<?> applyLeave(@RequestBody LeaveApplication leaveApplication) {
        try {
            leaveApplicationService.apply(leaveApplication);
            return Result.success("申请提交成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批请假申请
     * PUT /api/leave/{id}/approve?status=APPROVED&approver=admin&comment=同意
     */
    @PutMapping("/{id}/approve")
    public Result<?> approveLeave(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam String approver,
            @RequestParam(required = false) String comment) {
        try {
            leaveApplicationService.approve(id, status, approver, comment);
            return Result.success("审批成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询请假申请（分页）
     * GET /api/leave/records?pageNum=1&pageSize=10
     */
    @GetMapping("/records")
    public Result<?> getLeaveRecords(PageRequest pageRequest) {
        try {
            return Result.success(leaveApplicationService.findByPage(pageRequest));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询员工的请假申请
     * GET /api/leave/employee/{employeeId}
     */
    @GetMapping("/employee/{employeeId}")
    public Result<?> getEmployeeLeaveRecords(@PathVariable Long employeeId) {
        try {
            List<LeaveApplication> list = leaveApplicationService.findByEmployeeId(employeeId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
