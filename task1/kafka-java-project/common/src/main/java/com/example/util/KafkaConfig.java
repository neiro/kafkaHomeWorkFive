package com.example.util;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaConfig {

    /**
     * Возвращает общие настройки для подключения к Kafka.
     */
    public static Properties getCommonProperties(String bootstrapServers, String truststoreLocation, String truststorePassword, String user, String userPassword) {
        Properties props = new Properties();
        // Общие настройки подключения
        props.put("bootstrap.servers", bootstrapServers);
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "SCRAM-SHA-512");

        String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, user, userPassword);
        props.put("sasl.jaas.config", jaasCfg);
        props.put("ssl.truststore.location", truststoreLocation);
        props.put("ssl.truststore.password", truststorePassword);

        return props;
    }

    /**
     * Настройки для Kafka Producer.
     */
    public static Properties getProducerProperties(String bootstrapServers, String truststoreLocation, String truststorePassword, String user, String userPassword) {
        Properties props = getCommonProperties(bootstrapServers, truststoreLocation, truststorePassword, user, userPassword);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }

    /**
     * Настройки для Kafka Consumer.
     */
    public static Properties getConsumerProperties(String bootstrapServers, String truststoreLocation, String truststorePassword, String user, String userPassword, String groupId) {
        Properties props = getCommonProperties(bootstrapServers, truststoreLocation, truststorePassword, user, userPassword);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}
