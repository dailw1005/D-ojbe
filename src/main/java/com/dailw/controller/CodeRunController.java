package com.dailw.controller;

import com.dailw.common.BaseResponse;
import com.dailw.common.ErrorCode;
import com.dailw.common.ResultUtils;
import com.dailw.exception.BusinessException;
import com.dailw.interceptor.JwtAuthenticationInterceptor;
import com.dailw.model.dto.coderun.CodeRunRequest;
import com.dailw.model.vo.CodeRunVO;
import com.dailw.service.interfaces.CodeRunService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/code_run")
@Slf4j
public class CodeRunController {

    @Resource
    private CodeRunService codeRunService;

    @PostMapping("/do")
    public BaseResponse<CodeRunVO> doRun(@RequestBody CodeRunRequest codeRunRequest,
                                          HttpServletRequest request) {
        if (codeRunRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (codeRunRequest.getSourceCode() == null || codeRunRequest.getSourceCode().isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码不能为空");
        }
        if (codeRunRequest.getLanguage() == null || codeRunRequest.getLanguage().isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言不能为空");
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        log.info("用户 {} 请求运行代码, language={}", currentUserId, codeRunRequest.getLanguage());
        CodeRunVO result = codeRunService.doRun(codeRunRequest);
        return ResultUtils.success(result);
    }
}
