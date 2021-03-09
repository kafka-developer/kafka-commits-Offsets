package com.xargspratix.consumers ;



import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import java.time.Duration;
import java.util.*;

public class CommitAsyncSpecifiedOffset {
    private static String TOPIC_NAME = "example-topic-2020-5-28c";
    private static KafkaConsumer<String, String> consumer;
    private static TopicPartition topicPartition;

    public static void main(String[] args) throws Exception {
        Properties consumerProps = PropertiesConfig.getConsumerProps();
        consumer = new KafkaConsumer<>(consumerProps);
        topicPartition = new TopicPartition(TOPIC_NAME, 0);
        consumer.assign(Collections.singleton(topicPartition));
        printOffsets("before consumer loop");
        sendMessages();
        startConsumer();
    }

    private static void startConsumer() {

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("consumed: key = %s, value = %s, partition id= %s, offset = %s%n",
                        record.key(), record.value(), record.partition(), record.offset());
                consumer.commitAsync(
                        Collections.singletonMap(topicPartition,  new OffsetAndMetadata(record.offset())),
                        (offsets, exception) -> {
                    System.out.printf("Callback, offset: %s, exception %s%n", offsets, exception);
                });
            }
            if (records.isEmpty()) {
                System.out.println("-- terminating consumer --");
                break;
            }
        }
        printOffsets("after consumer loop");
    }

    private static void printOffsets(String message) {
        Map<TopicPartition, OffsetAndMetadata> committed = consumer
                .committed(new HashSet<>(Arrays.asList(topicPartition)));
        OffsetAndMetadata offsetAndMetadata = committed.get(topicPartition);
        long position = consumer.position(topicPartition);
        System.out
                .printf("Offset info %s, Committed: %s, current position %s%n", message,
                        offsetAndMetadata == null ? null : offsetAndMetadata
                        .offset(), position);
    }

    private static void sendMessages() {
        Properties producerProps = com.xargspratix.consumers.PropertiesConfig.getProducerProps();
        KafkaProducer producer = new KafkaProducer<>(producerProps);
        for (int i = 0; i < 4; i++) {
            String value = "message-" + i;
            System.out.printf("Sending message topic: %s, value: %s%n", TOPIC_NAME, value);
            producer.send(new ProducerRecord<>(TOPIC_NAME, value));
        }
        producer.flush();
        producer.close();
    }
}