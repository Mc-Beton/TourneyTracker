FROM openjdk:17-jdk
WORKDIR /app
COPY target/user-service-0.0.1-SNAPSHOT.jar app.jar
ENV SERVER_PORT=8081
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
