package com.ojbe.config;

import com.ojbe.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka 配置类
 * 配置消费者容器工厂的重试与死信机制
 */
@Configuration
@Slf4j
public class KafkaConfig {

    /**
     * 配置 Kafka 监听器容器工厂
     * - enable-auto-commit: false，offset 由容器管理
     * - 消费失败后由 DefaultErrorHandler 重试 3 次（间隔 3 秒）
     * - 重试耗尽后投递到死信主题 judge_topic.DLT
     * - BusinessException 标记为不可重试（业务逻辑错误重试无意义）
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (ConsumerRecord<?, ?> cr, Exception e) -> {
                    String dltTopic = cr.topic() + ".DLT";
                    log.warn("判题消息进入死信队列: topic={}, partition={}, offset={}, dltTopic={}, error={}",
                            cr.topic(), cr.partition(), cr.offset(), dltTopic, e.getMessage());
                    return new TopicPartition(dltTopic, cr.partition());
                });

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(3000L, 3L));
        errorHandler.addNotRetryableExceptions(BusinessException.class);

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);

        log.info("Kafka 消费者容器工厂配置完成（手动确认 + 3次重试 + DLT）");
        return factory;
    }
}
