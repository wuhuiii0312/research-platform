package com.research.project.controller;

import com.research.common.core.annotation.Log;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.enums.BusinessType;
import com.research.common.core.util.SecurityUtils;
import com.research.project.entity.Project;
import com.research.project.entity.ProjectMember;
import com.research.project.model.ProjectQuery;
import com.research.project.mapper.ProjectMemberMapper;
import com.research.project.service.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 项目控制器
 */
@RestController
@RequestMapping("/project")
@Api(tags = "项目管理")
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Log(title = "创建项目", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    @ApiOperation("创建项目")
    public CommonResult<?> create(@RequestBody Project project) {
        return projectService.createProject(project);
    }

    // ===================== 角色专用接口（按需求文档） =====================

    /** 游客授权项目查询：返回所有公开项目（支持项目名称、状态筛选） */
    @GetMapping("/visitor/auth")
    @ApiOperation("访客查看所有公开项目")
    public CommonResult<?> visitorAuthProjects(@RequestParam(required = false) String name,
                                               @RequestParam(required = false) String status) {
        if (!"VISITOR".equalsIgnoreCase(SecurityUtils.getRoleCode())) {
            return CommonResult.fail(403, "无权限");
        }
        Long visitorId = SecurityUtils.getUserId();
        if (visitorId == null || visitorId <= 0) {
            return CommonResult.fail(401, "未登录或登录已过期");
        }
        // 访客端：返回所有公开项目，不按成员关系过滤
        ProjectQuery query = new ProjectQuery();
        // 不设置 memberId，让 Service 层返回所有公开项目
        query.setName(name);
        query.setStatus(status);
        query.setPageNum(1);
        query.setPageSize(100);
        // 直接查询公开项目，不使用 getProjectPage（它会自动过滤成员）
        return projectService.getVisitorPublicProjects(query);
    }

    /** 负责人项目列表：当前用户作为 LEADER 的项目（create_by/成员LEADER） */
    @GetMapping("/leader/list")
    @ApiOperation("负责人项目列表")
    public CommonResult<?> leaderList(ProjectQuery query) {
        if (!"LEADER".equalsIgnoreCase(SecurityUtils.getRoleCode())) {
            return CommonResult.fail(403, "无权限");
        }
        if (query == null) query = new ProjectQuery();
        query.setMemberId(SecurityUtils.getUserId());
        return projectService.getProjectPage(query);
    }

    /** 负责人创建项目：leaderId 自动取当前登录用户，无需手动输入 */
    @PostMapping("/leader/create")
    @ApiOperation("负责人创建项目")
    public CommonResult<?> leaderCreate(@RequestBody Project project) {
        if (!"LEADER".equalsIgnoreCase(SecurityUtils.getRoleCode())) {
            return CommonResult.fail(403, "无权限");
        }
        project.setLeaderId(null); // 强制由服务端自动绑定当前用户
        return projectService.createProject(project);
    }

    /** 科研人员项目列表：role=MEMBER 的参与项目 */
    @GetMapping("/member/list")
    @ApiOperation("科研人员项目列表")
    public CommonResult<?> memberList(ProjectQuery query) {
        if (!"MEMBER".equalsIgnoreCase(SecurityUtils.getRoleCode())) {
            return CommonResult.fail(403, "无权限");
        }
        if (query == null) query = new ProjectQuery();
        query.setMemberId(SecurityUtils.getUserId());
        return projectService.getProjectPage(query);
    }

    /** 科研人员申请加入：输入 5 位项目编号 */
    @PostMapping("/member/apply")
    @ApiOperation("科研人员申请加入项目")
    public CommonResult<?> memberApply(@RequestParam Integer projectNo,
                                      @RequestParam(required = false) String applyReason) {
        if (!"MEMBER".equalsIgnoreCase(SecurityUtils.getRoleCode())) {
            return CommonResult.fail(403, "无权限");
        }
        return projectService.apply(null, projectNo, applyReason);
    }

    @Log(title = "更新项目", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    @ApiOperation("更新项目")
    public CommonResult<?> update(@RequestBody Project project) {
        return projectService.updateProject(project);
    }

    @Log(title = "删除项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除项目")
    public CommonResult<?> delete(@PathVariable Long id) {
        return projectService.deleteProject(id);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("项目详情")
    public CommonResult<?> detail(@PathVariable Long id) {
        return projectService.getProjectDetail(id);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public CommonResult<?> page(ProjectQuery query) {
        return projectService.getProjectPage(query);
    }

    /** 我参与的项目（身份绑定-项目关联，新用户注册后通过申请/邀请/创建获得） */
    @GetMapping("/my")
    @ApiOperation("我参与的项目")
    public CommonResult<?> my(ProjectQuery query) {
        Long userId = SecurityUtils.getUserId();
        if (query == null) query = new ProjectQuery();
        query.setMemberId(userId);
        return projectService.getProjectPage(query);
    }

    @GetMapping("/statistics")
    @ApiOperation("项目统计")
    public CommonResult<?> statistics(@RequestParam(required = false) Long leaderId) {
        return projectService.getProjectStatistics(leaderId);
    }

    @PostMapping("/apply")
    @ApiOperation("申请加入项目（传 projectId 或 projectNo）")
    public CommonResult<?> apply(@RequestParam(required = false) Long projectId,
                                @RequestParam(required = false) Integer projectNo,
                                @RequestParam(required = false) String applyReason) {
        return projectService.apply(projectId, projectNo, applyReason);
    }

    @PostMapping("/approve")
    @ApiOperation("负责人审批申请")
    public CommonResult<?> approve(@RequestParam Long applyId,
                                  @RequestParam Boolean approved,
                                  @RequestParam(required = false) String replyRemark) {
        return projectService.approve(applyId, approved, replyRemark != null ? replyRemark : "");
    }

    @GetMapping("/{projectId}/members")
    @ApiOperation("项目成员列表")
    public CommonResult<?> members(@PathVariable Long projectId) {
        return projectService.getProjectMembers(projectId);
    }

    @PutMapping("/member/updateRole")
    @ApiOperation("修改成员角色（仅负责人）")
    public CommonResult<?> updateMemberRole(@RequestParam Long projectId,
                                           @RequestParam Long userId,
                                           @RequestParam String role) {
        return projectService.updateMemberRole(projectId, userId, role);
    }

    @DeleteMapping("/member/remove")
    @ApiOperation("移除成员（仅负责人）")
    public CommonResult<?> removeMember(@RequestParam Long projectId, @RequestParam Long userId) {
        return projectService.removeMember(projectId, userId);
    }

    @GetMapping("/{projectId}/pending-applies")
    @ApiOperation("待审批申请列表（负责人）")
    public CommonResult<?> pendingApplies(@PathVariable Long projectId) {
        return projectService.getPendingApplies(projectId);
    }

    @GetMapping("/visitor/detail")
    @ApiOperation("访客查看所有公开项目的文档和成果")
    public CommonResult<?> visitorDetail(@RequestParam(required = false) Integer projectNo) {
        return projectService.getVisitorProjectDetail(projectNo);
    }

    /** 负责人立项审核列表：auditStatus=PENDING|PASSED|REJECTED|ALL */
    @GetMapping("/audit/list")
    @ApiOperation("立项审核列表")
    public CommonResult<?> auditList(@RequestParam(required = false, defaultValue = "ALL") String auditStatus,
                                    @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                    @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return projectService.getAuditList(auditStatus, pageNum, pageSize);
    }

    /** 负责人立项审核：通过/驳回 */
    @PostMapping("/audit")
    @ApiOperation("立项审核提交")
    public CommonResult<?> audit(@RequestParam Long projectId,
                                 @RequestParam String status,
                                 @RequestParam String opinion) {
        return projectService.auditProject(projectId, status, opinion);
    }
}
