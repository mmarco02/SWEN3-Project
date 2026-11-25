FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests
CMD ["java", "-jar", "target/ocr-worker-0.0.1-SNAPSHOT.jar"]
