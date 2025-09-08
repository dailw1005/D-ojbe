package com.dailw.model.redis;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户缓存信息模型
 * 用于Redis缓存的用户数据结构
 * 
 * @author dailw
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCacheInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 用户状态（0-禁用，1-正常）
     */
    private Integer status;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 用户积分
     */
    private Integer score;

    /**
     * 用户等级
     */
    private Integer level;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 缓存时间
     */
    private LocalDateTime cacheTime;

    /**
     * 检查用户是否为活跃状态
     * 
     * @return 是否活跃
     */
    public boolean isActive() {
        return status != null && status == 1;
    }

    /**
     * 检查用户是否为管理员
     * 
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    /**
     * 获取用户显示名称（优先显示昵称，其次用户名）
     * 
     * @return 显示名称
     */
    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        return username;
    }

    /**
     * 更新缓存时间为当前时间
     */
    public void updateCacheTime() {
        this.cacheTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "UserCacheInfo{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", role='" + role + '\'' +
                ", score=" + score +
                ", level=" + level +
                ", lastLoginTime=" + lastLoginTime +
                ", cacheTime=" + cacheTime +
                '}';
    }
}