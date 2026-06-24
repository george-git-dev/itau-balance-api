# Itau Balance API

## 📌 Overview

API responsible for consuming financial transaction events via Amazon SQS (simulated locally with LocalStack), processing and persisting account balances in MongoDB, and exposing them through a REST API.

The system is designed for **high throughput**, handling up to 2,000 messages per second with consistency guarantees — ensuring that even if messages arrive out of order, only the most recent balance is persisted.

---

## 🏗️ Architecture

```
Message Generator → SQS (LocalStack) → SQS Consumer → MongoDB → REST API → Client
                                              ↓
                                      Dead Letter Queue (DLQ)
                                   (on processing failure after 3 retries)
```

---

## ⚙️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Application language |
| Spring Boot 3 | Application framework |
| Spring Cloud AWS SQS | SQS consumer integration |
| MongoDB | Balance persistence |
| Docker / Docker Compose | Local infrastructure |
| LocalStack | AWS SQS simulation |
| Lombok | Boilerplate reduction |

---

## 📁 Project Structure (master branch)

```
src/
├── config/          # SQS and infrastructure configuration
├── consumer/        # SQS message listener
├── controller/      # REST endpoints
├── dto/             # Data transfer objects
├── entity/          # MongoDB document entities
├── exception/       # Custom exceptions
├── handler/         # Global exception handler
├── mapper/          # Message to entity mapping
├── repository/      # MongoDB repository (custom upsert logic)
└── service/         # Business logic
```

---

## 🚀 Running Locally

### Prerequisites

- Docker Desktop running
- Java 21+
- Maven

### 1. Place the provided `docker-compose.yml` in a folder

> ⚠️ The `docker-compose.yml` provided by the challenge **does not need to be started manually**. The `setup.sh` script handles everything automatically — just make sure the file is in the same folder as the script.

### 2. Run the setup script

```bash
./setup.sh
```

This script will:
- Start Docker containers (LocalStack + message-generator)
- Start MongoDB if not already running
- Wait for LocalStack to be healthy
- Wait for the main SQS queue to be created by the message-generator
- Create the Dead Letter Queue (DLQ): `transacoes-financeiras-dlq`
- Configure the Redrive Policy linking the main queue to the DLQ (maxReceiveCount: 3)
- Start a live monitor showing message counts in both queues every 5 seconds:

```
=== Tue Jun 23 18:30:00 2026 ===

--- FILA PRINCIPAL ---
{
    "Attributes": {
        "ApproximateNumberOfMessages": "1510",
        "ApproximateNumberOfMessagesNotVisible": "15"
    }
}

--- DLQ ---
{
    "Attributes": {
        "ApproximateNumberOfMessages": "0"
    }
}
```

### 3. Start the application

```bash
mvn spring-boot:run
```

Or run directly from your IDE.

---

## 📬 REST API

### GET /balances/{accountId}

Returns the most recent balance for a given account.

**Example request:**

```bash
curl --location 'http://localhost:8080/balances/5b19c8b6-0cc4-4c72-a989-0c2ee15fa975'
```

**Example response:**

```json
{
  "id": "5b19c8b6-0cc4-4c72-a989-0c2ee15fa975",
  "owner": "315e3cfe-f4af-4cd2-b298-a449e614349a",
  "balance": {
    "amount": 183.12,
    "currency": "BRL"
  },
  "updated_at": "2025-07-05T18:04:13.433-03:00"
}
```

**Error response (account not found):**

```json
{
  "message": "Account not found: 5b19c8b6-0cc4-4c72-a989-0c2ee15fa975",
  "status": 404
}
```

---

## 🧪 Testing with Postman / cURL

### Get account balance

```bash
curl --location 'http://localhost:8080/balances/5b19c8b6-0cc4-4c72-a989-0c2ee15fa975'
```

---

### Send a valid message to the main queue

```bash
curl --location 'http://localhost:4566' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'Action=SendMessage' \
--data-urlencode 'QueueUrl=http://sqs.sa-east-1.localhost.localstack.cloud:4566/000000000000/transacoes-financeiras-processadas' \
--data-urlencode 'MessageBody={
  "transaction": {
    "id": "8e8ae808-b154-48b5-9f3e-553935cc4543",
    "type": "CREDIT",
    "amount": 97.07,
    "currency": "BRL",
    "status": "APPROVED",
    "timestamp": "1782251106000000"
  },
  "account": {
    "id": "5b19c8b6-0cc4-4c72-a989-0c2ee15fa975",
    "owner": "George",
    "created_at": "1634874339",
    "status": "ENABLED",
    "balance": {
      "amount": 200.00,
      "currency": "BRL"
    }
  }
}'
```

---

### Send an invalid message to the main queue (DLQ test)

To test the DLQ flow, send an invalid payload — the consumer will fail to deserialize it, retry 3 times, and automatically route it to the DLQ.

```bash
curl --location 'http://localhost:4566' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'Action=SendMessage' \
--data-urlencode 'QueueUrl=http://sqs.sa-east-1.localhost.localstack.cloud:4566/000000000000/transacoes-financeiras-processadas' \
--data-urlencode 'MessageBody=invalid-payload'
```

After 3 failed processing attempts, the message will appear in the DLQ. You can verify with:

```bash
docker exec -it localstack awslocal sqs receive-message \
  --queue-url http://localhost:4566/000000000000/transacoes-financeiras-dlq \
  --max-number-of-messages 1
```

---

## 🔁 Resilience

### Dead Letter Queue (DLQ)

The DLQ `transacoes-financeiras-dlq` is configured via the `setup.sh` script with the following policy:

- **maxReceiveCount: 3** — if a message fails to be processed 3 times, it is automatically moved to the DLQ
- This prevents poison messages from blocking the main queue
- Messages in the DLQ can be inspected to identify processing failures

