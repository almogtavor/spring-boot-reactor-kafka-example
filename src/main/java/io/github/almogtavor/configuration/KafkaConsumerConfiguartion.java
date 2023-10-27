package io.github.almogtavor.configuration;

import io.github.almogtavor.model.InputPojo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;

@Configuration
public class KafkaConsumerConfiguartion {
    @Bean
    public ReceiverOptions<String, InputPojo> kafkaReceiverOptions(@Value(value = "${spring.kafka.consumer.topic}") String topic,
                                                                   KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, InputPojo>create(kafkaProperties.buildConsumerProperties())
                .subscription(Collections.singletonList(topic));
    }

    @Bean
    public KafkaReceiver<String, InputPojo> createKafkaReceiver(ReceiverOptions<String, InputPojo> kafkaReceiverOptions) {
        return KafkaReceiver.create(kafkaReceiverOptions);
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, InputPojo> reactiveKafkaConsumerTemplate(ReceiverOptions<String, InputPojo> kafkaReceiverOptions) {
        return new ReactiveKafkaConsumerTemplate<String, InputPojo>(kafkaReceiverOptions);
    }
}