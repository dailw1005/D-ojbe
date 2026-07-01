package com.ojbe.judge.sandbox;

import com.ojbe.judge.sandbox.model.ExecuteCodeRequest;
import com.ojbe.judge.sandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest 执行请求
     * @return 执行结果
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
