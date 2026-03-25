package cn.edu.zjut.back.vo;

import lombok.Data;
import java.util.List;

/**
 * 分页结果VO
 */
@Data
public class PageResultVO<T> {
    private List<T> records;  // 数据列表
    private List<T> list;     // 数据列表(兼容前端)
    private Long total;  // 总记录数
    private Integer page;  // 当前页
    private Integer pageSize;  // 每页大小
    private Integer size;  // 每页大小(兼容前端)
    private Integer totalPages;  // 总页数
    
    public PageResultVO() {
    }
    
    public PageResultVO(List<T> records, Long total, Integer page, Integer pageSize) {
        this.records = records;
        this.list = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.size = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }
    
    public void setList(List<T> list) {
        this.list = list;
        this.records = list;
    }
    
    public void setRecords(List<T> records) {
        this.records = records;
        this.list = records;
    }
}
