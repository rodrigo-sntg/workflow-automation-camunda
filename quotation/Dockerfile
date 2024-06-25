# Etapa de construção com Maven
FROM openjdk:17
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw clean install -DskipTests && ./mvnw clean package -DskipTests
COPY ./target/quotation-0.0.1-SNAPSHOT.jar /app/quotation.jar

EXPOSE 8085
#ENTRYPOINT ["java","-jar","app.jar"]
CMD ["java", "-jar", "quotation.jar"]
