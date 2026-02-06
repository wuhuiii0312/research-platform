package com.research.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.research.common.core.domain.CommonResult;
import com.research.project.entity.Project;
import com.research.project.model.ProjectQuery;
import com.research.project.model.ProjectVO;

/**
 * 项目服务接口
 */
public interface ProjectService extends IService<Project> {

    CommonResult<?> createProject(Project project);

    CommonResult<?> updateProject(Project project);

    CommonResult<?> deleteProject(Long id);

    CommonResult<ProjectVO> getProjectDetail(Long id);

    CommonResult<?> getProjectPage(ProjectQuery query);

    CommonResult<?> getProjectStatistics(Long leaderId);

    /** 申请加入项目（传 projectId 或 projectNo，二选一） */
    CommonResult<?> apply(Long projectId, Integer projectNo, String applyReason);

    /** 负责人审批申请：同意/拒绝 */
    CommonResult<?> approve(Long applyId, boolean approved, String replyRemark);

    /** 项目成员列表（负责人可访问） */
    CommonResult<?> getProjectMembers(Long projectId);

    /** 修改成员角色（仅负责人） */
    CommonResult<?> updateMemberRole(Long projectId, Long userId, String role);

    /** 移除成员（仅负责人） */
    CommonResult<?> removeMember(Long projectId, Long userId);

    /** 待审批申请列表（负责人） */
    CommonResult<?> getPendingApplies(Long projectId);

    /** 访客查看所有公开项目的文档和成果（projectNo 可选，不传则返回所有公开项目） */
    CommonResult<?> getVisitorProjectDetail(Integer projectNo);

    /** 访客查询所有公开项目（支持名称、状态筛选） */
    CommonResult<?> getVisitorPublicProjects(ProjectQuery query);

    /** 负责人立项审核列表：待审核/已审核/全部 */
    CommonResult<?> getAuditList(String auditStatus, Integer pageNum, Integer pageSize);

    /** 负责人立项审核：通过/驳回，填写意见并通知申请人 */
    CommonResult<?> auditProject(Long projectId, String status, String opinion);
}
