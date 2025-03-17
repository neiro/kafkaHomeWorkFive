package com.example.producer;

import com.example.util.KafkaConfig;
import com.github.javafaker.Faker;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KafkaProducerApp {
    public static void main(String[] args) {
        // Считываем параметры подключения из переменных окружения
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String topic = System.getenv("TOPIC");
        String user = System.getenv("PRODUCER_USER");
        String password = System.getenv("PRODUCER_PASSWORD");
        String truststoreLocation = System.getenv("TRUSTSTORE_LOCATION");
        String truststorePassword = System.getenv("TRUSTSTORE_PASSWORD");

        // Получаем настройки продюсера через утилиту
        Properties props = KafkaConfig.getProducerProperties(
                bootstrapServers, truststoreLocation, truststorePassword, user, password);
        
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // Создаем экземпляр Faker для генерации фейковых данных
        Faker faker = new Faker();

        // Генерация сообщений в течение 10 минут (600 000 мс)
        long durationMillis = TimeUnit.MINUTES.toMillis(10);
        long endTime = System.currentTimeMillis() + durationMillis;
        long messageCount = 0;

        try {
            while (System.currentTimeMillis() < endTime) {
                // Генерация полей согласно схеме
                String article = faker.code().isbn13(); // пример случайного кода
                String name = faker.commerce().productName();
                int quantity = faker.number().numberBetween(1, 1000);
                double price = faker.number().randomDouble(2, 10, 1000);
                boolean inStock = faker.bool().bool();
                // Выбираем валюту из заданного набора
                String[] currencies = {"USD", "EUR", "RUB"};
                String currency = currencies[faker.number().numberBetween(0, currencies.length)];
                String description = faker.lorem().sentence();

                // Формирование JSON-сообщения в соответствии с Avro-схемой
                String jsonPayload = String.format(
                        "{\"article\":\"%s\",\"name\":\"%s\",\"quantity\":%d,\"price\":%.2f,\"inStock\":%b,\"currency\":\"%s\",\"description\":\"%s\"}",
                        article, name, quantity, price, inStock, currency, description);

                // Формирование ProducerRecord (ключ можно формировать по-своему)
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, "key-" + messageCount, jsonPayload);
                
                // Отправляем сообщение асинхронно
                producer.send(record);
                messageCount++;

                // Задержка 10 мс перед отправкой следующего сообщения
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.flush();
            producer.close();
            System.out.println("Отправлено сообщений: " + messageCount);
        }
    }
}
