package com.research.project.query;

import com.research.common.core.domain.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResultQuery extends PageParam {
    private Long projectId;
    private String type;
    private String status;
    private String keyword;
    private String submitTimeStart;
    private String submitTimeEnd;
}