### Out-of-order message handling

Since the SQS Standard Queue does not guarantee ordering, messages may arrive out of sequence. The upsert logic ensures only the most recent balance is stored:

```
Message 1 (timestamp: 100, balance: 500) → saved
Message 2 (timestamp: 50,  balance: 200) → ignored (older timestamp)
Result: balance = 500 ✅
```

This is achieved through a two-step atomic MongoDB operation:

1. `updateFirst` — updates only if the incoming `updatedAt` is more recent than what's stored
2. `upsert` with `setOnInsert` — creates the document if it doesn't exist yet

### Timestamp precision

The SQS payload provides timestamps in **microseconds**. The application converts them correctly to avoid date overflow:

```java
Instant.ofEpochSecond(micros / 1_000_000L, (micros % 1_000_000L) * 1000L)
```

---

## 🧠 Architecture Decisions (ADR)

### MongoDB as the persistence layer

**Decision:** Use MongoDB instead of a relational database.

**Motivation:** The data model is document-oriented (one document per account with nested balance), writes are high-frequency and upsert-based, and horizontal scaling is simpler. MongoDB's atomic document-level operations are a natural fit for the idempotent upsert pattern required.

**Tradeoff:** No ACID transactions across multiple documents, but this use case only requires single-document atomicity.

---

### BigDecimal for monetary values

**Decision:** Use `BigDecimal` instead of `Double` for all monetary fields.

**Motivation:** `Double` uses binary floating-point representation, which can introduce precision errors (e.g. `183.12` becoming `183.11999999998`). For financial systems, this is unacceptable. `BigDecimal` stores values exactly as declared.

---

### Repository pattern with custom upsert

**Decision:** Separate the custom MongoDB logic into `AccountRepositoryCustom` / `AccountRepositoryCustomImpl`.

**Motivation:** Keeps the service layer free of infrastructure concerns. If the database changes in the future, only the repository implementation needs to change.

---

### DLQ with maxReceiveCount = 3

**Decision:** Configure the DLQ with 3 retry attempts before routing to the dead letter queue.

**Motivation:** Allows transient failures (network blip, temporary MongoDB unavailability) to recover on retry, while preventing poison messages from looping indefinitely.

---

## 🔮 Future Improvements

These patterns were not implemented due to time constraints but are documented here with their motivations:

| Pattern | Motivation |
|---|---|
| Resilience4j Circuit Breaker | Protect MongoDB from overload during high traffic bursts |
| Retry with exponential backoff + full jitter | Avoid thundering herd on transient failures |
| Testcontainers integration tests | Test real MongoDB and LocalStack behavior in CI |
| OpenAPI / Swagger | Self-documenting API for consumers |
| Micrometer + Prometheus metrics | Observe throughput, error rates, and latency in production |
| Bulk writes | Batch MongoDB writes to reduce round-trips under high load |

---

## ☁️ Cloud Deployment Diagram

For a production deployment on AWS, the following architecture would be used:

```
Internet
    ↓
API Gateway
    ↓
Application Load Balancer
    ↓
ECS Fargate (Auto Scaling)
    ↓                    ↓
MongoDB Atlas     AWS SQS (Standard Queue)
                         ↓
                  ECS Fargate Consumer
                  (separate service, auto scaling based on queue depth)
                         ↓
                  MongoDB Atlas
                         ↓
                  DLQ → CloudWatch Alarm → SNS → Alert
```

---

## 🔀 Architecture Branches

The `master` branch follows a **layered architecture** (Controller → Service → Repository), which is straightforward, effective, and production-ready for this use case.

A refactoring to **Hexagonal Architecture** (Ports and Adapters) is available in the `feature/hexagonal-architecture` branch, demonstrating how the same application would be structured following the pattern used internally at Itaú.

### Hexagonal Structure (feature/hexagonal-architecture)

```
adapters/
├── input/
│   ├── config/      → SqsConfig (infrastructure configuration)
│   ├── messaging/   → SqsConsumer, AccountMapper, DTOs (SQS adapter)
│   └── web/         → AccountController, DTOs, GlobalExceptionHandler (REST adapter)
└── output/
    └── persistence/ → AccountRepository, AccountRepositoryCustom, AccountRepositoryCustomImpl

application/
├── ports/
│   └── input/       → AccountService (business contract — the port)
└── services/        → AccountServiceImpl (use case implementation)

domain/
├── exception/       → AccountNotFoundException
└── models/          → Account
```

### Key design decision

In this implementation, `AccountRepository` and `AccountRepositoryCustom` are kept together with `AccountRepositoryCustomImpl` in `adapters/output/persistence`. This is intentional — they are **technical interfaces** required by Spring Data, not business contracts between the domain and the outside world. Moving them to `ports/output` would be over-engineering, as ports should represent domain-level contracts, not infrastructure details.

### References
- [Hexagonal Architecture Pattern — AWS Prescriptive Guidance](https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/hexagonal-architecture.html)
- [Ports and Adapters — Alistair Cockburn (original author)](https://alistair.cockburn.us/hexagonal-architecture/)
- [Hexagonal Architecture with Spring Boot — Reflectoring](https://reflectoring.io/spring-hexagonal/)

---

## 🚦 Pipeline Strategy

To minimize risk of a bug impacting all clients, a **Canary Release** strategy is recommended:

1. Deploy new version to **5% of traffic**
2. Monitor error rates, latency, and DLQ growth for 10 minutes
3. If metrics are healthy, gradually increase to **25% → 50% → 100%**
4. If anomaly detected, **rollback immediately** by routing all traffic back to the previous version

This is achievable in ECS using weighted target groups in the Application Load Balancer.