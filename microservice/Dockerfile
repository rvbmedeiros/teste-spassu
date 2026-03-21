# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

# Download dependencies first for layer caching
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -f pom.xml -pl . dependency:go-offline -q 2>/dev/null || true

COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS runtime
RUN addgroup -S spassu && adduser -S spassu -G spassu
WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar
RUN chown spassu:spassu app.jar
USER spassu

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
