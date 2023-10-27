package io.github.almogtavor.configuration;

import io.github.almogtavor.model.OutputPojo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

@Configuration
public class KafkaProducerConfiguration {
    @Bean
    public KafkaSender<String, OutputPojo> createKafkaSender(
            @Value("${spring.kafka.consumer.topic}") String outputTopic,
            KafkaProperties properties) {
        Map<String, Object> props = properties.buildProducerProperties();
        return KafkaSender.create(SenderOptions.create(props));
    }
    @Bean
    public ReactiveKafkaProducerTemplate<String, OutputPojo> reactiveKafkaProducerTemplate(
            @Value("${spring.kafka.consumer.topic}") String outputTopic,
            KafkaProperties properties) {
        Map<String, Object> props = properties.buildProducerProperties();
        return new ReactiveKafkaProducerTemplate<String, OutputPojo>(SenderOptions.create(props));
    }
}