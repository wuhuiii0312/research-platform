package com.research.common.core.filter;

import org.springframework.stereotype.Component;

/**
 * 数据过滤组件：与 @PreAuthorize 配合，表示是否应用测试数据过滤。
 * 所有文档/成果/批注查询处已强制添加 test_data_flag=0 条件。
 */
@Component("dataFilter")
public class DataFilter {

    /**
     * 是否过滤测试数据（当前统一为 true，即仅查非测试数据）
     */
    public boolean filterTestData(Long userId) {
        return true;
    }
}
