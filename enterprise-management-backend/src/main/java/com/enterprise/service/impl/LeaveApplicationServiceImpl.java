package com.enterprise.service.impl;

import com.enterprise.dao.EmployeeMapper;
import com.enterprise.dao.LeaveApplicationMapper;
import com.enterprise.dto.PageRequest;
import com.enterprise.entity.Employee;
import com.enterprise.entity.LeaveApplication;
import com.enterprise.service.LeaveApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请假申请 Service 实现类
 */
@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    @Autowired
    private LeaveApplicationMapper leaveApplicationMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public void apply(LeaveApplication leaveApplication) {
        Employee employee = employeeMapper.findById(leaveApplication.getEmployeeId());
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }

        // 计算请假天数
        long days = ChronoUnit.DAYS.between(leaveApplication.getStartTime(), leaveApplication.getEndTime());
        leaveApplication.setDays((double) days);

        leaveApplication.setStatus("PENDING");
        leaveApplication.setCreateTime(LocalDateTime.now());

        leaveApplicationMapper.insert(leaveApplication);
    }

    @Override
    public void approve(Long id, String status, String approver, String comment) {
        LeaveApplication leaveApplication = leaveApplicationMapper.findById(id);
        if (leaveApplication == null) {
            throw new RuntimeException("请假申请不存在");
        }
        if (!"PENDING".equals(leaveApplication.getStatus())) {
            throw new RuntimeException("该申请已审批");
        }

        leaveApplication.setStatus(status);
        leaveApplication.setApprover(approver);
        leaveApplication.setApprovalComment(comment);
        leaveApplication.setApprovalTime(LocalDateTime.now());

        leaveApplicationMapper.update(leaveApplication);
    }

    @Override
    public Map<String, Object> findByPage(PageRequest pageRequest) {
        int offset = (pageRequest.getPageNum() - 1) * pageRequest.getPageSize();
        pageRequest.setPageNum(offset);

        List<LeaveApplication> list = leaveApplicationMapper.findByPage(pageRequest);
        int total = leaveApplicationMapper.countTotal(pageRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public List<LeaveApplication> findByEmployeeId(Long employeeId) {
        return leaveApplicationMapper.findByEmployeeId(employeeId);
    }
}
