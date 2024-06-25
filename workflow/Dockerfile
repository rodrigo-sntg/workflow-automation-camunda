# Etapa de construção com Maven
FROM openjdk:17
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw clean install -DskipTests && ./mvnw clean package -DskipTests
COPY ./target/workflow-0.0.1-SNAPSHOT.jar /app/workflow.jar

EXPOSE 8081
CMD ["java", "-jar", "workflow.jar"]
