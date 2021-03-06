package com.xargspratix.consumers;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConsumerRebalance {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            int finalI = i;
            executorService.execute(() -> startConsumer("consumer-" + finalI));
            Thread.sleep(3000);
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.MINUTES);
    }

    private static KafkaConsumer<String, String> startConsumer(String name) {
        Properties consumerProps = PropertiesConfig.getConsumerProps(false,1000L);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singleton("commits-offsets"),
                new ConsumerRebalanceListener() {
                    @Override  // onPartitionsRevoked() This is called before rebalancing starts and after the consumer stopped consuming messages.
                               // This is where you want to commit offsets,so whomever get this partition next will know where to start
                    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                        System.out.printf("onPartitionsRevoked - consumerName: %s, partitions: %s%n", name,
                                formatPartitions(partitions));
                    }
                    @Override   // onPartitionsAssigned() This is called partitions have been reassigned to the broker, but before the consumer starts consuming messages.
                    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                        System.out.printf("onPartitionsAssigned - consumerName: %s, partitions: %s%n", name,
                                formatPartitions(partitions));
                    }
                });
        System.out.printf("starting consumerName: %s%n", name);
        consumer.poll(Duration.ofSeconds(10));
        System.out.printf("closing consumerName: %s%n", name);
        consumer.close();
        return consumer;
    }

    private static List<String> formatPartitions(Collection<TopicPartition> partitions) {
        return partitions.stream().map(topicPartition ->
                String.format("topic: %s, partition: %s", topicPartition.topic(), topicPartition.partition()))
                         .collect(Collectors.toList());
    }
}