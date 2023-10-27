package io.github.almogtavor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(topics = {"${FAKE_PRODUCER_DTO_TOPIC}", "${spring.kafka.consumer.topic}"})
class ReactivekafkaconsumerandproducerApplicationTests {

	@Test
	void main_contextLoads_DoesNotThrow() {
		assertDoesNotThrow(() -> SpringBootReactorKafkaExample.main(new String[]{"--server.port=0"}));
	}

}