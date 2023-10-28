package io.github.almogtavor;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.intellij.lang.annotations.Language;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class ProduceSampleMessages {
	public static final String TOPIC = "input_topic";
	public static final String BOOTSTRAP_SERVERS = "localhost:9093";

	public static void main(String[] args) {
		try (
				KafkaProducer<String, String> producer = new KafkaProducer<>(
						Map.of(
								ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
								BOOTSTRAP_SERVERS,
								ProducerConfig.CLIENT_ID_CONFIG,
								UUID.randomUUID().toString()
						),
						new StringSerializer(),
						new StringSerializer()
				);
		) {

			@Language("JSON") String a1 = """
                    { "name": "Jorge", "age": 30, "job": "Developer"}
                    """;
			@Language("JSON") String b1 = """
                    { "name": "Bob", "age": 32, "job": "Developer"}
                    """;
			@Language("JSON") String c1 = """
                    { "name": "Ou", "age": 42, "job": "Ba"}
                    """;
			@Language("JSON") String d1 = """
                    { "name": "Kim", "age": 54, "job": "Daaa"}
                    """;
			@Language("JSON") String e1 = """
                    { "name": "Bob", "age": 252423, "job": "Bla"}
                    """;

			producer.send(new ProducerRecord<>(TOPIC, "bla1", a1)).get();
			producer.send(new ProducerRecord<>(TOPIC, "bla2", b1)).get();
			producer.send(new ProducerRecord<>(TOPIC, "bla3", c1)).get();
			producer.send(new ProducerRecord<>(TOPIC, "bla4", d1)).get();
			producer.send(new ProducerRecord<>(TOPIC, "bla5", e1)).get();

			producer.send(new ProducerRecord<>(TOPIC, "bla1", a1)).get();
			producer.send(new ProducerRecord<>(TOPIC, "bla2", b1)).get();
			producer.send(new ProducerRecord<>(TOPIC, "bla3", c1)).get();
			producer.send(new ProducerRecord<>(TOPIC, "bla4", d1)).get();
			producer.send(new ProducerRecord<>(TOPIC, "bla5", e1)).get();

		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}