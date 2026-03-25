# 教学管理系统后端接口文档

## 基础说明

### 接口基础路径
```
http://localhost:8080/api
```

### 通用响应格式
```json
{
  "code": 200,          // 状态码：200成功，401未授权，403无权限，500服务器错误
  "message": "success", // 提示信息
  "data": {}           // 响应数据
}
```

### 认证方式
所有需要认证的接口都需要在请求头中携带 Token：
```
Authorization: Bearer {token}
```

---

## 一、用户认证模块

### 1.1 用户登录
**接口地址**: `POST /auth/login`

**请求参数**:
```json
{
  "username": "zhangsan",     // 用户名，必填
  "password": "123456",       // 密码，必填
  "role": "student"           // 角色：student/teacher/admin，必填
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "zhangsan",
      "realName": "张三",
      "role": "student",
      "email": "zhangsan@example.com",
      "phone": "13800138000",
      "avatar": "http://example.com/avatar.jpg"
    }
  }
}
```

### 1.2 用户注册
**接口地址**: `POST /auth/register`

**请求参数**:
```json
{
  "username": "zhangsan",           // 用户名，必填，唯一
  "password": "123456",             // 密码，必填，最少6位
  "confirmPassword": "123456",      // 确认密码，必填
  "realName": "张三",               // 真实姓名，必填
  "email": "zhangsan@example.com",  // 邮箱，必填，唯一
  "phone": "13800138000",           // 手机号，必填，唯一
  "role": "student",                // 角色：student/teacher，必填
  "studentId": "2021001",           // 学号（学生必填）
  "teacherId": "T001"               // 工号（教师必填）
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

### 1.3 获取用户信息
**接口地址**: `GET /user/info`

**请求头**: 需要 Token

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "realName": "张三",
    "role": "student",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "avatar": "http://example.com/avatar.jpg",
    "studentId": "2021001",        // 学生特有字段
    "major": "计算机科学与技术",
    "class": "计算机2101",
    "gender": "male",
    "createdAt": "2025-01-01 10:00:00"
  }
}
```

### 1.4 更新用户信息
**接口地址**: `PUT /user/update`

**请求头**: 需要 Token

**请求参数**:
```json
{
  "realName": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "avatar": "http://example.com/avatar.jpg",
  "gender": "male",
  "major": "计算机科学与技术",
  "class": "计算机2101"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

### 1.5 修改密码
**接口地址**: `POST /user/change-password`

**请求头**: 需要 Token

**请求参数**:
```json
{
  "oldPassword": "123456",       // 原密码，必填
  "newPassword": "654321",       // 新密码，必填
  "confirmPassword": "654321"    // 确认密码，必填
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

## 二、课程管理模块

### 2.1 获取课程列表
**接口地址**: `GET /course/list`

**请求头**: 需要 Token

**请求参数**:
```
pageNum=1           // 页码，默认1
pageSize=10         // 每页数量，默认10
keyword=Java        // 搜索关键词（可选）
semester=2025春季  // 学期（可选）
status=进行中       // 状态（可选）
```

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "list": [
      {
        "id": 1,
        "name": "Java程序设计",
        "code": "CS101",
        "teacherId": 10,
        "teacherName": "张老师",
        "credit": 3,
        "capacity": 50,
        "enrolled": 45,
        "semester": "2025年春季学期",
        "description": "Java基础编程课程",
        "status": "进行中",
        "time": "周一 8:00-10:00",
        "location": "A101",
        "createdAt": "2025-01-01 10:00:00"
      }
    ]
  }
}
```

### 2.2 获取课程详情
**接口地址**: `GET /course/detail/{id}`

**请求头**: 需要 Token

**路径参数**: 
- id: 课程ID

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "Java程序设计",
    "code": "CS101",
    "teacherId": 10,
    "teacherName": "张老师",
    "teacherEmail": "teacher@example.com",
    "credit": 3,
    "capacity": 50,
    "enrolled": 45,
    "semester": "2025年春季学期",
    "description": "Java基础编程课程，包含面向对象、集合框架等内容",
    "syllabus": "课程大纲详细内容...",
    "status": "进行中",
    "schedule": [
      {
        "weekday": "周一",
        "startTime": "08:00",
        "endTime": "10:00",
        "location": "A101"
      }
    ],
    "students": [
      {
        "id": 1,
        "studentId": "2021001",
        "realName": "张三",
        "class": "计算机2101"
      }
    ],
    "createdAt": "2025-01-01 10:00:00"
  }
}
```

