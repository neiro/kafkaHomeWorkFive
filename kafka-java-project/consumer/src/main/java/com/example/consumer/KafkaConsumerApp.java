package com.example.consumer;

import com.example.util.KafkaConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerApp {
    public static void main(String[] args) {
        // Считываем параметры подключения из переменных окружения
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String topic = System.getenv("TOPIC");
        String user = System.getenv("CONSUMER_USER");
        String password = System.getenv("CONSUMER_PASSWORD");
        String truststoreLocation = System.getenv("TRUSTSTORE_LOCATION");
        String truststorePassword = System.getenv("TRUSTSTORE_PASSWORD");
        // Группа консьюмера можно задать напрямую или через переменную
        String groupId = "my_consumer_group";

        // Получаем настройки консьюмера через утилиту
        Properties props = KafkaConfig.getConsumerProperties(bootstrapServers, truststoreLocation, truststorePassword, user, password, groupId);
 
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
 
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("Получено сообщение: ключ = %s, значение = %s%n", record.key(), record.value());
            }
        }
    }
}
