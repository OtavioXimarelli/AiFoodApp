# ---- Estágio de Build com Java 25 ----
FROM maven:3-eclipse-temurin-25-noble AS build


# Define o diretório de trabalho.
WORKDIR /app

# Otimiza o cache do Docker baixando as dependências primeiro
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código-fonte.
COPY src ./src

# Compila e empacota a aplicação.
RUN mvn package -DskipTests

# ---- Estágio Final (Produção) com Java 25 ----
FROM eclipse-temurin:25-jre-noble

# Define o diretório de trabalho.
WORKDIR /app

# Copia apenas o arquivo .jar gerado no estágio anterior.
COPY --from=build /app/target/AiFoodAPP-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta da aplicação.
EXPOSE 8080

# Comando para iniciar a aplicação.
ENTRYPOINT ["java", "-jar", "app.jar"]