### 2.3 创建课程（教师）
**接口地址**: `POST /course/create`

**请求头**: 需要 Token（教师角色）

**请求参数**:
```json
{
  "name": "Java程序设计",          // 课程名称，必填
  "code": "CS101",                 // 课程代码，必填，唯一
  "credit": 3,                     // 学分，必填
  "capacity": 50,                  // 容量，必填
  "semester": "2025年春季学期",    // 学期，必填
  "description": "Java基础编程",   // 课程简介
  "syllabus": "课程大纲...",       // 课程大纲
  "schedule": [                    // 课程安排
    {
      "weekday": "周一",
      "startTime": "08:00",
      "endTime": "10:00",
      "location": "A101"
    }
  ]
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "课程创建成功",
  "data": {
    "id": 1
  }
}
```

### 2.4 更新课程（教师）
**接口地址**: `PUT /course/update/{id}`

**请求头**: 需要 Token（教师角色）

**路径参数**: 
- id: 课程ID

**请求参数**: 同创建课程

**响应数据**:
```json
{
  "code": 200,
  "message": "课程更新成功",
  "data": null
}
```

### 2.5 删除课程（教师）
**接口地址**: `DELETE /course/delete/{id}`

**请求头**: 需要 Token（教师角色）

**路径参数**: 
- id: 课程ID

**响应数据**:
```json
{
  "code": 200,
  "message": "课程删除成功",
  "data": null
}
```

### 2.6 学生选课
**接口地址**: `POST /course/select`

**请求头**: 需要 Token（学生角色）

**请求参数**:
```json
{
  "courseId": 1    // 课程ID，必填
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "选课成功",
  "data": null
}
```

### 2.7 学生退课
**接口地址**: `POST /course/drop`

**请求头**: 需要 Token（学生角色）

**请求参数**:
```json
{
  "courseId": 1    // 课程ID，必填
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "退课成功",
  "data": null
}
```

### 2.8 获取我的课程
**接口地址**: `GET /course/my-courses`

**请求头**: 需要 Token

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "Java程序设计",
      "code": "CS101",
      "teacher": "张老师",
      "credit": 3,
      "time": "周一 8:00-10:00",
      "location": "A101",
      "status": "进行中"
    }
  ]
}
```

---

## 三、作业管理模块

### 3.1 获取作业列表
**接口地址**: `GET /homework/list`

**请求头**: 需要 Token

**请求参数**:
```
pageNum=1           // 页码，默认1
pageSize=10         // 每页数量，默认10
courseId=1          // 课程ID（可选）
status=pending      // 状态：pending待提交/submitted已提交（可选）
```

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 10,
    "list": [
      {
        "id": 1,
        "title": "Java第三章作业",
        "courseId": 1,
        "courseName": "Java程序设计",
        "description": "完成第三章课后习题",
        "score": 100,
        "deadline": "2025-11-25 23:59:00",
        "status": "pending",
        "submissionId": null,
        "grade": null,
        "createdAt": "2025-11-15 10:00:00"
      }
    ]
  }
}
```

### 3.2 获取作业详情
**接口地址**: `GET /homework/detail/{id}`

**请求头**: 需要 Token

