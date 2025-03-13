FROM openjdk:17-jdk
WORKDIR /app
COPY target/tourney-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]