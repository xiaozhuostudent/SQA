package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class FileUploadController implements InitializingBean {

    @Value("${file.upload.path:./storage/resources/}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:http://localhost:8080/resources/}")
    private String urlPrefix;

    /**
     * 初始化上传路径为绝对路径
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        File uploadDir = new File(uploadPath);
        if (!uploadDir.isAbsolute()) {
            // 如果是相对路径，转换为绝对路径（相对于项目根目录）
            uploadPath = new File(System.getProperty("user.dir"), uploadPath).getAbsolutePath() + File.separator;
        }
        log.info("文件上传路径已初始化为: {}", uploadPath);
        
        // 确保根目录存在
        uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                log.info("创建上传根目录成功: {}", uploadPath);
            } else {
                log.error("创建上传根目录失败: {}", uploadPath);
            }
        }
    }

    /**
     * 通用文件上传接口
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return Result.error("文件名不能为空");
            }

            // 限制文件大小为50MB
            if (file.getSize() > 50 * 1024 * 1024) {
                return Result.error("文件大小不能超过50MB");
            }

            // 获取文件扩展名
            String fileExtension = "";
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                fileExtension = originalFilename.substring(dotIndex);
            }

            // 按日期创建子目录
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String datePath = sdf.format(new Date());
            String uploadDir = uploadPath + datePath + File.separator;

            // 确保目录存在
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    log.error("创建目录失败: {}", uploadDir);
                    return Result.error("创建目录失败");
                }
                log.info("创建目录成功: {}", uploadDir);
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString().replace("-", "") + fileExtension;
            String filePath = uploadDir + fileName;

            // 保存文件
            File dest = new File(filePath);
            log.info("准备保存文件到: {}", dest.getAbsolutePath());
            file.transferTo(dest);

            // 构建文件访问URL
            String fileUrl = urlPrefix + datePath + "/" + fileName;

            // 返回文件信息
            Map<String, Object> result = new HashMap<>();
            result.put("name", originalFilename);
            result.put("url", fileUrl);
            result.put("size", file.getSize());
            result.put("type", file.getContentType());

            log.info("文件上传成功: {} -> {}", originalFilename, fileUrl);

            return Result.success(result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量文件上传接口
     */
    @PostMapping("/upload/batch")
    public Result<List<Map<String, Object>>> uploadBatch(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            Result<Map<String, Object>> result = upload(file);
            if (result.getCode() == 200) {
                results.add(result.getData());
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("name", file.getOriginalFilename());
                error.put("error", result.getMessage());
                results.add(error);
            }
        }
        
        return Result.success(results);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/upload/{dateStr}/{fileName}")
    public Result<String> deleteFile(@PathVariable String dateStr, @PathVariable String fileName) {
        try {
            // 将日期字符串转换为路径格式 (例如: 2026-01-04 -> 2026/01/04)
            String datePath = dateStr.replace("-", "/");
            String filePath = uploadPath + datePath + "/" + fileName;
            
            File file = new File(filePath);
            if (file.exists() && file.delete()) {
                log.info("文件删除成功: {}", filePath);
                return Result.success("文件删除成功");
            } else {
                return Result.error("文件不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
}
