package io.github.almogtavor.configuration;

import io.github.almogtavor.model.InputPojo;
import org.apache.kafka.common.serialization.StringDeserializer;
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
    public ReceiverOptions<String, String> kafkaReceiverOptions(@Value(value = "${spring.kafka.consumer.topic}") String topic,
                                                                   KafkaProperties kafkaProperties) {
        return ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties())
                .subscription(Collections.singletonList(topic))
                .withKeyDeserializer(new StringDeserializer())
                .withValueDeserializer(new StringDeserializer());
    }

    @Bean
    public KafkaReceiver<String, String> createKafkaReceiver(ReceiverOptions<String, String> kafkaReceiverOptions) {
        return KafkaReceiver.create(kafkaReceiverOptions);
    }
}