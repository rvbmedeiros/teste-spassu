# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /workspace

RUN apk add --no-cache maven

COPY pom.xml ./pom.xml
COPY microservice/pom.xml ./microservice/pom.xml
COPY orchestration/pom.xml ./orchestration/pom.xml
COPY bff/pom.xml ./bff/pom.xml
COPY microservice/src ./microservice/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -f /workspace/pom.xml -pl microservice -am package -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS runtime
RUN apk add --no-cache curl \
    && addgroup -S spassu \
    && adduser -S spassu -G spassu
WORKDIR /app

COPY --from=builder /workspace/microservice/target/*.jar app.jar
RUN chown -R spassu:spassu /app
USER spassu

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
