package com.research.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.research.project.entity.ProjectMember;
import com.research.project.model.ProjectMemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {

    /** 用户参与的项目ID列表（含角色） */
    List<Long> selectProjectIdsByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /** 项目成员列表（含用户名、角色、加入时间） */
    List<ProjectMemberVO> selectMembersByProjectId(@Param("projectId") Long projectId);
}
