terraform {
  required_providers {
    yandex = {
      source = "yandex-cloud/yandex"
    }
  }
  required_version = ">= 0.13"
}

provider "yandex" {
  cloud_id  = "b1gfoh3gf7b1hpron97a"
  folder_id = "b1gn2hbr2fo69fj8dj4a"
  token     = "t1.9euelZqSk5uOyZuPnZmNzMmcjpyXmu3rnpWayJLHlpuWis2TyJuYypicmJPl9Pc7XB5B-e8ZFRS83fT3ewocQfnvGRUUvM3n9euelZqOnM_OypeLmc6JzZSOx4vMlO_8zef1656VmsmSipKSjIrKxo2Sk8eZmcnI7_3F656Vmo6cz87Kl4uZzonNlI7Hi8yU.2FDfPV1yrTYMBHqIIur3HKixspq0zi70tnRAm6zTk6ghG0mLxQmEgoV0YDCpbyqsK3oiwadkYnpbFceFBfb6Aw"
  zone      = "ru-central1-a"
}

# Kafka Cluster
resource "yandex_mdb_kafka_cluster" "this" {
  environment         = "PRODUCTION"
  name                = "kafka-std"
  network_id          = yandex_vpc_network.mynet.id
  subnet_ids          = [
    yandex_vpc_subnet.mysubnet-a.id,
    yandex_vpc_subnet.mysubnet-b.id,
    yandex_vpc_subnet.mysubnet-d.id
  ]
  security_group_ids  = [ yandex_vpc_security_group.kafka-std-sg.id ]
  deletion_protection = false

  config {
    assign_public_ip = true
    brokers_count    = 3
    version          = "3.5"
    schema_registry  = true

    kafka {
      resources {
        disk_size          = 30
        disk_type_id       = "network-ssd"
        resource_preset_id = "s2.micro"
      }
      kafka_config {
        compression_type                = "COMPRESSION_TYPE_LZ4"  # Оптимально для скорости и сжатия
        log_flush_interval_messages     = 10000  # Чаще сбрасывать лог (ускоряет восстановление)
        log_flush_interval_ms           = 1000   # 1 секунда - снижает задержки
        log_flush_scheduler_interval_ms = 1000   # Запуск очистки логов раз в 1 сек
        log_retention_bytes             = -1     # Не ограничивать размер логов (управляем временем)
        log_retention_hours             = 168    # 7 дней хранения логов (регулируй под требования)
        log_retention_minutes           = null   # Используем retention_hours
        log_retention_ms                = null   # Используем retention_hours
        log_segment_bytes               = 1073741824  # 1GB (стандартный размер сегмента)
        num_partitions                  = 3      # Минимум 3 для балансировки нагрузки
        default_replication_factor      = 3      # Надежность (2 копии + 1 основная)
        message_max_bytes               = 10485760  # 10MB - для работы с крупными сообщениями
        replica_fetch_max_bytes         = 10485760  # Соответствует max message size
        ssl_cipher_suites               = [ "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384" ] # Современные безопасные алгоритмы
        offsets_retention_minutes       = 10080  # 7 дней (важно для потребителей)
        sasl_enabled_mechanisms         = ["SASL_MECHANISM_SCRAM_SHA_512"]  # Максимально надежный механизм аутентификации
      }
    }

    dynamic "zookeeper" {
      for_each = 3 > 1 ? [1] : []
      content {
        resources {
          resource_preset_id = "s2.micro"
          disk_size          = 30
          disk_type_id       = "network-ssd"
        }
      }
    }

    zones = [
      "ru-central1-a",
      "ru-central1-b",
      "ru-central1-d"
    ]
  }
}

#Topics
resource "yandex_mdb_kafka_topic" "this" {
  for_each           = { for topic in var.topics : topic.name => topic }
  cluster_id         = yandex_mdb_kafka_cluster.this.id
  name               = each.value.name
  partitions         = each.value.partitions
  replication_factor = each.value.replication_factor

  topic_config {
    cleanup_policy        = each.value.topic_config.cleanup_policy
    compression_type      = each.value.topic_config.compression_type
    delete_retention_ms   = each.value.topic_config.delete_retention_ms
    file_delete_delay_ms  = each.value.topic_config.file_delete_delay_ms
    flush_messages        = each.value.topic_config.flush_messages
    flush_ms              = each.value.topic_config.flush_ms
    min_compaction_lag_ms = each.value.topic_config.min_compaction_lag_ms
    retention_bytes       = each.value.topic_config.retention_bytes
    retention_ms          = each.value.topic_config.retention_ms
    max_message_bytes     = each.value.topic_config.max_message_bytes
    min_insync_replicas   = each.value.topic_config.min_insync_replicas
    segment_bytes         = each.value.topic_config.segment_bytes
  }
}

