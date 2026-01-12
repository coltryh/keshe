import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'employees',
        name: 'Employees',
        component: () => import('@/views/EmployeeManagement.vue'),
        meta: { title: '员工管理' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/AttendanceManagement.vue'),
        meta: { title: '考勤管理' }
      },
      {
        path: 'salary',
        name: 'Salary',
        component: () => import('@/views/SalaryManagement.vue'),
        meta: { title: '薪资管理' }
      },
      {
        path: 'leave',
        name: 'Leave',
        component: () => import('@/views/LeaveManagement.vue'),
        meta: { title: '请假申请' }
      },
      {
        path: 'ai',
        name: 'AI',
        component: () => import('@/views/AIAnalysis.vue'),
        meta: { title: 'AI 智能分析' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
