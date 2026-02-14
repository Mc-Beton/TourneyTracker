FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY common/pom.xml ./common/pom.xml
COPY common/src ./common/src
COPY tourney/pom.xml ./tourney/pom.xml
COPY tourney/src ./tourney/src
COPY pom.xml .
RUN mvn clean package -pl tourney -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/tourney/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]