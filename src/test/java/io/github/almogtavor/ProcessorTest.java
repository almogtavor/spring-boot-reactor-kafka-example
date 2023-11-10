package io.github.almogtavor;

import java.time.Duration;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.receiver.observation.KafkaReceiverObservation;
import reactor.kafka.receiver.observation.KafkaRecordReceiverContext;
import reactor.kafka.sender.KafkaSender;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.StringUtils;

@SpringBootTest
@EmbeddedKafka
@DirtiesContext
@AutoConfigureObservability
@Slf4j
class SpringBootReactorKafkaTracingApplicationTests {
    public static String MY_TOPIC ="MY_TOPIC";
    @Autowired
    KafkaSender<Integer, String> kafkaSender;
    @Autowired
    KafkaReceiver<Integer, String> kafkaReceiver;

    @Autowired
    KafkaProperties kafkaProperties;

    @Autowired
    ObservationRegistry observationRegistry;

    @Test
    void reactorKafkaPropagatesTraces() {
        Observation parentObservation = Observation.start("test parent observation", this.observationRegistry);

        kafkaSender.createOutbound()
                .send(Mono.just(new ProducerRecord<>(MY_TOPIC, "test data")))
                .then()
                .doOnTerminate(parentObservation::stop)
                .doOnError(parentObservation::error)
                .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, parentObservation))
                .subscribe();

        Flux<ReceiverRecord<Integer, String>> receive =
                this.kafkaReceiver
                        .receive()
                        .flatMap(record -> {
                            Observation receiverObservation =
                                    KafkaReceiverObservation.RECEIVER_OBSERVATION.start(null,
                                            KafkaReceiverObservation.DefaultKafkaReceiverObservationConvention.INSTANCE,
                                            () ->
                                                    new KafkaRecordReceiverContext(
                                                            record, "user.receiver",
                                                            StringUtils.collectionToCommaDelimitedString(this.kafkaProperties.getBootstrapServers())),
                                            this.observationRegistry);

                            return Mono.just(record)
                                    .<ReceiverRecord<Integer, String>>handle((consumerRecord, sink) -> {
                                        log.warn(consumerRecord.value());
                                        sink.next(consumerRecord);
                                    })
                                    .doOnTerminate(receiverObservation::stop)
                                    .doOnError(receiverObservation::error)
                                    .contextWrite(context ->
                                            context.put(ObservationThreadLocalAccessor.KEY, receiverObservation));
                        });

        StepVerifier.create(receive.map(ReceiverRecord::value))
                .expectNext("test data")
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

}