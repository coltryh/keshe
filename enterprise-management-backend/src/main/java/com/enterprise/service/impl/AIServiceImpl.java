package com.enterprise.service.impl;

import com.alibaba.fastjson2.JSON;
import com.enterprise.dao.AttendanceMapper;
import com.enterprise.dao.EmployeeMapper;
import com.enterprise.dao.SalaryMapper;
import com.enterprise.entity.Employee;
import com.enterprise.entity.Salary;
import com.enterprise.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 智能分析 Service 实现类
 * 集成智谱 AI / OpenAI API
 */
@Service
public class AIServiceImpl implements AIService {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.api-url}")
    private String apiUrl;

    @Value("${ai.model}")
    private String model;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private SalaryMapper salaryMapper;

    private final WebClient webClient;

    public AIServiceImpl() {
        this.webClient = WebClient.builder().build();
    }

    @Override
    public String chat(String question) {
        try {
            // 构建请求体
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", "你是一个企业管理助手，请回答以下问题：" + question);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(message));

            // 调用 AI API
            String response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(Mono.just(JSON.toJSONString(requestBody)), String.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 解析响应
            Map<String, Object> result = JSON.parseObject(response, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message1 = (Map<String, Object>) choices.get(0).get("message");
                return (String) message1.get("content");
            }

            return "AI 服务暂时不可用";

        } catch (Exception e) {
            // 降级处理：返回简单回复
            return getFallbackResponse(question);
        }
    }

    @Override
    public Map<String, Object> analyzeTurnoverRisk(Long employeeId) {
        Map<String, Object> result = new HashMap<>();

        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }

        // 获取考勤数据
        Map<String, Object> statistics = attendanceMapper.getStatistics(employeeId,
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));

        Integer lateCount = ((Number) statistics.getOrDefault("lateCount", 0)).intValue();
        Integer absenceCount = ((Number) statistics.getOrDefault("absenceCount", 0)).intValue();

        // 简单的风险评估算法
        int riskScore = 0;
        String riskLevel = "低";
        String advice = "员工表现良好，请继续保持";

        if (lateCount > 3 || absenceCount > 2) {
            riskScore = 70;
            riskLevel = "中";
            advice = "员工近期考勤较差，建议关注工作状态";
        }
        if (lateCount > 5 || absenceCount > 4) {
            riskScore = 90;
            riskLevel = "高";
            advice = "员工考勤异常，流失风险较高，建议及时沟通";
        }

        result.put("employeeId", employeeId);
        result.put("employeeName", employee.getName());
        result.put("lateCount", lateCount);
        result.put("absenceCount", absenceCount);
        result.put("riskScore", riskScore);
        result.put("riskLevel", riskLevel);
        result.put("advice", advice);

        return result;
    }

    @Override
    public Map<String, Object> analyzeSalary(String departmentId) {
        Map<String, Object> result = new HashMap<>();

        List<Employee> employees = employeeMapper.findByDepartmentId(Long.valueOf(departmentId));

        if (employees.isEmpty()) {
            result.put("message", "该部门暂无员工");
            return result;
        }

        // 计算平均薪资
        double totalSalary = 0;
        for (Employee emp : employees) {
            if (emp.getSalary() != null) {
                totalSalary += emp.getSalary().doubleValue();
            }
        }
        double avgSalary = totalSalary / employees.size();

        // 薪资分布分析
        String distribution = "薪资分布合理";
        if (avgSalary < 5000) {
            distribution = "部门平均薪资偏低，建议调整";
        } else if (avgSalary > 15000) {
            distribution = "部门平均薪资较高，成本控制需注意";
        }

        result.put("departmentId", departmentId);
        result.put("employeeCount", employees.size());
        result.put("avgSalary", avgSalary);
        result.put("distribution", distribution);

        return result;
    }

    @Override
    public Map<String, Object> generateReport(String type) {
        Map<String, Object> result = new HashMap<>();

        switch (type) {
            case "employee":
                List<Employee> employees = employeeMapper.findAll();
                result.put("totalEmployees", employees.size());
                result.put("activeEmployees", employees.stream().filter(e -> "ACTIVE".equals(e.getStatus())).count());
                result.put("reportType", "员工统计报表");
                break;

            case "salary":
                result.put("reportType", "薪资统计报表");
                result.put("message", "请选择月份查看详细报表");
                break;

            default:
                result.put("message", "暂不支持该类型报表");
        }

        return result;
    }

    /**
     * 降级响应（当 AI API 调用失败时使用）
     */
    private String getFallbackResponse(String question) {
        if (question.contains("考勤")) {
            return "关于考勤：建议员工每天9点前签到，18点后签退。迟到或早退会影响绩效考核。";
        } else if (question.contains("薪资")) {
            return "关于薪资：薪资由基本工资、绩效工资、奖金组成，会根据考勤情况进行扣款。";
        } else if (question.contains("请假")) {
            return "关于请假：请提前在系统提交请假申请，等待管理员审批。请假类型包括病假、事假、年假。";
        } else {
            return "我是企业管理助手，可以帮您解答关于考勤、薪资、请假等问题。请问有什么可以帮助您的？";
        }
    }
}
