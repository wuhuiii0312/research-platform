package com.research.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.entity.SysOperationLog;
import com.research.common.core.mapper.SysOperationLogMapper;
import com.research.common.core.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class OperationLogController {

    @Autowired
    private SysOperationLogMapper operationLogMapper;

    @GetMapping("/operationLog")
    public CommonResult<IPage<SysOperationLog>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) userId = 0L;
        IPage<SysOperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .eq(SysOperationLog::getUserId, userId)
                .orderByDesc(SysOperationLog::getCreateTime);
        IPage<SysOperationLog> logPage = operationLogMapper.selectPage(page, wrapper);
        return CommonResult.success(logPage);
    }
}
