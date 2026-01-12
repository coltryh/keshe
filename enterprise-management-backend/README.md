# 智能企业管理系统 - 后端

基于 Spring Boot + MyBatis 的智能企业管理系统后端项目，整合了 RESTful API、MVC 设计模式、ORM、依赖注入、AOP 等技术，并集成简单的 AI 功能。

---

## 技术栈

- **Spring Boot 3.2** - 核心框架
- **MyBatis** - ORM 框架
- **Maven** - 依赖管理
- **MySQL 8.0** - 数据库
- **Spring AOP** - 面向切面编程（日志记录）
- **JWT** - Token 认证
- **WebFlux** - HTTP 客户端（调用 AI API）

---

## 项目结构

```
enterprise-management-backend/
├── src/main/java/com/enterprise/
│   ├── EnterpriseManagementApplication.java    # 主启动类
│   ├── controller/                              # Controller 层（MVC）
│   │   ├── AuthController.java                  # 认证接口
│   │   ├── UserController.java                  # 用户管理接口
│   │   ├── EmployeeController.java              # 员工管理接口
│   │   ├── AttendanceController.java            # 考勤管理接口
│   │   ├── SalaryController.java                # 薪资管理接口
│   │   ├── LeaveController.java                 # 请假申请接口
│   │   └── AIController.java                    # AI 智能分析接口
│   ├── service/                                 # Service 接口层
│   ├── service/impl/                            # Service 实现层
│   ├── dao/                                     # Dao 层（数据访问）
│   ├── entity/                                  # 实体类（ORM）
│   ├── dto/                                     # 数据传输对象
│   ├── config/                                  # 配置类
│   ├── aspect/                                  # AOP 切面（日志）
│   ├── util/                                    # 工具类
│   └── common/                                  # 公共类（异常处理）
├── src/main/resources/
│   ├── application.yml                          # 配置文件
│   ├── init.sql                                 # 数据库初始化脚本
│   └── mapper/                                  # MyBatis XML 映射文件
└── pom.xml                                      # Maven 依赖配置
```

---

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 2. 数据库配置

#### 创建数据库并导入数据

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source C:/Users/2471197/keshe/enterprise-management-backend/src/main/resources/init.sql
```

#### 修改数据库配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/enterprise_management?...
    username: root
    password: 你的密码
```

### 3. 运行项目

#### 方式一：使用 Maven 命令

```bash
cd C:/Users/2471197/keshe/enterprise-management-backend
mvn spring-boot:run
```

#### 方式二：使用 IDE

1. 使用 IntelliJ IDEA 或 Eclipse 打开项目
2. 找到 `EnterpriseManagementApplication.java`
3. 右键 → Run 'EnterpriseManagementApplication'

### 4. 访问接口

项目启动成功后，访问：http://localhost:8080

默认测试账号：
- 管理员：admin / admin123
- 普通用户：user / user123

---

## RESTful API 接口文档

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/register | 用户注册 |

### 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/users | 获取用户列表（分页） |
| GET | /api/users/{id} | 获取用户详情 |
| POST | /api/users | 创建用户 |
| PUT | /api/users/{id} | 更新用户 |
| DELETE | /api/users/{id} | 删除用户 |

### 员工管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/employees | 获取员工列表（分页） |
| GET | /api/employees/all | 获取所有员工 |
| GET | /api/employees/{id} | 获取员工详情 |
| POST | /api/employees | 创建员工 |
| PUT | /api/employees/{id} | 更新员工 |
| DELETE | /api/employees/{id} | 删除员工 |

### 考勤管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/attendance/checkin | 员工签到 |
| POST | /api/attendance/checkout | 员工签退 |
| GET | /api/attendance/records | 查询考勤记录 |
| GET | /api/attendance/statistics | 考勤统计 |

### 薪资管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/salary/calculate | 计算薪资 |
| GET | /api/salary/records | 查询薪资记录 |
| GET | /api/salary/employee/{id} | 查询员工薪资 |

