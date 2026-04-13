package cn.edu.zjut.back.dto;

import lombok.Data;
import java.util.List;

/**
 * 批量导入结果DTO
 */
@Data
public class BatchImportResultDTO {
    private Integer total;  // 总数
    private Integer success;  // 成功数
    private Integer failed;  // 失败数
    private List<String> errors;  // 错误信息列表
    private List<ImportUserDTO> failedUsers;  // 失败的用户数据
}
