package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.service.AIAssistantService;
import cn.edu.zjut.back.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-assistant")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AIAssistantController {

    @Autowired
    private AIAssistantService aiAssistantService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String userMessage = (String) request.get("message");
            String role = resolveRole(httpRequest, request.get("role"));
            Long userId = resolveUserId(httpRequest, request.get("userId"));
            String sessionId = request.get("sessionId") == null ? null : request.get("sessionId").toString();
            String sessionTitle = request.get("sessionTitle") == null ? null : request.get("sessionTitle").toString();
            
            // 安全地处理history参数
            List<Map<String, String>> history = null;
            Object historyObj = request.get("history");
            if (historyObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> tempHistory = (List<Map<String, String>>) historyObj;
                history = tempHistory;
            }
            
            // 安全地处理images参数
            List<String> images = null;
            Object imagesObj = request.get("images");
            if (imagesObj instanceof List) {
                List<?> rawList = (List<?>) imagesObj;
                images = new java.util.ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof String) {
                        images.add((String) item);
                    } else if (item instanceof List) {
                        // 处理嵌套的ArrayList，取第一个元素
                        List<?> nestedList = (List<?>) item;
                        if (!nestedList.isEmpty() && nestedList.get(0) instanceof String) {
                            images.add((String) nestedList.get(0));
                        }
                    } else {
                        // 其他类型转为字符串
                        images.add(String.valueOf(item));
                    }
                }
            }
            
            String response = aiAssistantService.chat(userMessage, role, userId, sessionId, sessionTitle, history, images);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("response", response);
            result.put("sessionId", sessionId);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    @GetMapping("/sessions")
    public Map<String, Object> sessions(@RequestParam(required = false) Long userId,
                                        @RequestParam(required = false) String role,
                                        HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long resolvedUserId = resolveUserId(request, userId);
            String resolvedRole = resolveRole(request, role);
            List<Map<String, Object>> sessions = aiAssistantService.getSessions(resolvedUserId, resolvedRole);
            result.put("success", true);
            result.put("sessions", sessions);
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    @PostMapping("/sessions")
    public Map<String, Object> newSession(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = resolveUserId(httpRequest, request.get("userId"));
            String role = resolveRole(httpRequest, request.get("role"));
            String title = request.get("title") == null ? null : request.get("title").toString();
            Map<String, String> session = aiAssistantService.createSession(userId, role, title);
            result.put("success", true);
            result.putAll(session);
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    @GetMapping("/history")
    public Map<String, Object> getHistory(@RequestParam(required = false) Long userId,
                                          @RequestParam(required = false) String role,
                                          @RequestParam(required = false) String sessionId,
                                          HttpServletRequest request) {
        try {
            Long resolvedUserId = resolveUserId(request, userId);
            String resolvedRole = resolveRole(request, role);
            List<Map<String, Object>> messages = aiAssistantService.getHistory(resolvedUserId, resolvedRole, sessionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messages", messages);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    private Long resolveUserId(HttpServletRequest request, Object fallbackUserId) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        System.out.println("[AI Assistant] 从JWT解析的userId: " + userId);
        System.out.println("[AI Assistant] 请求体/参数中的fallbackUserId: " + fallbackUserId);
        if (userId != null) {
            System.out.println("[AI Assistant] 使用JWT中的userId: " + userId);
            return userId;
        }
        if (fallbackUserId != null) {
            try {
                Long parsed = Long.parseLong(fallbackUserId.toString());
                System.out.println("[AI Assistant] 使用fallback userId: " + parsed);
                return parsed;
            } catch (NumberFormatException ignored) {
            }
        }
        throw new IllegalArgumentException("无法获取用户ID，请重新登录");
    }

    private String resolveRole(HttpServletRequest request, Object fallbackRole) {
        String role = jwtUtil.getRoleFromRequest(request);
        System.out.println("[AI Assistant] 从JWT解析的role: " + role);
        System.out.println("[AI Assistant] 请求体/参数中的fallbackRole: " + fallbackRole);
        if (role != null && !role.isEmpty()) {
            System.out.println("[AI Assistant] 使用JWT中的role: " + role);
            return role;
        }
        if (fallbackRole != null) {
            String parsed = fallbackRole.toString();
            System.out.println("[AI Assistant] 使用fallback role: " + parsed);
            return parsed;
        }
        return "student";
    }
}
