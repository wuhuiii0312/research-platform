package com.research.auth.controller;

import com.research.common.core.domain.CommonResult;
import com.research.common.core.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统管理：测试数据清理（仅管理员可调用）
 * 网关路由：/api/system/** -> research-auth
 */
@Slf4j
@RestController
@RequestMapping("/system")
public class SystemController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/clear/testData")
    public CommonResult<?> clearTestData() {
        if (!SecurityUtils.isAdmin()) {
            return CommonResult.fail(403, "仅管理员可执行测试数据清理");
        }
        try {
            // 1) 按 test_data_flag 清理文档/成果/批注
            int a = jdbcTemplate.update("DELETE FROM document_annotation WHERE test_data_flag = 1");
            int b = jdbcTemplate.update("DELETE FROM document_meta WHERE test_data_flag = 1");
            int c = jdbcTemplate.update("DELETE FROM project_result WHERE test_data_flag = 1");

            // 2) 额外清理名称包含“测试”或无归属项目的任务/文档/成果
            int t1 = jdbcTemplate.update("DELETE FROM task WHERE name LIKE '测试%' OR project_id IS NULL");
            int t2 = jdbcTemplate.update("DELETE FROM document_meta WHERE project_id IS NULL OR name LIKE '测试%'");
            int t3 = jdbcTemplate.update("DELETE FROM project_result WHERE project_id IS NULL OR name LIKE '测试%'");

            log.info("测试数据清理完成: annotation={}, meta={}, result={}, taskDelByNameOrProjectNull={}, docDelByNameOrProjectNull={}, resultDelByNameOrProjectNull={}",
                    a, b, c, t1, t2, t3);
            return CommonResult.success("清理完成。MongoDB/ES 需在各自服务执行或通过脚本清理。");
        } catch (Exception e) {
            log.error("测试数据清理失败", e);
            return CommonResult.fail(500, "清理失败: " + e.getMessage());
        }
    }
}
