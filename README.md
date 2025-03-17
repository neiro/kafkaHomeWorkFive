# Развёртывание и настройка Kafka-кластера в Yandex Cloud

Это руководство описывает процесс автоматического развёртывания Kafka-кластера в Yandex Cloud с помощью Terraform, а также его базовую конфигурацию для работы в продакшн-среде.

## 🚀 1. Общая информация о кластере

Кластер Kafka был программно (over terraform) развёрнут в Yandex Managed Service for Apache Kafka® с такими параметрами:

| Параметр              | Значение                     |
|-----------------------|------------------------------|
| Количество брокеров   | 3                            |
| Версия Kafka          | 3.5                          |
| Schema Registry       | ✅ включён                   |
| Public IP             | ✅ включён                   |
| Зоны доступности      | ru-central1-a, ru-central1-b, ru-central1-d |
| Защита от удаления    | ❌ отключена                 |
| Тип окружения         | PRODUCTION                   |

## 🖥️ 2. Аппаратная конфигурация брокеров и Zookeeper

### 🟢 Kafka-брокеры:

- **Количество:** 3
- **CPU:** 2 ядра (s2.micro)
- **RAM:** 8 ГБ (s2.micro)
- **Тип и размер диска:** Network SSD, 30 GB

### 🔵 Zookeeper:

- **Количество узлов:** 3 (автоматически масштабируется вместе с брокерами)
- **CPU:** 2 ядра (s2.micro)
- **RAM:** 8 ГБ
- **Тип и размер диска:** Network SSD, 30 GB

#### параметры с более высокими параметрыми почему то были недоступны с ошибкой насчет квот (возможно слишком дорогие), поэтому взял средне-минимальные

## 🔧 3. Конфигурация Kafka-кластера задана через файл main.tf 

| Параметр                      | Значение                     |
|-------------------------------|------------------------------|
| Политика очистки              | compact                      |
| Время хранения логов          | 7 дней (168 часов)           |
| Размер сегмента логов         | 1 GB                         |
| Максимальный размер сообщения | 10 MB                        |
| Репликация                    | 3                            |
| Количество партиций           | 3                            |
| Тип компрессии                | LZ4                          |
| Интервал сброса логов на диск  | 1000 мс / 10000 сообщений    |
| Интервал планировщика сброса логов | 1000 мс                  |
| Хранение offset'ов            | 7 дней                       |
| SSL Cipher Suite              | TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 |
| SASL-механизм аутентификации  | SCRAM-SHA-512                |

## 📌 4. Конфигурация сети и безопасности

### 🌐 VPC и подсети:

| Подсеть      | Зона          | CIDR-блок     |
|--------------|---------------|---------------|
| mysubnet-a   | ru-central1-a | 10.5.0.0/24   |
| mysubnet-b   | ru-central1-b | 10.5.1.0/24   |
| mysubnet-d   | ru-central1-d | 10.5.2.0/24   |

### 🔑 Группа безопасности (kafka-std-sg):

| Назначение               | Порт | Протокол | Source CIDR     |
|--------------------------|------|----------|-----------------|
| Kafka Inter-Broker SSL    | 9091 | TCP      | 0.0.0.0/0       |
| Kafka Clients             | 9092 | TCP      | 0.0.0.0/0       |
| Kafka Internal Traffic    | 9092 | TCP      | 10.5.0.0/16     |
| Kafka External (опционально) | 9093 | TCP | 0.0.0.0/0       |
| Schema Registry API       | 443  | TCP      | 0.0.0.0/0       |
| Zookeeper Client          | 2181 | TCP      | 10.5.0.0/16     |
| Zookeeper Peer            | 2888 | TCP      | 10.5.0.0/16     |
| Zookeeper Election        | 3888 | TCP      | 10.5.0.0/16     |

## 🗃️ 5. Создание топиков

Создан топик с именем `my_topic`:

| Параметр              | Значение                     |
|-----------------------|------------------------------|
| Название              | my_topic                     |
| Партиции              | 3                            |
| Фактор репликации     | 3                            |
| Очистка               | compact                      |
| Retention             | 7 дней (604800000 ms)        |
| Сжатие                | lz4                          |
| Размер сегмента       | 1 GB                         |
| Max сообщение         | 10 MB                        |

## 👤 6. Пользователи Kafka

Созданы пользователи с ролями и доступом к топику:

| Пользователь | Роль      | Доступ к топику | Source CIDR     |
|--------------|-----------|-----------------|-----------------|
| producer     | PRODUCER  | my_topic        | 0.0.0.0/0       |
| consumer     | CONSUMER  | my_topic        | 0.0.0.0/0       |

## 📦 7. Terraform-команда для развёртывания

Развёртывание инфраструктуры выполняется стандартными командами Terraform:

```bash
terraform init
terraform plan
terraform apply

для удаления использовать
```bash
terraform destroy
