package com.ojbe.service.interfaces;

import com.ojbe.model.dto.coderun.CodeRunRequest;
import com.ojbe.model.vo.CodeRunVO;

public interface CodeRunService {

    CodeRunVO doRun(CodeRunRequest request);
}
