package com.xargspratix.consumers ;



import java.util.Properties;

public class PropertiesConfig {
    public static final String BROKERS = "192.168.1.158:9092";

    public static Properties getProducerProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BROKERS);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    public static Properties getConsumerProps(boolean autoCommit, Long autoCommitMillisInterval) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", BROKERS);
        props.setProperty("group.id", "testGroup");
        props.setProperty("enable.auto.commit", Boolean.toString(autoCommit));
        if (autoCommit) {
            props.setProperty("auto.commit.interval.ms", Long.toString(autoCommitMillisInterval));
        }
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }
}