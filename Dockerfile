# Sử dụng JDK 17
FROM openjdk:17-jdk-slim

# Thiết lập thư mục làm việc
WORKDIR /app

# Sao chép file JAR vào container
COPY target/*.jar app.jar

# Expose cổng 8080 (hoặc cổng bạn sử dụng)
EXPOSE 8080

# Chạy ứng dụng
CMD ["java", "-jar", "app.jar"]
