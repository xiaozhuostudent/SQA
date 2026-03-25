package cn.edu.zjut.back.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIAssistantService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    public String chat(String userMessage, String role, Long userId, String sessionId, String sessionTitle, List<Map<String, String>> history, List<String> images) {
        try {
            System.out.println("[AI Assistant] ========== 开始处理聊天请求 ==========");
            System.out.println("[AI Assistant] 用户消息: " + userMessage);
            System.out.println("[AI Assistant] 角色: " + role);
            System.out.println("[AI Assistant] 用户ID: " + userId);
            System.out.println("[AI Assistant] AI API URL: " + aiApiUrl);
            
            // 首先检查是否是常见问题，如果是则直接从数据库获取结果
            String directResponse = handleDirectQuestions(userMessage, role, userId);
            if (directResponse != null) {
                return directResponse;
            }
            
            // 构建系统提示，包含数据库schema信息
            String systemPrompt = buildSystemPrompt(role);
            
            // 构建完整的对话历史
            List<Map<String, String>> fullHistory = new ArrayList<>();
            fullHistory.add(Map.of("role", "system", "content", systemPrompt));
            
            if (history != null && !history.isEmpty()) {
                fullHistory.addAll(history);
            }
            
            fullHistory.add(Map.of("role", "user", "content", userMessage));
            
            // 调用AI服务
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", userMessage);
            requestBody.put("role", role);
            requestBody.put("userId", userId.toString());
            requestBody.put("history", fullHistory);
            if (sessionId != null) {
                requestBody.put("sessionId", sessionId);
            }
            if (sessionTitle != null) {
                requestBody.put("sessionTitle", sessionTitle);
            }
            if (images != null && !images.isEmpty()) {
                requestBody.put("images", images);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                aiApiUrl, 
                requestBody, 
                Map.class
            );
            
            if (response != null && response.containsKey("response")) {
                // 安全地获取response，可能是String或其他类型
                Object responseObj = response.get("response");
                String aiResponse;
                
                if (responseObj instanceof String) {
                    aiResponse = (String) responseObj;
                } else if (responseObj instanceof List) {
                    // 如果是List，取第一个元素
                    List<?> list = (List<?>) responseObj;
                    aiResponse = list.isEmpty() ? "无响应" : String.valueOf(list.get(0));
                } else if (responseObj instanceof Map) {
                    // 如果是Map（比如{text=...}），尝试提取text字段
                    Map<?, ?> map = (Map<?, ?>) responseObj;
                    if (map.containsKey("text")) {
                        aiResponse = String.valueOf(map.get("text"));
                    } else {
                        aiResponse = String.valueOf(responseObj);
                    }
                } else {
                    // 其他类型转为字符串
                    aiResponse = String.valueOf(responseObj);
                }
                
                System.out.println("[AI Assistant] Python返回的原始response类型: " + (responseObj != null ? responseObj.getClass().getName() : "null"));
                System.out.println("[AI Assistant] Python返回的原始response内容: " + responseObj);
                System.out.println("[AI Assistant] 提取后的aiResponse: " + aiResponse);
                
                // 检查AI响应中是否包含SQL查询请求
                String processedResponse = processSQLQueries(aiResponse, role, userId);
                
                return processedResponse;
            }
            
            System.out.println("[AI Assistant] Python响应中没有response字段，完整响应: " + response);
            // 如果AI服务无响应，再尝试处理直接问题
            String fallbackResponse = handleDirectQuestions(userMessage, role, userId);
            if (fallbackResponse != null) {
                return fallbackResponse;
            }
            
            // 如果都不是常见问题且AI服务无响应，则返回提示信息
            return "抱歉，AI服务暂时无法响应。";
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[AI Assistant] 调用AI服务出错: " + e.getMessage());
            
            // 首先尝试直接处理问题
            String directResponse = handleDirectQuestions(userMessage, role, userId);
            if (directResponse != null) {
                return directResponse;
            }
            
            // 如果都不是常见问题且发生异常，则返回错误信息
            return "抱歉，AI服务暂时无法响应。";
        }
    }
    
    private String handleDirectQuestions(String userMessage, String role, Long userId) {
        // 问题1: 我的课程中有多少学生？
        if (userMessage.toLowerCase().contains("我的课程中有多少学生") || 
            userMessage.toLowerCase().contains("课程中有多少学生") ||
            userMessage.toLowerCase().contains("课程中多少学生") ||
            userMessage.toLowerCase().contains("多少学生")) {
            
            if ("teacher".equals(role)) {
                // 查询教师所授课程的学生数量
                String sql = "SELECT c.name AS 课程名, c.course_code AS 课程代码, c.enrolled AS 已选人数, c.capacity AS 容量 " +
                           "FROM courses c " +
                           "WHERE c.teacher_id = ?";
                
                try {
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userId);
                    
                    if (result.isEmpty()) {
                        return "您目前没有开设任何课程。";
                    }
                    
                    StringBuilder response = new StringBuilder();
                    response.append("根据查询结果：\n");
                    
                    for (Map<String, Object> row : result) {
                        response.append("课程《").append(row.get("课程名")).append("》(").append(row.get("课程代码")).append(") 有 ")
                               .append(row.get("已选人数")).append(" 名学生（容量:").append(row.get("容量")).append("）\n");
                    }
                    
                    return response.toString();
                } catch (Exception e) {
                    System.out.println("查询教师课程学生数出错: " + e.getMessage());
                }
            } else if ("admin".equals(role)) {
                // 管理员可以查看所有课程的学生数
                String sql = "SELECT c.name AS 课程名, c.course_code AS 课程代码, c.enrolled AS 已选人数, c.capacity AS 容量 " +
                           "FROM courses c " +
                           "ORDER BY c.enrolled DESC LIMIT 10";
                           
                try {
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
                    
                    if (result.isEmpty()) {
                        return "系统中没有课程。";
                    }
                    
                    StringBuilder response = new StringBuilder();
                    response.append("系统中的热门课程学生数：\n");
                    
                    for (Map<String, Object> row : result) {
                        response.append("课程《").append(row.get("课程名")).append("》(").append(row.get("课程代码")).append(") 有 ")
                               .append(row.get("已选人数")).append(" 名学生（容量:").append(row.get("容量")).append("）\n");
                    }
                    
                    return response.toString();
                } catch (Exception e) {
                    System.out.println("查询系统课程学生数出错: " + e.getMessage());
                }
            }
        }
        
        // 问题2: 我有哪些课程？
        if (userMessage.toLowerCase().contains("我有哪些课程") || 
            userMessage.toLowerCase().contains("我的课程") ||
            userMessage.toLowerCase().contains("课程列表") ||
            userMessage.toLowerCase().contains("课程情况")) {
            
            if ("teacher".equals(role)) {
                String sql = "SELECT name, course_code, description, credit, semester FROM courses WHERE teacher_id = ?";
                try {
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userId);
                    
                    if (result.isEmpty()) {
                        return "您目前没有开设任何课程。";
                    }
                    
                    StringBuilder response = new StringBuilder();
                    response.append("您开设的课程如下：\n");
                    
                    for (Map<String, Object> row : result) {
                        response.append("《").append(row.get("name")).append("》(").append(row.get("course_code"))
                               .append(")，学分:").append(row.get("credit")).append("，学期:").append(row.get("semester"))
                               .append("，简介:").append(row.get("description")).append("\n");
                    }
                    
                    return response.toString();
                } catch (Exception e) {
                    System.out.println("查询教师课程列表出错: " + e.getMessage());
                }
            } else if ("student".equals(role)) {
                String sql = "SELECT c.name, c.course_code, c.description, u.real_name AS teacher_name " +
                           "FROM courses c JOIN enrollments e ON c.id = e.course_id " +
                           "JOIN users u ON c.teacher_id = u.id " +
                           "WHERE e.student_id = ? AND e.status = 'selected'";
                try {
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userId);
                    
                    if (result.isEmpty()) {
                        return "您目前没有选择任何课程。";
                    }
                    
                    StringBuilder response = new StringBuilder();
                    response.append("您选择的课程如下：\n");
                    
                    for (Map<String, Object> row : result) {
                        response.append("《").append(row.get("name")).append("》(").append(row.get("course_code"))
                               .append(")，教师:").append(row.get("teacher_name"))
                               .append("，简介:").append(row.get("description")).append("\n");
                    }
                    
                    return response.toString();
                } catch (Exception e) {
                    System.out.println("查询学生课程列表出错: " + e.getMessage());
                }
            }
        }
        
        // 问题3: 我是谁？
        if (userMessage.contains("我是谁")) {
            String userInfoSql = "SELECT real_name, username, role, student_number FROM users WHERE id = ?";
            try {
                Map<String, Object> user = jdbcTemplate.queryForMap(userInfoSql, userId);
                String roleName = "";
                switch ((String) user.get("role")) {
                    case "teacher": roleName = "教师"; break;
                    case "admin": roleName = "管理员"; break;
                    case "student": roleName = "学生"; break;
                    default: roleName = "用户";
                }
                
                String identifier = user.get("real_name") != null ? (String) user.get("real_name") : (String) user.get("username");
                if ("student".equals(user.get("role")) && user.get("student_number") != null) {
                    identifier = identifier + "(" + user.get("student_number") + ")";
                }
                
                return "您好！您是" + roleName + " " + identifier;
            } catch (Exception e) {
                return "抱歉，无法获取您的用户信息。";
            }
        }
        
        // 问题4: 系统有多少课程？
        if (userMessage.toLowerCase().contains("系统有多少课程") || 
            userMessage.toLowerCase().contains("课程总数") ||
            userMessage.toLowerCase().contains("共有多少课程")) {
            
            String sql = "SELECT COUNT(*) AS total_courses FROM courses";
            try {
                Map<String, Object> result = jdbcTemplate.queryForMap(sql);
                Long count = (Long) result.get("total_courses");
                
                return "系统中共有 " + count + " 门课程。";
            } catch (Exception e) {
                System.out.println("查询课程总数出错: " + e.getMessage());
            }
        }
        
        // 问题5: 系统有多少学生？
        if (userMessage.toLowerCase().contains("系统有多少学生") || 
            userMessage.toLowerCase().contains("学生总数") ||
            userMessage.toLowerCase().contains("共有多少学生")) {
            
            String sql = "SELECT COUNT(DISTINCT id) AS total_students FROM users WHERE role = 'student'";
            try {
                Map<String, Object> result = jdbcTemplate.queryForMap(sql);
                Long count = (Long) result.get("total_students");
                
                return "系统中共有 " + count + " 名学生。";
            } catch (Exception e) {
                System.out.println("查询学生总数出错: " + e.getMessage());
            }
        }
        
        // 问题6: 我的作业完成情况如何？
        if (userMessage.toLowerCase().contains("我的作业完成情况") || 
            userMessage.toLowerCase().contains("作业完成情况") ||
            userMessage.toLowerCase().contains("我完成了多少作业") ||
            userMessage.toLowerCase().contains("作业进度")) {
            
            if ("student".equals(role)) {
                String sql = "SELECT " +
                           "COUNT(hs.id) AS total_submitted, " +
                           "COUNT(h.id) AS total_assignments, " +
                           "(COUNT(h.id) - COUNT(hs.id)) AS pending_count " +
                           "FROM courses c " +
                           "JOIN enrollments e ON c.id = e.course_id " +
                           "JOIN homework h ON c.id = h.course_id " +
                           "LEFT JOIN homework_submissions hs ON h.id = hs.homework_id AND hs.student_id = ? " +
                           "WHERE e.student_id = ? AND e.status = 'selected'";
                
                try {
                    Map<String, Object> result = jdbcTemplate.queryForMap(sql, userId, userId);
                    Long submitted = (Long) result.get("total_submitted");
                    Long total = (Long) result.get("total_assignments");
                    Long pending = (Long) result.get("pending_count");
                    
                    return "根据查询结果：您总共有 " + total + " 个作业，已完成 " + submitted + " 个，待完成 " + pending + " 个。";
                } catch (Exception e) {
                    System.out.println("查询学生作业完成情况出错: " + e.getMessage());
                }
            }
        }
        
        return null;
    }
    
    private String buildSystemPrompt(String role) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能教学助手，可以帮助用户查询和分析教学系统中的数据。\n\n");
        
        switch (role) {
            case "teacher":
                prompt.append("你正在为教师提供服务，可以帮助查询课程、学生、作业、考试等信息。\n");
                break;
            case "admin":
                prompt.append("你正在为管理员提供服务，可以帮助查询系统中的所有数据和统计信息。\n");
                break;
            default: // student
                prompt.append("你正在为学生提供服务，可以帮助查询课程、作业、成绩等个人学习信息。\n");
        }
        
        prompt.append("\n数据库表结构信息：\n");
        prompt.append("- users: 用户表（id, username, password, email, phone, role, real_name, student_number, created_at）\n");
        prompt.append("- courses: 课程表（id, name, course_code, description, teacher_id, semester, credit, capacity, enrolled, created_at）\n");
        prompt.append("- enrollments: 选课表（id, student_id, course_id, select_time, status）\n");
        prompt.append("- homework: 作业表（id, course_id, title, description, deadline, creator_id, created_at）\n");
        prompt.append("- homework_submissions: 作业提交表（id, homework_id, student_id, content, submit_time, grade, feedback）\n");
        prompt.append("- exams: 考试表（id, course_id, title, description, start_time, end_time, duration, created_by）\n");
        prompt.append("- exam_questions: 考试题目表（id, exam_id, question_id, score）\n");
        prompt.append("- exam_submissions: 考试提交表（id, exam_id, student_id, answers, score, submitted_at）\n");
        prompt.append("- questions: 题库表（id, type, content, options, correct_answer, difficulty, created_by）\n");
        prompt.append("- resources: 资源表（id, course_id, title, description, file_path, file_type, file_size, uploader_id）\n");
        prompt.append("- discussions: 讨论表（id, course_id, user_id, title, content, created_at）\n");
        prompt.append("- discussion_replies: 讨论回复表（id, discussion_id, user_id, content, created_at）\n");
        
        prompt.append("\n当用户询问需要查询数据库的问题时，你可以在回答中使用特殊标记来触发SQL查询：\n");
        prompt.append("格式：[SQL_QUERY]SELECT ... FROM ... WHERE ...[/SQL_QUERY]\n");
        prompt.append("查询结果将自动嵌入到你的回答中。\n");
        prompt.append("\n注意：\n");
        prompt.append("- 只使用SELECT查询，不要使用INSERT、UPDATE、DELETE等修改数据的操作\n");
        prompt.append("- 根据用户角色限制查询范围（学生只能查看自己的数据）\n");
        prompt.append("- 使用清晰、友好的语言回答用户问题\n");
        
        return prompt.toString();
    }
    
    private String processSQLQueries(String aiResponse, String role, Long userId) {
        // 查找所有SQL查询标记
        Pattern pattern = Pattern.compile("\\[SQL_QUERY\\](.*?)\\[/SQL_QUERY\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(aiResponse);
        
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String sqlQuery = matcher.group(1).trim();
            
            // 验证SQL安全性
            if (!isSafeSQLQuery(sqlQuery, role, userId)) {
                matcher.appendReplacement(result, "[查询被拒绝：权限不足或查询不安全]");
                continue;
            }
            
            try {
                // 执行SQL查询
                List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(sqlQuery);
                
                // 格式化查询结果
                String formattedResult = formatQueryResult(queryResult);
                matcher.appendReplacement(result, Matcher.quoteReplacement(formattedResult));
                
            } catch (Exception e) {
                matcher.appendReplacement(result, "[查询执行失败：" + e.getMessage() + "]");
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    private boolean isSafeSQLQuery(String sql, String role, Long userId) {
        // 转换为大写以便检查
        String upperSQL = sql.toUpperCase().trim();
        
        // 只允许SELECT查询
        if (!upperSQL.startsWith("SELECT")) {
            return false;
        }
        
        // 禁止使用某些危险关键字
        String[] dangerousKeywords = {"DROP", "DELETE", "UPDATE", "INSERT", "ALTER", "CREATE", "TRUNCATE", "EXEC", "EXECUTE"};
        for (String keyword : dangerousKeywords) {
            if (upperSQL.contains(keyword)) {
                return false;
            }
        }
        
        // 学生角色需要额外限制
        if ("student".equals(role)) {
            // 确保查询中包含用户ID限制（简单检查）
            // 在实际生产环境中，应该更严格地验证
            if (!sql.contains(String.valueOf(userId))) {
                // 允许查询公共信息（课程列表等）
                if (!upperSQL.contains("FROM COURSES") && 
                    !upperSQL.contains("FROM USERS") &&
                    !upperSQL.contains("FROM RESOURCES")) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private String formatQueryResult(List<Map<String, Object>> queryResult) {
        if (queryResult == null || queryResult.isEmpty()) {
            return "查询结果为空。";
        }
        
        StringBuilder formatted = new StringBuilder();
        formatted.append("\n查询结果（共 ").append(queryResult.size()).append(" 条记录）：\n\n");
        
        // 如果结果太多，只显示前10条
        int displayCount = Math.min(queryResult.size(), 10);
        
        for (int i = 0; i < displayCount; i++) {
            Map<String, Object> row = queryResult.get(i);
            formatted.append(i + 1).append(". ");
            
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                formatted.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
            }
            
            // 删除最后的逗号和空格
            formatted.setLength(formatted.length() - 2);
            formatted.append("\n");
        }
        
        if (queryResult.size() > displayCount) {
            formatted.append("\n...(还有 ").append(queryResult.size() - displayCount).append(" 条记录)\n");
        }
        
        return formatted.toString();
    }

    public List<Map<String, Object>> getHistory(Long userId, String role, String sessionId) {
        try {
            // 直接转发到Python AI服务的/history接口
            String url = aiApiUrl.replace("/chat", "/history") + "?userId=" + userId + "&role=" + role;
            if (sessionId != null && !sessionId.isEmpty()) {
                url += "&sessionId=" + sessionId;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("messages")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> messages = (List<Map<String, Object>>) response.get("messages");
                return messages;
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getSessions(Long userId, String role) {
        try {
            String url = aiApiUrl.replace("/chat", "/sessions") + "?userId=" + userId + "&role=" + role;
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("sessions")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> sessions = (List<Map<String, Object>>) response.get("sessions");
                return sessions;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Map<String, String> createSession(Long userId, String role, String title) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId.toString());
            body.put("role", role);
            if (title != null) {
                body.put("title", title);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                aiApiUrl.replace("/chat", "/sessions"),
                body,
                Map.class
            );
            Map<String, String> result = new HashMap<>();
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                result.put("sessionId", Objects.toString(response.get("session_id"), Objects.toString(response.get("sessionId"), "")));
                result.put("sessionTitle", Objects.toString(response.get("session_title"), Objects.toString(response.get("sessionTitle"), "")));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }
}