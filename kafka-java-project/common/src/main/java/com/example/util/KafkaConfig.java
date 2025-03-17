package com.example.util;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaConfig {

    /**
     * Возвращает общие настройки для подключения к Kafka с аутентификацией и SSL.
     *
     * @param bootstrapServers   Список Kafka-брокеров
     * @param truststoreLocation Путь к truststore-файлу для SSL
     * @param truststorePassword Пароль для truststore
     * @param user               Имя пользователя для аутентификации
     * @param userPassword       Пароль пользователя
     * @return Настройки Kafka
     */
    public static Properties getCommonProperties(String bootstrapServers, String truststoreLocation, String truststorePassword, String user, String userPassword) {
        Properties props = new Properties();

        // Адреса Kafka-брокеров
        props.put("bootstrap.servers", bootstrapServers);

        // Используем безопасное соединение по SASL_SSL
        props.put("security.protocol", "SASL_SSL");

        // Механизм аутентификации SCRAM-SHA-512
        props.put("sasl.mechanism", "SCRAM-SHA-512");

        // Конфигурация JAAS для аутентификации с использованием логина и пароля
        String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, user, userPassword);
        props.put("sasl.jaas.config", jaasCfg);

        // Подключаем доверенный сертификат (truststore) для проверки SSL-соединений
        props.put("ssl.truststore.location", truststoreLocation);
        props.put("ssl.truststore.password", truststorePassword);

        return props;
    }

    /**
     * Возвращает настройки для Kafka Producer.
     *
     * @param bootstrapServers   Список Kafka-брокеров
     * @param truststoreLocation Путь к truststore-файлу для SSL
     * @param truststorePassword Пароль для truststore
     * @param user               Имя пользователя для аутентификации
     * @param userPassword       Пароль пользователя
     * @return Настройки Kafka Producer
     */
    public static Properties getProducerProperties(String bootstrapServers, String truststoreLocation, String truststorePassword, String user, String userPassword) {
        Properties props = getCommonProperties(bootstrapServers, truststoreLocation, truststorePassword, user, userPassword);

        // Гарантируем, что сообщение будет подтверждено всеми репликами перед подтверждением
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        // Указываем сериализаторы ключей и значений для отправки строковых данных
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return props;
    }

    /**
     * Возвращает настройки для Kafka Consumer.
     *
     * @param bootstrapServers   Список Kafka-брокеров
     * @param truststoreLocation Путь к truststore-файлу для SSL
     * @param truststorePassword Пароль для truststore
     * @param user               Имя пользователя для аутентификации
     * @param userPassword       Пароль пользователя
     * @param groupId            Идентификатор группы потребителей (consumer group)
     * @return Настройки Kafka Consumer
     */
    public static Properties getConsumerProperties(String bootstrapServers, String truststoreLocation, String truststorePassword, String user, String userPassword, String groupId) {
        Properties props = getCommonProperties(bootstrapServers, truststoreLocation, truststorePassword, user, userPassword);

        // Устанавливаем идентификатор группы консьюмера
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Указываем десериализаторы ключей и значений для обработки строковых данных
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Консьюмер начинает чтение с самого раннего доступного оффсета (если ранее не было сохранено оффсетов)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return props;
    }
}
