package com.research.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.exception.BusinessException;
import com.research.common.core.util.SecurityUtils;
import com.research.project.entity.Project;
import com.research.project.entity.ProjectApply;
import com.research.project.entity.ProjectMember;
import com.research.project.entity.ProjectResult;
import com.research.project.mapper.ProjectApplyMapper;
import com.research.project.mapper.ProjectMapper;
import com.research.project.mapper.ProjectMemberMapper;
import com.research.project.mapper.ProjectResultMapper;
import com.research.project.model.ProjectMemberVO;
import com.research.project.model.ProjectQuery;
import com.research.project.model.ProjectVO;
import com.research.project.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 项目服务实现
 */
@Slf4j
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private static final int PROJECT_NO_MIN = 10000;
    private static final int PROJECT_NO_MAX = 99999;
    private static final int PROJECT_NO_RETRY = 50;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    @Autowired
    private ProjectApplyMapper projectApplyMapper;
    @Autowired
    private ProjectResultMapper projectResultMapper;

    @Autowired
    private RestTemplate restTemplate;

    private static final String NOTIFICATION_SERVICE_BASE = "http://research-notification";

    @Override
    @Transactional
    public CommonResult<?> createProject(Project project) {
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new BusinessException("项目名称不能为空");
        }
        Long currentUserId = SecurityUtils.getUserId();
        if (project.getLeaderId() == null) project.setLeaderId(currentUserId);
        project.setProgress(0);
        project.setStatus("INIT");
        if (project.getIsPublic() == null) project.setIsPublic(false);
        project.setCreateBy(currentUserId);
        project.setUpdateBy(currentUserId);
        project.setProjectNo(generateUniqueProjectNo());
        save(project);
        // 开题报告：创建后负责人自动绑定为项目成员（LEADER）
        ProjectMember pm = new ProjectMember();
        pm.setProjectId(project.getId());
        pm.setUserId(project.getLeaderId());
        pm.setRole("LEADER");
        pm.setStatus(1);
        projectMemberMapper.insert(pm);
        log.info("创建项目成功: id={}, name={}", project.getId(), project.getName());
        return CommonResult.success(project);
    }

    @Override
    @Transactional
    public CommonResult<?> updateProject(Project project) {
        Project exist = getById(project.getId());
        if (exist == null) {
            throw new BusinessException("项目不存在或已被删除");
        }
        project.setUpdateBy(SecurityUtils.getUserId());
        updateById(project);
        return CommonResult.success("更新成功");
    }

    @Override
    @Transactional
    public CommonResult<?> deleteProject(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException("项目不存在或已被删除");
        }
        
        Long currentUserId = SecurityUtils.getUserId();
        
        // 1. 逻辑删除项目
        project.setDelFlag(1);
        project.setUpdateBy(currentUserId);
        updateById(project);
        
        // 2. 逻辑删除项目相关的成果（使用 @TableLogic，通过 removeById 或 update 批量删除）
        List<ProjectResult> results = projectResultMapper.selectList(
                new LambdaQueryWrapper<ProjectResult>()
                        .eq(ProjectResult::getProjectId, id)
                        .eq(ProjectResult::getDelFlag, 0)); // 只查询未删除的成果
        if (results != null && !results.isEmpty()) {
            // 批量逻辑删除：使用 deleteById 触发 @TableLogic
            for (ProjectResult result : results) {
                projectResultMapper.deleteById(result.getId());
            }
            log.info("项目解散：已删除 {} 个成果", results.size());
        }
        
        // 3. 通过 RestTemplate 调用任务服务，删除项目相关的任务
        try {
            if (restTemplate != null) {
                String taskServiceUrl = "http://research-task/task/delete/byProjectId";
                java.util.Map<String, Object> body = new java.util.HashMap<>();
                body.put("projectId", id);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<java.util.Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                restTemplate.postForObject(taskServiceUrl, requestEntity, CommonResult.class);
                log.info("项目解散：已调用任务服务删除项目相关任务");
            }
        } catch (Exception e) {
            log.warn("项目解散：调用任务服务删除任务失败: projectId={}, error={}", id, e.getMessage());
        }
        
        // 4. 通过 RestTemplate 调用文档服务，删除项目相关的文档
        try {
            if (restTemplate != null) {
                String docServiceUrl = "http://research-document/document/delete/byProjectId";
                java.util.Map<String, Object> body = new java.util.HashMap<>();
                body.put("projectId", id);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<java.util.Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                restTemplate.postForObject(docServiceUrl, requestEntity, CommonResult.class);
                log.info("项目解散：已调用文档服务删除项目相关文档");
            }
        } catch (Exception e) {
            log.warn("项目解散：调用文档服务删除文档失败: projectId={}, error={}", id, e.getMessage());
        }
        
        log.info("项目解散成功: projectId={}, name={}", id, project.getName());
        return CommonResult.success("删除成功");
    }

    @Override
    public CommonResult<ProjectVO> getProjectDetail(Long id) {
        Project project = baseMapper.selectProjectDetail(id);
        if (project == null) {
            throw new BusinessException("项目不存在或已被删除");
        }
        // 不公开项目仅成员和负责人可查看
        if (!Boolean.TRUE.equals(project.getIsPublic())) {
            Long currentUserId = SecurityUtils.getUserId();
            if (currentUserId == null || currentUserId <= 0) {
                throw new BusinessException(403, "无权限查看该项目");
            }
            long count = projectMemberMapper.selectCount(
                    new LambdaQueryWrapper<ProjectMember>()
                            .eq(ProjectMember::getProjectId, id)
                            .eq(ProjectMember::getUserId, currentUserId)
                            .in(ProjectMember::getStatus, 0, 1));
            if (count == 0) {
                throw new BusinessException(403, "无权限查看该项目");
            }
        }
        ProjectVO vo = BeanUtil.copyProperties(project, ProjectVO.class);
        int taskCount = baseMapper.countTaskByProjectId(id);
        int docCount = baseMapper.countDocumentByProjectId(id);
        long achievementCount = projectResultMapper.selectCount(
                new LambdaQueryWrapper<ProjectResult>().eq(ProjectResult::getProjectId, id));
        vo.setTaskCount(taskCount);
        vo.setDocCount(docCount);
        vo.setAchievementCount((int) achievementCount);
        long memberCount = projectMemberMapper.selectCount(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, id).in(ProjectMember::getStatus, 0, 1));
        vo.setMemberCount((int) memberCount);
        return CommonResult.success(vo);
    }

    @Override
    public CommonResult<?> getProjectPage(ProjectQuery query) {
        Page<Project> page = new Page<>(query.getPageNum(), query.getPageSize());
        Long currentUserId = SecurityUtils.getUserId();
        // 非管理员且未显式指定 leaderId/memberId 时，默认仅返回当前用户参与的项目
        if (!SecurityUtils.isAdmin()
                && currentUserId != null && currentUserId > 0
                && query.getLeaderId() == null
                && query.getMemberId() == null) {
            query.setMemberId(currentUserId);
        }
        IPage<Project> result = baseMapper.selectProjectPage(page, query);
        
        // 确保成员数已从 SQL 中获取（如果 SQL 返回的 member_count 为空，则重新统计）
        for (Project project : result.getRecords()) {
            if (project.getMemberCount() == null) {
                long memberCount = projectMemberMapper.selectCount(
                        new LambdaQueryWrapper<ProjectMember>()
                                .eq(ProjectMember::getProjectId, project.getId())
                                .in(ProjectMember::getStatus, 0, 1));
                project.setMemberCount((int) memberCount);
            }
        }
        
        return CommonResult.success(result);
    }

    @Override
    public CommonResult<?> getProjectStatistics(Long leaderId) {
        return CommonResult.success(baseMapper.selectProjectStatistics(leaderId));
    }

    @Override
    @Transactional
    public CommonResult<?> apply(Long projectId, Integer projectNo, String applyReason) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "请先登录");
        }
        Project project = null;
        if (projectNo != null) {
            // 显式限定查询字段，避免 MyBatis-Plus 选择不存在的 code/priority 等旧列
            project = getOne(
                    new LambdaQueryWrapper<Project>()
                            .select(Project::getId, Project::getProjectNo, Project::getName,
                                    Project::getDescription, Project::getLeaderId,
                                    Project::getStatus, Project::getStartTime,
                                    Project::getEndTime, Project::getProgress,
                                    Project::getIsPublic, Project::getCreateBy,
                                    Project::getCreateTime, Project::getUpdateBy,
                                    Project::getUpdateTime, Project::getDelFlag,
                                    Project::getRemark)
                            .eq(Project::getProjectNo, projectNo)
                            .eq(Project::getDelFlag, 0)
            );
        }
        if (project == null && projectId != null) {
            project = getById(projectId);
        }
        if (project == null) {
            throw new BusinessException("项目不存在、已被删除或项目编号错误");
        }
        long exists = projectApplyMapper.selectCount(
                new LambdaQueryWrapper<ProjectApply>()
                        .eq(ProjectApply::getProjectId, project.getId())
                        .eq(ProjectApply::getUserId, userId)
                        .eq(ProjectApply::getStatus, "PENDING"));
        if (exists > 0) {
            throw new BusinessException("您已提交过申请，请等待负责人审批");
        }
        long memberExists = projectMemberMapper.selectCount(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, project.getId())
                        .eq(ProjectMember::getUserId, userId));
        if (memberExists > 0) {
            throw new BusinessException("您已是项目成员");
        }
        ProjectApply apply = new ProjectApply();
        apply.setProjectId(project.getId());
        apply.setUserId(userId);
        apply.setStatus("PENDING");
        apply.setApplyReason(applyReason);
        projectApplyMapper.insert(apply);
        // 通知项目负责人：有新的成员加入申请
        notifyProjectLeaderJoinRequest(project, apply);
        return CommonResult.success("申请已提交，请等待负责人审批");
    }

    @Override
    @Transactional
    public CommonResult<?> approve(Long applyId, boolean approved, String replyRemark) {
        ProjectApply apply = projectApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        if (!"PENDING".equals(apply.getStatus())) {
            throw new BusinessException("该申请已处理");
        }
        Project project = getById(apply.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在或已被删除");
        }
        Long currentUserId = SecurityUtils.getUserId();
        if (!project.getLeaderId().equals(currentUserId)) {
            throw new BusinessException(403, "仅项目负责人可审批");
        }
        apply.setStatus(approved ? "APPROVED" : "REJECTED");
        apply.setReplyRemark(replyRemark);
        apply.setReplyBy(currentUserId);
        apply.setReplyTime(LocalDateTime.now());
        projectApplyMapper.updateById(apply);
        if (approved) {
            ProjectMember pm = new ProjectMember();
            pm.setProjectId(project.getId());
            pm.setUserId(apply.getUserId());
            pm.setRole("MEMBER");
            pm.setStatus(1);
            projectMemberMapper.insert(pm);
        }
        // 通知申请人：审批结果
        notifyApplicantJoinResult(project, apply);
        return CommonResult.success(approved ? "已同意加入" : "已拒绝");
    }

    @Override
    public CommonResult<?> getProjectMembers(Long projectId) {
        Project project = getById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在或已被删除");
        }
        Long currentUserId = SecurityUtils.getUserId();
        boolean isLeader = project.getLeaderId().equals(currentUserId);
        long asMember = projectMemberMapper.selectCount(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getUserId, currentUserId));
        if (!isLeader && asMember == 0) {
            throw new BusinessException(403, "无权限查看成员列表");
        }
        List<ProjectMemberVO> list = projectMemberMapper.selectMembersByProjectId(projectId);
        return CommonResult.success(list);
    }

    @Override
    @Transactional
    public CommonResult<?> updateMemberRole(Long projectId, Long userId, String role) {
        ensureProjectLeader(projectId);
        if (!"LEADER".equals(role) && !"MEMBER".equals(role) && !"VISITOR".equals(role)) {
            throw new BusinessException("角色只能是 LEADER/MEMBER/VISITOR");
        }
        ProjectMember pm = projectMemberMapper.selectOne(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getUserId, userId));
        if (pm == null) {
            throw new BusinessException("该用户不是项目成员");
        }
        pm.setRole(role);
        projectMemberMapper.updateById(pm);
        return CommonResult.success("角色已更新");
    }

    @Override
    @Transactional
    public CommonResult<?> removeMember(Long projectId, Long userId) {
        Project project = getById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在或已被删除");
        }
        Long currentUserId = SecurityUtils.getUserId();
        if (!project.getLeaderId().equals(currentUserId)) {
            throw new BusinessException(403, "仅项目负责人可移除成员");
        }
        if (project.getLeaderId().equals(userId)) {
            throw new BusinessException("不能移除项目负责人");
        }
        projectMemberMapper.delete(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getUserId, userId));
        return CommonResult.success("已移除");
    }

    @Override
    public CommonResult<?> getPendingApplies(Long projectId) {
        ensureProjectLeader(projectId);
        List<ProjectApply> list = projectApplyMapper.selectList(
                new LambdaQueryWrapper<ProjectApply>()
                        .eq(ProjectApply::getProjectId, projectId)
                        .eq(ProjectApply::getStatus, "PENDING")
                        .orderByAsc(ProjectApply::getCreateTime));
        return CommonResult.success(list);
    }

    @Override
    public CommonResult<?> getAuditList(String auditStatus, Integer pageNum, Integer pageSize) {
        Long leaderId = SecurityUtils.getUserId();
        if (leaderId == null || leaderId <= 0) {
            throw new BusinessException(403, "请先登录");
        }
        java.util.List<String> statuses = new java.util.ArrayList<>();
        if (auditStatus != null && !auditStatus.isEmpty()) {
            switch (auditStatus.toUpperCase()) {
                case "PENDING":
                    statuses.add("INIT");
                    statuses.add("PENDING_AUDIT");
                    break;
                case "PASSED":
                    statuses.add("RUNNING");
                    break;
                case "REJECTED":
                    statuses.add("REJECTED");
                    break;
                case "ALL":
                default:
                    break;
            }
        }
        if (pageNum == null) pageNum = 1;
        if (pageSize == null) pageSize = 10;
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Project> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        IPage<Project> result = baseMapper.selectAuditPage(page, leaderId, statuses.isEmpty() ? null : statuses);
        return CommonResult.success(result);
    }

    @Override
    @Transactional
    public CommonResult<?> auditProject(Long projectId, String status, String opinion) {
        Project project = getById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在或已被删除");
        }
        if (!project.getLeaderId().equals(SecurityUtils.getUserId())) {
            throw new BusinessException(403, "仅项目负责人可审核");
        }
        if (!"INIT".equals(project.getStatus()) && !"PENDING_AUDIT".equals(project.getStatus())) {
            throw new BusinessException("该项目已审核，无法重复操作");
        }
        if (opinion == null || opinion.trim().isEmpty()) {
            throw new BusinessException("审核意见不能为空");
        }
        String newStatus = "PASS".equalsIgnoreCase(status) ? "RUNNING" : "REJECTED";
        project.setStatus(newStatus);
        project.setAuditOpinion(opinion.trim());
        project.setAuditTime(LocalDateTime.now());
        project.setAuditById(SecurityUtils.getUserId());
        updateById(project);
        // TODO: 发送系统通知给申请人 project.getCreateBy()
        log.info("立项审核: projectId={}, status={}", projectId, newStatus);
        return CommonResult.success("PASS".equalsIgnoreCase(status) ? "审核通过" : "已驳回");
    }

    @Override
    public CommonResult<?> getVisitorProjectDetail(Integer projectNo) {
        // 访客端查看：返回所有公开项目的文档和成果统计
        // 如果指定了 projectNo，只查询该公开项目；否则查询所有公开项目
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getIsPublic, true)
                .eq(Project::getDelFlag, 0);
        
        if (projectNo != null) {
            queryWrapper.eq(Project::getProjectNo, projectNo);
        }
        
        // 1. 查询公开项目
        List<Project> publicProjects = list(queryWrapper);
        
        if (publicProjects == null || publicProjects.isEmpty()) {
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("projects", new java.util.ArrayList<>());
            result.put("authDocCount", 0);
            result.put("authAchievementCount", 0);
            return CommonResult.success(result);
        }
        
        // 2. 统计所有公开项目的公开文档数量
        List<Long> publicProjectIds = publicProjects.stream()
                .map(Project::getId)
                .collect(java.util.stream.Collectors.toList());
        
        int totalDocCount = 0;
        int totalAchievementCount = 0;
        
        if (!publicProjectIds.isEmpty()) {
            // 统计公开文档：属于公开项目且 is_public=1 的文档
            totalDocCount = baseMapper.countPublicDocumentsByProjectIds(publicProjectIds);
            
            // 统计成果：属于公开项目且审核通过的成果（ARCHIVED 或 PASSED）
            Long achievementCountLong = projectResultMapper.selectCount(
                    new LambdaQueryWrapper<ProjectResult>()
                            .in(ProjectResult::getProjectId, publicProjectIds)
                            .in(ProjectResult::getStatus, Arrays.asList("ARCHIVED", "PASSED")));
            totalAchievementCount = achievementCountLong != null ? achievementCountLong.intValue() : 0;
        }
        
        // 3. 组装返回结果
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        // 返回公开项目列表（简化信息）
        List<java.util.Map<String, Object>> projectList = publicProjects.stream()
                .map(p -> {
                    java.util.Map<String, Object> proj = new java.util.HashMap<>();
                    proj.put("id", p.getId());
                    proj.put("projectNo", p.getProjectNo());
                    proj.put("name", p.getName());
                    proj.put("description", p.getDescription());
                    proj.put("leaderId", p.getLeaderId());
                    proj.put("status", p.getStatus());
                    proj.put("progress", p.getProgress());
                    proj.put("startTime", p.getStartTime());
                    proj.put("endTime", p.getEndTime());
                    proj.put("createTime", p.getCreateTime());
                    return proj;
                })
                .collect(java.util.stream.Collectors.toList());
        
        result.put("projects", projectList);
        result.put("authDocCount", totalDocCount);
        result.put("authAchievementCount", totalAchievementCount);
        
        return CommonResult.success(result);
    }

    @Override
    public CommonResult<?> getVisitorPublicProjects(ProjectQuery query) {
        // 访客端：返回所有公开项目，不按成员关系过滤
        Page<Project> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        // 构建查询条件：只查询公开项目
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getIsPublic, true)
                .eq(Project::getDelFlag, 0);
        
        if (query.getName() != null && !query.getName().trim().isEmpty()) {
            queryWrapper.like(Project::getName, query.getName());
        }
        if (query.getStatus() != null && !query.getStatus().trim().isEmpty()) {
            queryWrapper.eq(Project::getStatus, query.getStatus());
        }
        
        queryWrapper.orderByDesc(Project::getCreateTime);
        
        IPage<Project> result = page(page, queryWrapper);
        
        // 确保成员数已统计
        for (Project project : result.getRecords()) {
            if (project.getMemberCount() == null) {
                long memberCount = projectMemberMapper.selectCount(
                        new LambdaQueryWrapper<ProjectMember>()
                                .eq(ProjectMember::getProjectId, project.getId())
                                .in(ProjectMember::getStatus, 0, 1));
                project.setMemberCount((int) memberCount);
            }
        }
        
        return CommonResult.success(result);
    }

    private void ensureProjectLeader(Long projectId) {
        Project project = getById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在或已被删除");
        }
        if (!project.getLeaderId().equals(SecurityUtils.getUserId())) {
            throw new BusinessException(403, "仅项目负责人可操作");
        }
    }

    /**
     * 通知项目负责人：新的成员加入申请（负责人角色）
     */
    private void notifyProjectLeaderJoinRequest(Project project, ProjectApply apply) {
        if (project == null || apply == null) {
            log.warn("项目加入申请通知跳过：project或apply为空");
            return;
        }
        if (restTemplate == null) {
            log.error("项目加入申请通知失败：RestTemplate未注入, projectId={}, applyId={}", 
                    project.getId(), apply.getId());
            return;
        }
        if (project.getLeaderId() == null) {
            log.warn("项目加入申请通知跳过：项目负责人ID为空, projectId={}", project.getId());
            return;
        }
        
        try {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("targetUserIds", java.util.Collections.singletonList(project.getLeaderId()));
            body.put("title", "项目加入申请");
            // 通知内容：包含项目名称和申请说明（如果有）
            String content = "有成员申请加入项目【" + project.getName() + "】";
            if (apply.getApplyReason() != null && !apply.getApplyReason().trim().isEmpty()) {
                content += "，申请说明：" + apply.getApplyReason();
            }
            body.put("content", content);
            body.put("bizType", "PROJECT");
            body.put("bizId", project.getId());
            body.put("projectId", project.getId());
            body.put("priority", "HIGH");
            body.put("actionType", "APPROVAL");
            body.put("notificationType", "PROJECT_JOIN_REQUEST");
            body.put("relatedId", String.valueOf(apply.getId()));
            body.put("relatedType", "PROJECT_APPLY");
            body.put("extra", "{\"applyId\":" + apply.getId() + ",\"projectId\":" + project.getId() + "}");
            
            String url = NOTIFICATION_SERVICE_BASE + "/internal/notification/send";
            log.info("发送项目加入申请通知: projectId={}, projectName={}, leaderId={}, applyId={}, applicantId={}, url={}", 
                    project.getId(), project.getName(), project.getLeaderId(), apply.getId(), apply.getUserId(), url);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<java.util.Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            CommonResult<?> result = restTemplate.postForObject(url, requestEntity, CommonResult.class);
            
            if (result != null && result.getCode() == 200) {
                log.info("项目加入申请通知发送成功: projectId={}, applyId={}, leaderId={}", 
                        project.getId(), apply.getId(), project.getLeaderId());
            } else {
                log.warn("项目加入申请通知返回异常: projectId={}, applyId={}, result={}", 
                        project.getId(), apply.getId(), result);
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 服务发现失败，尝试直接调用本地端口
            log.warn("服务发现调用失败，尝试直接调用: projectId={}, applyId={}, error={}", 
                    project.getId(), apply.getId(), e.getMessage());
            tryDirectCallForJoinRequest(project, apply);
        } catch (Exception e) {
            log.error("发送项目加入申请通知失败: projectId={}, applyId={}, leaderId={}, error={}", 
                    project.getId(), apply.getId(), project.getLeaderId(), e.getMessage(), e);
        }
    }
    
    /**
     * 直接调用通知服务（备用方案，当服务发现失败时使用）
     */
    private void tryDirectCallForJoinRequest(Project project, ProjectApply apply) {
        try {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("targetUserIds", java.util.Collections.singletonList(project.getLeaderId()));
            body.put("title", "项目加入申请");
            // 通知内容：包含项目名称和申请说明（如果有）
            String content = "有成员申请加入项目【" + project.getName() + "】";
            if (apply.getApplyReason() != null && !apply.getApplyReason().trim().isEmpty()) {
                content += "，申请说明：" + apply.getApplyReason();
            }
            body.put("content", content);
            body.put("bizType", "PROJECT");
            body.put("bizId", project.getId());
            body.put("projectId", project.getId());
            body.put("priority", "HIGH");
            body.put("actionType", "APPROVAL");
            body.put("notificationType", "PROJECT_JOIN_REQUEST");
            body.put("relatedId", String.valueOf(apply.getId()));
            body.put("relatedType", "PROJECT_APPLY");
            body.put("extra", "{\"applyId\":" + apply.getId() + ",\"projectId\":" + project.getId() + "}");
            
            String directUrl = "http://localhost:8089/internal/notification/send";
            log.info("直接调用通知服务: projectId={}, applyId={}, leaderId={}, url={}", 
                    project.getId(), apply.getId(), project.getLeaderId(), directUrl);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<java.util.Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            CommonResult<?> result = restTemplate.postForObject(directUrl, requestEntity, CommonResult.class);
            
            if (result != null && result.getCode() == 200) {
                log.info("直接调用通知服务成功: projectId={}, applyId={}", project.getId(), apply.getId());
            } else {
                log.error("直接调用通知服务返回异常: projectId={}, applyId={}, result={}", 
                        project.getId(), apply.getId(), result);
            }
        } catch (Exception e) {
            log.error("直接调用通知服务失败: projectId={}, applyId={}, error={}", 
                    project.getId(), apply.getId(), e.getMessage(), e);
        }
    }

    /**
     * 通知申请人：加入项目审批结果（成员角色）
     */
    private void notifyApplicantJoinResult(Project project, ProjectApply apply) {
        if (restTemplate == null || project == null || apply == null) {
            return;
        }
        try {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("targetUserIds", java.util.Collections.singletonList(apply.getUserId()));
            body.put("title", "项目加入审批结果");
            String statusText = "APPROVED".equals(apply.getStatus()) ? "已通过" : "已拒绝";
            body.put("content", "您加入项目【" + project.getName() + "】的申请" + statusText +
                    (apply.getReplyRemark() != null ? "，备注：" + apply.getReplyRemark() : ""));
            body.put("bizType", "PROJECT");
            body.put("bizId", project.getId());
            body.put("projectId", project.getId());
            body.put("priority", "NORMAL");
            body.put("actionType", "VIEW_DETAIL");
            body.put("notificationType", "PROJECT_JOIN_REQUEST");
            body.put("relatedId", String.valueOf(project.getId()));
            body.put("relatedType", "PROJECT");
            body.put("extra", "{\"applyId\":" + apply.getId() + ",\"result\":\"" + apply.getStatus() + "\"}");
            restTemplate.postForObject(
                    NOTIFICATION_SERVICE_BASE + "/internal/notification/send",
                    body,
                    CommonResult.class
            );
        } catch (Exception e) {
            log.warn("发送项目加入审批结果通知失败: projectId={}, applyId={}, error={}",
                    project.getId(), apply.getId(), e.getMessage());
        }
    }

    private Integer generateUniqueProjectNo() {
        Random r = new Random();
        for (int i = 0; i < PROJECT_NO_RETRY; i++) {
            int no = PROJECT_NO_MIN + r.nextInt(PROJECT_NO_MAX - PROJECT_NO_MIN + 1);
            long c = count(new LambdaQueryWrapper<Project>().eq(Project::getProjectNo, no));
            if (c == 0) return no;
        }
        throw new BusinessException("生成项目编号失败，请重试");
    }
}
