package com.dailw.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图对象
 * 用于返回用户信息，排除敏感字段
 */
@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID，主键
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 账户状态：0-禁用，1-正常，2-锁定
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 登录次数
     */
    private Integer loginCount;

    /**
     * 用户角色：admin-管理员，user-普通用户，guest-访客
     */
    private String role;

    /**
     * 声望值
     */
    private Integer reputation;

    /**
     * 用户等级：0-9
     */
    private Integer level;

    /**
     * 创建时间
     */
    private Date createTime;
}