**路径参数**: 
- id: 作业ID

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "Java第三章作业",
    "courseId": 1,
    "courseName": "Java程序设计",
    "teacherId": 10,
    "teacherName": "张老师",
    "description": "完成第三章课后习题，包括...",
    "requirements": "要求详细说明...",
    "score": 100,
    "deadline": "2025-11-25 23:59:00",
    "attachments": [
      {
        "id": 1,
        "name": "作业要求.pdf",
        "url": "http://example.com/files/homework1.pdf",
        "size": "1.5MB"
      }
    ],
    "submission": {
      "id": 10,
      "content": "作业内容...",
      "files": [],
      "submitTime": "2025-11-20 18:30:00",
      "grade": 95,
      "feedback": "完成得很好",
      "gradedAt": "2025-11-22 10:00:00"
    },
    "createdAt": "2025-11-15 10:00:00"
  }
}
```

### 3.3 创建作业（教师）
**接口地址**: `POST /homework/create`

**请求头**: 需要 Token（教师角色）

**请求参数**:
```json
{
  "title": "Java第三章作业",           // 标题，必填
  "courseId": 1,                      // 课程ID，必填
  "description": "完成第三章习题",     // 描述，必填
  "requirements": "要求说明...",       // 要求
  "score": 100,                       // 分值，必填
  "deadline": "2025-11-25 23:59:00",  // 截止时间，必填
  "attachments": [                    // 附件列表
    {
      "name": "作业要求.pdf",
      "url": "http://example.com/files/homework1.pdf"
    }
  ]
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "作业创建成功",
  "data": {
    "id": 1
  }
}
```

### 3.4 更新作业（教师）
**接口地址**: `PUT /homework/update/{id}`

**请求头**: 需要 Token（教师角色）

**路径参数**: 
- id: 作业ID

**请求参数**: 同创建作业

**响应数据**:
```json
{
  "code": 200,
  "message": "作业更新成功",
  "data": null
}
```

### 3.5 删除作业（教师）
**接口地址**: `DELETE /homework/delete/{id}`

**请求头**: 需要 Token（教师角色）

**路径参数**: 
- id: 作业ID

**响应数据**:
```json
{
  "code": 200,
  "message": "作业删除成功",
  "data": null
}
```

### 3.6 提交作业（学生）
**接口地址**: `POST /homework/submit`

**请求头**: 需要 Token（学生角色）

**请求参数**:
```json
{
  "homeworkId": 1,              // 作业ID，必填
  "content": "作业内容...",      // 作业内容，必填
  "files": [                    // 附件列表
    {
      "name": "作业.docx",
      "url": "http://example.com/files/submission1.docx"
    }
  ]
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "作业提交成功",
  "data": {
    "submissionId": 10
  }
}
```

### 3.7 批改作业（教师）
**接口地址**: `POST /homework/grade/{submissionId}`

**请求头**: 需要 Token（教师角色）

**路径参数**: 
- submissionId: 提交记录ID

**请求参数**:
```json
{
  "grade": 95,                  // 成绩，必填
  "feedback": "完成得很好"       // 评语
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "批改成功",
  "data": null
}
```

### 3.8 获取作业提交列表（教师）
**接口地址**: `GET /homework/submissions/{homeworkId}`

**请求头**: 需要 Token（教师角色）

**路径参数**: 
- homeworkId: 作业ID

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 10,
      "homeworkId": 1,
      "studentId": 1,
      "studentName": "张三",
      "studentNumber": "2021001",
      "content": "作业内容...",
      "files": [],
      "submitTime": "2025-11-20 18:30:00",
      "grade": 95,
      "feedback": "完成得很好",
      "gradedAt": "2025-11-22 10:00:00",
      "status": "graded"
    }
  ]
}
```

---

## 四、实验管理模块

### 4.1 获取实验列表
**接口地址**: `GET /experiment/list`

**请求头**: 需要 Token

