package com.dailw.model.dto.questionsolution;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 创建题解请求
 */
@Data
public class QuestionSolutionAddRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

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
