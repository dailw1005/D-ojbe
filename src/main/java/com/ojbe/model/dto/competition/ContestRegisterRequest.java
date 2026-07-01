package com.ojbe.model.dto.competition;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ContestRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String password;
}
