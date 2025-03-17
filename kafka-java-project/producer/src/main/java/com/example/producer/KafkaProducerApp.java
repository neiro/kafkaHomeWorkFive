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

        // Получаем настройки Kafka-продюсера с учётом безопасности (SSL, аутентификация)
        Properties props = KafkaConfig.getProducerProperties(
                bootstrapServers, truststoreLocation, truststorePassword, user, password);
        
        // Создаём Kafka-продюсер с заданными параметрами
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // Используем Faker для генерации случайных данных
        Faker faker = new Faker();

        // Продюсер отправляет сообщения в течение 10 минут
        long durationMillis = TimeUnit.MINUTES.toMillis(10);
        long endTime = System.currentTimeMillis() + durationMillis;
        long messageCount = 0; // Счётчик отправленных сообщений

        try {
            while (System.currentTimeMillis() < endTime) {
                // Генерация случайных данных, моделирующих товарные позиции
                String article = faker.code().isbn13(); // Генерируем случайный артикул
                String name = faker.commerce().productName(); // Название товара
                int quantity = faker.number().numberBetween(1, 1000); // Количество единиц
                double price = faker.number().randomDouble(2, 10, 1000); // Цена
                boolean inStock = faker.bool().bool(); // В наличии или нет

                // Выбор случайной валюты из доступного списка
                String[] currencies = {"USD", "EUR", "RUB"};
                String currency = currencies[faker.number().numberBetween(0, currencies.length)];
                
                String description = faker.lorem().sentence(); // Описание товара

                // Формируем JSON-сообщение в соответствии с предполагаемой схемой
                String jsonPayload = String.format(
                        "{\"article\":\"%s\",\"name\":\"%s\",\"quantity\":%d,\"price\":%.2f,\"inStock\":%b,\"currency\":\"%s\",\"description\":\"%s\"}",
                        article, name, quantity, price, inStock, currency, description);

                // Создаём Kafka-сообщение с ключом, привязанным к порядковому номеру
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, "key-" + messageCount, jsonPayload);
                
                // Асинхронно отправляем сообщение в Kafka
                producer.send(record);
                messageCount++;

                // Короткая задержка в 10 мс перед отправкой следующего сообщения
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Логируем возможные ошибки
        } finally {
            // Завершаем работу продюсера корректно
            producer.flush();
            producer.close();
            System.out.println("Отправлено сообщений: " + messageCount);
        }
    }
}