**请求参数**:
```
pageNum=1           // 页码，默认1
pageSize=10         // 每页数量，默认10
courseId=1          // 课程ID（可选）
```

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 30,
    "pageNum": 1,
    "pageSize": 10,
    "list": [
      {
        "id": 1,
        "title": "Java面向对象编程实验",
        "courseId": 1,
        "courseName": "Java程序设计",
        "description": "实现图书管理系统",
        "deadline": "2025-11-30 23:59:00",
        "status": "进行中",
        "hasSubmitted": false,
        "createdAt": "2025-11-10 10:00:00"
      }
    ]
  }
}
```

### 4.2 获取实验详情
**接口地址**: `GET /experiment/detail/{id}`

**请求头**: 需要 Token

**路径参数**: 
- id: 实验ID

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "Java面向对象编程实验",
    "courseId": 1,
    "courseName": "Java程序设计",
    "description": "实现一个简单的图书管理系统",
    "requirements": "实验要求详细说明...",
    "steps": "实验步骤：1..., 2..., 3...",
    "deadline": "2025-11-30 23:59:00",
    "resources": [
      {
        "id": 1,
        "name": "实验指导书.pdf",
        "url": "http://example.com/files/guide.pdf"
      },
      {
        "id": 2,
        "name": "示例代码.zip",
        "url": "http://example.com/files/code.zip"
      }
    ],
    "environmentType": "cloud",
    "environmentConfig": {
      "image": "java:17",
      "cpu": "2核",
      "memory": "4GB",
      "storage": "20GB"
    },
    "submission": {
      "id": 20,
      "summary": "实验总结...",
      "files": [],
      "submitTime": "2025-11-25 20:00:00"
    },
    "createdAt": "2025-11-10 10:00:00"
  }
}
```

### 4.3 创建实验（教师）
**接口地址**: `POST /experiment/create`

**请求头**: 需要 Token（教师角色）

**请求参数**:
```json
{
  "title": "Java面向对象实验",          // 标题，必填
  "courseId": 1,                       // 课程ID，必填
  "description": "实现图书管理系统",    // 描述，必填
  "requirements": "要求说明...",        // 要求
  "steps": "实验步骤...",              // 步骤
  "deadline": "2025-11-30 23:59:00",   // 截止时间，必填
  "resources": [                       // 资源列表
    {
      "name": "实验指导书.pdf",
      "url": "http://example.com/files/guide.pdf"
    }
  ],
  "environmentType": "cloud",          // 环境类型：cloud/local
  "environmentConfig": {               // 环境配置
    "image": "java:17",
    "cpu": "2核",
    "memory": "4GB",
    "storage": "20GB"
  }
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "实验创建成功",
  "data": {
    "id": 1
  }
}
```

### 4.4 更新实验（教师）
**接口地址**: `PUT /experiment/update/{id}`

**请求头**: 需要 Token（教师角色）

**路径参数**: 
- id: 实验ID

**请求参数**: 同创建实验

**响应数据**:
```json
{
  "code": 200,
  "message": "实验更新成功",
  "data": null
}
```

### 4.5 删除实验（教师）
**接口地址**: `DELETE /experiment/delete/{id}`

**请求头**: 需要 Token（教师角色）

**路径参数**: 
- id: 实验ID

**响应数据**:
```json
{
  "code": 200,
  "message": "实验删除成功",
  "data": null
}
```

### 4.6 提交实验报告（学生）
**接口地址**: `POST /experiment/submit-report`

**请求头**: 需要 Token（学生角色）

**请求参数**:
```json
{
  "experimentId": 1,            // 实验ID，必填
  "summary": "实验总结...",      // 实验总结，必填
  "files": [                    // 报告文件列表
    {
      "name": "实验报告.docx",
      "url": "http://example.com/files/report.docx"
    }
  ]
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "实验报告提交成功",
  "data": {
    "submissionId": 20
  }
}
```

### 4.7 获取实验环境信息
**接口地址**: `GET /experiment/environment/{experimentId}`

**请求头**: 需要 Token

