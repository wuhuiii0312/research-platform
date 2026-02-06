package com.research.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档元数据实体（MySQL document_meta 表，与 GridFS/Mongo 内容关联）
 * 归属标识：project_id、creator_id、permission_type，用于识别项目文档
 */
@Data
@TableName("document_meta")
public class Document {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属项目（与 project_member.project_id 一致） */
    private Long projectId;
    /** 创建者 user_id */
    private Long creatorId;
    /** 访问权限：LEADER/MEMBER/VISITOR 可见 */
    private String permissionType;

    /** 文档名称 */
    private String name;
    /** 备用文件URL（可选） */
    private String fileUrl;
    /** 文件在本地或OSS中的路径 */
    private String filePath;
    /** 文件大小 */
    private Long fileSize;
    /** 文件类型 */
    private String fileType;
    /** 版本号 */
    private Integer version;
    /** 对应 GridFS/Mongo 中的文件ID */
    private String mongoId;
    /** 逻辑删除标记：0-正常，1-删除 */
    private Integer delFlag;
    /** 是否测试数据（0-否/1-是），查询默认过滤为 0 */
    @TableField("test_data_flag")
    private Integer testDataFlag;
    /** 是否公开（0-私有 1-公开） */
    @TableField("is_public")
    private Integer isPublic;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
