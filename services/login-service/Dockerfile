
# ---------- Build Stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd --system devspace && \
    useradd --system --gid devspace devspace

COPY --from=build /build/target/*.jar app.jar

RUN chown devspace:devspace app.jar

USER devspace

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]

