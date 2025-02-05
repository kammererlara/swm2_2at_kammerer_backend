FROM openjdk:23-slim

COPY build/libs/*.jar /app.jar

EXPOSE 8080/tcp

CMD ["java", "-jar", "/app.jar"]