package com.ojbe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ojbe.model.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author trave
* @description 针对表【user(用户信息表)】的数据库操作Mapper
* @createDate 2025-08-11 14:42:47
* @Entity generator.domain.User
*/
public interface UserMapper extends BaseMapper<User> {

    List<Map<String, Object>> selectDailyNewUsers(@Param("days") int days);

    List<Map<String, Object>> selectRoleDistribution();
}




