package com.research.project.controller;

import com.research.common.core.domain.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/result")
@Api(tags = "成果管理（占位，统计功能通过 AchievementController 提供）")
public class ResultController {
    // 为避免继续引入大量未落地的 CRUD 改造，这里暂不提供具体实现，
    // 成果的主要展示与统计通过 AchievementController + 统计接口完成。
}
