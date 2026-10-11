
# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build application
COPY src ./src
RUN mvn clean package -DskipTests -B


# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl and create a non-root user
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system devspace \
    && useradd --system --gid devspace devspace

# Copy executable JAR
COPY --from=builder --chown=devspace:devspace /app/target/*.jar app.jar

# Run as a non-root user
USER devspace

EXPOSE 8086

# Container health check
HEALTHCHECK --interval=30s --timeout=5s \
    --start-period=60s --retries=3 \
    CMD curl --fail --silent http://localhost:8086/actuator/health || exit 1

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

