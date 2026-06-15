# Resilient AI-Driven Java with Quarkus

A **WeAreDev conference demo** showcasing how to build resilient, AI-powered Java microservices using Quarkus. This application integrates **OpenAI** via Quarkus LangChain4j and demonstrates production-grade resilience patterns via **SmallRye Fault Tolerance**.

## Features

| Feature | Extension |
|---|---|
| AI Chat endpoint (GPT-4o-mini) | `quarkus-langchain4j-openai` |
| AI Code Review endpoint | `quarkus-langchain4j-openai` |
| Circuit Breaker | `quarkus-smallrye-fault-tolerance` |
| Retry with backoff | `quarkus-smallrye-fault-tolerance` |
| Timeout | `quarkus-smallrye-fault-tolerance` |
| Fallback responses | `quarkus-smallrye-fault-tolerance` |
| Health checks (liveness + readiness) | `quarkus-smallrye-health` |
| Bean validation on requests | `quarkus-hibernate-validator` |
| JSON serialization | `quarkus-rest-jackson` |

## Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/ai/ping` | Health ping (plain text) |
| `POST` | `/api/ai/chat` | Ask the AI a question |
| `POST` | `/api/ai/review` | Submit code for AI review |
| `GET` | `/q/health/live` | Liveness health check |
| `GET` | `/q/health/ready` | Readiness health check (includes AI service check) |
| `GET` | `/q/health` | Combined health check |

## Resilience Patterns

### Chat endpoint (`/api/ai/chat`)
- **Timeout**: 30 seconds
- **Retry**: up to 2 retries, 1s delay, on `RuntimeException`
- **Circuit Breaker**: opens after 60% failure rate over 5 requests, stays open for 15s
- **Fallback**: returns a graceful degraded response when the circuit is open or all retries are exhausted

### Code Review endpoint (`/api/ai/review`)
- **Timeout**: 45 seconds
- **Retry**: 1 retry, 2s delay
- **Circuit Breaker**: opens after 50% failure rate over 4 requests, stays open for 20s
- **Fallback**: returns a graceful degraded response

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.9+
- An [OpenAI API key](https://platform.openai.com/api-keys)

### Run in dev mode

```bash
export OPENAI_API_KEY=sk-...
./mvnw quarkus:dev
```

### Example requests

**Chat:**
```bash
curl -s -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "What are the benefits of using Quarkus for AI workloads?"}' | jq .
```

**Code Review:**
```bash
curl -s -X POST http://localhost:8080/api/ai/review \
  -H "Content-Type: application/json" \
  -d '{"code": "public String greet(String name) { return \"Hello \" + name; }"}' | jq .
```

**Health check:**
```bash
curl -s http://localhost:8080/q/health | jq .
```

## Project Structure

```
src/main/java/org/acme/
├── ai/
│   └── AiAssistant.java          # LangChain4j AI service interface (@RegisterAiService)
├── dto/
│   ├── ChatRequest.java          # Validated chat request DTO
│   ├── ChatResponse.java         # Chat/fallback response DTO
│   └── CodeReviewRequest.java    # Validated code review request DTO
├── health/
│   └── AiServiceHealthCheck.java # @Readiness health check for AI service
├── resilience/
│   └── ResilientAiService.java   # Fault tolerance wrapper (@Retry, @CircuitBreaker, @Fallback, @Timeout)
└── rest/
    └── AiResource.java           # REST endpoints (@Path /api/ai)
```

## Running Tests

```bash
./mvnw test
```

All 9 tests cover: ping, chat success/validation/fallback, code review success/validation, and health endpoints.

## Quarkus Guides

- [Quarkus LangChain4j OpenAI](https://docs.quarkiverse.io/quarkus-langchain4j/dev/openai.html)
- [SmallRye Fault Tolerance](https://quarkus.io/guides/smallrye-fault-tolerance)
- [SmallRye Health](https://quarkus.io/guides/smallrye-health)
- [Quarkus REST](https://quarkus.io/guides/rest)
- [Hibernate Validator](https://quarkus.io/guides/validation)
