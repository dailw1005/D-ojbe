package com.ojbe.service.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ojbe.model.dto.tag.TagAddRequest;
import com.ojbe.model.dto.tag.TagQueryRequest;
import com.ojbe.model.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author trave
* @description 针对表【tag(标签)】的数据库操作Service
* @createDate 2026-01-23 15:02:51
*/
public interface TagService extends IService<Tag> {

    /**
     * 创建标签
     */
    Long addTag(TagAddRequest tagAddRequest, Long userId);

    /**
     * 删除标签
     */
    Boolean deleteTag(Long id);

    /**
     * 分页查询标签
     */
    Page<Tag> listTagByPage(TagQueryRequest tagQueryRequest);

    /**
     * 获取所有标签
     */
    List<Tag> listAllTags();
}
