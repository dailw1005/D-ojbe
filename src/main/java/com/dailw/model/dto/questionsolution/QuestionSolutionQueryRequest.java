package com.dailw.model.dto.questionsolution;

import com.dailw.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 题解查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionSolutionQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 发布用户 id
     */
    private Long userId;

    /**
     * 题解标题
     */
    private String title;

    /**
     * 题解标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
