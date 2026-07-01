package com.ojbe.model.vo;

import com.ojbe.model.entity.QuestionSolution;
import com.ojbe.utils.StaticJsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题解视图
 */
@Data
public class QuestionSolutionVO implements Serializable {

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
     * 题解内容
     */
    private String content;

    /**
     * 题解标签
     */
    private List<String> tags;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 浏览量
     */
    private Integer viewNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人信息
     */
    private UserSimpleVO userVO;

    /**
     * 当前用户是否已点赞
     */
    private Boolean hasLiked;

    /**
     * 关联题目标题
     */
    private String questionTitle;

    /**
     * 关联题目难度
     */
    private String difficulty;

    private static final long serialVersionUID = 1L;

    /**
     * 包装类转对象
     *
     * @param questionSolutionVO
     * @return
     */
    public static QuestionSolution voToObj(QuestionSolutionVO questionSolutionVO) {
        if (questionSolutionVO == null) {
            return null;
        }
        QuestionSolution questionSolution = new QuestionSolution();
        BeanUtils.copyProperties(questionSolutionVO, questionSolution);
        List<String> tagList = questionSolutionVO.getTags();
        if (tagList != null) {
            questionSolution.setTags(StaticJsonUtil.toJsonStr(tagList));
        }
        return questionSolution;
    }

    /**
     * 对象转包装类
     *
     * @param questionSolution
     * @return
     */
    public static QuestionSolutionVO objToVo(QuestionSolution questionSolution) {
        if (questionSolution == null) {
            return null;
        }
        QuestionSolutionVO questionSolutionVO = new QuestionSolutionVO();
        BeanUtils.copyProperties(questionSolution, questionSolutionVO);
        String tagsStr = questionSolution.getTags();
        if (tagsStr != null && !tagsStr.isEmpty()) {
            questionSolutionVO.setTags(StaticJsonUtil.toObj(tagsStr, new TypeReference<List<String>>() {
            }));
        }
        return questionSolutionVO;
    }
}
