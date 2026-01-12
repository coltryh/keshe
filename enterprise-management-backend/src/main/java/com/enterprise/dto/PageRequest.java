package com.enterprise.dto;

import lombok.Data;

/**
 * 分页请求 DTO
 */
@Data
public class PageRequest {

    private Integer pageNum = 1;  // 页码，默认第1页
    private Integer pageSize = 10;  // 每页大小，默认10条
    private String keyword;  // 搜索关键字
    private String sortBy;  // 排序字段
    private String sortOrder;  // 排序方向：asc, desc
}
