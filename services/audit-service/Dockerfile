FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG JAR_FILE=target/audit-service-*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
