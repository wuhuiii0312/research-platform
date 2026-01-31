package com.research.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.research.task.entity.TaskComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskCommentMapper extends BaseMapper<TaskComment> {

    // 查询任务评论（包括回复）
    List<TaskComment> selectTaskComments(@Param("taskId") Long taskId);

    // 点赞评论
    int likeComment(@Param("commentId") Long commentId);

    // 取消点赞评论
    int cancelLikeComment(@Param("commentId") Long commentId);
}