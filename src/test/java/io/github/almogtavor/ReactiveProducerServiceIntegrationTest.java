package io.github.almogtavor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(topics = {"${FAKE_PRODUCER_DTO_TOPIC}", "${spring.kafka.consumer.topic}"})
class ReactiveProducerServiceIntegrationTest {

    @Test
    void reactiveProducerServiceMethodSend_FakeProducerDto_publishFakeProducerDtoOnTopic() {
        // TODO
    }
}