package com.research.task.model;

import com.research.common.core.domain.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskQuery extends PageParam {
    private Long projectId;
    private Long assigneeId;
    private String status;
    private String priority;
    private String keyword;
}
