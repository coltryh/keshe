# 智能企业管理系统 - 前端

基于 Vue 3 + Element Plus 的智能企业管理系统前端项目，提供简洁美观的用户界面。

---

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 新一代前端构建工具
- **Element Plus** - Vue 3 组件库
- **Vue Router** - 路由管理
- **Axios** - HTTP 客户端

---

## 项目结构

```
enterprise-management-frontend/
├── src/
│   ├── views/                    # 页面组件
│   │   ├── Login.vue             # 登录注册页面
│   │   ├── Layout.vue            # 主布局组件
│   │   ├── Dashboard.vue         # 首页仪表盘
│   │   ├── EmployeeManagement.vue    # 员工管理页面
│   │   ├── AttendanceManagement.vue  # 考勤管理页面
│   │   ├── SalaryManagement.vue      # 薪资管理页面
│   │   ├── LeaveManagement.vue       # 请假申请页面
│   │   └── AIAnalysis.vue            # AI 智能分析页面
│   ├── api/                      # API 接口封装
│   │   ├── auth.js               # 认证接口
│   │   ├── employee.js           # 员工接口
│   │   ├── attendance.js         # 考勤接口
│   │   ├── salary.js             # 薪资接口
│   │   ├── leave.js              # 请假接口
│   │   └── ai.js                 # AI 接口
│   ├── router/                   # 路由配置
│   ├── utils/                    # 工具函数
│   ├── assets/                   # 静态资源
│   ├── App.vue                   # 根组件
│   └── main.js                   # 入口文件
├── index.html                    # HTML 入口
├── vite.config.js                # Vite 配置
└── package.json                  # 依赖配置
```

---

## 快速开始

### 1. 安装依赖

```bash
cd C:/Users/2471197/keshe/enterprise-management-frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问：http://localhost:3000

### 3. 构建生产版本

```bash
npm run build
```

---

## 功能模块

### 1. 登录注册
- 用户登录/注册
- JWT Token 认证
- 路由守卫保护

### 2. 首页仪表盘
- 数据统计概览
- 快捷操作入口
- 系统公告

### 3. 员工管理
- 员工列表查询（分页、搜索）
- 新增/编辑/删除员工
- 员工信息管理

### 4. 考勤管理
- 在线签到/签退
- 考勤记录查询
- 考勤统计分析

### 5. 薪资管理
- 薪资自动计算
- 薪资记录查询
- 薪资详情展示

### 6. 请假申请
- 提交请假申请
- 审批流程管理
- 请假记录查询

### 7. AI 智能分析 ⭐
- AI 智能问答助手
- 员工流失风险分析
- 薪资合理性分析
- 智能报表生成

---

## 默认账号

- 管理员：admin / admin123
- 普通用户：user / user123

---

## API 配置

前端通过 Vite 代理转发请求到后端：

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

---

## 页面截图说明

### 登录页面
- 支持登录和注册切换
- 表单验证
- 美观的渐变背景

### 主布局
- 左侧导航菜单
- 顶部用户信息
- 内容区域展示

### 员工管理
- 表格展示员工列表
- 分页查询
- 新增/编辑对话框
- 删除确认

### 考勤管理
- 签到/签退卡片
- 实时时间显示
- 考勤统计
- 考勤记录表格

### 薪资管理
- 员工选择器
- 月份选择器
- 薪资计算功能
- 薪资详情展示

### AI 智能分析
- AI 对话界面
- 智能分析工具
- 常见问题快捷标签
- 分析结果展示

---

## 技术特点

✅ **Vue 3 Composition API** - 更好的代码组织
✅ **Element Plus 组件库** - 丰富的 UI 组件
✅ **响应式设计** - 适配各种屏幕尺寸
✅ **路由守卫** - 未登录自动跳转
✅ **Axios 拦截器** - 统一的请求处理
✅ **模块化 API** - 清晰的接口封装

---

**开发完成时间：2025-01-12**
**适用场景：课程设计、毕业设计、学习参考**
