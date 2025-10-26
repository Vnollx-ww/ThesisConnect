import Vue from "vue";
import VueRouter from "vue-router";

import Layout from "@/components/Layout";

Vue.use(VueRouter);

const router = new VueRouter({
    mode: 'hash', // 使用 hash 模式
    routes: [
        {
            path: '/',
            redirect: '/login'
        },
        {
            path: '/login',
            name: '系统登录',
            component: () => import('@/views/login.vue'),
        },
        {
            path: '/register',
            name: '用户注册',
            component: () => import('@/views/register.vue'),
        },
        //嵌套路由 - 学生
        {
            path: '/student',
            name: '学生系统',
            component: Layout,
            redirect: '/student/topics',
            children:[
                // 学生路由
                {
                    path: 'topics',
                    name: '选题列表',
                    component: () => import('@/views/student/Topics.vue')
                },
                {
                    path: 'my-topic',
                    name: '我的选题',
                    component: () => import('@/views/student/MyTopic.vue')
                },
                {
                    path: 'profile',
                    name: '个人信息',
                    component: () => import('@/views/student/Profile.vue')
                }
            ]
        },
        
        //嵌套路由 - 教师
        {
            path: '/teacher',
            name: '教师系统',
            component: Layout,
            redirect: '/teacher/topics',
            children:[
                // 教师路由
                {
                    path: 'topics',
                    name: '课题管理',
                    component: () => import('@/views/teacher/Topics.vue')
                },
                {
                    path: 'students',
                    name: '学生管理',
                    component: () => import('@/views/teacher/Students.vue')
                },
                {
                    path: 'reports',
                    name: '统计报表',
                    component: () => import('@/views/teacher/Reports.vue')
                },
                {
                    path: 'profile',
                    name: '个人设置',
                    component: () => import('@/views/teacher/Profile.vue')
                }
            ]
        },
        
        //嵌套路由 - 管理员
        {
            path: '/admin',
            name: '管理员系统',
            component: Layout,
            redirect: '/admin/dashboard',
            children:[
                // 管理员路由
                {
                    path: 'dashboard',
                    name: '数据概览',
                    component: () => import('@/views/admin/Dashboard.vue')
                },
                {
                    path: 'users',
                    name: '用户管理',
                    component: () => import('@/views/admin/Users.vue')
                },
                {
                    path: 'topics',
                    name: '课题管理',
                    component: () => import('@/views/admin/Topics.vue')
                },
                {
                    path: 'system',
                    name: '系统设置',
                    component: () => import('@/views/admin/System.vue')
                }
            ]
        },
        
        // 兼容旧路由（已废弃，保留以便向后兼容）
        {
            path: '/layout',
            redirect: '/login' // 重定向到登录页
        },
    ]
});

// 路由守卫 - 检查token
router.beforeEach((to, from, next) => {
    // 获取token
    const token = localStorage.getItem('token');
    
    // 如果访问登录或注册页面，直接放行
    if (to.path === '/login' || to.path === '/register') {
        // 如果已经登录，访问登录页面时根据角色重定向
        if (token && to.path === '/login') {
            const userRole = localStorage.getItem('role');
            if (userRole === 'student') {
                next('/student/topics');
            } else if (userRole === 'teacher') {
                next('/teacher/topics');
            } else if (userRole === 'admin') {
                next('/admin/dashboard');
            } else {
                next('/admin/dashboard'); // 默认管理员
            }
            return;
        }
        next();
        return;
    }
    
    // 如果没有token，重定向到登录页面
    if (!token) {
        next('/login');
        return;
    }
    
    // 有token，正常访问
    next();
});

export default router;