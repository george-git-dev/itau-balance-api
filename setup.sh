#!/bin/bash

set -e

echo "🚀 Starting environment..."

docker compose up -d

echo "🍃 Starting MongoDB..."
if [ "$(docker ps -q -f name=mongo)" ]; then
  echo "   MongoDB already running, skipping..."
else
  docker run -d \
    --name mongo \
    -p 27017:27017 \
    --restart unless-stopped \
    mongo:latest > /dev/null
  echo "✅ MongoDB started!"
fi

echo "⏳ Waiting for LocalStack to be ready..."
RETRIES=0
until docker exec localstack curl -s http://localhost:4566/_localstack/health | grep '"sqs": "available"' > /dev/null 2>&1; do
  echo "   Waiting for SQS... ($RETRIES/20)"
  sleep 3
  RETRIES=$((RETRIES+1))
  if [ $RETRIES -ge 20 ]; then
    echo "❌ Timeout! LocalStack did not start in time."
    exit 1
  fi
done
echo "✅ LocalStack is ready!"

MAIN_QUEUE="transacoes-financeiras-processadas"
DLQ="transacoes-financeiras-dlq"
ENDPOINT="http://localhost:4566"
ACCOUNT_ID="000000000000"

echo "⏳ Waiting for main queue to be available..."
RETRIES=0
until docker exec localstack awslocal sqs list-queues | grep $MAIN_QUEUE > /dev/null 2>&1; do
  echo "   Waiting for main queue... ($RETRIES/30)"
  sleep 3
  RETRIES=$((RETRIES+1))
  if [ $RETRIES -ge 30 ]; then
    echo "❌ Timeout! Main queue was not found."
    exit 1
  fi
done
echo "✅ Main queue found!"

echo "📦 Creating DLQ: $DLQ..."
docker exec localstack awslocal sqs create-queue --queue-name $DLQ > /dev/null

echo "🔍 Getting DLQ ARN..."
DLQ_ARN=$(docker exec localstack awslocal sqs get-queue-attributes \
  --queue-url $ENDPOINT/$ACCOUNT_ID/$DLQ \
  --attribute-name QueueArn \
  --query "Attributes.QueueArn" \
  --output text)
echo "   DLQ ARN: $DLQ_ARN"

echo "🔗 Configuring Redrive Policy..."
docker exec localstack awslocal sqs set-queue-attributes \
  --queue-url $ENDPOINT/$ACCOUNT_ID/$MAIN_QUEUE \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$DLQ_ARN\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\"}"

echo ""
echo "✅ Setup completed successfully!"
echo "   Main queue : $ENDPOINT/$ACCOUNT_ID/$MAIN_QUEUE"
echo "   DLQ        : $ENDPOINT/$ACCOUNT_ID/$DLQ"
echo "   MongoDB    : mongodb://localhost:27017"

echo ""
echo "📊 Monitoring queues (Ctrl+C to stop)..."
echo ""

while true; do
  clear
  echo "=== $(date) ==="

  echo ""
  echo "--- FILA PRINCIPAL ---"
  docker exec localstack awslocal sqs get-queue-attributes \
    --queue-url http://sqs.sa-east-1.localhost.localstack.cloud:4566/$ACCOUNT_ID/$MAIN_QUEUE \
    --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible

  echo ""
  echo "--- DLQ ---"
  docker exec localstack awslocal sqs get-queue-attributes \
    --queue-url http://sqs.sa-east-1.localhost.localstack.cloud:4566/$ACCOUNT_ID/$DLQ \
    --attribute-names ApproximateNumberOfMessages

  sleep 5
done