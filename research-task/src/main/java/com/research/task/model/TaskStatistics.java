package com.research.task.model;

import lombok.Data;

/**
 * 任务统计
 */
@Data
public class TaskStatistics {
    private Integer totalTasks;
    private Integer todoCount;
    private Integer processingCount;
    private Integer reviewCount;
    private Integer doneCount;
    private Integer avgProgress;
}
