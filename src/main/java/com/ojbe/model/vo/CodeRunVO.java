package com.ojbe.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeRunVO {

    private String output;

    private String status;

    private Long timeUsed;

    private Long memoryUsed;

    private String message;
}
