package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.annotation.RequirePermission;
import cn.edu.zjut.back.dto.BatchImportResultDTO;
import cn.edu.zjut.back.dto.UserQueryDTO;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.service.AdminStudentService;
import cn.edu.zjut.back.vo.PageResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-学生管理Controller
 */
@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentController {
    
    @Autowired
    private AdminStudentService studentService;
    
    /**
     * 分页查询学生列表
     */
    @GetMapping
    public ResponseEntity<PageResultVO<User>> getStudents(UserQueryDTO query) {
        PageResultVO<User> result = studentService.getStudentList(query);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取学生详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getStudent(@PathVariable Long id) {
        // 这里可以添加具体的查询逻辑
        return ResponseEntity.ok(new User());
    }
    
    /**
     * 添加学生
     */
    @RequirePermission(level = 3, description = "添加学生")
    @PostMapping
    public ResponseEntity<Map<String, Object>> addStudent(@RequestBody User student) {
        Map<String, Object> response = new HashMap<>();
        try {
            User newStudent = studentService.addStudent(student);
            response.put("success", true);
            response.put("message", "学生添加成功");
            response.put("data", newStudent);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新学生信息
     */
    @RequirePermission(level = 3, description = "更新学生信息")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateStudent(@PathVariable Long id, @RequestBody User student) {
        Map<String, Object> response = new HashMap<>();
        try {
            student.setId(id);
            User updatedStudent = studentService.updateStudent(student);
            response.put("success", true);
            response.put("message", "学生信息更新成功");
            response.put("data", updatedStudent);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除学生
     */
    @RequirePermission(level = 5, description = "删除学生")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            studentService.deleteStudent(id);
            response.put("success", true);
            response.put("message", "学生删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 重置学生密码
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String newPassword = request.getOrDefault("password", "123456");
            studentService.resetPassword(id, newPassword);
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
     * 获取所有专业列表
     */
    @GetMapping("/majors")
    public ResponseEntity<List<String>> getAllMajors() {
        List<String> majors = studentService.getAllMajors();
        return ResponseEntity.ok(majors);
    }
    
    /**
     * 获取所有班级列表
     */
    @GetMapping("/classes")
    public ResponseEntity<List<String>> getAllClasses() {
        List<String> classes = studentService.getAllClasses();
        return ResponseEntity.ok(classes);
    }
    
    /**
     * 批量导入学生
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
            
            BatchImportResultDTO result = studentService.batchImport(file);
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
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("学生导入模板");
            
            // 创建表头
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("用户名/学号");
            headerRow.createCell(1).setCellValue("密码");
            headerRow.createCell(2).setCellValue("姓名");
            headerRow.createCell(3).setCellValue("性别(male/female)");
            headerRow.createCell(4).setCellValue("邮箱");
            headerRow.createCell(5).setCellValue("电话");
            headerRow.createCell(6).setCellValue("专业");
            headerRow.createCell(7).setCellValue("班级");
            
            // 添加示例数据
            org.apache.poi.ss.usermodel.Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("2021001");
            exampleRow.createCell(1).setCellValue("123456");
            exampleRow.createCell(2).setCellValue("张三");
            exampleRow.createCell(3).setCellValue("male");
            exampleRow.createCell(4).setCellValue("student001@example.com");
            exampleRow.createCell(5).setCellValue("13800138000");
            exampleRow.createCell(6).setCellValue("计算机科学");
            exampleRow.createCell(7).setCellValue("计科2101");
            
            // 设置列宽
            for (int i = 0; i < 8; i++) {
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
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-template.xlsx")
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