**路径参数**: 
- experimentId: 实验ID

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "experimentId": 1,
    "environmentType": "cloud",
    "status": "running",
    "accessUrl": "https://ide.example.com/env-123456",
    "credentials": {
      "username": "student001",
      "password": "temp123456"
    },
    "expiresAt": "2025-11-25 23:59:00",
    "config": {
      "image": "java:17",
      "cpu": "2核",
      "memory": "4GB"
    }
  }
}
```

### 4.8 启动实验环境
**接口地址**: `POST /experiment/start-environment/{experimentId}`

**请求头**: 需要 Token

**路径参数**: 
- experimentId: 实验ID

**响应数据**:
```json
{
  "code": 200,
  "message": "实验环境启动成功",
  "data": {
    "environmentId": "env-123456",
    "accessUrl": "https://ide.example.com/env-123456",
    "credentials": {
      "username": "student001",
      "password": "temp123456"
    }
  }
}
```

### 4.9 停止实验环境
**接口地址**: `POST /experiment/stop-environment/{experimentId}`

**请求头**: 需要 Token

**路径参数**: 
- experimentId: 实验ID

**响应数据**:
```json
{
  "code": 200,
  "message": "实验环境已停止",
  "data": null
}
```

---

## 五、排班管理模块

### 5.1 获取排班列表
**接口地址**: `GET /schedule/list`

**请求头**: 需要 Token

**请求参数**:
```
pageNum=1           // 页码，默认1
pageSize=10         // 每页数量，默认10
semester=2025春季   // 学期（可选）
weekday=周一        // 星期（可选）
classroomId=1       // 教室ID（可选）
```

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "list": [
      {
        "id": 1,
        "courseId": 1,
        "courseName": "Java程序设计",
        "teacherId": 10,
        "teacherName": "张老师",
        "classroomId": 1,
        "classroomName": "A101",
        "weekday": "周一",
        "startTime": "08:00",
        "endTime": "10:00",
        "semester": "2025年春季学期",
        "weeks": "1-16周",
        "createdAt": "2025-01-01 10:00:00"
      }
    ]
  }
}
```

### 5.2 创建排班（管理员）
**接口地址**: `POST /schedule/create`

**请求头**: 需要 Token（管理员角色）

**请求参数**:
```json
{
  "courseId": 1,                    // 课程ID，必填
  "classroomId": 1,                 // 教室ID，必填
  "weekday": "周一",                // 星期，必填
  "startTime": "08:00",             // 开始时间，必填
  "endTime": "10:00",               // 结束时间，必填
  "semester": "2025年春季学期",     // 学期，必填
  "weeks": "1-16周"                 // 周次，必填
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "排班创建成功",
  "data": {
    "id": 1
  }
}
```

### 5.3 更新排班（管理员）
**接口地址**: `PUT /schedule/update/{id}`

**请求头**: 需要 Token（管理员角色）

**路径参数**: 
- id: 排班ID

**请求参数**: 同创建排班

**响应数据**:
```json
{
  "code": 200,
  "message": "排班更新成功",
  "data": null
}
```

### 5.4 删除排班（管理员）
**接口地址**: `DELETE /schedule/delete/{id}`

**请求头**: 需要 Token（管理员角色）

**路径参数**: 
- id: 排班ID

**响应数据**:
```json
{
  "code": 200,
  "message": "排班删除成功",
  "data": null
}
```

### 5.5 检查排班冲突
**接口地址**: `POST /schedule/check-conflict`

**请求头**: 需要 Token（管理员角色）

**请求参数**:
```json
{
  "teacherId": 10,              // 教师ID（可选）
  "classroomId": 1,             // 教室ID（可选）
  "weekday": "周一",
  "startTime": "08:00",
  "endTime": "10:00",
  "semester": "2025年春季学期",
  "weeks": "1-16周"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hasConflict": true,
    "conflicts": [
      {
        "type": "teacher",
        "message": "张老师在该时间段已有课程安排",
        "scheduleId": 5,
        "courseName": "数据结构"
      },
      {
        "type": "classroom",
        "message": "A101教室在该时间段已被占用",
        "scheduleId": 8,
        "courseName": "算法设计"
      }
    ]
  }
}
```

