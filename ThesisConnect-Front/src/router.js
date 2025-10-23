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
        //嵌套路由
        {
            path: '/layout',
            name: '系统管理',
            component: Layout,
            children:[
                // 学生路由
                {
                    path: 'student/topics',
                    name: '选题列表',
                    component: () => import('@/views/student/Topics.vue')
                },
                {
                    path: 'student/my-topic',
                    name: '我的选题',
                    component: () => import('@/views/student/MyTopic.vue')
                },
                {
                    path: 'student/profile',
                    name: '个人信息',
                    component: () => import('@/views/student/Profile.vue')
                },
                
                // 教师路由
                {
                    path: 'teacher/topics',
                    name: '课题管理',
                    component: () => import('@/views/teacher/Topics.vue')
                },
                {
                    path: 'teacher/students',
                    name: '学生管理',
                    component: () => import('@/views/teacher/Students.vue')
                },
                {
                    path: 'teacher/reports',
                    name: '统计报表',
                    component: () => import('@/views/teacher/Reports.vue')
                },
                {
                    path: 'teacher/profile',
                    name: '个人设置',
                    component: () => import('@/views/teacher/Profile.vue')
                },
                
                // 管理员路由
                {
                    path: 'admin/dashboard',
                    name: '数据概览',
                    component: () => import('@/views/admin/Dashboard.vue')
                },
                {
                    path: 'admin/users',
                    name: '用户管理',
                    component: () => import('@/views/admin/Users.vue')
                },
                {
                    path: 'admin/topics',
                    name: '课题管理',
                    component: () => import('@/views/admin/Topics.vue')
                },
                {
                    path: 'admin/system',
                    name: '系统设置',
                    component: () => import('@/views/admin/System.vue')
                }
            ]
        },
    ]
});
export default router;