package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.dto.BatchImportResultDTO;
import cn.edu.zjut.back.dto.UserQueryDTO;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.service.AdminTeacherService;
import cn.edu.zjut.back.vo.PageResultVO;
import cn.edu.zjut.back.vo.TeacherStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-教师管理Controller
 */
@RestController
@RequestMapping("/api/admin/teachers")
public class AdminTeacherController {
    
    @Autowired
    private AdminTeacherService teacherService;
    
    /**
     * 分页查询教师列表
     */
    @GetMapping
    public ResponseEntity<PageResultVO<User>> getTeachers(UserQueryDTO query) {
        PageResultVO<User> result = teacherService.getTeacherList(query);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取教师详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getTeacher(@PathVariable Long id) {
        // 这里可以添加具体的查询逻辑
        return ResponseEntity.ok(new User());
    }
    
    /**
     * 添加教师
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addTeacher(@RequestBody User teacher) {
        Map<String, Object> response = new HashMap<>();
        try {
            User newTeacher = teacherService.addTeacher(teacher);
            response.put("success", true);
            response.put("message", "教师添加成功");
            response.put("data", newTeacher);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新教师信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTeacher(@PathVariable Long id, @RequestBody User teacher) {
        Map<String, Object> response = new HashMap<>();
        try {
            teacher.setId(id);
            User updatedTeacher = teacherService.updateTeacher(teacher);
            response.put("success", true);
            response.put("message", "教师信息更新成功");
            response.put("data", updatedTeacher);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除教师
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTeacher(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            teacherService.deleteTeacher(id);
            response.put("success", true);
            response.put("message", "教师删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 重置教师密码
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String newPassword = request.getOrDefault("password", "123456");
            teacherService.resetPassword(id, newPassword);
            response.put("success", true);
            response.put("message", "密码重置成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取教师授课统计
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<TeacherStatsVO> getTeacherStats(@PathVariable Long id) {
        TeacherStatsVO stats = teacherService.getTeacherStats(id);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 批量导入教师
     */
    @PostMapping("/batch-import")
    public ResponseEntity<Map<String, Object>> batchImport(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            BatchImportResultDTO result = teacherService.batchImport(file);
            response.put("success", true);
            response.put("message", "导入完成");
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "导入失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 下载导入模板
     */
    @GetMapping("/download-template")
    public ResponseEntity<org.springframework.core.io.Resource> downloadTemplate() {
        try {
            // 创建内存中Excel文件
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("教师导入模板");
            
            // 创建表头
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("用户名");
            headerRow.createCell(1).setCellValue("密码");
            headerRow.createCell(2).setCellValue("姓名");
            headerRow.createCell(3).setCellValue("邮箱");
            headerRow.createCell(4).setCellValue("电话");
            headerRow.createCell(5).setCellValue("性别(male/female)");
            
            // 添加示例数据
            org.apache.poi.ss.usermodel.Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("teacher001");
            exampleRow.createCell(1).setCellValue("123456");
            exampleRow.createCell(2).setCellValue("张教授");
            exampleRow.createCell(3).setCellValue("teacher001@example.com");
            exampleRow.createCell(4).setCellValue("13800138000");
            exampleRow.createCell(5).setCellValue("male");
            
            // 设置列宽
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 2000);
            }
            
            // 将工作簿写入字节流
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            byte[] bytes = outputStream.toByteArray();
            org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(bytes);
            
            return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teacher-template.xlsx")
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
