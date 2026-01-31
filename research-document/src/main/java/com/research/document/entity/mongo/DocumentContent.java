package com.research.document.entity.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "document_contents")
public class DocumentContent {

    @Id
    private String id;

    @Field("doc_id")
    private Long docId;

    private String content;

    @Field("html_content")
    private String htmlContent;

    private Integer version;

    @Field("change_log")
    private String changeLog;

    @Field("created_by")
    private Long createdBy;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    // 文档版本历史
    @Field("version_history")
    private List<DocumentVersion> versionHistory;
}

/**
 * 文档版本信息
 */
@Data
class DocumentVersion {
    private Integer version;
    private String content;
    private String changeLog;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}