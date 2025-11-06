FROM openjdk:26-ea-21-slim

WORKDIR /app

COPY target/ip53helper-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]