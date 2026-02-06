package com.research.project.request;

import lombok.Data;

@Data
public class AuditRequest {
    private Long id;
    private String status;
    private String remark;
}