#Users
resource "random_password" "password" {
  for_each         = { for v in var.users : v.name => v if v.password == null }
  length           = 16
  special          = true
  min_lower        = 1
  min_numeric      = 1
  min_special      = 1
  min_upper        = 1
  override_special = "_"
}
resource "yandex_mdb_kafka_user" "this" {
  for_each   = { for user in var.users : user.name => user }
  cluster_id = yandex_mdb_kafka_cluster.this.id
  name       = each.value.name
  password   = each.value.password == null ? random_password.password[each.value.name].result : each.value.password

  dynamic "permission" {
    for_each = each.value.permissions
    content {
      topic_name  = permission.value.topic_name
      role        = permission.value.role
      allow_hosts = permission.value.allow_hosts
    }
  }
}

# VPC Network
resource "yandex_vpc_network" "mynet" {
  name = "mynet"
}

# Subnets in Different Zones
resource "yandex_vpc_subnet" "mysubnet-a" {
  name           = "mysubnet-a"
  zone           = "ru-central1-a"
  network_id     = yandex_vpc_network.mynet.id
  v4_cidr_blocks = ["10.5.0.0/24"]
}

resource "yandex_vpc_subnet" "mysubnet-b" {
  name           = "mysubnet-b"
  zone           = "ru-central1-b"
  network_id     = yandex_vpc_network.mynet.id
  v4_cidr_blocks = ["10.5.1.0/24"]
}

resource "yandex_vpc_subnet" "mysubnet-d" {
  name           = "mysubnet-d"
  zone           = "ru-central1-d"
  network_id     = yandex_vpc_network.mynet.id
  v4_cidr_blocks = ["10.5.2.0/24"]
}

# Security Group for Kafka and Zookeeper
resource "yandex_vpc_security_group" "kafka-std-sg" {
  name       = "kafka-std-sg"
  network_id = yandex_vpc_network.mynet.id

  # Обновлённое правило: разрешаем трафик с любых IP на порт 9091
  ingress {
    description    = "Kafka Inter-Broker Communication"
    port           = 9091
    protocol       = "TCP"
    v4_cidr_blocks = ["0.0.0.0/0"]
  }
  
  ingress {
    description    = "Kafka Broker Internal"
    port           = 9092
    protocol       = "TCP"
    v4_cidr_blocks = ["10.5.0.0/16"]
  }

  ingress {
    description    = "Kafka External (если нужен доступ снаружи)"
    port           = 9093
    protocol       = "TCP"
    v4_cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description    = "Zookeeper Client"
    port           = 2181
    protocol       = "TCP"
    v4_cidr_blocks = ["10.5.0.0/16"]
  }

  ingress {
    description    = "Zookeeper Peer Communication"
    port           = 2888
    protocol       = "TCP"
    v4_cidr_blocks = ["10.5.0.0/16"]
  }

  ingress {
    description    = "Zookeeper Leader Election"
    port           = 3888
    protocol       = "TCP"
    v4_cidr_blocks = ["10.5.0.0/16"]
  }

  # Добавлено новое правило для доступа к API Managed Service for Apache Kafka® (например, Managed Schema Registry)
  ingress {
    description    = "Managed Kafka API (Managed Schema Registry)"
    port           = 443
    protocol       = "TCP"
    v4_cidr_blocks = ["0.0.0.0/0"]
  }

  # Разрешаем egress для всех протоколов внутри VPC
  egress {
    protocol       = "TCP"
    description    = "Allow all traffic inside VPC"
    v4_cidr_blocks = ["10.5.0.0/16"]
  }

  # Разрешаем клиентам Kafka подключаться к брокерам
  ingress {
    description    = "Kafka Clients"
    port           = 9092
    protocol       = "TCP"
    v4_cidr_blocks = ["0.0.0.0/0"]
  }
}
