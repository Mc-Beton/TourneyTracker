FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -pl user-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/user-service/target/*.jar app.jar
ENV SERVER_PORT=8081
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
