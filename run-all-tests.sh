#!/bin/bash
# 黑盒测试完整执行脚本
# 用于截图演示

echo "========================================"
echo "  黑盒测试 - 完整执行"
echo "========================================"
echo ""

# 获取TOKEN
echo "正在获取TOKEN..."
TEACHER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher001","password":"123456"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

STUDENT_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student001","password":"123456"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "TOKEN获取成功"
echo ""

# 7.1.1 用户登录测试
echo "========================================"
echo "【7.1.1 用户登录测试】"
echo "========================================"
echo ""

echo "[TC_LOGIN_001] 教师登录:"
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher001","password":"123456"}' | python3 -m json.tool | head -15
echo ""

echo "[TC_LOGIN_003] 错误密码:"
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher001","password":"wrongpass"}' | python3 -m json.tool
echo ""

echo "[TC_LOGIN_004] 用户不存在:"
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"nonexist","password":"123456"}' | python3 -m json.tool
echo ""

# 7.1.2 获取用户信息测试
echo "========================================"
echo "【7.1.2 获取用户信息测试】"
echo "========================================"
echo ""

echo "[TC_GETUSER_001] 获取当前用户信息:"
curl -s -X GET "http://localhost:8080/api/user/info?userId=3" \
  -H "Authorization: Bearer $TEACHER_TOKEN" | python3 -m json.tool | head -20
echo ""

echo "[TC_GETUSER_003] 用户不存在:"
curl -s -X GET "http://localhost:8080/api/user/info/99999" \
  -H "Authorization: Bearer $TEACHER_TOKEN" | python3 -m json.tool
echo ""

# 7.1.3 用户注册测试
echo "========================================"
echo "【7.1.3 用户注册测试】"
echo "========================================"
echo ""

TIMESTAMP=$(date +%s)
echo "[TC_REG_001] 注册新教师用户:"
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testteacher$TIMESTAMP\",\"password\":\"123456\",\"email\":\"test$TIMESTAMP@test.com\",\"role\":\"TEACHER\",\"realName\":\"测试教师\"}" | python3 -m json.tool
echo ""

echo "[TC_REG_003] 用户名已存在:"
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher001","password":"123456","email":"new@test.com","role":"TEACHER","realName":"测试"}' | python3 -m json.tool
echo ""

# 7.1.4 修改用户信息测试
echo "========================================"
echo "【7.1.4 修改用户信息测试】"
echo "========================================"
echo ""

echo "[TC_UPDATE_001] 更新用户信息:"
curl -s -X PUT http://localhost:8080/api/user/update \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -d '{"id":3,"realName":"张三","phone":"13800138000","email":"zhangsan@test.com","bio":"这是我的个人简介"}' | python3 -m json.tool
echo ""

# 7.2.1 创建课程测试
echo "========================================"
echo "【7.2.1 创建课程测试】"
echo "========================================"
echo ""

TIMESTAMP=$(date +%s)
echo "[TC_COURSE_001] 创建课程:"
curl -s -X POST "http://localhost:8080/api/course/create?teacherId=3" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -d "{\"name\":\"测试课程$TIMESTAMP\",\"courseCode\":\"TEST$TIMESTAMP\",\"description\":\"测试课程描述\",\"credit\":3,\"capacity\":50,\"semester\":\"2025-2026-2\"}" | python3 -m json.tool
echo ""

# 7.2.2 查询课程测试
echo "========================================"
echo "【7.2.2 查询课程测试】"
echo "========================================"
echo ""

echo "[TC_QUERY_001] 查询所有课程:"
curl -s -X GET "http://localhost:8080/api/course/all" \
  -H "Authorization: Bearer $TEACHER_TOKEN" | python3 -m json.tool | head -30
echo "..."
echo ""

# 7.2.3 选课测试
echo "========================================"
echo "【7.2.3 选课测试】"
echo "========================================"
echo ""

echo "[TC_ENROLL_001] 学生选课:"
curl -s -X POST "http://localhost:8080/api/course/enroll/2?studentId=6" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | python3 -m json.tool
echo ""

# 7.3.1 创建直播测试
echo "========================================"
echo "【7.3.1 创建直播测试】"
echo "========================================"
echo ""

echo "[TC_LIVE_001] 创建直播:"
curl -s -X POST "http://localhost:8080/api/livestream/create?teacherId=3" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -d '{"title":"测试直播课程","scheduledTime":"2026-01-25 14:00:00","teacherId":3,"teacherName":"张三","courseId":1,"courseName":"Java程序设计"}' | python3 -m json.tool | head -25
echo ""

# 7.4.1 创建考试测试
echo "========================================"
echo "【7.4.1 创建考试测试】"
echo "========================================"
echo ""

echo "[TC_EXAM_001] 创建考试:"
curl -s -X POST "http://localhost:8080/api/exam/paper/create?teacherId=3" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -d '{"title":"Java期末测试","courseId":1,"courseName":"Java程序设计","duration":120,"totalScore":100,"passScore":60,"startTime":"2026-02-01 09:00:00","endTime":"2026-02-01 11:00:00","description":"Java期末考试","creatorId":3,"creatorName":"张三"}' | python3 -m json.tool
echo ""

echo "========================================"
echo "  测试完成！"
echo "========================================"
