package com.ojbe.model.dto.coderun;

import lombok.Data;

import java.io.Serializable;

@Data
public class CodeRunRequest implements Serializable {

    private Long questionId;

    private String language;

    private String sourceCode;

    private String inputCase;

    private static final long serialVersionUID = 1L;
}
