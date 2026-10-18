# syntax=docker/dockerfile:1

# ---- Build stage: compile and extract the Spring Boot layers ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Resolve dependencies first so they are cached independently of source changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q package -DskipTests \
    && java -Djarmode=tools -jar target/taskflow.jar extract --layers --destination target/extracted

# ---- Runtime stage: JRE only, non-root, layered for fast rebuilds ----
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S taskflow && adduser -S taskflow -G taskflow
WORKDIR /app

# Least- to most-frequently changing layers.
COPY --from=build /workspace/target/extracted/dependencies/ ./
COPY --from=build /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/target/extracted/application/ ./

USER taskflow
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "taskflow.jar"]
