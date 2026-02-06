package com.research.auth.controller;

import com.research.common.core.domain.CommonResult;
import com.research.common.core.util.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 个人中心相关接口
 */
@RestController
@RequestMapping("/auth")
public class ProfileController {

    /**
     * 个人中心统计数据（按角色返回差异化数据）
     * 
     * @param role 角色（LEADER/MEMBER/VISITOR），可选，默认从 SecurityUtils 获取
     * @return 统计数据
     */
    @GetMapping("/profile/stats")
    public CommonResult<Map<String, Object>> getProfileStats(@RequestParam(required = false) String role) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null || userId <= 0) {
            return CommonResult.fail(401, "未登录或登录已过期");
        }
        
        // 如果没有传 role 参数，从 SecurityUtils 获取
        if (role == null || role.trim().isEmpty()) {
            role = SecurityUtils.getRoleCode();
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        // 根据角色返回不同的统计数据
        if ("LEADER".equalsIgnoreCase(role)) {
            // 负责人：项目数、待审核成果数、团队成员数
            stats.put("projectCount", 0);
            stats.put("pendingResultCount", 0);
            stats.put("teamMemberCount", 0);
        } else if ("MEMBER".equalsIgnoreCase(role)) {
            // 成员：参与项目数、待办任务数、已提交成果数
            stats.put("joinProjectCount", 0);
            stats.put("todoTaskCount", 0);
            stats.put("submittedResultCount", 0);
        } else if ("VISITOR".equalsIgnoreCase(role)) {
            // 访客：授权项目数、已查看文档数
            stats.put("authProjectCount", 0);
            stats.put("viewedDocCount", 0);
        } else {
            // 默认返回空统计
            stats.put("count", 0);
        }
        
        return CommonResult.success(stats);
    }
}
