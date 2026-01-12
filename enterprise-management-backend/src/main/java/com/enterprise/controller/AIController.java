package com.enterprise.controller;

import com.enterprise.dto.Result;
import com.enterprise.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 智能分析 Controller
 * 系统亮点功能
 */
@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * AI 智能问答
     * POST /api/ai/chat
     * Body: {"question": "如何管理考勤？"}
     */
    @PostMapping("/chat")
    public Result<?> chat(@RequestBody Map<String, String> request) {
        try {
            String answer = aiService.chat(request.get("question"));
            return Result.success(answer);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 员工流失风险分析
     * POST /api/ai/analyze/turnover?employeeId=1
     */
    @PostMapping("/analyze/turnover")
    public Result<?> analyzeTurnover(@RequestParam Long employeeId) {
        try {
            return Result.success(aiService.analyzeTurnoverRisk(employeeId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 薪资合理性分析
     * POST /api/ai/analyze/salary?departmentId=1
     */
    @PostMapping("/analyze/salary")
    public Result<?> analyzeSalary(@RequestParam String departmentId) {
        try {
            return Result.success(aiService.analyzeSalary(departmentId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 生成智能报表
     * POST /api/ai/report/generate?type=employee
     */
    @PostMapping("/report/generate")
    public Result<?> generateReport(@RequestParam String type) {
        try {
            return Result.success(aiService.generateReport(type));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
