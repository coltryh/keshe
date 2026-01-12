package com.enterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智能企业管理系统 - 主启动类
 */
@SpringBootApplication
public class EnterpriseManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseManagementApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("智能企业管理系统启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("========================================\n");
    }
}
