package com.dailw.model.dto.tag;

import lombok.Data;
import java.io.Serializable;

/**
 * 创建标签请求
 */
@Data
public class TagAddRequest implements Serializable {

    /**
     * 标签名称
     */
    private String name;

    private static final long serialVersionUID = 1L;
}
