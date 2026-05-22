package com.dailw.mq;

import com.dailw.judge.JudgeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 判题消息消费者
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
            // 调用判题服务进行判题
            judgeService.doJudge(questionSubmitId);
        } catch (Exception e) {
            log.error("处理判题消息失败，提交 ID：{}", message, e);
        }
    }
}
