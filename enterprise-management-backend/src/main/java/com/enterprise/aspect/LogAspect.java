package com.enterprise.aspect;

import com.enterprise.dao.OperationLogMapper;
import com.enterprise.entity.OperationLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 日志记录切面（AOP 面向切面编程）
 * 自动记录用户操作日志
 */
@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 定义切点：拦截所有 Controller 方法
     */
    @Pointcut("execution(* com.enterprise.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：记录方法执行信息
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return point.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        // 获取方法信息
        String className = point.getTarget().getClass().getSimpleName();
        String methodName = point.getSignature().getName();

        // 获取参数
        Object[] args = point.getArgs();
        String params = Arrays.toString(args);

        // 执行方法
        Object result = point.proceed();

        // 计算执行时长
        long executeTime = System.currentTimeMillis() - beginTime;

        // 获取用户信息（从请求头或 token 中获取）
        String username = request.getHeader("username");
        if (username == null || username.isEmpty()) {
            username = "anonymous";
        }

        // 获取 IP 地址
        String ip = getClientIp(request);

        // 保存日志
        OperationLog log = new OperationLog();
        log.setUsername(username);
        log.setOperation(className + "." + methodName);
        log.setMethod(className + "." + methodName + "()");
        log.setParams(params.length() > 1000 ? params.substring(0, 1000) : params);
        log.setIp(ip);
        log.setExecuteTime(executeTime);
        log.setCreateTime(LocalDateTime.now());

        try {
            operationLogMapper.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
