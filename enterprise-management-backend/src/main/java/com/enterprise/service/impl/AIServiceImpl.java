package com.enterprise.service.impl;

import com.enterprise.dao.AttendanceMapper;
import com.enterprise.dao.EmployeeMapper;
import com.enterprise.dao.SalaryMapper;
import com.enterprise.entity.Employee;
import com.enterprise.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

    @Value("${ai.timeout:60}")
    private Integer timeoutSeconds;

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
        // 配置HttpClient，添加超时设置
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(60))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS)));

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
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

            // 构建提示词（简化版，减少超时风险）
            String systemPrompt = "你是HR专家，分析员工流失风险。" +
                "输出JSON格式：{\"riskScore\":0-100,\"riskLevel\":\"低/中/高\",\"analysis\":\"分析\",\"suggestions\":[\"建议1\",\"建议2\"],\"keyFactors\":[\"因素1\",\"因素2\"]}";

            String userPrompt = "分析员工流失风险：\n" + context + "\n请评估风险并给出建议。";

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

            // 构建提示词（简化版，减少超时风险）
            String systemPrompt = "你是薪酬专家，分析薪资合理性。" +
                "输出JSON：{\"overallScore\":0-100,\"rating\":\"优秀/良好/一般/需改进\",\"analysis\":\"分析\",\"marketComparison\":\"市场对比\",\"internalEquity\":\"内部公平\",\"recommendations\":[\"建议1\",\"建议2\"],\"highEarners\":数量,\"lowEarners\":数量,\"avgSalary\":数值}";

            String userPrompt = "分析部门薪资：\n" + context + "\n给出评估和建议。";

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

            // 先获取基础数据，即使AI失败也能返回
            List<Employee> employees = null;
            if ("employee".equals(type)) {
                long startTime = System.currentTimeMillis();
                employees = employeeMapper.findAll();
                logger.info("查询员工数据耗时: {}ms", System.currentTimeMillis() - startTime);
            }

            String systemPrompt;
            String userPrompt;

            switch (type) {
                case "employee":
                    if (employees == null || employees.isEmpty()) {
                        result.put("message", "暂无员工数据");
                        return result;
                    }

                    String employeeContext = buildEmployeeReportContext(employees);
                    logger.debug("员工上下文: {}", employeeContext);

                    // 极简提示词，减少超时
                    systemPrompt = "你是HR分析师。输出JSON:{\"title\":\"员工报表\",\"summary\":\"概况\",\"keyMetrics\":{\"totalEmployees\":0,\"activeEmployees\":0},\"departmentDistribution\":\"分布\",\"insights\":[\"洞察1\"],\"recommendations\":[\"建议1\"]}";

                    userPrompt = "基于数据生成报告：" + employeeContext;
                    break;

                case "salary":
                    systemPrompt = "输出JSON:{\"title\":\"薪资报表\",\"summary\":\"说明\",\"availableMetrics\":[\"指标1\"],\"suggestions\":[\"建议1\"]}";
                    userPrompt = "生成薪资报表说明。";
                    break;

                default:
                    result.put("message", "暂不支持该类型报表");
                    return result;
            }

            // 调用AI生成报告（带超时控制）
            logger.info("开始调用AI生成报表内容...");
            long aiStartTime = System.currentTimeMillis();

            String aiResponse = callAI(systemPrompt, userPrompt);

            logger.info("AI报表生成完成, 耗时: {}ms", System.currentTimeMillis() - aiStartTime);
            logger.debug("AI响应: {}", aiResponse);

            // 解析JSON响应
            JsonNode jsonNode = objectMapper.readTree(aiResponse);

            // 构建结果
            result.put("reportType", type);
            result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            if (type.equals("employee")) {
                // 安全地提取数据
                result.put("title", jsonNode.path("title").asText("员工统计报表"));
                result.put("summary", jsonNode.path("summary").asText());

                JsonNode metrics = jsonNode.path("keyMetrics");
                result.put("totalEmployees", metrics.path("totalEmployees").asInt(employees.size()));
                result.put("activeEmployees", metrics.path("activeEmployees").asInt());
                result.put("departmentDistribution", jsonNode.path("departmentDistribution").asText());

                // 解析数组
                try {
                    List<String> insights = objectMapper.convertValue(
                        jsonNode.path("insights"),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                    );
                    result.put("insights", insights);
                } catch (Exception e) {
                    result.put("insights", List.of("数据统计完成"));
                }

                try {
                    List<String> recommendations = objectMapper.convertValue(
                        jsonNode.path("recommendations"),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                    );
                    result.put("recommendations", recommendations);
                } catch (Exception e) {
                    result.put("recommendations", List.of("持续关注员工发展"));
                }
            } else if (type.equals("salary")) {
                result.put("title", jsonNode.path("title").asText("薪资统计报表"));
                result.put("summary", jsonNode.path("summary").asText());

                try {
                    List<String> metrics = objectMapper.convertValue(
                        jsonNode.path("availableMetrics"),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                    );
                    result.put("availableMetrics", metrics);
                } catch (Exception e) {
                    result.put("availableMetrics", List.of("基础统计"));
                }

                try {
                    List<String> suggestions = objectMapper.convertValue(
                        jsonNode.path("suggestions"),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                    );
                    result.put("suggestions", suggestions);
                } catch (Exception e) {
                    result.put("suggestions", List.of("定期分析薪资数据"));
                }
            }

            logger.info("智能报表生成完成");

        } catch (Exception e) {
            logger.error("智能报表AI生成失败，使用降级方案，错误: {}", e.getMessage(), e);
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
            logger.info("使用缓存的AI响应");
            return cached.response;
        }

        logger.info("开始调用AI API, URL: {}", apiUrl);

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
        logger.info("发送AI请求, model: {}, max_tokens: {}", model, maxTokens);
        logger.debug("请求体: {}", jsonBody);

        try {
            long startTime = System.currentTimeMillis();

            String response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(Mono.just(jsonBody), String.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            long endTime = System.currentTimeMillis();
            logger.info("AI API调用成功, 耗时: {}ms", (endTime - startTime));
            logger.debug("AI响应: {}", response);

            // 解析响应
            JsonNode jsonResponse = objectMapper.readTree(response);

            // 检查是否有错误
            if (jsonResponse.has("error")) {
                String errorMsg = jsonResponse.path("error").path("message").asText();
                logger.error("AI API返回错误: {}", errorMsg);
                throw new RuntimeException("AI API错误: " + errorMsg);
            }

            JsonNode choices = jsonResponse.path("choices");

            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText();

                // 存入缓存
                cache.put(cacheKey, new CacheEntry(content));

                return content;
            }

            logger.error("AI API返回格式错误, 响应: {}", response);
            throw new RuntimeException("AI API返回格式错误");

        } catch (Exception e) {
            logger.error("AI API调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI API调用失败: " + e.getMessage(), e);
        }
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
        StringBuilder sb = new StringBuilder();

        // 安全地添加员工信息，处理null值
        sb.append("- 员工姓名: ").append(employee.getName() != null ? employee.getName() : "未知").append("\n");
        sb.append("- 部门: ").append(employee.getDepartmentName() != null ? employee.getDepartmentName() : "未分配").append("\n");
        sb.append("- 职位: ").append(employee.getPosition() != null ? employee.getPosition() : "未设置").append("\n");
        sb.append("- 入职时间: ").append(employee.getHireDate() != null ? employee.getHireDate() : "未知").append("\n");
        sb.append("- 状态: ").append(employee.getStatus() != null ? employee.getStatus() : "未知").append("\n");
        sb.append("- 联系电话: ").append(employee.getPhone() != null ? employee.getPhone() : "未填写").append("\n\n");

        // 考勤数据
        sb.append("本月考勤数据:\n");
        sb.append("- 迟到次数: ").append(statistics.getOrDefault("lateCount", 0)).append(" 次\n");
        sb.append("- 缺勤次数: ").append(statistics.getOrDefault("absenceCount", 0)).append(" 次\n");
        sb.append("- 早退次数: ").append(statistics.getOrDefault("earlyCount", 0)).append(" 次\n");
        sb.append("- 正常出勤: ").append(statistics.getOrDefault("normalDays", 0)).append(" 天\n");

        return sb.toString();
    }

    /**
     * 构建薪资分析的上下文
     */
    private String buildSalaryAnalysisContext(List<Employee> employees) {
        StringBuilder sb = new StringBuilder();
        sb.append("部门员工总数: ").append(employees.size()).append("人\n\n");

        double totalSalary = 0;
        int highSalaryCount = 0;
        int lowSalaryCount = 0;
        int salaryCount = 0;

        sb.append("薪资分布统计:\n");
        for (Employee emp : employees) {
            if (emp.getSalary() != null) {
                double salary = emp.getSalary().doubleValue();
                totalSalary += salary;
                salaryCount++;

                if (salary >= 15000) highSalaryCount++;
                if (salary <= 5000) lowSalaryCount++;

                // 安全处理null值
                String name = emp.getName() != null ? emp.getName() : "未知";
                String position = emp.getPosition() != null ? emp.getPosition() : "未设置";

                sb.append("- ").append(name).append(" (").append(position).append("): ")
                    .append(String.format("%.2f", salary)).append("元\n");
            }
        }

        double avgSalary = salaryCount > 0 ? totalSalary / salaryCount : 0;

        sb.append("\n统计摘要:\n");
        sb.append("- 平均薪资: ").append(String.format("%.2f", avgSalary)).append("元\n");
        sb.append("- 高薪员工(≥15000元): ").append(highSalaryCount).append("人\n");
        sb.append("- 低薪员工(≤5000元): ").append(lowSalaryCount).append("人\n");
        sb.append("- 中等薪资员工: ").append(employees.size() - highSalaryCount - lowSalaryCount).append("人\n");

        return sb.toString();
    }

    /**
     * 构建员工报表的上下文
     */
    private String buildEmployeeReportContext(List<Employee> employees) {
        StringBuilder sb = new StringBuilder();
        sb.append("员工总数: ").append(employees.size()).append("人\n\n");

        // 统计数据
        long activeCount = employees.stream().filter(e -> "ACTIVE".equals(e.getStatus())).count();
        long inactiveCount = employees.size() - activeCount;

        sb.append("在职状态:\n");
        sb.append("- 在职员工: ").append(activeCount).append("人\n");
        sb.append("- 离职员工: ").append(inactiveCount).append("人\n\n");

        // 部门分布（处理null值）
        sb.append("部门分布:\n");
        employees.stream()
            .filter(e -> e.getDepartmentName() != null) // 过滤掉null值
            .collect(java.util.stream.Collectors.groupingBy(Employee::getDepartmentName, java.util.stream.Collectors.counting()))
            .forEach((dept, count) -> sb.append("- ").append(dept).append(": ").append(count).append("人\n"));

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
     * 报表生成降级方案（快速返回）
     */
    private Map<String, Object> getFallbackReport(String type) {
        logger.warn("使用报表生成降级方案, type={}", type);
        Map<String, Object> result = new HashMap<>();

        if (type.equals("employee")) {
            try {
                // 快速查询，设置超时
                long startTime = System.currentTimeMillis();
                List<Employee> employees = employeeMapper.findAll();
                logger.info("降级方案查询员工数据耗时: {}ms", System.currentTimeMillis() - startTime);

                long activeCount = employees.stream().filter(e -> "ACTIVE".equals(e.getStatus())).count();

                result.put("reportType", type);
                result.put("title", "员工统计报表");
                result.put("summary", "基础数据统计");
                result.put("totalEmployees", employees.size());
                result.put("activeEmployees", activeCount);
                result.put("departmentDistribution", "各部门数据分布正常");
                result.put("insights", List.of(
                    "在职员工: " + activeCount + "人",
                    "离职员工: " + (employees.size() - activeCount) + "人"
                ));
                result.put("recommendations", List.of("定期更新员工信息", "关注考勤数据"));
                result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (Exception e) {
                logger.error("降级方案也失败了，返回最小化数据", e);
                result.put("reportType", type);
                result.put("title", "员工统计报表");
                result.put("summary", "数据查询中...");
                result.put("totalEmployees", 0);
                result.put("activeEmployees", 0);
                result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        } else if (type.equals("salary")) {
            result.put("reportType", type);
            result.put("title", "薪资统计报表");
            result.put("summary", "请选择具体月份和部门进行详细分析");
            result.put("availableMetrics", List.of("基础统计", "部门对比", "趋势分析"));
            result.put("suggestions", List.of("选择月份查看详细数据", "对比不同部门薪资水平"));
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
