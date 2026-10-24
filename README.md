# API Shield — Lightweight API Gateway

A backend-focused API gateway built with **Java, Spring Boot, PostgreSQL, and Redis**. API Shield provides API-key-based client authentication, request rate limiting, and forwarding to external APIs through a centralized gateway.

## Overview

When applications consume external APIs directly, managing client access, request limits, and upstream communication can become difficult. API Shield provides a single entry point for protected API access.

It authenticates API clients, identifies the associated client record, enforces rate limits, and forwards permitted requests to a configured upstream API.

The project demonstrates practical backend engineering concepts, including REST API development, Spring Security, database persistence, cryptographic hashing, Redis-based rate limiting, and Docker-based deployment.

## Key Features

- **API-key authentication:** Clients access protected endpoints using an API key supplied in the `X-API-Key` request header.
- **Secure key storage:** API keys are generated using secure random bytes and stored as SHA-256 hashes rather than plaintext keys.
- **Client management:** Register clients and support API-key rotation and revocation.
- **Rate limiting:** Redis maintains request counters using time-window-based rate limiting.
- **HTTP 429 responses:** Excessive requests are rejected when a client's configured limit is exceeded.
- **API gateway forwarding:** Forward requests to a configured upstream API using Spring's `RestClient`.
- **Error handling:** Handle upstream HTTP errors and connection failures.
- **Docker-based environment:** Run the application alongside PostgreSQL and Redis using Docker Compose.

## Architecture

```text
                    API Client
                         |
                         v
                +------------------+
                |   API Shield     |
                |  Spring Boot     |
                +------------------+
                         |
                         v
                +------------------+
                | API Key          |
                | Authentication   |
                +------------------+
                         |
                         v
                +------------------+
                | Redis Rate       |
                | Limiter          |
                +------------------+
                         |
                +--------+--------+
                |                 |
                v                 v
        +---------------+  +----------------+
        | PostgreSQL    |  | Upstream API   |
        | Client Records|  | JSONPlaceholder|
        +---------------+  +----------------+
```

### Request lifecycle

1. A client sends a request to API Shield.
2. The authentication layer validates the API key against the stored key hash and checks whether the client is active.
3. Redis tracks the client's request count within the current time window.
4. Requests within the configured limit proceed; excessive requests receive HTTP `429 Too Many Requests`.
5. Gateway requests are forwarded to the configured upstream API.
6. API Shield returns the upstream response or an appropriate error response.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Backend programming language |
| Spring Boot | Application framework and REST APIs |
| Spring Security | Request authorization and security configuration |
| Spring Data JPA / Hibernate | Database persistence and ORM |
| PostgreSQL | Persistent client records and API-key hashes |
| Redis | Rate-limit counters |
| Spring `RestClient` | Upstream HTTP communication |
| Maven | Dependency and build management |
| Docker Compose | Containerized application environment |

## Project Structure

```text
api-shield/
├── src/
│   ├── main/
│   │   ├── java/com/apishield/
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── ClientManagementController.java
│   │   │   │   ├── ClientRegistrationController.java
│   │   │   │   ├── GatewayController.java
│   │   │   │   ├── HealthController.java
│   │   │   │   └── ProtectedController.java
│   │   │   ├── filter/
│   │   │   │   ├── AdminAuthenticationFilter.java
│   │   │   │   └── ApiKeyAuthenticationFilter.java
│   │   │   ├── model/
│   │   │   │   └── ApiClient.java
│   │   │   ├── repository/
│   │   │   │   └── ApiClientRepository.java
│   │   │   ├── service/
│   │   │   │   ├── ApiKeyService.java
│   │   │   │   ├── ClientRegistrationService.java
│   │   │   │   └── RateLimiterService.java
│   │   │   └── ApiShieldApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── .gitignore
├── Dockerfile
├── compose.yaml
├── pom.xml
└── mvnw
```

## Getting Started

### Prerequisites

- Java version compatible with the project's Maven configuration
- Docker and Docker Compose
- Git
- `curl` or an API client such as Postman

### 1. Clone the repository

```bash
git clone https://github.com/AMITABHA7430/API-Shield.git
cd API-Shield
```

### 2. Configure environment variables

The Docker Compose configuration expects database and admin credentials to be supplied through environment variables.

Create a local `.env` file in the project root:

```dotenv
DB_PASSWORD=replace_with_a_strong_unique_password
APISHIELD_ADMIN_TOKEN=replace_with_a_long_random_token
```

Replace both example values with your own securely generated secrets. Keep `.env` private and never commit it to Git.

### 3. Start the application

```bash
docker compose up --build -d
```

Check the container status:

```bash
docker compose ps
```

View application logs:

