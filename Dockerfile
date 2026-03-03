# ── Stage 1: Build ──
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests -B

# ── Stage 2: Run ──
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ecommerce-sqs-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
