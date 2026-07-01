package com.ojbe.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "competition_question")
@Data
public class CompetitionQuestion implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long competitionId;

    private Long questionId;

    private Integer displayOrder;

    private Integer score;

    private Date createTime;

    private Date updateTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        CompetitionQuestion other = (CompetitionQuestion) that;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CompetitionQuestion{id=" + id + ", competitionId=" + competitionId + ", questionId=" + questionId + "}";
    }
}
