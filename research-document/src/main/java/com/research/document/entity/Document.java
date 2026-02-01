package com.research.document.entity;

import lombok.Data;

/**
 * 文档元数据实体（MySQL 存储，与 GridFS/Mongo 内容关联）
 */
@Data
public class Document {

    private Long id;

    private String fileUrl;

    private Long fileSize;

    private String mongoId;
}
