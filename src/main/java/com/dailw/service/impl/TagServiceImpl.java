package com.dailw.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dailw.model.entity.Tag;
import com.dailw.service.interfaces.TagService;
import com.dailw.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author trave
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2026-01-23 15:02:53
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




