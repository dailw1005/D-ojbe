package com.ojbe.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目提交状态枚举
 */
public enum QuestionSubmitStatusEnum {

    /**
     * 等待中，表示代码刚提交，尚未开始评测。
     */
    PENDING("等待中", 0),

    /**
     * 评测中，表示系统正在对代码进行编译或运行测试。
     */
    JUDGING("评测中", 1),

    /**
     * 通过，表示代码逻辑正确且在限制的时间、内存范围内完成了所有测试用例。
     */
    ACCEPTED("通过", 2),

    /**
     * 答案错误，表示代码运行完成，但输出结果与预期不符。
     */
    WRONG_ANSWER("答案错误", 3),
    
    /**
     * 时间超限，表示代码运行时间超过了题目设定的最大时间限制。
     */
    TIME_LIMIT_EXCEEDED("时间超限", 4),
    
    /**
     * 内存超限，表示代码运行过程中消耗的内存超过了题目设定的最大内存限制。
     */
    MEMORY_LIMIT_EXCEEDED("内存超限", 5),
    
    /**
     * 运行错误，表示代码在运行过程中发生了异常（如数组越界、除以零等）。
     */
    RUNTIME_ERROR("运行错误", 6),
    
    /**
     * 编译错误，表示提交的代码无法通过编译器的编译。
     */
    COMPILATION_ERROR("编译错误", 7),
    
    /**
     * 系统错误，表示判题系统内部出现异常（如沙箱执行异常等），并非用户代码导致的问题。
     */
    SYSTEM_ERROR("系统错误", 8);

    private final String text;

    private final Integer value;

    QuestionSubmitStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     */
    public static QuestionSubmitStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (QuestionSubmitStatusEnum anEnum : QuestionSubmitStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
