cd workflow
./mvnw clean install -DskipTests && ./mvnw clean package -DskipTests
docker build -t rodrigosntg/workflow:latest . && docker push rodrigosntg/workflow:latest
cd ../quotation
./mvnw clean install -DskipTests && ./mvnw clean package -DskipTests
docker build -t rodrigosntg/quotation:latest . && docker push rodrigosntg/quotation:latest