### 5.6 获取教室列表
**接口地址**: `GET /schedule/classrooms`

**请求头**: 需要 Token

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "A101",
      "building": "A栋",
      "floor": 1,
      "capacity": 50,
      "type": "普通教室",
      "facilities": ["投影仪", "音响", "网络"],
      "status": "available"
    }
  ]
}
```

### 5.7 获取时间段列表
**接口地址**: `GET /schedule/timeslots`

**请求头**: 需要 Token

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "第1-2节",
      "startTime": "08:00",
      "endTime": "10:00"
    },
    {
      "id": 2,
      "name": "第3-4节",
      "startTime": "10:15",
      "endTime": "12:15"
    }
  ]
}
```

---

## 六、资源管理模块

### 6.1 获取资源列表
**接口地址**: `GET /resource/list`

**请求头**: 需要 Token

**请求参数**:
```
pageNum=1           // 页码，默认1
pageSize=10         // 每页数量，默认10
keyword=Java        // 搜索关键词（可选）
type=document       // 类型：document/video/code/other（可选）
courseId=1          // 课程ID（可选）
```

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 200,
    "pageNum": 1,
    "pageSize": 10,
    "list": [
      {
        "id": 1,
        "name": "Java基础语法.pdf",
        "type": "document",
        "courseId": 1,
        "courseName": "Java程序设计",
        "uploaderId": 10,
        "uploaderName": "张老师",
        "size": "2.5MB",
        "url": "http://example.com/files/java-basics.pdf",
        "downloadCount": 150,
        "uploadTime": "2025-11-01 10:00:00"
      }
    ]
  }
}
```

### 6.2 获取资源详情
**接口地址**: `GET /resource/detail/{id}`

**请求头**: 需要 Token

**路径参数**: 
- id: 资源ID

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "Java基础语法.pdf",
    "type": "document",
    "courseId": 1,
    "courseName": "Java程序设计",
    "uploaderId": 10,
    "uploaderName": "张老师",
    "description": "Java基础语法介绍",
    "size": "2.5MB",
    "format": "pdf",
    "url": "http://example.com/files/java-basics.pdf",
    "previewUrl": "http://example.com/preview/java-basics.pdf",
    "downloadCount": 150,
    "tags": ["Java", "基础", "语法"],
    "uploadTime": "2025-11-01 10:00:00"
  }
}
```

### 6.3 上传资源
**接口地址**: `POST /resource/upload`

**请求头**: 需要 Token
**Content-Type**: multipart/form-data

**请求参数**:
```
file: 文件对象（必填）
name: 资源名称（必填）
type: 资源类型（必填）
courseId: 课程ID（必填）
description: 描述（可选）
tags: 标签，逗号分隔（可选）
```

**响应数据**:
```json
{
  "code": 200,
  "message": "资源上传成功",
  "data": {
    "id": 1,
    "name": "Java基础语法.pdf",
    "url": "http://example.com/files/java-basics.pdf",
    "size": "2.5MB"
  }
}
```

### 6.4 更新资源信息
**接口地址**: `PUT /resource/update/{id}`

**请求头**: 需要 Token

**路径参数**: 
- id: 资源ID

**请求参数**:
```json
{
  "name": "Java基础语法-更新版.pdf",
  "description": "更新后的描述",
  "tags": ["Java", "基础", "语法", "更新"]
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "资源信息更新成功",
  "data": null
}
```

### 6.5 删除资源
**接口地址**: `DELETE /resource/delete/{id}`

**请求头**: 需要 Token

**路径参数**: 
- id: 资源ID

**响应数据**:
```json
{
  "code": 200,
  "message": "资源删除成功",
  "data": null
}
```

### 6.6 下载资源
**接口地址**: `GET /resource/download/{id}`

**请求头**: 需要 Token

**路径参数**: 
- id: 资源ID

**响应**: 直接返回文件流

### 6.7 获取资源分类
**接口地址**: `GET /resource/categories`

