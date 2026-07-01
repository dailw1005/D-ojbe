package com.ojbe.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判题信息消息枚举
 */
public enum JudgeInfoMessageEnum {

    ACCEPTED("成功", "ACCEPTED"),
    WRONG_ANSWER("答案错误", "WRONG_ANSWER"),
    COMPILE_ERROR("编译错误", "COMPILATION_ERROR"),
    MEMORY_LIMIT_EXCEEDED("内存溢出", "MEMORY_LIMIT_EXCEEDED"),
    TIME_LIMIT_EXCEEDED("超时", "TIME_LIMIT_EXCEEDED"),
    PRESENTATION_ERROR("展示错误", "PRESENTATION_ERROR"),
    WAITING("等待中", "WAITING"),
    OUTPUT_LIMIT_EXCEEDED("输出溢出", "OUTPUT_LIMIT_EXCEEDED"),
    DANGEROUS_OPERATION("危险操作", "DANGEROUS_OPERATION"),
    RUNTIME_ERROR("运行错误", "RUNTIME_ERROR"),
    SYSTEM_ERROR("系统错误", "SYSTEM_ERROR"),
    PENDING("排队中", "PENDING"),
    JUDGING("判题中", "JUDGING");

    private final String text;

    private final String value;

    JudgeInfoMessageEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     */
    public static JudgeInfoMessageEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (JudgeInfoMessageEnum anEnum : JudgeInfoMessageEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
