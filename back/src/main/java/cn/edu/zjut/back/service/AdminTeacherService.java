package cn.edu.zjut.back.service;

import cn.edu.zjut.back.dto.BatchImportResultDTO;
import cn.edu.zjut.back.dto.ImportUserDTO;
import cn.edu.zjut.back.dto.UserQueryDTO;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.mapper.UserMapper;
import cn.edu.zjut.back.vo.PageResultVO;
import cn.edu.zjut.back.vo.TeacherStatsVO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员-教师管理Service
 */
@Service
public class AdminTeacherService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 分页查询教师列表
     */
    public PageResultVO<User> getTeacherList(UserQueryDTO query) {
        query.setRole("teacher");  // 强制设置为教师角色
        int offset = (query.getPage() - 1) * query.getPageSize();
        List<User> teachers = userMapper.queryUsersWithPagination(query, offset);
        long total = userMapper.countUsersByQuery(query);
        return new PageResultVO<>(teachers, total, query.getPage(), query.getPageSize());
    }
    
    /**
     * 添加教师
     */
    @Transactional
    public User addTeacher(User teacher) {
        // 验证用户名唯一性
        if (userMapper.checkUsernameExists(teacher.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 设置角色和状态
        teacher.setRole("teacher");
        teacher.setStatus("active");
        
        // 加密密码
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        
        // 插入基础用户信息
        userMapper.insert(teacher);
        
        // 插入教师扩展信息
        if (teacher.getTeacherNumber() != null) {
            userMapper.insertTeacherInfo(
                teacher.getId(),
                teacher.getTeacherNumber(),
                teacher.getDepartment(),
                teacher.getTitle(),
                teacher.getResearchField()
            );
        }
        
        return teacher;
    }
    
    /**
     * 更新教师信息
     */
    @Transactional
    public User updateTeacher(User teacher) {
        // 更新基础信息
        userMapper.update(teacher);
        
        // 更新教师扩展信息
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        if (teacher.getTeacherNumber() != null) updates.put("teacherNumber", teacher.getTeacherNumber());
        if (teacher.getDepartment() != null) updates.put("department", teacher.getDepartment());
        if (teacher.getTitle() != null) updates.put("title", teacher.getTitle());
        if (teacher.getResearchField() != null) updates.put("researchField", teacher.getResearchField());
        
        if (!updates.isEmpty()) {
            userMapper.updateTeacherInfo(teacher.getId(), updates);
        }
        
        return userMapper.findByIdWithDetails(teacher.getId());
    }
    
    /**
     * 删除教师
     */
    @Transactional
    public void deleteTeacher(Long id) {
        userMapper.delete(id);
    }
    
    /**
     * 重置教师密码
     */
    public void resetPassword(Long id, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(id, encodedPassword);
    }
    
    /**
     * 获取教师授课统计
     */
    public TeacherStatsVO getTeacherStats(Long teacherId) {
        return userMapper.getTeacherStats(teacherId);
    }
    
    /**
     * 批量导入教师（Excel）
     */
    @Transactional
    public BatchImportResultDTO batchImport(MultipartFile file) {
        BatchImportResultDTO result = new BatchImportResultDTO();
        result.setTotal(0);
        result.setSuccess(0);
        result.setFailed(0);
        result.setErrors(new ArrayList<>());
        result.setFailedUsers(new ArrayList<>());
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            result.setTotal(rowCount - 1);  // 减去表头
            
            for (int i = 1; i < rowCount; i++) {  // 从第2行开始（跳过表头）
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    ImportUserDTO importUser = parseTeacherRow(row, i + 1);
                    
                    // 验证必填字段
                    if (importUser.getUsername() == null || importUser.getUsername().isEmpty()) {
                        result.getErrors().add("第" + (i + 1) + "行：用户名不能为空");
                        result.setFailed(result.getFailed() + 1);
                        result.getFailedUsers().add(importUser);
                        continue;
                    }
                    
                    // 检查用户名是否已存在
                    if (userMapper.checkUsernameExists(importUser.getUsername()) > 0) {
                        result.getErrors().add("第" + (i + 1) + "行：用户名 " + importUser.getUsername() + " 已存在");
                        result.setFailed(result.getFailed() + 1);
                        result.getFailedUsers().add(importUser);
                        continue;
                    }
                    
                    // 创建用户
                    User teacher = new User();
                    teacher.setUsername(importUser.getUsername());
                    teacher.setPassword(passwordEncoder.encode(importUser.getPassword() != null ? importUser.getPassword() : "123456"));
                    teacher.setRealName(importUser.getName());
                    teacher.setEmail(importUser.getEmail());
                    teacher.setPhone(importUser.getPhone());
                    teacher.setGender(importUser.getGender());
                    teacher.setRole("teacher");
                    teacher.setStatus("active");
                    
                    // 插入基础信息
                    userMapper.insert(teacher);
                    
                    // 插入教师扩展信息（从Excel中解析的其他字段）
                    // 这里可以根据Excel格式添加更多字段
                    
                    result.setSuccess(result.getSuccess() + 1);
                    
                } catch (Exception e) {
                    result.getErrors().add("第" + (i + 1) + "行：" + e.getMessage());
                    result.setFailed(result.getFailed() + 1);
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Excel文件解析失败：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 解析Excel行数据（教师）
     */
    private ImportUserDTO parseTeacherRow(Row row, int rowNumber) {
        ImportUserDTO user = new ImportUserDTO();
        user.setRowNumber(rowNumber);
        user.setUsername(getCellValue(row.getCell(0)));  // A列：用户名
        user.setPassword(getCellValue(row.getCell(1)));  // B列：密码
        user.setName(getCellValue(row.getCell(2)));      // C列：姓名
        user.setEmail(getCellValue(row.getCell(3)));     // D列：邮箱
        user.setPhone(getCellValue(row.getCell(4)));     // E列：电话
        user.setGender(getCellValue(row.getCell(5)));    // F列：性别
        user.setRole("teacher");
        return user;
    }
    
    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}
