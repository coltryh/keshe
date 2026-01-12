package com.enterprise.service;

import java.util.Map;

/**
 * AI 智能分析 Service 接口
 */
public interface AIService {

    /**
     * AI 智能问答
     */
    String chat(String question);

    /**
     * 员工流失风险分析
     */
    Map<String, Object> analyzeTurnoverRisk(Long employeeId);

    /**
     * 薪资合理性分析
     */
    Map<String, Object> analyzeSalary(String departmentId);

    /**
     * 生成智能报表
     */
    Map<String, Object> generateReport(String type);
}
