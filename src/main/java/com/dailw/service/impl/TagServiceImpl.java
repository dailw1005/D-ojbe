package com.dailw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dailw.common.ErrorCode;
import com.dailw.exception.BusinessException;
import com.dailw.model.dto.tag.TagAddRequest;
import com.dailw.model.dto.tag.TagQueryRequest;
import com.dailw.model.entity.Tag;
import com.dailw.service.interfaces.TagService;
import com.dailw.mapper.TagMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author trave
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2026-01-23 15:02:51
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

    @Override
    public Long addTag(TagAddRequest tagAddRequest, Long userId) {
        String name = tagAddRequest.getName();
        if (StringUtils.isBlank(name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名不能为空");
        }

        // 检查标签名是否重复
        LambdaQueryWrapper<Tag> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Tag::getName, name);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "标签已存在");
        }

        Tag tag = new Tag();
        tag.setName(name);
        tag.setUserId(userId);
        boolean result = this.save(tag);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建标签失败");
        }
        return tag.getId();
    }

    @Override
    public Boolean deleteTag(Long id) {
        Tag tag = this.getById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "标签不存在");
        }
        return this.removeById(id);
    }

    @Override
    public Page<Tag> listTagByPage(TagQueryRequest tagQueryRequest) {
        long current = tagQueryRequest.getCurrent();
        long size = tagQueryRequest.getPageSize();
        
        LambdaQueryWrapper<Tag> queryWrapper = Wrappers.lambdaQuery();
        String name = tagQueryRequest.getName();
        queryWrapper.like(StringUtils.isNotBlank(name), Tag::getName, name);
        queryWrapper.orderByDesc(Tag::getCreateTime);
        
        return this.page(new Page<>(current, size), queryWrapper);
    }

    @Override
    public List<Tag> listAllTags() {
        LambdaQueryWrapper<Tag> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.orderByAsc(Tag::getName);
        return this.list(queryWrapper);
    }
}




