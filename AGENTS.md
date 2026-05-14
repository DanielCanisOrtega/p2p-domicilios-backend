# AGENTS

## Setup essentials
- App reads environment from .env via spring.config.import in src/main/resources/application.yaml; define variables listed in .env.example or the app will fail at startup.
- Default port comes from SERVER_PORT; scripts assume localhost:8080.

## Commands
- Run locally: ./mvnw spring-boot:run
- Run tests: ./mvnw test
- Build jar: ./mvnw clean package
- Docker build uses mvn clean package -DskipTests (see Dockerfile).

## Smoke test script
- scripts/test_requests.sh expects the server running on localhost:8080, uses jq if available, and ends with a psql query against an external DB (requires psql installed).

## Entry point
- Main class: com.p2pdomicilios.P2pDomicilios.P2pDomiciliosApplication
