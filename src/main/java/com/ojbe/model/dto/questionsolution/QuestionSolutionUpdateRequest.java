package com.ojbe.model.dto.questionsolution;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 更新题解请求
 */
@Data
public class QuestionSolutionUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题解标题
     */
    private String title;

    /**
     * 题解内容
     */
    private String content;

    /**
     * 题解标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
