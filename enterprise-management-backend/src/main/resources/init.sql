-- ===============================================
-- 智能企业管理系统 - 数据库初始化脚本
-- ===============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS enterprise_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE enterprise_management;

-- ===============================================
-- 1. 用户表
-- ===============================================
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN, USER',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '用户表';

-- 插入默认管理员账号（用户名：admin，密码：admin123）
INSERT INTO t_user (username, password, email, role) VALUES
('admin', 'admin123', 'admin@enterprise.com', 'ADMIN'),
('user', 'user123', 'user@enterprise.com', 'USER');

-- ===============================================
-- 2. 部门表
-- ===============================================
DROP TABLE IF EXISTS t_department;
CREATE TABLE t_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    name VARCHAR(50) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT NULL COMMENT '父部门ID',
    description VARCHAR(200) COMMENT '部门描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '部门表';

-- 插入测试数据
INSERT INTO t_department (name, parent_id, description) VALUES
('技术部', NULL, '负责产品研发'),
('市场部', NULL, '负责市场营销'),
('人事部', NULL, '负责人力资源'),
('研发组', 1, '负责核心研发'),
('测试组', 1, '负责产品测试');

-- ===============================================
-- 3. 员工表
-- ===============================================
DROP TABLE IF EXISTS t_employee;
CREATE TABLE t_employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '员工ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    department_id BIGINT COMMENT '部门ID',
    position VARCHAR(50) COMMENT '职位',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    hire_date DATE COMMENT '入职日期',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, RESIGNED',
    salary DECIMAL(10, 2) DEFAULT 5000.00 COMMENT '基本工资',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (department_id) REFERENCES t_department(id)
) COMMENT '员工表';

-- 插入测试数据
INSERT INTO t_employee (name, gender, age, department_id, position, phone, email, hire_date, salary) VALUES
('张三', '男', 28, 1, 'Java开发工程师', '13800138001', 'zhangsan@enterprise.com', '2023-01-15', 8000.00),
('李四', '女', 26, 1, '前端开发工程师', '13800138002', 'lisi@enterprise.com', '2023-03-20', 7500.00),
('王五', '男', 30, 2, '市场经理', '13800138003', 'wangwu@enterprise.com', '2022-06-10', 10000.00),
('赵六', '女', 25, 3, 'HR专员', '13800138004', 'zhaoliu@enterprise.com', '2023-05-12', 6000.00),
('孙七', '男', 27, 1, '测试工程师', '13800138005', 'sunqi@enterprise.com', '2023-02-28', 7000.00);

-- ===============================================
-- 4. 考勤表
-- ===============================================
DROP TABLE IF EXISTS t_attendance;
CREATE TABLE t_attendance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '考勤ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    employee_name VARCHAR(50) COMMENT '员工姓名',
    date DATE NOT NULL COMMENT '考勤日期',
    checkin_time DATETIME COMMENT '签到时间',
    checkout_time DATETIME COMMENT '签退时间',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态：NORMAL, LATE, EARLY_LEAVE, ABSENCE',
    work_hours VARCHAR(20) COMMENT '工作时长',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (employee_id) REFERENCES t_employee(id),
    UNIQUE KEY uk_employee_date (employee_id, date)
) COMMENT '考勤表';

-- 插入测试数据
INSERT INTO t_attendance (employee_id, employee_name, date, checkin_time, checkout_time, status, work_hours) VALUES
(1, '张三', CURDATE(), DATE_ADD(NOW(), INTERVAL -3 HOUR), DATE_ADD(NOW(), INTERVAL -1 HOUR), 'NORMAL', '8小时0分钟'),
(2, '李四', CURDATE(), DATE_ADD(NOW(), INTERVAL -2 HOUR), NULL, 'LATE', NULL),
(3, '王五', CURDATE(), DATE_ADD(NOW(), INTERVAL -4 HOUR), DATE_ADD(NOW(), INTERVAL -30 MINUTE), 'NORMAL', '8小时30分钟');

