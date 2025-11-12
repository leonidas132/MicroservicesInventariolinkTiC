FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/MicroserviceInventarioLinkTiC-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]