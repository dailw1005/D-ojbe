package com.ojbe.mapper;

import com.ojbe.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author trave
* @description 针对表【question_submit(题目提交)】的数据库操作Mapper
* @createDate 2026-01-23 15:02:27
* @Entity com.dailw.model.entity.QuestionSubmit
*/
public interface QuestionSubmitMapper extends BaseMapper<QuestionSubmit> {

    List<Map<String, Object>> selectDailySubmitCount(@Param("days") int days);

    List<Map<String, Object>> selectStatusDistribution();
}




