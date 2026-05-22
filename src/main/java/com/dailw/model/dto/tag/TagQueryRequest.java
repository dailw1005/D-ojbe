package com.dailw.model.dto.tag;

import com.dailw.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 标签查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TagQueryRequest extends PageRequest implements Serializable {

    /**
     * 标签名称
     */
    private String name;

    private static final long serialVersionUID = 1L;
}
