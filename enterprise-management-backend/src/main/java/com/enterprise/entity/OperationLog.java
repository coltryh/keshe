package com.enterprise.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志实体类（AOP 日志记录使用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {

    private Long id;
    private String username;  // 操作用户
    private String operation;  // 操作类型
    private String method;  // 调用方法
    private String params;  // 参数
    private String ip;  // IP 地址
    private Long executeTime;  // 执行时长（毫秒）
    private LocalDateTime createTime;
}
