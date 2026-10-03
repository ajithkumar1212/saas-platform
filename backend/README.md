# SaaS Platform Backend

Enterprise Multi-Tenant SaaS Platform Backend built with Spring Boot 3.5.x, Java 21 LTS, Hibernate 6, PostgreSQL schema isolation, and Flyway.

## Quick Links
- **Onboarding Guide:** See [LOCAL_DEVELOPMENT_ONBOARDING_GUIDE.md](../LOCAL_DEVELOPMENT_ONBOARDING_GUIDE.md) for step-by-step setup instructions for new teammates.
- **Swagger UI:** `http://localhost:8081/swagger-ui.html`
- **Health Endpoint:** `http://localhost:8081/actuator/health`

## Build & Test
```bash
mvn clean test
```

## Run Locally
```bash
mvn spring-boot:run
```

## Package Executable JAR
```bash
mvn package -DskipTests
java -jar target/saas-platform-backend-0.0.1-SNAPSHOT.jar
```
