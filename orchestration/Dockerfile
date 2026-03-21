# ─── Build stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY ../pom.xml ./pom.xml
COPY ../microservice/pom.xml ./microservice/pom.xml
COPY ../orchestration/pom.xml ./orchestration/pom.xml
COPY ../bff/pom.xml ./bff/pom.xml

COPY pom.xml ./module-pom.xml
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -f /workspace/pom.xml -pl orchestration -am package -DskipTests -q

# ─── Runtime stage ────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spassu && adduser -S spassu -G spassu
USER spassu

COPY --from=build /workspace/orchestration/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", \
  "-XX:+UseZGC", \
  "-XX:+ZGenerational", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
