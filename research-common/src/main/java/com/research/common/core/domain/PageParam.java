package com.research.common.core.domain;

import lombok.Data;

/**
 * 分页查询参数基类
 */
@Data
public class PageParam {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String orderByColumn;
    private String isAsc = "asc";

    public Integer getPageNum() {
        return pageNum == null || pageNum <= 0 ? 1 : pageNum;
    }

    public Integer getPageSize() {
        return pageSize == null || pageSize <= 0 ? 10 : pageSize;
    }
}
