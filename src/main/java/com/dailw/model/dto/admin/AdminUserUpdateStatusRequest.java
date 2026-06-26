package com.dailw.model.dto.admin;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AdminUserUpdateStatusRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Integer status;
}