**请求头**: 需要 Token

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "文档课件",
      "value": "document",
      "count": 150
    },
    {
      "id": 2,
      "name": "视频教程",
      "value": "video",
      "count": 80
    },
    {
      "id": 3,
      "name": "示例代码",
      "value": "code",
      "count": 120
    },
    {
      "id": 4,
      "name": "其他资源",
      "value": "other",
      "count": 50
    }
  ]
}
```

---

## 七、管理员模块

### 7.1 获取用户列表（管理员）
**接口地址**: `GET /admin/users`

**请求头**: 需要 Token（管理员角色）

**请求参数**:
```
pageNum=1           // 页码，默认1
pageSize=10         // 每页数量，默认10
keyword=张三        // 搜索关键词（可选）
role=student        // 角色：student/teacher/admin（可选）
status=active       // 状态：active/inactive（可选）
```

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 500,
    "pageNum": 1,
    "pageSize": 10,
    "list": [
      {
        "id": 1,
        "username": "zhangsan",
        "realName": "张三",
        "role": "student",
        "email": "zhangsan@example.com",
        "phone": "13800138000",
        "studentId": "2021001",
        "major": "计算机科学与技术",
        "class": "计算机2101",
        "status": "active",
        "lastLoginTime": "2025-11-21 10:00:00",
        "createdAt": "2025-01-01 10:00:00"
      }
    ]
  }
}
```

### 7.2 创建用户（管理员）
**接口地址**: `POST /admin/user/create`

**请求头**: 需要 Token（管理员角色）

**请求参数**:
```json
{
  "username": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "role": "student",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "studentId": "2021001",
  "major": "计算机科学与技术",
  "class": "计算机2101"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "用户创建成功",
  "data": {
    "id": 1
  }
}
```

### 7.3 更新用户信息（管理员）
**接口地址**: `PUT /admin/user/update/{id}`

**请求头**: 需要 Token（管理员角色）

**路径参数**: 
- id: 用户ID

**请求参数**: 同创建用户

**响应数据**:
```json
{
  "code": 200,
  "message": "用户信息更新成功",
  "data": null
}
```

### 7.4 删除用户（管理员）
**接口地址**: `DELETE /admin/user/delete/{id}`

**请求头**: 需要 Token（管理员角色）

**路径参数**: 
- id: 用户ID

**响应数据**:
```json
{
  "code": 200,
  "message": "用户删除成功",
  "data": null
}
```

### 7.5 重置用户密码（管理员）
**接口地址**: `POST /admin/user/reset-password/{id}`

**请求头**: 需要 Token（管理员角色）

**路径参数**: 
- id: 用户ID

**响应数据**:
```json
{
  "code": 200,
  "message": "密码重置成功，新密码为：123456",
  "data": {
    "newPassword": "123456"
  }
}
```

### 7.6 获取系统统计信息
**接口地址**: `GET /admin/statistics`

**请求头**: 需要 Token（管理员角色）

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userCount": {
      "total": 500,
      "student": 450,
      "teacher": 45,
      "admin": 5
    },
    "courseCount": {
      "total": 100,
      "active": 80,
      "inactive": 20
    },
    "homeworkCount": {
      "total": 500,
      "pending": 150,
      "submitted": 350
    },
    "experimentCount": {
      "total": 200,
      "active": 80,
      "completed": 120
    },
    "resourceCount": {
      "total": 800,
      "document": 300,
      "video": 200,
      "code": 250,
      "other": 50
    }
  }
}
```

### 7.7 获取课程统计信息
**接口地址**: `GET /admin/statistics/courses`

**请求头**: 需要 Token（管理员角色）

**请求参数**:
```
semester=2025春季   // 学期（可选）
```

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "courseId": 1,
      "courseName": "Java程序设计",
      "teacherName": "张老师",
      "studentCount": 45,
      "homeworkCount": 10,
      "experimentCount": 5,
      "avgScore": 88.5,
      "passRate": 95.5
    }
  ]
}
```

