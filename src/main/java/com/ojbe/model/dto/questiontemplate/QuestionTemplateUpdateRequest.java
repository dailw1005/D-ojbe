package com.ojbe.model.dto.questiontemplate;

import lombok.Data;
import java.io.Serializable;

/**
 * 更新代码模板请求
 */
@Data
public class QuestionTemplateUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 模板代码
     */
    private String code;

    private static final long serialVersionUID = 1L;
}
