package com.research.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档批注表
 */
@Data
@TableName("document_annotation")
public class DocumentAnnotation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long documentId;
    private String versionNo;
    private String content;
    private String positionJson;
    private Long creatorId;
    private LocalDateTime createTime;
    private Integer delFlag;
    /** 是否测试数据（0-否/1-是） */
    private Integer testDataFlag;
}
