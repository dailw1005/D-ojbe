package com.dailw.service.interfaces;

import com.dailw.model.dto.coderun.CodeRunRequest;
import com.dailw.model.vo.CodeRunVO;

public interface CodeRunService {

    CodeRunVO doRun(CodeRunRequest request);
}
