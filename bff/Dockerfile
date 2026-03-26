# ─── Build stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

RUN apk add --no-cache maven

COPY pom.xml ./pom.xml
COPY microservice/pom.xml ./microservice/pom.xml
COPY orchestration/pom.xml ./orchestration/pom.xml
COPY bff/pom.xml ./bff/pom.xml
COPY bff/src ./bff/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -f /workspace/pom.xml -pl bff -am package -DskipTests -q

# ─── Runtime stage ────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

RUN apk add --no-cache curl \
  && addgroup -S spassu \
  && adduser -S spassu -G spassu

COPY --from=build /workspace/bff/target/*.jar app.jar
RUN chown -R spassu:spassu /app
USER spassu

EXPOSE 8083

ENTRYPOINT ["java", \
  "-XX:+UseZGC", \
  "-XX:+ZGenerational", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
