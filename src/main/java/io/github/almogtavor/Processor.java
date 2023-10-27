package io.github.almogtavor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.almogtavor.model.InputPojo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class Processor implements ApplicationRunner {
    private final KafkaReceiver<String, String> kafkaReceiver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Mono<List<MessageBuilder<InputPojo>>> myAsyncOperation(List<MessageBuilder<InputPojo>> msgs) {
        return Mono.just(msgs);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        kafkaReceiver.receive()
                .doOnNext(consumerRecord -> log.info("received key={}, value={} from topic={}, offset={}",
                        consumerRecord.key(),
                        consumerRecord.value(),
                        consumerRecord.topic(),
                        consumerRecord.offset())
                )
                .map(ConsumerRecord::value)
                .doOnNext(fakeConsumerDTO -> log.info("successfully consumed {}={}", InputPojo.class.getSimpleName(), fakeConsumerDTO))
                .map(msg -> {
                    try {
                        return MessageBuilder.withPayload(objectMapper.readValue(msg, InputPojo.class));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .buffer(5)
                .concatMap(this::myAsyncOperation)
                .doOnError(throwable -> log.error("something bad happened while consuming : {}", throwable.getMessage()))
                .subscribe();
    }
}