```bash
docker compose logs -f app
```

The application should be reachable on the host port configured in `compose.yaml`. The examples below use port `8080`; adjust the URL if your configuration uses another port.

### 4. Verify application health

```bash
curl -i http://localhost:8080/api/v1/health
```

A successful health response indicates that the health endpoint is reachable.

## API Reference

Base URL: `http://localhost:8080`

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/health` | Health check |
| POST | `/api/v1/clients/register` | Register an API client |
| POST | `/api/v1/clients/{id}/revoke` | Revoke a client's access |
| POST | `/api/v1/clients/{id}/rotate-key` | Rotate a client's API key |
| GET | `/api/v1/protected` | Example protected endpoint |
| GET | `/gateway/weather` | Example gateway route |
| GET | `/gateway/posts/{id}` | Forward a request for a specific post |

Administrative operations are intended to require the `X-Admin-Token` header. Protected API access uses `X-API-Key`.

### Register a client

```bash
curl -i -X POST http://localhost:8080/api/v1/clients/register \
  -H "Content-Type: application/json" \
  -H "X-Admin-Token: $APISHIELD_ADMIN_TOKEN" \
  -d '{"clientName":"demo-client"}'
```

A successful registration returns client information and a generated API key.

**Important:** Store the returned API key securely when it is issued. Do not commit it to source control or include it in logs, screenshots, or public issue reports.

### Access a protected endpoint

Replace the placeholder with the API key returned during registration:

```bash
curl -i http://localhost:8080/api/v1/protected \
  -H "X-API-Key: YOUR_API_KEY"
```

Requests without valid credentials should be rejected by the protected endpoint's authentication rules.

### Use the gateway

Example route:

```bash
curl -i http://localhost:8080/gateway/weather \
  -H "X-API-Key: YOUR_API_KEY"
```

Forward a request for a specific post:

```bash
curl -i http://localhost:8080/gateway/posts/1 \
  -H "X-API-Key: YOUR_API_KEY"
```

The gateway uses the configured upstream base URL and forwards requests to the corresponding upstream resource.

### Common HTTP responses

| Status | Meaning |
|---|---|
| `200 OK` | Request succeeded |
| `201 Created` | Client registration succeeded |
| `401 Unauthorized` | Authentication credentials are missing or invalid |
| `429 Too Many Requests` | Client exceeded the configured rate limit |
| `502 Bad Gateway` | Upstream communication failed |

Exact responses depend on the endpoint and error-handling configuration.

## Rate Limiting

API Shield uses Redis to maintain request counters associated with a client and a time window.

The rate limiter uses a key structure similar to:

```text
rate-limit:<clientId>:<window>
```

A client can make requests up to its configured limit within the current window. Once the limit is exceeded, API Shield rejects further requests with HTTP `429 Too Many Requests`.

Redis provides a shared counter store, allowing the application to avoid relying exclusively on in-memory counters.

## Security Considerations

- API keys are generated using secure random bytes.
- API-key hashes are persisted instead of storing the raw API keys.
- Administrative operations are designed to use a separate admin token.
- Database passwords and admin tokens should be supplied through environment configuration.
- Secrets must not be committed to GitHub.
- Public deployment requires reviewing authentication-filter registration, authorization rules, secret management, and rate-limit behavior.
- Production deployments should use HTTPS, appropriately restricted network access, monitoring, and managed secret storage.

## Testing

Run the automated test suite using the Maven wrapper:

```bash
./mvnw test
```

Build the application without rerunning tests:

```bash
./mvnw -DskipTests package
```

The repository includes Spring Boot application and security test classes. Expand the integration test suite as needed to cover API-key lifecycle operations, Redis rate limiting, upstream failures, and administrative authorization.

## Future Improvements

Potential enhancements include:

- Configurable upstream connection and response timeouts
- More flexible per-client rate-limit policies
- Distributed rate limiting and concurrency testing
- Centralized logging, metrics, and observability
- Improved gateway routing and upstream configuration
- Automated integration tests using containerized dependencies
- API documentation with OpenAPI and Swagger UI

## Learning Outcomes

This project provides hands-on experience with:

- Designing REST APIs using Spring Boot
- Implementing authentication filters and authorization
- Secure API-key generation and hashing
- Integrating PostgreSQL with Spring Data JPA
- Implementing Redis-based rate limiting
- Forwarding HTTP requests to external services
- Containerizing backend applications with Docker Compose
- Testing and documenting a backend service

## Author

**Amitabha Ghosh**

GitHub: [@AMITABHA7430](https://github.com/AMITABHA7430)

---

*API Shield is a backend engineering project intended to demonstrate API gateway fundamentals, authentication, rate limiting, and service integration.*
