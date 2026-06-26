package com.dailw.model.dto.admin;

import com.dailw.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    private String role;

    private Integer status;

    private Date startTime;

    private Date endTime;
}
