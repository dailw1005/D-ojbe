package com.dailw.model.dto.questiontemplate;

import lombok.Data;
import java.io.Serializable;

/**
 * 创建代码模板请求
 */
@Data
public class QuestionTemplateAddRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 模板代码
     */
    private String code;

    private static final long serialVersionUID = 1L;
}
