# ---- Estágio de Build com Java 23 ----
FROM eclipse-temurin:23-jdk AS build

# Define variáveis para a versão do Maven
ARG MAVEN_VERSION=3.9.6
# CORREÇÃO: Usando um link de download mais estável do repositório de arquivos da Apache
ARG MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz

# Instala ferramentas necessárias e baixa e instala o Maven.
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL ${MAVEN_URL} -o /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /usr/share && \
    ln -s /usr/share/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn && \
    rm /tmp/maven.tar.gz && \
    apt-get purge -y --auto-remove curl

# Define o diretório de trabalho.
WORKDIR /app

# Otimiza o cache do Docker baixando as dependências primeiro
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código-fonte.
COPY src ./src

# Compila e empacota a aplicação.
RUN mvn package -DskipTests

# ---- Estágio Final (Produção) com Java 23 ----
FROM eclipse-temurin:23-jre

# Define o diretório de trabalho.
WORKDIR /app

# Copia apenas o arquivo .jar gerado no estágio anterior.
COPY --from=build /app/target/AiFoodAPP-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta da aplicação.
EXPOSE 8080

# Comando para iniciar a aplicação.
ENTRYPOINT ["java", "-jar", "app.jar"]