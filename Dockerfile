FROM eclipse-temurin:23
LABEL author="Otavio Ximarelli" maintainer="otavio@dev.com"
WORKDIR /app
COPY target/AiFoodAPP-0.0.1-SNAPSHOT.jar /app/aifoodapp.jar
#COPY .env /app/.env
ENTRYPOINT ["java", "-jar", "aifoodapp.jar"]
EXPOSE 8080