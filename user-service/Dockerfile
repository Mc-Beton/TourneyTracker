FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY common/pom.xml ./common/pom.xml
COPY common/src ./common/src
COPY user-service/pom.xml ./user-service/pom.xml
COPY user-service/src ./user-service/src
COPY pom.xml .
RUN mvn clean package -pl user-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/user-service/target/*.jar app.jar
ENV SERVER_PORT=8081
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
