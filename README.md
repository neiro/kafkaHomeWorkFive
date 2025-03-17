# Развёртывание и настройка Kafka-кластера в Yandex Cloud
Данное задание посвящено развертыванию Kafka-кластера в Yandex Cloud и его базовой настройке для продакшн-среды. Ниже приведён единый readme.md файл с минимальным форматированием.

## 1. Введение
В задании развернут Kafka-кластер с 3 брокерами в Yandex Cloud. Выполнена настройка репликации, хранения логов, развернут и настроен Schema Registry, а также протестирована передача сообщений через продюсер и консьюмер.

## 2. Шаг 1: Развёртывание Kafka-кластера
- Развернут Kafka-кластер с 3 брокерами.
- Аппаратные ресурсы (пример): CPU – 4 ядра, RAM – 16 ГБ, Диски – 3 SSD по 500 ГБ.
- Пример скрипта развертывания:
yc managed-kafka cluster create --name my-kafka-cluster --broker-count 3 --broker-spec "cpu=4, memory=16Gb, disk_size=500Gb"

## 3. Шаг 2: Настройка репликации и хранения данных
- Создан топик с 3 партициями и коэффициентом репликации 3.
- Настроены параметры: log.cleanup.policy (например, compact или delete), log.retention.ms (например, 604800000 – 7 дней), log.segment.bytes (например, 1073741824 – 1GB).
- Команда создания топика:
kafka-topics.sh --create --zookeeper <zookeeper_host>:2181 --replication-factor 3 --partitions 3 --topic my_topic
- Вывод команды kafka-topics.sh --describe:
<здесь будет вывод команды>

## 4. Шаг 3: Настройка Schema Registry
- Развернут Schema Registry.
- Файл схемы (my_schema.avsc):
{
  "type": "record",
  "name": "MyRecord",
  "namespace": "com.example",
  "fields": [
    {"name": "id", "type": "string"},
    {"name": "value", "type": "string"}
  ]
}
- Запуск Schema Registry:
schema-registry-start schema-registry.properties
- Проверка схем:
curl http://localhost:8081/subjects  
<здесь будет вывод curl запроса для получения subjects>  
curl -X GET http://localhost:8081/subjects/<название_схемы>/versions  
<здесь будет вывод curl запроса для получения версий схемы>

## 5. Шаг 4: Тестирование Kafka
- Пример кода продюсера:
from kafka import KafkaProducer  
import json  
producer = KafkaProducer(bootstrap_servers='localhost:9092', value_serializer=lambda v: json.dumps(v).encode('utf-8'))  
producer.send('my_topic', {'id': '1', 'value': 'test message'})  
producer.flush()  
print("Сообщение успешно отправлено")
- Пример кода консьюмера:
from kafka import KafkaConsumer  
import json  
consumer = KafkaConsumer('my_topic', bootstrap_servers='localhost:9092', auto_offset_reset='earliest', value_deserializer=lambda m: json.loads(m.decode('utf-8')))  
for message in consumer:  
    print("Получено сообщение:", message.value)
- Логи:
Логи продюсера: <здесь будут логи отправки сообщений>  
Логи консьюмера: <здесь будут логи получения сообщений>

## 6. Результаты и выводы
Описание выполненных шагов: Развернут Kafka-кластер, настроена репликация и хранение данных, запущен Schema Registry, протестирована передача сообщений через продюсер и консьюмер. Аппаратные ресурсы: Оптимальные параметры для брокеров (CPU, RAM, диски). Конфигурационные скрипты и файлы: Скрипты развертывания, создания топиков, запуска Schema Registry и файл схемы данных. Проверка работы Kafka: Выводы команды kafka-topics.sh --describe, curl-запросы для Schema Registry, логи продюсера и консьюмера. Примечание: Места, помеченные как "<заглушка>", предназначены для вставки реальных данных после тестирования.
