# Stage 1: Build với Gradle
FROM gradle:7-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test
# Stage 2: Runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy JAR file từ builder stage
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
# Lệnh để chạy ứng dụng
CMD ["java", "-jar", "app.jar"]
