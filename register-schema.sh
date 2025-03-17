#!/bin/sh
set -e

echo "Начало регистрации схемы..."

# Формируем JSON и выводим его для отладки
json_payload=$(jq -n --slurpfile data /app/Product.avsc '{
  "schemaType": "AVRO",
  "schema": "\($data)"
}')
echo "Сформированный JSON:"
echo "$json_payload"

echo "Отправляем запрос в Schema Registry (без проверки SSL)..."
response=$(echo "$json_payload" | curl --verbose --fail --insecure \
  --url "${SCHEMA_REGISTRY_URL}/subjects/my_topic-value/versions" \
  --user "${SCHEMA_REGISTRY_USER}:${SCHEMA_REGISTRY_PASSWORD}" \
  --header 'Content-Type: application/vnd.schemaregistry.v1+json' \
  --data "@-")
echo "Ответ сервера:"
echo "$response"

echo "Регистрация схемы завершена."
