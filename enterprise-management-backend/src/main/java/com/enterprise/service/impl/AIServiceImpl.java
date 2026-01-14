package com.enterprise.service.impl;

import com.enterprise.dao.AttendanceMapper;
import com.enterprise.dao.EmployeeMapper;
import com.enterprise.dao.SalaryMapper;
import com.enterprise.entity.Employee;
import com.enterprise.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 智能分析 Service 实现类
 * 集成智谱AI API，提供真正的AI智能分析能力
 */
@Service
public class AIServiceImpl implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.api-url}")
    private String apiUrl;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.max-tokens:2000}")
    private Integer maxTokens;

    @Value("${ai.temperature:0.7}")
    private Double temperature;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private SalaryMapper salaryMapper;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // 简单的缓存机制，避免重复调用AI API
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY_MINUTES = 30;

    public AIServiceImpl() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String chat(String question) {
        try {
            logger.info("收到AI问答请求: {}", question);

            // 构建系统提示词
            String systemPrompt = buildChatSystemPrompt();
            String userPrompt = "用户问题：" + question;

            // 调用AI API
            String aiResponse = callAI(systemPrompt, userPrompt);

            logger.info("AI回答成功");
            return aiResponse;

        } catch (Exception e) {
            logger.error("AI问答调用失败", e);
            return getFallbackResponse(question);
        }
    }

    @Override
    public Map<String, Object> analyzeTurnoverRisk(Long employeeId) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("开始分析员工流失风险, employeeId={}", employeeId);

            // 获取员工数据
            Employee employee = employeeMapper.findById(employeeId);
            if (employee == null) {
                throw new RuntimeException("员工不存在");
            }

            // 获取考勤数据
            String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Map<String, Object> statistics = attendanceMapper.getStatistics(employeeId, currentMonth);

            // 构建AI分析的上下文数据
            String context = buildTurnoverRiskContext(employee, statistics);

            // 构建提示词
            String systemPrompt = "你是一个专业的人力资源管理专家和员工流失风险分析师。\n" +
                "你的任务是分析员工的考勤数据、工作表现等维度，评估员工的流失风险等级。\n" +
                "请提供专业的、有针对性的建议。\n\n" +
                "输出格式要求（严格遵循JSON格式）：\n" +
                "{\n" +
                "  \"riskScore\": 数字(0-100),\n" +
                "  \"riskLevel\": \"低\"或\"中\"或\"高\",\n" +
                "  \"analysis\": \"详细的分析说明\",\n" +
                "  \"suggestions\": [\"建议1\", \"建议2\", \"建议3\"],\n" +
                "  \"keyFactors\": [\"关键因素1\", \"关键因素2\"]\n" +
                "}";

            String userPrompt = "请分析以下员工的流失风险：\n\n" +
                "员工信息：\n" + context + "\n\n" +
                "请根据以上数据，给出专业的风险评估和改进建议。";

            // 调用AI分析
            String aiResponse = callAI(systemPrompt, userPrompt);

            // 解析AI响应
            JsonNode jsonNode = objectMapper.readTree(aiResponse);
            result.put("employeeId", employeeId);
            result.put("employeeName", employee.getName());
            result.put("department", employee.getDepartmentName());
            result.put("position", employee.getPosition());
            result.put("riskScore", jsonNode.path("riskScore").asInt());
            result.put("riskLevel", jsonNode.path("riskLevel").asText());
            result.put("analysis", jsonNode.path("analysis").asText());

            // 转换suggestions和keyFactors为数组
            List<String> suggestions = objectMapper.convertValue(
                jsonNode.path("suggestions"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            List<String> keyFactors = objectMapper.convertValue(
                jsonNode.path("keyFactors"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            result.put("suggestions", suggestions);
            result.put("keyFactors", keyFactors);

            // 添加原始数据供参考
            result.put("lateCount", statistics.getOrDefault("lateCount", 0));
            result.put("absenceCount", statistics.getOrDefault("absenceCount", 0));

            logger.info("员工流失风险分析完成, riskLevel={}", result.get("riskLevel"));

        } catch (Exception e) {
            logger.error("员工流失风险AI分析失败，使用降级方案", e);
            return getFallbackTurnoverRiskAnalysis(employeeId);
        }

        return result;
    }

    @Override
    public Map<String, Object> analyzeSalary(String departmentId) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("开始薪资合理性分析, departmentId={}", departmentId);

            // 获取部门员工数据
            List<Employee> employees = employeeMapper.findByDepartmentId(Long.valueOf(departmentId));
            if (employees.isEmpty()) {
                result.put("message", "该部门暂无员工");
                return result;
            }

            // 构建AI分析的上下文数据
            String context = buildSalaryAnalysisContext(employees);

            // 构建提示词
            String systemPrompt = "你是一个专业的薪酬管理专家和HR顾问。\n" +
                "你的任务是分析部门薪资数据，评估薪资结构的合理性。\n" +
                "请从市场竞争、内部公平、绩效导向等维度进行分析。\n\n" +
                "输出格式要求（严格遵循JSON格式）：\n" +
                "{\n" +
                "  \"overallScore\": 数字(0-100),\n" +
                "  \"rating\": \"优秀\"或\"良好\"或\"一般\"或\"需改进\",\n" +
                "  \"analysis\": \"综合分析说明\",\n" +
                "  \"marketComparison\": \"市场对比分析\",\n" +
                "  \"internalEquity\": \"内部公平性分析\",\n" +
                "  \"recommendations\": [\"建议1\", \"建议2\", \"建议3\"],\n" +
                "  \"highEarners\": 员工数量,\n" +
                "  \"lowEarners\": 员工数量,\n" +
                "  \"avgSalary\": 平均薪资数值\n" +
                "}";

            String userPrompt = "请分析以下部门的薪资结构：\n\n" +
                "部门薪资数据：\n" + context + "\n\n" +
                "请根据以上数据，给出专业的薪资评估和优化建议。";

            // 调用AI分析
            String aiResponse = callAI(systemPrompt, userPrompt);

            // 解析AI响应
            JsonNode jsonNode = objectMapper.readTree(aiResponse);
            result.put("departmentId", departmentId);
            result.put("employeeCount", employees.size());
            result.put("overallScore", jsonNode.path("overallScore").asInt());
            result.put("rating", jsonNode.path("rating").asText());
            result.put("analysis", jsonNode.path("analysis").asText());
            result.put("marketComparison", jsonNode.path("marketComparison").asText());
            result.put("internalEquity", jsonNode.path("internalEquity").asText());
            result.put("avgSalary", jsonNode.path("avgSalary").asDouble());
            result.put("highEarners", jsonNode.path("highEarners").asInt());
            result.put("lowEarners", jsonNode.path("lowEarners").asInt());

            List<String> recommendations = objectMapper.convertValue(
                jsonNode.path("recommendations"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            result.put("recommendations", recommendations);

            logger.info("薪资合理性分析完成, rating={}", result.get("rating"));

        } catch (Exception e) {
            logger.error("薪资合理性AI分析失败，使用降级方案", e);
            return getFallbackSalaryAnalysis(departmentId);
        }

        return result;
    }

    @Override
    public Map<String, Object> generateReport(String type) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("开始生成智能报表, type={}", type);

            String systemPrompt;
            String userPrompt;

            switch (type) {
                case "employee":
                    // 员工统计报表
                    List<Employee> employees = employeeMapper.findAll();
                    String employeeContext = buildEmployeeReportContext(employees);

                    systemPrompt = "你是一个专业的人力资源数据分析师。\n" +
                        "你的任务是根据员工数据，生成一份全面、专业的员工统计分析报告。\n\n" +
                        "输出格式要求（严格遵循JSON格式）：\n" +
                        "{\n" +
                        "  \"title\": \"报表标题\",\n" +
                        "  \"summary\": \"总体概况\",\n" +
                        "  \"keyMetrics\": {\n" +
                        "    \"totalEmployees\": 总人数,\n" +
                        "    \"activeEmployees\": 在职人数,\n" +
                        "    \"newHiresThisMonth\": 本月新入职,\n" +
                        "    \"resignationsThisMonth\": 本月离职,\n" +
                        "    \"avgAge\": 平均年龄,\n" +
                        "    \"genderRatio\": \"性别比例\"\n" +
                        "  },\n" +
                        "  \"departmentDistribution\": \"部门分布分析\",\n" +
                        "  \"insights\": [\"洞察1\", \"洞察2\", \"洞察3\"],\n" +
                        "  \"recommendations\": [\"建议1\", \"建议2\", \"建议3\"]\n" +
                        "}";

                    userPrompt = "请根据以下员工数据生成分析报告：\n\n" +
                        employeeContext + "\n\n" +
                        "请生成专业的数据分析报告。";
                    break;

                case "salary":
                    systemPrompt = "你是一个专业的薪酬数据分析师。\n" +
                        "你的任务是生成薪酬统计分析报告的框架和建议。\n\n" +
                        "输出格式要求（严格遵循JSON格式）：\n" +
                        "{\n" +
                        "  \"title\": \"报表标题\",\n" +
                        "  \"summary\": \"报表说明\",\n" +
                        "  \"availableMetrics\": [\"可分析指标1\", \"可分析指标2\"],\n" +
                        "  \"suggestions\": [\"使用建议1\", \"使用建议2\"]\n" +
                        "}";

                    userPrompt = "请生成薪资报表的使用指南和可分析维度说明。";
                    break;

                default:
                    result.put("message", "暂不支持该类型报表");
                    return result;
            }

            // 调用AI生成报告
            String aiResponse = callAI(systemPrompt, userPrompt);
            JsonNode jsonNode = objectMapper.readTree(aiResponse);

            // 解析并构建结果
            result.put("reportType", type);
            result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            if (type.equals("employee")) {
                JsonNode metrics = jsonNode.path("keyMetrics");
                result.put("title", jsonNode.path("title").asText());
                result.put("summary", jsonNode.path("summary").asText());
                result.put("totalEmployees", metrics.path("totalEmployees").asInt());
                result.put("activeEmployees", metrics.path("activeEmployees").asInt());
                result.put("departmentDistribution", jsonNode.path("departmentDistribution").asText());

                List<String> insights = objectMapper.convertValue(
                    jsonNode.path("insights"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                List<String> recommendations = objectMapper.convertValue(
                    jsonNode.path("recommendations"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                result.put("insights", insights);
                result.put("recommendations", recommendations);
            } else if (type.equals("salary")) {
                result.put("title", jsonNode.path("title").asText());
                result.put("summary", jsonNode.path("summary").asText());

                List<String> metrics = objectMapper.convertValue(
                    jsonNode.path("availableMetrics"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                List<String> suggestions = objectMapper.convertValue(
                    jsonNode.path("suggestions"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                result.put("availableMetrics", metrics);
                result.put("suggestions", suggestions);
            }

            logger.info("智能报表生成完成");

        } catch (Exception e) {
            logger.error("智能报表AI生成失败，使用降级方案", e);
            return getFallbackReport(type);
        }

        return result;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 调用AI API的通用方法
     */
    private String callAI(String systemPrompt, String userPrompt) throws Exception {
        // 生成缓存键
        String cacheKey = generateCacheKey(systemPrompt, userPrompt);

        // 检查缓存
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            logger.debug("使用缓存的AI响应");
            return cached.response;
        }

        // 构建请求体
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(systemMessage, userMessage));
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);

        // 调用AI API
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        logger.debug("发送AI请求: {}", jsonBody);

        String response = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(Mono.just(jsonBody), String.class)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        logger.debug("收到AI响应: {}", response);

        // 解析响应
        JsonNode jsonResponse = objectMapper.readTree(response);
        JsonNode choices = jsonResponse.path("choices");

        if (choices.isArray() && choices.size() > 0) {
            String content = choices.get(0).path("message").path("content").asText();

            // 存入缓存
            cache.put(cacheKey, new CacheEntry(content));

            return content;
        }

        throw new RuntimeException("AI API返回格式错误");
    }

    /**
     * 构建聊天系统的提示词
     */
    private String buildChatSystemPrompt() {
        return "你是一个专业的企业管理助手，专门帮助企业管理者解决日常管理问题。\n\n" +
            "你的能力包括：\n" +
            "1. 回答关于人力资源管理的问题\n" +
            "2. 提供考勤管理建议\n" +
            "3. 解释薪酬制度\n" +
            "4. 给出员工管理建议\n" +
            "5. 分析企业数据\n\n" +
            "请用友好、专业的语气回答问题。\n" +
            "如果问题涉及具体数据分析，建议用户使用专门的AI分析功能。";
    }

    /**
     * 构建员工流失风险分析的上下文
     */
    private String buildTurnoverRiskContext(Employee employee, Map<String, Object> statistics) {
        return "- 员工姓名: " + employee.getName() + "\n" +
            "- 部门: " + employee.getDepartmentName() + "\n" +
            "- 职位: " + employee.getPosition() + "\n" +
            "- 入职时间: " + employee.getHireDate() + "\n" +
            "- 状态: " + employee.getStatus() + "\n" +
            "- 联系电话: " + employee.getPhone() + "\n\n" +
            "本月考勤数据:\n" +
            "- 迟到次数: " + statistics.getOrDefault("lateCount", 0) + " 次\n" +
            "- 缺勤次数: " + statistics.getOrDefault("absenceCount", 0) + " 次\n" +
            "- 早退次数: " + statistics.getOrDefault("earlyCount", 0) + " 次\n" +
            "- 正常出勤: " + statistics.getOrDefault("normalDays", 0) + " 天\n\n" +
            "请基于以上数据进行流失风险分析。";
    }

    /**
     * 构建薪资分析的上下文
     */
    private String buildSalaryAnalysisContext(List<Employee> employees) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("部门员工总数: %d人\n\n", employees.size()));

        double totalSalary = 0;
        int highSalaryCount = 0;
        int lowSalaryCount = 0;

        sb.append("薪资分布统计:\n");
        for (Employee emp : employees) {
            if (emp.getSalary() != null) {
                double salary = emp.getSalary().doubleValue();
                totalSalary += salary;

                if (salary >= 15000) highSalaryCount++;
                if (salary <= 5000) lowSalaryCount++;

                sb.append(String.format("- %s (%s): %.2f元\n",
                    emp.getName(), emp.getPosition(), salary));
            }
        }

        double avgSalary = totalSalary / employees.size();

        sb.append(String.format("\n统计摘要:\n"));
        sb.append(String.format("- 平均薪资: %.2f元\n", avgSalary));
        sb.append(String.format("- 高薪员工(≥15000元): %d人\n", highSalaryCount));
        sb.append(String.format("- 低薪员工(≤5000元): %d人\n", lowSalaryCount));
        sb.append(String.format("- 中等薪资员工: %d人\n", employees.size() - highSalaryCount - lowSalaryCount));

        return sb.toString();
    }

    /**
     * 构建员工报表的上下文
     */
    private String buildEmployeeReportContext(List<Employee> employees) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("员工总数: %d人\n\n", employees.size()));

        // 统计数据
        long activeCount = employees.stream().filter(e -> "ACTIVE".equals(e.getStatus())).count();
        long inactiveCount = employees.size() - activeCount;

        sb.append(String.format("在职状态:\n"));
        sb.append(String.format("- 在职员工: %d人\n", activeCount));
        sb.append(String.format("- 离职员工: %d人\n\n", inactiveCount));

        // 部门分布
        sb.append("部门分布:\n");
        employees.stream()
            .collect(java.util.stream.Collectors.groupingBy(Employee::getDepartmentName, java.util.stream.Collectors.counting()))
            .forEach((dept, count) -> sb.append(String.format("- %s: %d人\n", dept, count)));

        return sb.toString();
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String systemPrompt, String userPrompt) {
        return systemPrompt.hashCode() + "_" + userPrompt.hashCode();
    }

    // ==================== 降级处理方法 ====================

    /**
     * 降级响应（当 AI API 调用失败时使用）
     */
    private String getFallbackResponse(String question) {
        if (question.contains("考勤")) {
            return "关于考勤：建议员工每天9点前签到，18点后签退。迟到或早退会影响绩效考核。\n\n（注：AI服务暂时不可用，以上为系统建议）";
        } else if (question.contains("薪资")) {
            return "关于薪资：薪资由基本工资、绩效工资、奖金组成，会根据考勤情况进行扣款。\n\n（注：AI服务暂时不可用，以上为系统建议）";
        } else if (question.contains("请假")) {
            return "关于请假：请提前在系统提交请假申请，等待管理员审批。请假类型包括病假、事假、年假。\n\n（注：AI服务暂时不可用，以上为系统建议）";
        } else {
            return "我是企业管理助手，可以帮您解答关于考勤、薪资、请假等问题。请问有什么可以帮助您的？\n\n（注：AI服务暂时不可用）";
        }
    }

    /**
     * 员工流失风险分析降级方案
     */
    private Map<String, Object> getFallbackTurnoverRiskAnalysis(Long employeeId) {
        Map<String, Object> result = new HashMap<>();

        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }

        Map<String, Object> statistics = attendanceMapper.getStatistics(employeeId,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));

        Integer lateCount = ((Number) statistics.getOrDefault("lateCount", 0)).intValue();
        Integer absenceCount = ((Number) statistics.getOrDefault("absenceCount", 0)).intValue();

        int riskScore = 0;
        String riskLevel = "低";

        if (lateCount > 3 || absenceCount > 2) {
            riskScore = 70;
            riskLevel = "中";
        }
        if (lateCount > 5 || absenceCount > 4) {
            riskScore = 90;
            riskLevel = "高";
        }

        result.put("employeeId", employeeId);
        result.put("employeeName", employee.getName());
        result.put("riskScore", riskScore);
        result.put("riskLevel", riskLevel);
        result.put("analysis", "基于规则的简单分析（AI服务暂时不可用）");
        result.put("suggestions", List.of("建议关注员工考勤状况"));
        result.put("keyFactors", List.of("考勤数据"));
        result.put("lateCount", lateCount);
        result.put("absenceCount", absenceCount);

        return result;
    }

    /**
     * 薪资分析降级方案
     */
    private Map<String, Object> getFallbackSalaryAnalysis(String departmentId) {
        Map<String, Object> result = new HashMap<>();

        List<Employee> employees = employeeMapper.findByDepartmentId(Long.valueOf(departmentId));
        if (employees.isEmpty()) {
            result.put("message", "该部门暂无员工");
            return result;
        }

        double totalSalary = 0;
        int highEarners = 0;
        int lowEarners = 0;

        for (Employee emp : employees) {
            if (emp.getSalary() != null) {
                double salary = emp.getSalary().doubleValue();
                totalSalary += salary;
                if (salary >= 15000) highEarners++;
                if (salary <= 5000) lowEarners++;
            }
        }

        double avgSalary = totalSalary / employees.size();

        result.put("departmentId", departmentId);
        result.put("employeeCount", employees.size());
        result.put("overallScore", 75);
        result.put("rating", "良好");
        result.put("analysis", "基于简单统计分析（AI服务暂时不可用）");
        result.put("marketComparison", "无法进行市场对比");
        result.put("internalEquity", "基于平均值判断");
        result.put("avgSalary", avgSalary);
        result.put("highEarners", highEarners);
        result.put("lowEarners", lowEarners);
        result.put("recommendations", List.of("建议配置AI服务以获取更专业的分析"));

        return result;
    }

    /**
     * 报表生成降级方案
     */
    private Map<String, Object> getFallbackReport(String type) {
        Map<String, Object> result = new HashMap<>();

        if (type.equals("employee")) {
            List<Employee> employees = employeeMapper.findAll();
            result.put("reportType", type);
            result.put("title", "员工统计报表（降级版本）");
            result.put("summary", "AI服务暂时不可用，仅提供基础统计");
            result.put("totalEmployees", employees.size());
            result.put("activeEmployees", employees.stream().filter(e -> "ACTIVE".equals(e.getStatus())).count());
            result.put("departmentDistribution", "请联系管理员配置AI服务");
            result.put("insights", List.of("AI服务不可用"));
            result.put("recommendations", List.of("请检查API配置"));
            result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else if (type.equals("salary")) {
            result.put("reportType", type);
            result.put("title", "薪资统计报表");
            result.put("summary", "请选择具体月份和部门进行详细分析");
            result.put("availableMetrics", List.of("基础统计"));
            result.put("suggestions", List.of("建议配置AI服务"));
            result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        return result;
    }

    /**
     * 缓存条目类
     */
    private static class CacheEntry {
        final String response;
        final long timestamp;

        CacheEntry(String response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            long ageMinutes = (System.currentTimeMillis() - timestamp) / (1000 * 60);
            return ageMinutes > CACHE_EXPIRY_MINUTES;
        }
    }
}
