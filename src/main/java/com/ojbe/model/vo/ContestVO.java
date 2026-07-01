package com.ojbe.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ContestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String description;

    private String type;

    private String startTime;

    private String endTime;

    private Boolean hasPassword;

    private String status;

    private Integer participantCount;

    private Integer questionCount;

    private Long userId;

    private UserVO creator;

    private String createTime;
}
