FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG JAR_FILE=target/user-service-*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
