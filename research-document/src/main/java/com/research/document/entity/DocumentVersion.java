package com.research.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档版本表（MySQL 存版本元数据，文件内容在 MongoDB GridFS）
 */
@Data
@TableName("document_version")
public class DocumentVersion {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long documentId;
    private String versionNo;
    private String mongoFileId;
    private LocalDateTime uploadTime;
    private String changeDesc;
    private Long uploaderId;
    /** 是否测试数据（0-否/1-是） */
    private Integer testDataFlag;
}
