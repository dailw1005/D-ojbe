package com.ojbe.mq;

import com.ojbe.exception.BusinessException;
import com.ojbe.judge.JudgeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 判题消息消费者
 *
 * 异常处理策略:
 * - BusinessException（如提交不存在、状态非 PENDING）：业务逻辑错误，不重试，直接记录日志
 * - 其他 Exception（如 DB 连接超时、网络异常）：透传给 DefaultErrorHandler 重试（3次，3s 间隔），
 *   重试耗尽后投递到死信队列 judge_topic.DLT
 */
@Component
@Slf4j
public class JudgeConsumer {

    @Resource
    private JudgeService judgeService;

    @KafkaListener(topics = "judge_topic", groupId = "judge-group")
    public void receiveMessage(ConsumerRecord<String, String> record) {
        String message = record.value();
        log.info("接收到判题消息，提交 ID：{}", message);
        try {
            Long questionSubmitId = Long.parseLong(message);
            judgeService.doJudge(questionSubmitId);
        } catch (BusinessException e) {
            log.error("判题业务异常，消息跳过: 提交 ID={}, code={}, msg={}",
                    message, e.getCode(), e.getMessage());
        }
    }
}
