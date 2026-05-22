package com.dailw.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户简要信息视图（仅包含头像、昵称、性别、生日）
 */
@Data
public class UserSimpleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private Date birthday;
}
