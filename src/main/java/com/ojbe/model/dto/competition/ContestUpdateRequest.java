package com.ojbe.model.dto.competition;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class ContestUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String description;

    private String type;

    private Date startTime;

    private Date endTime;

    private String password;
}
