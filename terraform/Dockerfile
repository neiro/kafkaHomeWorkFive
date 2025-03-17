FROM alpine:latest

# Используем шелл с опцией -e (выход при ошибке) и -o pipefail для корректного контроля ошибок
SHELL ["/bin/sh", "-eo", "pipefail", "-c"]

# Обновляем apk и устанавливаем необходимые пакеты
RUN apk update && apk add --no-cache curl jq ca-certificates wget

# Добавляем сертификат Yandex в доверенные сертификаты
RUN mkdir -p /usr/local/share/ca-certificates && \
    wget "https://storage.yandexcloud.net/cloud-certs/CA.pem" -O /usr/local/share/ca-certificates/YandexInternalRootCA.crt && \
    update-ca-certificates

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файл схемы и скрипт регистрации в контейнер
COPY Product.avsc /app/Product.avsc
COPY register-schema.sh /app/register-schema.sh

# Делаем скрипт исполняемым
RUN chmod +x /app/register-schema.sh

# Определяем переменные окружения (их можно переопределить при запуске контейнера)
ENV SCHEMA_REGISTRY_URL=https://rc1a-3rdr436j5lotnvfo.mdb.yandexcloud.net:443
ENV SCHEMA_REGISTRY_USER=producer
ENV SCHEMA_REGISTRY_PASSWORD=kafkaSuperPuper

# Выполняем скрипт регистрации схемы
ENTRYPOINT ["/app/register-schema.sh"]
