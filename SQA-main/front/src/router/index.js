import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { hasPermission } from '@/utils/permission'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue')
  },
  // 学生端路由
  {
    path: '/student',
    name: 'StudentLayout',
    component: () => import('@/layouts/StudentLayout.vue'),
    meta: { requiresAuth: true, role: 'student' },
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/views/student/Dashboard.vue')
      },
      {
        path: 'courses',
        name: 'StudentCourses',
        component: () => import('@/views/student/Courses.vue')
      },
      {
        path: 'homework',
        name: 'StudentHomework',
        component: () => import('@/views/student/Homework.vue')
      },
      {
        path: 'homework/:id/answer',
        name: 'StudentHomeworkAnswer',
        component: () => import('@/views/student/HomeworkAnswer.vue')
      },
      {
        path: 'experiment',
        name: 'StudentExperiment',
        component: () => import('@/views/student/Experiment.vue')
      },
      {
        path: 'experiment/:id/problems',
        name: 'StudentExperimentProblems',
        component: () => import('@/views/student/ExperimentProblems.vue')
      },
      {
        path: 'experiment/:experimentId/problem/:problemId',
        name: 'StudentProblemSolve',
        component: () => import('@/views/student/ProblemSolve.vue')
      },
      {
        path: 'resources',
        name: 'StudentResources',
        component: () => import('@/views/student/Resources.vue')
      },
      {
        path: 'exam',
        name: 'StudentExam',
        component: () => import('@/views/student/Exam.vue')
      },
      {
        path: 'exam/result/:studentExamId',
        name: 'StudentExamResult',
        component: () => import('@/views/student/ExamResultView.vue')
      },
      {
        path: 'practice',
        name: 'StudentPractice',
        component: () => import('@/views/student/Practice.vue')
      },
      {
        path: 'discussion',
        name: 'StudentDiscussion',
        component: () => import('@/views/student/Discussion.vue')
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/Profile.vue')
      },
      {
        path: 'livestream',
        name: 'StudentLiveStream',
        component: () => import('@/views/student/LiveStream.vue')
      },
      {
        path: 'livestream/room/:id',
        name: 'StudentLiveRoom',
        component: () => import('@/views/student/LiveRoom.vue')
      },
      {
        path: 'ai-assistant',
        name: 'StudentAIAssistant',
        component: () => import('@/views/student/AIAssistant.vue')
      }
    ]
  },
  // 教师端路由
  {
    path: '/teacher',
    name: 'TeacherLayout',
    component: () => import('@/layouts/TeacherLayout.vue'),
    meta: { requiresAuth: true, role: 'teacher' },
    children: [
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/views/teacher/Dashboard.vue')
      },
      {
        path: 'courses',
        name: 'TeacherCourses',
        component: () => import('@/views/teacher/Courses.vue')
      },
      {
        path: 'homework',
        name: 'TeacherHomework',
        component: () => import('@/views/teacher/Homework.vue')
      },
      {
        path: 'experiment',
        name: 'TeacherExperiment',
        component: () => import('@/views/teacher/Experiment.vue')
      },
      {
        path: 'resources',
        name: 'TeacherResources',
        component: () => import('@/views/teacher/Resources.vue')
      },
      {
        path: 'preparation',
        name: 'TeacherPreparation',
        component: () => import('@/views/teacher/Preparation.vue')
      },
      {
        path: 'exam',
        name: 'TeacherExam',
        component: () => import('@/views/teacher/Exam.vue')
      },
      {
        path: 'question-bank',
        name: 'TeacherQuestionBank',
        component: () => import('@/views/teacher/QuestionBank.vue')
      },
      {
        path: 'discussion',
        name: 'TeacherDiscussion',
        component: () => import('@/views/teacher/Discussion.vue')
      },
      {
        path: 'profile',
        name: 'TeacherProfile',
        component: () => import('@/views/teacher/Profile.vue')
      },
      {
        path: 'livestream',
        name: 'TeacherLiveStream',
        component: () => import('@/views/teacher/LiveStream.vue')
      },
      {
        path: 'livestream/room/:id',
        name: 'TeacherLiveRoom',
        component: () => import('@/views/teacher/LiveRoom.vue')
      },
      {
        path: 'livestream/wordcloud/:id',
        name: 'TeacherLiveWordCloud',
        component: () => import('@/views/teacher/LiveWordCloud.vue')
      },
      {
        path: 'ai-assistant',
        name: 'TeacherAIAssistant',
        component: () => import('@/views/teacher/AIAssistant.vue')
      }
    ]
  },
  // 管理员端路由
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { requiresPermission: 'user:manage' }  // 需要超级管理员权限
      },
      {
        path: 'teachers',
        name: 'AdminTeachers',
        component: () => import('@/views/admin/Teachers.vue')
      },
      {
        path: 'students',
        name: 'AdminStudents',
        component: () => import('@/views/admin/Students.vue')
      },
      {
        path: 'courses',
        name: 'AdminCourses',
        component: () => import('@/views/admin/Courses.vue')
      },
      {
        path: 'course-selection-management',
        name: 'AdminCourseSelectionManagement',
        component: () => import('@/views/admin/CourseSelectionManagement.vue')
      },
      {
        path: 'course-requests',
        name: 'AdminCourseRequests',
        component: () => import('@/views/admin/CourseRequests.vue')
      },
      {
        path: 'schedule',
        name: 'AdminSchedule',
        component: () => import('@/views/admin/Schedule.vue')
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/Announcements.vue')
      },
      {
        path: 'resources',
        name: 'AdminResources',
        component: () => import('@/views/admin/Resources.vue')
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/Statistics.vue')
      },
      {
        path: 'live-monitor',
        name: 'AdminLiveMonitor',
        component: () => import('@/views/admin/LiveMonitor.vue')
      },
      {
        path: 'redis-monitor',
        name: 'AdminRedisMonitor',
        component: () => import('@/views/admin/RedisMonitor.vue')
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/LogManagement.vue'),
        meta: { requiresPermission: 'log:view' },
        redirect: '/admin/logs/admin',
        children: [
          {
            path: 'admin',
            name: 'AdminLogsAdmin',
            component: () => import('@/views/admin/LogManagement.vue'),
            meta: { logType: 'admin' }
          },
          {
            path: 'teacher',
            name: 'AdminLogsTeacher',
            component: () => import('@/views/admin/LogManagement.vue'),
            meta: { logType: 'teacher' }
          },
          {
            path: 'student',
            name: 'AdminLogsStudent',
            component: () => import('@/views/admin/LogManagement.vue'),
            meta: { logType: 'student' }
          }
        ]
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue')
      },
      {
        path: 'ai-assistant',
        name: 'AdminAIAssistant',
        component: () => import('@/views/admin/AIAssistant.vue')
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
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth) {
    if (!userStore.token) {
      next('/login')
    } else if (to.meta.role && to.meta.role !== userStore.userInfo.role) {
      next('/login')
    } else if (to.meta.requiresPermission) {
      // 检查是否有所需权限
      if (hasPermission(to.meta.requiresPermission)) {
        next()
      } else {
        // 没有权限，重定向到首页
        next('/admin/dashboard')
      }
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