-- ===============================================
-- 5. 薪资表
-- ===============================================
DROP TABLE IF EXISTS t_salary;
CREATE TABLE t_salary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '薪资ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    employee_name VARCHAR(50) COMMENT '员工姓名',
    month VARCHAR(10) NOT NULL COMMENT '薪资月份（格式：2024-01）',
    base_salary DECIMAL(10, 2) COMMENT '基本工资',
    performance_salary DECIMAL(10, 2) COMMENT '绩效工资',
    bonus DECIMAL(10, 2) COMMENT '奖金',
    deduction DECIMAL(10, 2) COMMENT '扣款',
    total_salary DECIMAL(10, 2) COMMENT '实发工资',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING, PAID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (employee_id) REFERENCES t_employee(id),
    UNIQUE KEY uk_employee_month (employee_id, month)
) COMMENT '薪资表';

-- 插入测试数据
INSERT INTO t_salary (employee_id, employee_name, month, base_salary, performance_salary, bonus, deduction, total_salary, status) VALUES
(1, '张三', '2024-12', 8000.00, 2000.00, 1000.00, 50.00, 10950.00, 'PAID'),
(2, '李四', '2024-12', 7500.00, 2000.00, 1000.00, 100.00, 10400.00, 'PAID'),
(3, '王五', '2024-12', 10000.00, 2000.00, 1000.00, 0.00, 13000.00, 'PAID');

-- ===============================================
-- 6. 请假申请表
-- ===============================================
DROP TABLE IF EXISTS t_leave_application;
CREATE TABLE t_leave_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '请假ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    employee_name VARCHAR(50) COMMENT '员工姓名',
    leave_type VARCHAR(20) NOT NULL COMMENT '请假类型：SICK, PERSONAL, ANNUAL',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    days DECIMAL(5, 1) COMMENT '请假天数',
    reason VARCHAR(500) COMMENT '请假原因',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING, APPROVED, REJECTED',
    approver VARCHAR(50) COMMENT '审批人',
    approval_comment VARCHAR(200) COMMENT '审批意见',
    approval_time DATETIME COMMENT '审批时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (employee_id) REFERENCES t_employee(id)
) COMMENT '请假申请表';

-- 插入测试数据
INSERT INTO t_leave_application (employee_id, employee_name, leave_type, start_time, end_time, days, reason, status) VALUES
(1, '张三', 'ANNUAL', '2025-01-15 09:00:00', '2025-01-17 18:00:00', 3.0, '家中有事需要请假', 'APPROVED'),
(2, '李四', 'SICK', '2025-01-10 09:00:00', '2025-01-10 18:00:00', 1.0, '身体不适需要就医', 'APPROVED'),
(4, '赵六', 'PERSONAL', '2025-01-20 09:00:00', '2025-01-20 18:00:00', 1.0, '个人事务', 'PENDING');

-- ===============================================
-- 7. 操作日志表（AOP 日志记录使用）
-- ===============================================
DROP TABLE IF EXISTS t_operation_log;
CREATE TABLE t_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    username VARCHAR(50) COMMENT '操作用户',
    operation VARCHAR(100) COMMENT '操作类型',
    method VARCHAR(200) COMMENT '调用方法',
    params TEXT COMMENT '参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    execute_time BIGINT COMMENT '执行时长（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '操作日志表';

-- ===============================================
-- 查看数据
-- ===============================================
SELECT '用户数据：' AS info;
SELECT * FROM t_user;

SELECT '部门数据：' AS info;
SELECT * FROM t_department;

SELECT '员工数据：' AS info;
SELECT * FROM t_employee;

SELECT '考勤数据：' AS info;
SELECT * FROM t_attendance;

SELECT '薪资数据：' AS info;
SELECT * FROM t_salary;

SELECT '请假申请数据：' AS info;
SELECT * FROM t_leave_application;
