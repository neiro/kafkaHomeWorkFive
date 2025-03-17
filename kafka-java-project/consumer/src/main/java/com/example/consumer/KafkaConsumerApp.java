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
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS"); // Список Kafka-брокеров
        String topic = System.getenv("TOPIC"); // Топик, который консьюмер будет слушать
        String user = System.getenv("CONSUMER_USER"); // Пользователь Kafka (если используется аутентификация)
        String password = System.getenv("CONSUMER_PASSWORD"); // Пароль пользователя Kafka
        String truststoreLocation = System.getenv("TRUSTSTORE_LOCATION"); // Путь к truststore (если используется SSL)
        String truststorePassword = System.getenv("TRUSTSTORE_PASSWORD"); // Пароль от truststore

        // Идентификатор группы консьюмера (можно задать статично или через переменную окружения)
        String groupId = "my_consumer_group";

        // Получаем настройки Kafka-консьюмера с учётом безопасности (SSL, аутентификация)
        Properties props = KafkaConfig.getConsumerProperties(bootstrapServers, truststoreLocation, truststorePassword, user, password, groupId);
 
        // Создаём экземпляр KafkaConsumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // Подписываемся на один топик (можно подписаться на несколько)
        consumer.subscribe(Collections.singletonList(topic));
 
        // Бесконечный цикл для получения сообщений из Kafka
        while (true) {
            // Запрашиваем сообщения у Kafka с таймаутом в 1 секунду
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            // Обрабатываем полученные сообщения
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("Получено сообщение: ключ = %s, значение = %s%n", record.key(), record.value());
            }
        }
    }
}
