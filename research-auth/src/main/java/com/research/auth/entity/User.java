package com.research.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.research.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 状态（0-禁用，1-正常）
     */
    private Integer status;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 岗位ID
     */
    private Long postId;

    /**
     * 性别（0-女，1-男）
     */
    private Integer gender;

    /**
     * 个人简介
     */
    private String profile;

    /**
     * 用户角色（非数据库字段）
     */
    @TableField(exist = false)
    private List<Role> roles;

    /**
     * 用户权限（非数据库字段）
     */
    @TableField(exist = false)
    private List<String> permissions;
}

/**
 * 角色实体
 */
@Data
@TableName("sys_role")
class Role {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色排序
     */
    private Integer roleSort;

    /**
     * 状态（0-正常，1-停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}

/**
 * 用户登录请求参数
 */
@Data
class LoginRequest {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码UUID
     */
    private String uuid;
}