package com.xargspratix.consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AutoCommitFalse {
    private static String TOPIC_NAME = "example-topic-2020-5-7a";
    private static int MSG_COUNT = 4;

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(AutoCommitFalse::startConsumer);
        executorService.execute(AutoCommitFalse::sendMessages);
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
    }

    private static void startConsumer() {
        Properties consumerProps = PropertiesConfig.getConsumerProps(false, null);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        TopicPartition topicPartition = new TopicPartition(TOPIC_NAME, 0);
        consumer.assign(Collections.singleton(topicPartition));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("consumed: key = %s, value = %s, partition id= %s, offset = %s%n",
                        record.key(), record.value(), record.partition(), record.offset());
            }
            if(records.isEmpty()){
                System.out.println("-- terminating consumer --");
                break;
            }

            printOffsets(consumer, topicPartition);
        }
        printOffsets(consumer, topicPartition);
    }

    private static void printOffsets(KafkaConsumer<String, String> consumer, TopicPartition topicPartition) {
        Map<TopicPartition, OffsetAndMetadata> committed = consumer
                .committed(new HashSet<>(Arrays.asList(topicPartition)));
        OffsetAndMetadata offsetAndMetadata = committed.get(topicPartition);
        long position = consumer.position(topicPartition);
        System.out.printf("Committed: %s, current position %s%n", offsetAndMetadata == null ? null : offsetAndMetadata
                .offset(), position);
    }

    private static void sendMessages() {
        Properties producerProps = PropertiesConfig.getProducerProps();
        KafkaProducer producer = new KafkaProducer<>(producerProps);
        for (int i = 0; i < MSG_COUNT; i++) {
            String value = "message-" + i;
            System.out.printf("Sending message topic: %s, value: %s%n", TOPIC_NAME, value);
            producer.send(new ProducerRecord<>(TOPIC_NAME, value));
        }
        producer.flush();
        producer.close();
    }
}