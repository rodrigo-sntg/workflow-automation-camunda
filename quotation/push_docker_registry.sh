./mvnw clean install -DskipTests && ./mvnw clean package -DskipTests
docker build -t rodrigosntg/workflow:latest . && docker push rodrigosntg/workflow:latest