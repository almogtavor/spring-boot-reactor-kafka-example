package io.github.almogtavor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.almogtavor.model.InputPojo;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.receiver.observation.KafkaReceiverObservation;
import reactor.kafka.receiver.observation.KafkaRecordReceiverContext;
import reactor.kafka.sender.KafkaSender;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class Processor implements ApplicationRunner {
    private final KafkaReceiver<String, String> kafkaReceiver;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservationRegistry observationRegistry;
    private final KafkaProperties kafkaProperties;

    private Mono<List<MessageBuilder<InputPojo>>> myAsyncOperation(List<MessageBuilder<InputPojo>> msgs) {
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8080/employees")).GET().build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request,HttpResponse.BodyHandlers.ofString());
        return Mono.fromFuture(response).map(responses -> msgs);
    }
    private Mono<MessageBuilder<InputPojo>> mySingleAsyncOperation(MessageBuilder<InputPojo> msgs) {
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8080/employees")).GET().build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request,HttpResponse.BodyHandlers.ofString());
        return Mono.fromFuture(response).map(responses -> msgs);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Observation parentObservation = Observation.start("test parent observation", this.observationRegistry);

//        kafkaSender.createOutbound()
//                .send(Mono.just(new ProducerRecord<>(SpringBootReactorKafkaTracingApplication.MY_TOPIC, "test data")))
//                .then()
//                .doOnTerminate(parentObservation::stop)
//                .doOnError(parentObservation::error)
//                .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, parentObservation))
//                .subscribe();

        kafkaReceiver.receive()
                .flatMap(r -> {
                    Observation receiverObservation =
                            KafkaReceiverObservation.RECEIVER_OBSERVATION.start(null,
                                    KafkaReceiverObservation.DefaultKafkaReceiverObservationConvention.INSTANCE,
                                    () ->
                                            new KafkaRecordReceiverContext(
                                                    r, "user.receiver",
                                                    StringUtils.collectionToCommaDelimitedString(this.kafkaProperties.getBootstrapServers())),
                                    this.observationRegistry);
                    return Mono.just(r)
                            .<ReceiverRecord<String, String>>handle((consumerRecord, sink) -> {
                                log.info(consumerRecord.value());
                                sink.next(consumerRecord);
                            })
                            .doOnTerminate(receiverObservation::stop)
                            .doOnError(receiverObservation::error)
                            .contextWrite(context ->
                                    context.put(ObservationThreadLocalAccessor.KEY, receiverObservation));
                })
                .publishOn(Schedulers.parallel())
                //.retryWhen(Retry.max(3))
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
                //.concatMap(this::mySingleAsyncOperation)
                .buffer(5)
                .concatMap(this::myAsyncOperation)
                .doOnError(throwable -> log.error("something bad happened while consuming : {}", throwable.getMessage()))
                .subscribe();
    }
}