### 7.8 获取用户统计信息
**接口地址**: `GET /admin/statistics/users`

**请求头**: 需要 Token（管理员角色）

**响应数据**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "activeUsers": {
      "today": 120,
      "thisWeek": 450,
      "thisMonth": 480
    },
    "newUsers": {
      "today": 5,
      "thisWeek": 20,
      "thisMonth": 50
    },
    "userGrowth": [
      {
        "date": "2025-11-01",
        "count": 450
      },
      {
        "date": "2025-11-02",
        "count": 455
      }
    ]
  }
}
```

---

## 八、文件上传模块

### 8.1 通用文件上传
**接口地址**: `POST /upload`

**请求头**: 需要 Token
**Content-Type**: multipart/form-data

**请求参数**:
```
file: 文件对象（必填）
type: 文件类型（可选）：avatar/document/video/code/other
```

**响应数据**:
```json
{
  "code": 200,
  "message": "文件上传成功",
  "data": {
    "fileId": "file-123456",
    "fileName": "example.pdf",
    "fileUrl": "http://example.com/files/example.pdf",
    "fileSize": "2.5MB",
    "mimeType": "application/pdf",
    "uploadTime": "2025-11-21 10:00:00"
  }
}
```

---

## 九、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 数据冲突（如用户名已存在） |
| 500 | 服务器内部错误 |

---

## 十、数据库设计建议

### 主要数据表

1. **用户表 (user)**
   - id, username, password, real_name, role, email, phone, avatar, status, created_at, updated_at

2. **学生信息表 (student_info)**
   - id, user_id, student_id, major, class, gender, created_at

3. **教师信息表 (teacher_info)**
   - id, user_id, teacher_id, department, title, created_at

4. **课程表 (course)**
   - id, name, code, teacher_id, credit, capacity, enrolled, semester, description, syllabus, status, created_at

5. **课程选课表 (course_enrollment)**
   - id, course_id, student_id, enroll_time, status

6. **作业表 (homework)**
   - id, title, course_id, teacher_id, description, requirements, score, deadline, created_at

7. **作业提交表 (homework_submission)**
   - id, homework_id, student_id, content, submit_time, grade, feedback, graded_at

8. **实验表 (experiment)**
   - id, title, course_id, description, requirements, steps, deadline, environment_type, environment_config, created_at

9. **实验提交表 (experiment_submission)**
   - id, experiment_id, student_id, summary, submit_time, created_at

10. **排班表 (schedule)**
    - id, course_id, classroom_id, weekday, start_time, end_time, semester, weeks, created_at

11. **教室表 (classroom)**
    - id, name, building, floor, capacity, type, facilities, status

12. **资源表 (resource)**
    - id, name, type, course_id, uploader_id, description, size, format, url, download_count, tags, upload_time

13. **文件表 (file)**
    - id, file_name, file_url, file_size, mime_type, uploader_id, upload_time

---

## 十一、开发建议

1. **使用Spring Boot框架**
   - Spring Web
   - Spring Security + JWT
   - MyBatis Plus
   - Spring Validation

2. **数据库使用MySQL 8.0+**

3. **文件存储建议使用阿里云OSS**

4. **实验环境可以集成**
   - 阿里云ECS
   - 容器服务ACK
   - 或使用云IDE服务

5. **部署建议**
   - 使用Docker容器化
   - 部署到阿里云ECS或容器服务
   - 使用Redis做缓存
   - 使用Nginx做反向代理

6. **安全建议**
   - 密码使用BCrypt加密
   - 使用JWT做身份认证
   - 接口做防刷限流
   - 敏感操作加操作日志

---

## 十二、测试账号建议

创建以下测试账号方便开发调试：

**学生账号**:
- 用户名: student001
- 密码: 123456
- 角色: student

**教师账号**:
- 用户名: teacher001
- 密码: 123456
- 角色: teacher

**管理员账号**:
- 用户名: admin
- 密码: 123456
- 角色: admin
