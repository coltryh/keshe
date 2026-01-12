# 智能企业管理系统

一个基于 **Spring Boot + Vue 3** 的智能企业管理系统，适用于课程设计水平。系统整合了 RESTful API、MVC 设计模式、ORM、依赖注入、AOP 等技术，并集成简单的 AI 功能。

---

## 项目概述

这是一个完整的企业管理系统课设项目，包含前端和后端两部分，实现员工管理、考勤管理、薪资管理、请假申请等核心功能，并集成 AI 智能分析功能。

---

## 技术栈

### 后端
- Spring Boot 3.2
- MyBatis（ORM 对象关系映射）
- Maven（依赖管理）
- MySQL 8.0
- Spring AOP（面向切面编程）
- JWT Token 认证
- WebFlux（HTTP 客户端）

### 前端
- Vue 3（Composition API）
- Vite（构建工具）
- Element Plus（UI 组件库）
- Vue Router（路由管理）
- Axios（HTTP 请求）

---

## 项目结构

```
keshe/
├── enterprise-management-backend/     # 后端项目
│   ├── src/main/java/com/enterprise/
│   │   ├── controller/                # Controller 层
│   │   ├── service/                   # Service 层
│   │   ├── dao/                       # Dao 层
│   │   ├── entity/                    # 实体类
│   │   ├── aspect/                    # AOP 切面
│   │   └── ...
│   ├── src/main/resources/
│   │   ├── mapper/                    # MyBatis XML
│   │   ├── application.yml            # 配置文件
│   │   └── init.sql                   # 数据库脚本
│   └── pom.xml
│
└── enterprise-management-frontend/    # 前端项目
    ├── src/
    │   ├── views/                     # 页面组件
    │   ├── api/                       # API 接口
    │   ├── router/                    # 路由配置
    │   └── ...
    ├── index.html
    ├── vite.config.js
    └── package.json
```

---

## 核心功能

### 1. 用户管理
- 用户注册/登录
- JWT Token 认证
- 用户信息管理

### 2. 员工管理
- 员工信息增删改查
- 部门管理
- 分页查询

### 3. 考勤管理
- 员工签到/签退
- 考勤记录查询
- 考勤统计分析

### 4. 薪资管理
- 薪资自动计算
- 薪资记录查询
- 薪资明细展示

### 5. 请假申请
- 提交请假申请
- 管理员审批
- 审批流程管理

### 6. AI 智能分析 ⭐
- AI 智能问答助手
- 员工流失风险分析
- 薪资合理性分析
- 智能报表生成

---

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Node.js 16+
- npm 或 yarn

### 2. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source C:/Users/2471197/keshe/enterprise-management-backend/src/main/resources/init.sql
```

### 3. 启动后端

```bash
cd C:/Users/2471197/keshe/enterprise-management-backend

# 修改数据库密码（如需要）
# 编辑 src/main/resources/application.yml

# 启动项目
mvn spring-boot:run
```

后端访问：http://localhost:8080

### 4. 启动前端

```bash
cd C:/Users/2471197/keshe/enterprise-management-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端访问：http://localhost:3000

### 5. 登录系统

- 管理员账号：admin / admin123
- 普通用户：user / user123

---

## 技术亮点

### 后端技术实现

| 技术 | 说明 | 应用位置 |
|------|------|----------|
| MVC 设计模式 | Controller → Service → Dao 分层 | 所有业务模块 |
| 依赖注入（DI） | @Autowired 自动装配 | Service、Dao 层 |
| AOP 切面编程 | LogAspect 自动记录操作日志 | 所有 Controller |
| ORM 映射 | MyBatis 实体类与数据库表映射 | Entity、Mapper |
| RESTful API | 标准化接口设计 | Controller 层 |
| JWT 认证 | Token 用户认证 | AuthController |

### 前端技术实现

| 技术 | 说明 | 应用位置 |
|------|------|----------|
| Vue 3 组合式 API | 更好的代码组织 | 所有页面组件 |
| 路由守卫 | 未登录自动跳转 | router/index.js |
| Axios 拦截器 | 统一请求/响应处理 | utils/request.js |
| 组件化开发 | 可复用的页面组件 | views/ 目录 |
| 响应式设计 | 适配各种屏幕 | CSS Flexbox |

---

## 数据库设计

| 表名 | 说明 | 字段数 |
|------|------|--------|
| t_user | 用户表 | 7 |
| t_department | 部门表 | 6 |
| t_employee | 员工表 | 13 |
| t_attendance | 考勤表 | 9 |
| t_salary | 薪资表 | 12 |
| t_leave_application | 请假申请表 | 12 |
| t_operation_log | 操作日志表（AOP） | 8 |

---

## AI 功能集成

系统集成了智谱 AI API，实现以下功能：

1. **AI 智能问答**：回答企业管理相关问题
2. **流失风险分析**：基于考勤数据预测员工流失风险
3. **薪资合理性分析**：分析部门薪资分布是否合理
4. **智能报表生成**：自动生成数据统计报表

配置 AI API：

```yaml
# backend/src/main/resources/application.yml
ai:
  api-key: your-ai-api-key-here
  api-url: https://open.bigmodel.cn/api/paas/v4/chat/completions
  model: glm-4-flash
```

---

## 开发文档

- [后端开发文档](./enterprise-management-backend/README.md)
- [前端开发文档](./enterprise-management-frontend/README.md)
- [功能设计文档](./智能企业管理系统-功能设计文档.md)

---

## 适用场景

✅ 课程设计作业
✅ 毕业设计项目
✅ 编程学习参考
✅ 技术栈学习实践

---

## 课设亮点总结

1. **完整的 MVC 架构**：清晰的分层设计
2. **AOP 切面编程**：自动记录操作日志
3. **ORM 对象关系映射**：MyBatis 实现
4. **RESTful API 设计**：标准化的接口
5. **AI 智能集成**：创新的功能亮点
6. **前后端分离**：现代化的开发模式
7. **响应式 UI**：美观的用户界面
8. **JWT Token 认证**：安全的用户认证
9. **分页查询**：实用的功能实现
10. **统一异常处理**：GlobalExceptionHandler

---

**项目创建时间：2025-01-12**
**开发周期：约 2 周**
**适用水平：课程设计、本科毕业设计**
