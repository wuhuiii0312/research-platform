package com.research.search.controller;

import com.research.common.core.domain.CommonResult;
import com.research.search.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 检索接口：公开检索（过滤 test_data_flag，仅返回 is_public=1 且非测试数据）
 */
@RestController
@RequestMapping("/search")
@Api(tags = "检索")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/public")
    @ApiOperation("公开检索（文档/成果），过滤测试数据")
    public CommonResult<?> searchPublic(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "document") String type,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return searchService.searchPublic(keyword, type, pageNum, pageSize);
    }
}
