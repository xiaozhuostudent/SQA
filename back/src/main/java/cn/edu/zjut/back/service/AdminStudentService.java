package cn.edu.zjut.back.service;

import cn.edu.zjut.back.dto.BatchImportResultDTO;
import cn.edu.zjut.back.dto.ImportUserDTO;
import cn.edu.zjut.back.dto.UserQueryDTO;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.mapper.UserMapper;
import cn.edu.zjut.back.vo.PageResultVO;
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
 * 管理员-学生管理Service
 */
@Service
public class AdminStudentService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 分页查询学生列表
     */
    public PageResultVO<User> getStudentList(UserQueryDTO query) {
        query.setRole("student");  // 强制设置为学生角色
        int offset = (query.getPage() - 1) * query.getPageSize();
        List<User> students = userMapper.queryUsersWithPagination(query, offset);
        long total = userMapper.countUsersByQuery(query);
        return new PageResultVO<>(students, total, query.getPage(), query.getPageSize());
    }
    
    /**
     * 添加学生
     */
    @Transactional
    public User addStudent(User student) {
        // 验证用户名唯一性
        if (userMapper.checkUsernameExists(student.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 设置角色和状态
        student.setRole("student");
        student.setStatus("active");
        
        // 加密密码
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        
        // 插入基础用户信息
        userMapper.insert(student);
        
        // 插入学生扩展信息
        if (student.getStudentNumber() != null) {
            userMapper.insertStudentInfo(
                student.getId(),
                student.getStudentNumber(),
                student.getMajor(),
                student.getClassName(),
                student.getEnrollmentYear(),
                student.getGrade()
            );
        }
        
        return student;
    }
    
    /**
     * 更新学生信息
     */
    @Transactional
    public User updateStudent(User student) {
        // 更新基础信息
        userMapper.update(student);
        
        // 更新学生扩展信息
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        if (student.getStudentNumber() != null) updates.put("studentNumber", student.getStudentNumber());
        if (student.getMajor() != null) updates.put("major", student.getMajor());
        if (student.getClassName() != null) updates.put("className", student.getClassName());
        if (student.getEnrollmentYear() != null) updates.put("enrollmentYear", student.getEnrollmentYear());
        if (student.getGrade() != null) updates.put("grade", student.getGrade());
        
        if (!updates.isEmpty()) {
            userMapper.updateStudentInfo(student.getId(), updates);
        }
        
        return userMapper.findByIdWithDetails(student.getId());
    }
    
    /**
     * 删除学生
     */
    @Transactional
    public void deleteStudent(Long id) {
        userMapper.delete(id);
    }
    
    /**
     * 重置学生密码
     */
    public void resetPassword(Long id, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(id, encodedPassword);
    }
    
    /**
     * 获取所有专业列表
     */
    public List<String> getAllMajors() {
        return userMapper.getAllMajors();
    }
    
    /**
     * 获取所有班级列表
     */
    public List<String> getAllClasses() {
        return userMapper.getAllClasses();
    }
    
    /**
     * 批量导入学生（Excel）
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
                    ImportUserDTO importUser = parseStudentRow(row, i + 1);
                    
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
                    User student = new User();
                    student.setUsername(importUser.getUsername());
                    student.setPassword(passwordEncoder.encode(importUser.getPassword() != null ? importUser.getPassword() : "123456"));
                    student.setRealName(importUser.getName());
                    student.setEmail(importUser.getEmail());
                    student.setPhone(importUser.getPhone());
                    student.setGender(importUser.getGender());
                    student.setRole("student");
                    student.setStatus("active");
                    
                    // 插入基础信息
                    userMapper.insert(student);
                    
                    // 插入学生扩展信息
                    if (importUser.getMajor() != null || importUser.getClassName() != null) {
                        Integer enrollmentYear = LocalDate.now().getYear();  // 默认当前年份
                        Integer grade = calculateGrade(enrollmentYear);
                        
                        userMapper.insertStudentInfo(
                            student.getId(),
                            importUser.getUsername(),  // 默认学号=用户名
                            importUser.getMajor(),
                            importUser.getClassName(),
                            enrollmentYear,
                            grade
                        );
                    }
                    
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
     * 解析Excel行数据（学生）
     */
    private ImportUserDTO parseStudentRow(Row row, int rowNumber) {
        ImportUserDTO user = new ImportUserDTO();
        user.setRowNumber(rowNumber);
        user.setUsername(getCellValue(row.getCell(0)));  // A列：用户名/学号
        user.setPassword(getCellValue(row.getCell(1)));  // B列：密码
        user.setName(getCellValue(row.getCell(2)));      // C列：姓名
        user.setGender(getCellValue(row.getCell(3)));    // D列：性别
        user.setEmail(getCellValue(row.getCell(4)));     // E列：邮箱
        user.setPhone(getCellValue(row.getCell(5)));     // F列：电话
        user.setMajor(getCellValue(row.getCell(6)));     // G列：专业
        user.setClassName(getCellValue(row.getCell(7))); // H列：班级
        user.setRole("student");
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
    
    /**
     * 根据入学年份计算年级
     */
    private Integer calculateGrade(Integer enrollmentYear) {
        int currentYear = LocalDate.now().getYear();
        int yearDiff = currentYear - enrollmentYear;
        
        if (yearDiff < 1) return 1;
        else if (yearDiff < 2) return 2;
        else if (yearDiff < 3) return 3;
        else return 4;
    }
}