### 请假申请

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/leave/apply | 提交请假申请 |
| PUT | /api/leave/{id}/approve | 审批请假申请 |
| GET | /api/leave/records | 查询请假记录 |

### AI 智能分析 ⭐

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/ai/chat | AI 智能问答 |
| POST | /api/ai/analyze/turnover | 员工流失风险分析 |
| POST | /api/ai/analyze/salary | 薪资合理性分析 |
| POST | /api/ai/report/generate | 生成智能报表 |

---

## 核心技术实现

### MVC 设计模式

```java
@RestController  // Controller 层：接收 HTTP 请求
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;  // 依赖注入

    @GetMapping
    public Result<?> getUserList(PageRequest pageRequest) {
        return Result.success(userService.findByPage(pageRequest));
    }
}

@Service  // Service 层：业务逻辑
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;  // 依赖注入

    public Map<String, Object> findByPage(PageRequest pageRequest) {
        // 业务逻辑处理
    }
}

@Mapper  // Dao 层：数据访问（ORM）
public interface UserMapper {
    List<User> findByPage(PageRequest pageRequest);
}
```

### AOP 面向切面编程（日志记录）

```java
@Aspect
@Component
public class LogAspect {
    @Around("execution(* com.enterprise.controller..*.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 记录方法执行信息
        long beginTime = System.currentTimeMillis();
        Object result = point.proceed();
        long executeTime = System.currentTimeMillis() - beginTime;

        // 保存到数据库
        operationLogMapper.insert(log);
        return result;
    }
}
```

### ORM 对象关系映射（MyBatis）

```xml
<!-- UserMapper.xml -->
<select id="findByPage" resultMap="UserResultMap">
    SELECT * FROM t_user
    <where>
        <if test="keyword != null and keyword != ''">
            AND username LIKE CONCAT('%', #{keyword}, '%')
        </if>
    </where>
    LIMIT #{pageSize} OFFSET #{pageNum}
</select>
```

---

## AI 集成说明

项目集成了智谱 AI API，实现智能分析功能：

1. **AI 智能问答**：回答企业管理相关问题
2. **流失风险分析**：基于考勤数据预测员工流失风险
3. **薪资合理性分析**：分析部门薪资分布
4. **智能报表生成**：自动生成数据统计报表

配置 AI API Key：

```yaml
# application.yml
ai:
  api-key: your-ai-api-key-here
  api-url: https://open.bigmodel.cn/api/paas/v4/chat/completions
  model: glm-4-flash
```

---

## 数据库表设计

| 表名 | 说明 |
|------|------|
| t_user | 用户表 |
| t_department | 部门表 |
| t_employee | 员工表 |
| t_attendance | 考勤表 |
| t_salary | 薪资表 |
| t_leave_application | 请假申请表 |
| t_operation_log | 操作日志表（AOP 自动记录） |

---

## 常见问题

### 1. 端口被占用

修改 `application.yml` 中的端口号：

```yaml
server:
  port: 8081  # 改为其他端口
```

### 2. 数据库连接失败

检查 MySQL 服务是否启动，以及用户名密码是否正确。

### 3. Maven 依赖下载失败

配置 Maven 阿里云镜像：

```xml
<mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

---

## 课设亮点总结

✅ **MVC 设计模式**：Controller → Service → Dao 清晰分层
✅ **依赖注入（DI）**：Spring @Autowired 自动装配
✅ **面向切面编程（AOP）**：自动记录操作日志
✅ **ORM 对象关系映射**：MyBatis 实现
✅ **RESTful API**：标准化的接口设计
✅ **AI 智能分析**：集成智谱 AI 实现智能功能
✅ **JWT Token 认证**：安全的用户认证
✅ **统一异常处理**：GlobalExceptionHandler
✅ **分页查询**：支持条件查询和排序
✅ **跨域支持**：WebConfig 配置

---

**开发完成时间：2025-01-12**
**适用场景：课程设计、毕业设计、学习参考**
