# ---- Estágio de Build ----
# Usamos uma imagem completa do Maven com JDK 23 para compilar o projeto.
# Damos um nome a este estágio, "build", para nos referirmos a ele mais tarde.
FROM maven:3.9.6-eclipse-temurin-23 AS build

# Define o diretório de trabalho dentro do contêiner.
WORKDIR /app

# Copia apenas o pom.xml primeiro. O Docker armazena essa camada em cache.
# As dependências só serão baixadas novamente se o pom.xml mudar.
COPY pom.xml .

# Baixa todas as dependências do projeto.
RUN mvn dependency:go-offline

# Copia todo o resto do código-fonte do projeto.
COPY src ./src

# Compila a aplicação, empacota em um .jar e pula os testes (que já devem ter rodado no CI/CD).
# O Spring Boot Plugin irá criar um único JAR executável.
RUN mvn package -DskipTests

# ---- Estágio Final (Produção) ----
# Usamos uma imagem "slim", que contém apenas o Java Runtime Environment (JRE).
# É muito menor e mais segura que a imagem completa do JDK.
FROM eclipse-temurin:23-jre

# Define o diretório de trabalho.
WORKDIR /app

# Copia APENAS o arquivo .jar do estágio de "build" para a imagem final.
# Nenhum código-fonte ou dependência do Maven é incluído aqui.
COPY --from=build /app/target/AiFoodAPP-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta em que a aplicação Spring roda.
EXPOSE 8080

# Comando que será executado quando o contêiner iniciar.
ENTRYPOINT ["java", "-jar", "app.jar"]