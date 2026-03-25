package cn.edu.zjut.back.service.impl;

import cn.edu.zjut.back.service.LogService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getAdminLogs(Integer page, Integer size, String startDate, String endDate, String adminName, String operation, String module) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, admin_id AS adminId, admin_name AS adminName, " +
            "operation, module, request_url AS requestUrl, " +
            "ip_address AS ipAddress, operation_time AS operationTime " +
            "FROM tb_admin_operation_log WHERE 1=1"
        );
        
        List<Object> args = new ArrayList<>();
        
        if (adminName != null && !adminName.trim().isEmpty()) {
            sql.append(" AND admin_name LIKE ?");
            args.add("%" + adminName + "%");
        }
        
        if (operation != null && !operation.trim().isEmpty()) {
            sql.append(" AND operation = ?");
            args.add(operation);
        }
        
        if (module != null && !module.trim().isEmpty()) {
            sql.append(" AND module = ?");
            args.add(module);
        }
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) >= ?");
            args.add(startDate);
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) <= ?");
            args.add(endDate);
        }
        
        sql.append(" ORDER BY operation_time DESC");
        
        int offset = (page - 1) * size;
        sql.append(" LIMIT ? OFFSET ?");
        args.add(size);
        args.add(offset);
        
        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    @Override
    public int getAdminLogsCount(String startDate, String endDate, String adminName, String operation, String module) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM tb_admin_operation_log WHERE 1=1"
        );
        
        List<Object> args = new ArrayList<>();
        
        if (adminName != null && !adminName.trim().isEmpty()) {
            sql.append(" AND admin_name LIKE ?");
            args.add("%" + adminName + "%");
        }
        
        if (operation != null && !operation.trim().isEmpty()) {
            sql.append(" AND operation = ?");
            args.add(operation);
        }
        
        if (module != null && !module.trim().isEmpty()) {
            sql.append(" AND module = ?");
            args.add(module);
        }
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) >= ?");
            args.add(startDate);
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) <= ?");
            args.add(endDate);
        }
        
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return count != null ? count : 0;
    }

    @Override
    public List<Map<String, Object>> getTeacherLogs(Integer page, Integer size, String startDate, String endDate, String teacherName, String operation, String module) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, teacher_id AS teacherId, teacher_name AS teacherName, " +
            "operation, module, request_url AS requestUrl, " +
            "ip_address AS ipAddress, operation_time AS operationTime " +
            "FROM tb_teacher_operation_log WHERE 1=1"
        );
        
        List<Object> args = new ArrayList<>();
        
        if (teacherName != null && !teacherName.trim().isEmpty()) {
            sql.append(" AND teacher_name LIKE ?");
            args.add("%" + teacherName + "%");
        }
        
        if (operation != null && !operation.trim().isEmpty()) {
            sql.append(" AND operation = ?");
            args.add(operation);
        }
        
        if (module != null && !module.trim().isEmpty()) {
            sql.append(" AND module = ?");
            args.add(module);
        }
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) >= ?");
            args.add(startDate);
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) <= ?");
            args.add(endDate);
        }
        
        sql.append(" ORDER BY operation_time DESC");
        
        int offset = (page - 1) * size;
        sql.append(" LIMIT ? OFFSET ?");
        args.add(size);
        args.add(offset);
        
        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    @Override
    public int getTeacherLogsCount(String startDate, String endDate, String teacherName, String operation, String module) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM tb_teacher_operation_log WHERE 1=1"
        );
        
        List<Object> args = new ArrayList<>();
        
        if (teacherName != null && !teacherName.trim().isEmpty()) {
            sql.append(" AND teacher_name LIKE ?");
            args.add("%" + teacherName + "%");
        }
        
        if (operation != null && !operation.trim().isEmpty()) {
            sql.append(" AND operation = ?");
            args.add(operation);
        }
        
        if (module != null && !module.trim().isEmpty()) {
            sql.append(" AND module = ?");
            args.add(module);
        }
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) >= ?");
            args.add(startDate);
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) <= ?");
            args.add(endDate);
        }
        
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return count != null ? count : 0;
    }

    @Override
    public List<Map<String, Object>> getStudentLogs(Integer page, Integer size, String startDate, String endDate, String studentName, String operation, String module) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, student_id AS studentId, student_name AS studentName, " +
            "operation, module, request_url AS requestUrl, " +
            "ip_address AS ipAddress, operation_time AS operationTime " +
            "FROM tb_student_operation_log WHERE 1=1"
        );
        
        List<Object> args = new ArrayList<>();
        
        if (studentName != null && !studentName.trim().isEmpty()) {
            sql.append(" AND student_name LIKE ?");
            args.add("%" + studentName + "%");
        }
        
        if (operation != null && !operation.trim().isEmpty()) {
            sql.append(" AND operation = ?");
            args.add(operation);
        }
        
        if (module != null && !module.trim().isEmpty()) {
            sql.append(" AND module = ?");
            args.add(module);
        }
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) >= ?");
            args.add(startDate);
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) <= ?");
            args.add(endDate);
        }
        
        sql.append(" ORDER BY operation_time DESC");
        
        int offset = (page - 1) * size;
        sql.append(" LIMIT ? OFFSET ?");
        args.add(size);
        args.add(offset);
        
        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    @Override
    public int getStudentLogsCount(String startDate, String endDate, String studentName, String operation, String module) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM tb_student_operation_log WHERE 1=1"
        );
        
        List<Object> args = new ArrayList<>();
        
        if (studentName != null && !studentName.trim().isEmpty()) {
            sql.append(" AND student_name LIKE ?");
            args.add("%" + studentName + "%");
        }
        
        if (operation != null && !operation.trim().isEmpty()) {
            sql.append(" AND operation = ?");
            args.add(operation);
        }
        
        if (module != null && !module.trim().isEmpty()) {
            sql.append(" AND module = ?");
            args.add(module);
        }
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) >= ?");
            args.add(startDate);
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) <= ?");
            args.add(endDate);
        }
        
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return count != null ? count : 0;
    }

    @Override
    public byte[] exportLogs(String type, String startDate, String endDate) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("日志数据");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            String[] headers;
            String sql;
            
            // 根据类型设置不同的列标题和SQL
            if ("teacher".equals(type)) {
                headers = new String[]{"ID", "教师姓名", "工号", "操作", "模块", "请求URL", "IP地址", "操作时间"};
                sql = buildExportSql("tb_teacher_operation_log", "teacher_name", "teacher_number", startDate, endDate);
            } else if ("student".equals(type)) {
                headers = new String[]{"ID", "学生姓名", "学号", "操作", "模块", "请求URL", "IP地址", "操作时间"};
                sql = buildExportSql("tb_student_operation_log", "student_name", "student_number", startDate, endDate);
            } else {
                headers = new String[]{"ID", "管理员姓名", "操作", "模块", "请求URL", "IP地址", "操作时间"};
                sql = buildExportSql("tb_admin_operation_log", "admin_name", null, startDate, endDate);
            }
            
            // 填充标题
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }
            
            // 查询数据
            List<Map<String, Object>> logs = jdbcTemplate.queryForList(sql);
            
            // 填充数据
            int rowNum = 1;
            for (Map<String, Object> log : logs) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;
                
                row.createCell(colNum++).setCellValue(log.get("id").toString());
                
                if ("teacher".equals(type)) {
                    row.createCell(colNum++).setCellValue((String) log.get("teacher_name"));
                    row.createCell(colNum++).setCellValue((String) log.get("teacher_number"));
                } else if ("student".equals(type)) {
                    row.createCell(colNum++).setCellValue((String) log.get("student_name"));
                    row.createCell(colNum++).setCellValue((String) log.get("student_number"));
                } else {
                    row.createCell(colNum++).setCellValue((String) log.get("admin_name"));
                }
                
                row.createCell(colNum++).setCellValue((String) log.get("operation"));
                row.createCell(colNum++).setCellValue((String) log.get("module"));
                row.createCell(colNum++).setCellValue((String) log.get("request_url"));
                row.createCell(colNum++).setCellValue((String) log.get("ip_address"));
                row.createCell(colNum++).setCellValue(log.get("operation_time").toString());
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("导出日志失败: " + e.getMessage(), e);
        }
    }
    
    private String buildExportSql(String tableName, String nameColumn, String numberColumn, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE 1=1");
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) >= '").append(startDate).append("'");
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND DATE(operation_time) <= '").append(endDate).append("'");
        }
        
        sql.append(" ORDER BY operation_time DESC");
        return sql.toString();
    }

    @Override
    public int clearOldLogs(int days) {
        String adminSql = "DELETE FROM tb_admin_operation_log WHERE operation_time < DATE_SUB(NOW(), INTERVAL ? DAY)";
        String teacherSql = "DELETE FROM tb_teacher_operation_log WHERE operation_time < DATE_SUB(NOW(), INTERVAL ? DAY)";
        String studentSql = "DELETE FROM tb_student_operation_log WHERE operation_time < DATE_SUB(NOW(), INTERVAL ? DAY)";
        
        int adminDeleted = jdbcTemplate.update(adminSql, days);
        int teacherDeleted = jdbcTemplate.update(teacherSql, days);
        int studentDeleted = jdbcTemplate.update(studentSql, days);
        
        return adminDeleted + teacherDeleted + studentDeleted;
    }
}
