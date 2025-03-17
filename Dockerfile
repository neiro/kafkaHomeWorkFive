# Используем Alpine в качестве базового образа
FROM alpine:latest

# Устанавливаем необходимые пакеты: curl, jq, ca-certificates, wget, openjdk11-jre-headless
RUN apk update && apk add --no-cache \
    curl \
    jq \
    ca-certificates \
    wget \
    openjdk11-jre-headless

# Создаем директорию для сертификата Яндекса и скачиваем CA.pem
RUN mkdir -p /usr/local/share/ca-certificates/Yandex && \
    wget "https://storage.yandexcloud.net/cloud-certs/CA.pem" \
         -O /usr/local/share/ca-certificates/Yandex/YandexInternalRootCA.crt && \
    chmod 0655 /usr/local/share/ca-certificates/Yandex/YandexInternalRootCA.crt && \
    update-ca-certificates

# Импортируем сертификат в Java Keystore с использованием опции -cacerts и стандартного пароля "changeit"
RUN keytool -importcert \
    -alias YandexCA \
    -file /usr/local/share/ca-certificates/Yandex/YandexInternalRootCA.crt \
    -cacerts \
    -storepass changeit \
    -noprompt


# Создаем рабочую директорию
WORKDIR /app

# Копируем файлы AVRO-схемы и скрипт регистрации в контейнер
COPY Product.avsc /app/Product.avsc
COPY register-schema.sh /app/register-schema.sh
RUN chmod +x /app/register-schema.sh

# По умолчанию контейнер выполняет скрипт регистрации схемы
ENTRYPOINT ["/bin/sh", "/app/register-schema.sh"]
