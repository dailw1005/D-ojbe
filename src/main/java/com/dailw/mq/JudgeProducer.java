package com.dailw.mq;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 判题消息生产者
 */
@Component
@Slf4j
public class JudgeProducer {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送判题请求消息
     *
     * @param questionSubmitId 提交的题目 ID
     */
    public void sendMessage(String questionSubmitId) {
        log.info("发送判题消息，提交 ID：{}", questionSubmitId);
        kafkaTemplate.send("judge_topic", questionSubmitId)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("判题消息发送失败，提交 ID：{}", questionSubmitId, ex);
                    } else {
                        log.info("判题消息发送成功，提交 ID：{}, offset: {}",
                                questionSubmitId,
                                result != null && result.getRecordMetadata() != null
                                        ? result.getRecordMetadata().offset() : "unknown");
                    }
                });
    }
}
