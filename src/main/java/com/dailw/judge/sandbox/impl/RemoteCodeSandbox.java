package com.dailw.judge.sandbox.impl;

import com.dailw.common.ErrorCode;
import com.dailw.exception.BusinessException;
import com.dailw.judge.sandbox.CodeSandbox;
import com.dailw.judge.sandbox.model.ExecuteCodeRequest;
import com.dailw.judge.sandbox.model.ExecuteCodeResponse;
import com.dailw.utils.StaticJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

/**
 * 远程代码沙箱（调用已开发好的远程 API）
 */
@Component
@Slf4j
public class RemoteCodeSandbox implements CodeSandbox {

    @Value("${dailw.codesandbox.url:http://localhost:9119/judge}")
    private String remoteSandboxUrl;

    private final RestTemplate restTemplate;

    public RemoteCodeSandbox() {
        // 这里可以直接使用 new 也可以注入配置了连接池的 RestTemplate
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("准备调用远程代码沙箱，请求参数: {}", executeCodeRequest.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonRequest = StaticJsonUtil.toJsonStr(executeCodeRequest);
        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(remoteSandboxUrl, entity, String.class);
            String responseBody = response.getBody();
            if (StringUtils.isBlank(responseBody)) {
                log.error("远程代码沙箱响应为空");
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "远程代码沙箱响应为空");
            }
            log.info("远程代码沙箱调用成功，返回结果: {}", responseBody);
            return StaticJsonUtil.toObj(responseBody, ExecuteCodeResponse.class);
        } catch (Exception e) {
            log.error("调用远程代码沙箱异常", e);
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "判题服务暂时不可用，请稍后重试");
        }
    }
}
