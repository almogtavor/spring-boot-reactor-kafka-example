# spring-boot-reactor-kafka-example

## Run Locally

Set up Kafka:
```bash
cd docker
docker> docker-compose -f kafka-docker-compose.yaml up
```
Go to localhost:9000 to use Kafdrop and create a topic named `input_topic`.

Finally run the `ProduceSampleMessages` class to produce messages to Kafka.

Now you can run `SpringBootReactorKafkaExample`.