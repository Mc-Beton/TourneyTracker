FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -pl tourney -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/tourney/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]