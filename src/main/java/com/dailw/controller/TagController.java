package com.dailw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.annotation.AuthCheck;
import com.dailw.common.BaseResponse;
import com.dailw.common.DeleteRequest;
import com.dailw.common.ErrorCode;
import com.dailw.common.ResultUtils;
import com.dailw.constant.UserConstant;
import com.dailw.exception.BusinessException;
import com.dailw.interceptor.JwtAuthenticationInterceptor;
import com.dailw.model.dto.tag.TagAddRequest;
import com.dailw.model.dto.tag.TagQueryRequest;
import com.dailw.model.entity.Tag;
import com.dailw.service.interfaces.TagService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签接口
 */
@RestController
@RequestMapping("/tag")
@Slf4j
public class TagController {

    @Resource
    private TagService tagService;

    /**
     * 创建标签
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addTag(@RequestBody TagAddRequest tagAddRequest, HttpServletRequest request) {
        if (tagAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long result = tagService.addTag(tagAddRequest, currentUserId);
        return ResultUtils.success(result);
    }

    /**
     * 删除标签
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteTag(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = tagService.deleteTag(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 分页获取标签列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Tag>> listTagByPage(@RequestBody TagQueryRequest tagQueryRequest) {
        if (tagQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Tag> result = tagService.listTagByPage(tagQueryRequest);
        return ResultUtils.success(result);
    }

    /**
     * 获取所有标签列表
     */
    @GetMapping("/list/all")
    public BaseResponse<List<Tag>> listAllTags() {
        List<Tag> result = tagService.listAllTags();
        return ResultUtils.success(result);
    }
}
