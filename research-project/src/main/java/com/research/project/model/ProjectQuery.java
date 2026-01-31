package com.research.project.model;

import com.research.common.core.domain.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectQuery extends PageParam {
    private String name;
    private String status;
    private Long leaderId;
    private Long memberId;
    private String startTimeStart;
    private String startTimeEnd;
}